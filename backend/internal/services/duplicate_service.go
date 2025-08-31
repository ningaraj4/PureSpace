package services

import (
	"context"
	"fmt"
	"sort"

	"github.com/google/uuid"
	"github.com/purespace/backend/internal/models"
	"gorm.io/gorm"
)

type DuplicateService struct {
	db *gorm.DB
}

func NewDuplicateService(db *gorm.DB) *DuplicateService {
	return &DuplicateService{
		db: db,
	}
}

// GetDuplicateGroups returns groups of duplicate files for a user
func (s *DuplicateService) GetDuplicateGroups(ctx context.Context, userID uuid.UUID) ([]models.DuplicateGroup, error) {
	var groups []models.DuplicateGroup

	// Find files with same SHA256 hash (duplicates)
	rows, err := s.db.WithContext(ctx).Raw(`
		SELECT 
			sha256,
			COUNT(*) as count,
			SUM(size) as total_size
		FROM files 
		WHERE user_id = ? 
		GROUP BY sha256 
		HAVING COUNT(*) > 1
		ORDER BY total_size DESC
	`, userID).Rows()

	if err != nil {
		return nil, fmt.Errorf("failed to query duplicate groups: %w", err)
	}
	defer rows.Close()

	for rows.Next() {
		var group models.DuplicateGroup
		if err := rows.Scan(&group.SHA256, &group.Count, &group.TotalSize); err != nil {
			continue
		}
		groups = append(groups, group)
	}

	return groups, nil
}

// GetDuplicateGroupFiles returns files in a specific duplicate group
func (s *DuplicateService) GetDuplicateGroupFiles(ctx context.Context, userID uuid.UUID, sha256 string) ([]models.File, error) {
	var files []models.File

	result := s.db.WithContext(ctx).
		Where("user_id = ? AND sha256 = ?", userID, sha256).
		Order("created_at ASC").
		Find(&files)

	if result.Error != nil {
		return nil, fmt.Errorf("failed to get duplicate files: %w", result.Error)
	}

	return files, nil
}

// GetDuplicateGroupsWithFiles returns duplicate groups with their files
func (s *DuplicateService) GetDuplicateGroupsWithFiles(ctx context.Context, userID uuid.UUID, limit int) ([]models.DuplicateGroup, error) {
	groups, err := s.GetDuplicateGroups(ctx, userID)
	if err != nil {
		return nil, err
	}

	// Apply limit if specified
	if limit > 0 && len(groups) > limit {
		groups = groups[:limit]
	}

	// Fetch files for each group
	for i := range groups {
		files, err := s.GetDuplicateGroupFiles(ctx, userID, groups[i].SHA256)
		if err != nil {
			continue // Skip this group on error
		}
		groups[i].Files = files
	}

	return groups, nil
}

// DeleteDuplicateFiles deletes specified files from a duplicate group
func (s *DuplicateService) DeleteDuplicateFiles(ctx context.Context, userID uuid.UUID, fileIDs []uint) error {
	if len(fileIDs) == 0 {
		return fmt.Errorf("no file IDs provided")
	}

	// Verify all files belong to the user
	var count int64
	s.db.WithContext(ctx).Model(&models.File{}).
		Where("id IN ? AND user_id = ?", fileIDs, userID).
		Count(&count)

	if count != int64(len(fileIDs)) {
		return fmt.Errorf("some files don't belong to user or don't exist")
	}

	// Delete the files
	result := s.db.WithContext(ctx).
		Where("id IN ? AND user_id = ?", fileIDs, userID).
		Delete(&models.File{})

	if result.Error != nil {
		return fmt.Errorf("failed to delete files: %w", result.Error)
	}

	return nil
}

// GetLargeFiles returns files above a certain size threshold
func (s *DuplicateService) GetLargeFiles(ctx context.Context, userID uuid.UUID, minSize int64, limit int) ([]models.File, error) {
	var files []models.File

	query := s.db.WithContext(ctx).
		Where("user_id = ? AND size >= ?", userID, minSize).
		Order("size DESC")

	if limit > 0 {
		query = query.Limit(limit)
	}

	result := query.Find(&files)
	if result.Error != nil {
		return nil, fmt.Errorf("failed to get large files: %w", result.Error)
	}

	return files, nil
}

// AnalyzeDuplicates provides detailed analysis of duplicate files
func (s *DuplicateService) AnalyzeDuplicates(ctx context.Context, userID uuid.UUID) (*DuplicateAnalysis, error) {
	groups, err := s.GetDuplicateGroups(ctx, userID)
	if err != nil {
		return nil, err
	}

	analysis := &DuplicateAnalysis{
		TotalGroups:      len(groups),
		TotalDuplicates:  0,
		PotentialSavings: 0,
		LargestGroup:     nil,
	}

	var largestGroupSize int64 = 0

	for _, group := range groups {
		// Count duplicates (total files - 1 original)
		duplicateCount := group.Count - 1
		analysis.TotalDuplicates += duplicateCount

		// Calculate potential savings (size of duplicates)
		avgFileSize := group.TotalSize / int64(group.Count)
		savings := avgFileSize * int64(duplicateCount)
		analysis.PotentialSavings += savings

		// Track largest group
		if group.TotalSize > largestGroupSize {
			largestGroupSize = group.TotalSize
			analysis.LargestGroup = &group
		}
	}

	return analysis, nil
}

type DuplicateAnalysis struct {
	TotalGroups      int                    `json:"total_groups"`
	TotalDuplicates  int                    `json:"total_duplicates"`
	PotentialSavings int64                  `json:"potential_savings"`
	LargestGroup     *models.DuplicateGroup `json:"largest_group,omitempty"`
}
