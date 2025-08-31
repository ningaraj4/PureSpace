package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/purespace/backend/internal/services"
)

type DuplicateAdvancedHandler struct {
	duplicateDetector *services.DuplicateDetector
}

func NewDuplicateAdvancedHandler(duplicateDetector *services.DuplicateDetector) *DuplicateAdvancedHandler {
	return &DuplicateAdvancedHandler{
		duplicateDetector: duplicateDetector,
	}
}

// DetectDuplicatesAdvanced performs advanced duplicate detection with multiple strategies
func (h *DuplicateAdvancedHandler) DetectDuplicatesAdvanced(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	uid, ok := userID.(uuid.UUID)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Invalid user ID"})
		return
	}

	// Parse strategy parameter
	strategyParam := c.DefaultQuery("strategy", "hash")
	var strategy services.DetectionStrategy

	switch strategyParam {
	case "hash":
		strategy = services.StrategyHash
	case "size":
		strategy = services.StrategySize
	case "size_name":
		strategy = services.StrategySizeAndName
	case "advanced":
		strategy = services.StrategyAdvanced
	default:
		strategy = services.StrategyHash
	}

	clusters, err := h.duplicateDetector.DetectDuplicates(c.Request.Context(), uid, strategy)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   "Failed to detect duplicates",
			"details": err.Error(),
		})
		return
	}

	// Calculate summary statistics
	totalClusters := len(clusters)
	totalDuplicates := 0
	potentialSavings := int64(0)

	for _, cluster := range clusters {
		duplicateCount := cluster.Count - 1 // Subtract one original
		totalDuplicates += duplicateCount
		potentialSavings += int64(duplicateCount) * cluster.Size
	}

	response := gin.H{
		"strategy": strategyParam,
		"summary": gin.H{
			"total_clusters":     totalClusters,
			"total_duplicates":   totalDuplicates,
			"potential_savings":  potentialSavings,
		},
		"clusters": clusters,
	}

	c.JSON(http.StatusOK, response)
}

// GetDuplicateCluster returns details for a specific duplicate cluster
func (h *DuplicateAdvancedHandler) GetDuplicateCluster(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	uid, ok := userID.(uuid.UUID)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Invalid user ID"})
		return
	}

	clusterID := c.Param("cluster_id")
	if clusterID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Cluster ID is required"})
		return
	}

	// For now, we'll use the advanced strategy to find all clusters and filter
	// In a production system, you might want to cache cluster results
	clusters, err := h.duplicateDetector.DetectDuplicates(c.Request.Context(), uid, services.StrategyAdvanced)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":   "Failed to get cluster details",
			"details": err.Error(),
		})
		return
	}

	// Find the specific cluster
	for _, cluster := range clusters {
		if cluster.ID == clusterID {
			c.JSON(http.StatusOK, cluster)
			return
		}
	}

	c.JSON(http.StatusNotFound, gin.H{"error": "Cluster not found"})
}

// CompareDuplicateStrategies compares results from different detection strategies
func (h *DuplicateAdvancedHandler) CompareDuplicateStrategies(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	uid, ok := userID.(uuid.UUID)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Invalid user ID"})
		return
	}

	// Run all strategies
	strategies := []struct {
		Name     string
		Strategy services.DetectionStrategy
	}{
		{"hash", services.StrategyHash},
		{"size", services.StrategySize},
		{"size_name", services.StrategySizeAndName},
		{"advanced", services.StrategyAdvanced},
	}

	comparison := make(map[string]interface{})

	for _, s := range strategies {
		clusters, err := h.duplicateDetector.DetectDuplicates(c.Request.Context(), uid, s.Strategy)
		if err != nil {
			comparison[s.Name] = gin.H{"error": err.Error()}
			continue
		}

		totalClusters := len(clusters)
		totalDuplicates := 0
		potentialSavings := int64(0)
		avgConfidence := 0.0

		for _, cluster := range clusters {
			duplicateCount := cluster.Count - 1
			totalDuplicates += duplicateCount
			potentialSavings += int64(duplicateCount) * cluster.Size

			// Calculate average confidence for this strategy
			clusterConfidence := 0.0
			for _, candidate := range cluster.Candidates {
				clusterConfidence += candidate.Confidence
			}
			if len(cluster.Candidates) > 0 {
				avgConfidence += clusterConfidence / float64(len(cluster.Candidates))
			}
		}

		if totalClusters > 0 {
			avgConfidence /= float64(totalClusters)
		}

		comparison[s.Name] = gin.H{
			"total_clusters":    totalClusters,
			"total_duplicates":  totalDuplicates,
			"potential_savings": potentialSavings,
			"avg_confidence":    avgConfidence,
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"comparison": comparison,
		"recommendation": h.getStrategyRecommendation(comparison),
	})
}

func (h *DuplicateAdvancedHandler) getStrategyRecommendation(comparison map[string]interface{}) string {
	// Simple recommendation logic
	hashResult, hasHash := comparison["hash"].(gin.H)
	advancedResult, hasAdvanced := comparison["advanced"].(gin.H)

	if hasHash && hasAdvanced {
		hashSavings, _ := hashResult["potential_savings"].(int64)
		advancedSavings, _ := advancedResult["potential_savings"].(int64)

		if hashSavings > 0 {
			return "hash - Most accurate for exact duplicates"
		} else if advancedSavings > hashSavings*2 {
			return "advanced - Finds more potential duplicates but requires manual review"
		}
	}

	return "hash - Recommended for most users"
}
