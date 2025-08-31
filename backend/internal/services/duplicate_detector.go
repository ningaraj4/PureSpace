package services

import (
	"context"
	"crypto/sha256"
	"fmt"
	"io"
	"sort"
	"strings"

	"github.com/google/uuid"
	"github.com/purespace/backend/internal/models"
	"gorm.io/gorm"
)

// DuplicateDetector provides advanced duplicate detection algorithms
// Inspired by twpayne/find-duplicates approach
type DuplicateDetector struct {
	db *gorm.DB
}

func NewDuplicateDetector(db *gorm.DB) *DuplicateDetector {
	return &DuplicateDetector{
		db: db,
	}
}

// DetectionStrategy defines different duplicate detection strategies
type DetectionStrategy int

const (
	// StrategyHash - SHA-256 hash comparison (fastest, most accurate)
	StrategyHash DetectionStrategy = iota
	// StrategySize - Size-based grouping (fast, less accurate)
	StrategySize
	// StrategySizeAndName - Size + filename similarity (balanced)
	StrategySizeAndName
	// StrategyAdvanced - Multi-factor analysis (slowest, most comprehensive)
	StrategyAdvanced
)

// DuplicateCandidate represents a potential duplicate file
type DuplicateCandidate struct {
	File       models.File `json:"file"`
	Confidence float64     `json:"confidence"` // 0.0 to 1.0
	Reason     string      `json:"reason"`
}

// DuplicateCluster represents a group of duplicate files
type DuplicateCluster struct {
	ID         string                `json:"id"`
	SHA256     string                `json:"sha256,omitempty"`
	Size       int64                 `json:"size"`
	Count      int                   `json:"count"`
	TotalSize  int64                 `json:"total_size"`
	Candidates []DuplicateCandidate  `json:"candidates"`
	Strategy   DetectionStrategy     `json:"strategy"`
	CreatedAt  string                `json:"created_at"`
}

// DetectDuplicates finds duplicate files using the specified strategy
func (dd *DuplicateDetector) DetectDuplicates(ctx context.Context, userID uuid.UUID, strategy DetectionStrategy) ([]DuplicateCluster, error) {
	switch strategy {
	case StrategyHash:
		return dd.detectByHash(ctx, userID)
	case StrategySize:
		return dd.detectBySize(ctx, userID)
	case StrategySizeAndName:
		return dd.detectBySizeAndName(ctx, userID)
	case StrategyAdvanced:
		return dd.detectAdvanced(ctx, userID)
	default:
		return dd.detectByHash(ctx, userID)
	}
}

// detectByHash finds exact duplicates using SHA-256 hash comparison
func (dd *DuplicateDetector) detectByHash(ctx context.Context, userID uuid.UUID) ([]DuplicateCluster, error) {
	var results []struct {
		SHA256    string
		Size      int64
		Count     int64
		TotalSize int64
	}

	// Find files with identical SHA-256 hashes
	err := dd.db.WithContext(ctx).
		Model(&models.File{}).
		Select("sha256, size, COUNT(*) as count, SUM(size) as total_size").
		Where("user_id = ? AND sha256 != ''", userID).
		Group("sha256, size").
		Having("COUNT(*) > 1").
		Order("total_size DESC").
		Scan(&results).Error

	if err != nil {
		return nil, fmt.Errorf("failed to detect hash duplicates: %w", err)
	}

	var clusters []DuplicateCluster
	for _, result := range results {
		// Get all files in this duplicate group
		var files []models.File
		err := dd.db.WithContext(ctx).
			Where("user_id = ? AND sha256 = ?", userID, result.SHA256).
			Order("created_at ASC").
			Find(&files).Error

		if err != nil {
			continue
		}

		// Create candidates with 100% confidence for hash matches
		var candidates []DuplicateCandidate
		for _, file := range files {
			candidates = append(candidates, DuplicateCandidate{
				File:       file,
				Confidence: 1.0,
				Reason:     "Identical SHA-256 hash",
			})
		}

		cluster := DuplicateCluster{
			ID:         generateClusterID(result.SHA256),
			SHA256:     result.SHA256,
			Size:       result.Size,
			Count:      int(result.Count),
			TotalSize:  result.TotalSize,
			Candidates: candidates,
			Strategy:   StrategyHash,
		}

		clusters = append(clusters, cluster)
	}

	return clusters, nil
}

// detectBySize finds potential duplicates based on file size
func (dd *DuplicateDetector) detectBySize(ctx context.Context, userID uuid.UUID) ([]DuplicateCluster, error) {
	var results []struct {
		Size      int64
		Count     int64
		TotalSize int64
	}

	// Find files with identical sizes
	err := dd.db.WithContext(ctx).
		Model(&models.File{}).
		Select("size, COUNT(*) as count, SUM(size) as total_size").
		Where("user_id = ? AND size > 0", userID).
		Group("size").
		Having("COUNT(*) > 1").
		Order("total_size DESC").
		Scan(&results).Error

	if err != nil {
		return nil, fmt.Errorf("failed to detect size duplicates: %w", err)
	}

	var clusters []DuplicateCluster
	for _, result := range results {
		// Get all files with this size
		var files []models.File
		err := dd.db.WithContext(ctx).
			Where("user_id = ? AND size = ?", userID, result.Size).
			Order("created_at ASC").
			Find(&files).Error

		if err != nil {
			continue
		}

		// Create candidates with confidence based on size uniqueness
		confidence := calculateSizeConfidence(result.Size, int(result.Count))
		var candidates []DuplicateCandidate
		for _, file := range files {
			candidates = append(candidates, DuplicateCandidate{
				File:       file,
				Confidence: confidence,
				Reason:     fmt.Sprintf("Identical size (%d bytes)", result.Size),
			})
		}

		cluster := DuplicateCluster{
			ID:         generateClusterID(fmt.Sprintf("size_%d", result.Size)),
			Size:       result.Size,
			Count:      int(result.Count),
			TotalSize:  result.TotalSize,
			Candidates: candidates,
			Strategy:   StrategySize,
		}

		clusters = append(clusters, cluster)
	}

	return clusters, nil
}

// detectBySizeAndName finds duplicates using size and filename similarity
func (dd *DuplicateDetector) detectBySizeAndName(ctx context.Context, userID uuid.UUID) ([]DuplicateCluster, error) {
	// First get size-based groups
	sizeClusters, err := dd.detectBySize(ctx, userID)
	if err != nil {
		return nil, err
	}

	var refinedClusters []DuplicateCluster

	// Refine each size cluster by filename similarity
	for _, cluster := range sizeClusters {
		if len(cluster.Candidates) < 2 {
			continue
		}

		// Group by filename similarity
		nameGroups := dd.groupByNameSimilarity(cluster.Candidates)

		for groupID, group := range nameGroups {
			if len(group) < 2 {
				continue
			}

			// Calculate confidence based on name similarity
			avgConfidence := dd.calculateNameSimilarityConfidence(group)

			refinedCluster := DuplicateCluster{
				ID:         fmt.Sprintf("%s_name_%s", cluster.ID, groupID),
				Size:       cluster.Size,
				Count:      len(group),
				TotalSize:  int64(len(group)) * cluster.Size,
				Candidates: group,
				Strategy:   StrategySizeAndName,
			}

			// Update confidence for all candidates
			for i := range refinedCluster.Candidates {
				refinedCluster.Candidates[i].Confidence = avgConfidence
				refinedCluster.Candidates[i].Reason = fmt.Sprintf("Size + filename similarity (%.1f%%)", avgConfidence*100)
			}

			refinedClusters = append(refinedClusters, refinedCluster)
		}
	}

	return refinedClusters, nil
}

// detectAdvanced uses multiple factors for comprehensive duplicate detection
func (dd *DuplicateDetector) detectAdvanced(ctx context.Context, userID uuid.UUID) ([]DuplicateCluster, error) {
	// Start with hash-based detection (highest confidence)
	hashClusters, err := dd.detectByHash(ctx, userID)
	if err != nil {
		return nil, err
	}

	// Add size + name based detection for files without hashes or hash collisions
	sizeNameClusters, err := dd.detectBySizeAndName(ctx, userID)
	if err != nil {
		return nil, err
	}

	// Combine and deduplicate results
	allClusters := append(hashClusters, sizeNameClusters...)
	
	// Remove overlapping clusters (prefer higher confidence)
	deduplicatedClusters := dd.deduplicateClusters(allClusters)

	// Sort by potential savings (total size - size of one file)
	sort.Slice(deduplicatedClusters, func(i, j int) bool {
		savingsI := deduplicatedClusters[i].TotalSize - deduplicatedClusters[i].Size
		savingsJ := deduplicatedClusters[j].TotalSize - deduplicatedClusters[j].Size
		return savingsI > savingsJ
	})

	return deduplicatedClusters, nil
}

// Helper functions

func generateClusterID(input string) string {
	hash := sha256.Sum256([]byte(input))
	return fmt.Sprintf("%x", hash[:8]) // Use first 8 bytes as ID
}

func calculateSizeConfidence(size int64, count int) float64 {
	// Larger files with fewer duplicates = higher confidence
	// Smaller files with many duplicates = lower confidence
	baseConfidence := 0.3
	
	if size > 100*1024*1024 { // > 100MB
		baseConfidence = 0.8
	} else if size > 10*1024*1024 { // > 10MB
		baseConfidence = 0.6
	} else if size > 1024*1024 { // > 1MB
		baseConfidence = 0.4
	}

	// Reduce confidence for very common sizes
	if count > 10 {
		baseConfidence *= 0.5
	} else if count > 5 {
		baseConfidence *= 0.7
	}

	return baseConfidence
}

func (dd *DuplicateDetector) groupByNameSimilarity(candidates []DuplicateCandidate) map[string][]DuplicateCandidate {
	groups := make(map[string][]DuplicateCandidate)

	for _, candidate := range candidates {
		// Extract filename from path
		filename := extractFilename(candidate.File.PathTail)
		normalizedName := normalizeFilename(filename)
		
		// Find similar group or create new one
		groupKey := findSimilarGroup(normalizedName, groups)
		if groupKey == "" {
			groupKey = normalizedName
		}

		groups[groupKey] = append(groups[groupKey], candidate)
	}

	return groups
}

func (dd *DuplicateDetector) calculateNameSimilarityConfidence(candidates []DuplicateCandidate) float64 {
	if len(candidates) < 2 {
		return 0.0
	}

	// Calculate average similarity between all pairs
	totalSimilarity := 0.0
	pairs := 0

	for i := 0; i < len(candidates); i++ {
		for j := i + 1; j < len(candidates); j++ {
			name1 := extractFilename(candidates[i].File.PathTail)
			name2 := extractFilename(candidates[j].File.PathTail)
			similarity := calculateStringSimilarity(name1, name2)
			totalSimilarity += similarity
			pairs++
		}
	}

	if pairs == 0 {
		return 0.0
	}

	avgSimilarity := totalSimilarity / float64(pairs)
	
	// Boost confidence for exact name matches
	if avgSimilarity > 0.95 {
		return 0.9
	} else if avgSimilarity > 0.8 {
		return 0.7
	} else if avgSimilarity > 0.6 {
		return 0.5
	}

	return avgSimilarity * 0.6 // Cap at 60% for name-only similarity
}

func (dd *DuplicateDetector) deduplicateClusters(clusters []DuplicateCluster) []DuplicateCluster {
	// Track files that are already in high-confidence clusters
	usedFiles := make(map[uint]bool)
	var result []DuplicateCluster

	// Sort by confidence (hash-based first, then by average confidence)
	sort.Slice(clusters, func(i, j int) bool {
		if clusters[i].Strategy != clusters[j].Strategy {
			return clusters[i].Strategy < clusters[j].Strategy // Hash strategy = 0 comes first
		}
		
		avgConfI := calculateAverageConfidence(clusters[i].Candidates)
		avgConfJ := calculateAverageConfidence(clusters[j].Candidates)
		return avgConfI > avgConfJ
	})

	for _, cluster := range clusters {
		// Check if any files in this cluster are already used
		hasUsedFile := false
		for _, candidate := range cluster.Candidates {
			if usedFiles[candidate.File.ID] {
				hasUsedFile = true
				break
			}
		}

		if !hasUsedFile {
			// Mark all files in this cluster as used
			for _, candidate := range cluster.Candidates {
				usedFiles[candidate.File.ID] = true
			}
			result = append(result, cluster)
		}
	}

	return result
}

// Utility functions

func extractFilename(path string) string {
	parts := strings.Split(path, "/")
	if len(parts) == 0 {
		return path
	}
	return parts[len(parts)-1]
}

func normalizeFilename(filename string) string {
	// Remove extension and normalize
	name := strings.ToLower(filename)
	if idx := strings.LastIndex(name, "."); idx > 0 {
		name = name[:idx]
	}
	
	// Remove common patterns
	name = strings.ReplaceAll(name, "_", " ")
	name = strings.ReplaceAll(name, "-", " ")
	name = strings.TrimSpace(name)
	
	return name
}

func findSimilarGroup(name string, groups map[string][]DuplicateCandidate) string {
	threshold := 0.8
	
	for groupName := range groups {
		if calculateStringSimilarity(name, groupName) >= threshold {
			return groupName
		}
	}
	
	return ""
}

func calculateStringSimilarity(s1, s2 string) float64 {
	if s1 == s2 {
		return 1.0
	}
	
	// Simple Levenshtein-based similarity
	maxLen := len(s1)
	if len(s2) > maxLen {
		maxLen = len(s2)
	}
	
	if maxLen == 0 {
		return 1.0
	}
	
	distance := levenshteinDistance(s1, s2)
	return 1.0 - float64(distance)/float64(maxLen)
}

func levenshteinDistance(s1, s2 string) int {
	if len(s1) == 0 {
		return len(s2)
	}
	if len(s2) == 0 {
		return len(s1)
	}

	matrix := make([][]int, len(s1)+1)
	for i := range matrix {
		matrix[i] = make([]int, len(s2)+1)
		matrix[i][0] = i
	}
	
	for j := 0; j <= len(s2); j++ {
		matrix[0][j] = j
	}

	for i := 1; i <= len(s1); i++ {
		for j := 1; j <= len(s2); j++ {
			cost := 0
			if s1[i-1] != s2[j-1] {
				cost = 1
			}

			matrix[i][j] = min(
				matrix[i-1][j]+1,      // deletion
				matrix[i][j-1]+1,      // insertion
				matrix[i-1][j-1]+cost, // substitution
			)
		}
	}

	return matrix[len(s1)][len(s2)]
}

func min(a, b, c int) int {
	if a < b && a < c {
		return a
	} else if b < c {
		return b
	}
	return c
}

func calculateAverageConfidence(candidates []DuplicateCandidate) float64 {
	if len(candidates) == 0 {
		return 0.0
	}
	
	total := 0.0
	for _, candidate := range candidates {
		total += candidate.Confidence
	}
	
	return total / float64(len(candidates))
}
