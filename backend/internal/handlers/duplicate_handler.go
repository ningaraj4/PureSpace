package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/purespace/backend/internal/services"
)

type DuplicateHandler struct {
	duplicateService *services.DuplicateService
}

func NewDuplicateHandler(duplicateService *services.DuplicateService) *DuplicateHandler {
	return &DuplicateHandler{
		duplicateService: duplicateService,
	}
}

// GetDuplicateGroups returns duplicate file groups for the authenticated user
func (h *DuplicateHandler) GetDuplicateGroups(c *gin.Context) {
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

	// Parse optional limit parameter
	limit := 0
	if limitStr := c.Query("limit"); limitStr != "" {
		if l, err := strconv.Atoi(limitStr); err == nil && l > 0 {
			limit = l
		}
	}

	// Parse include_files parameter
	includeFiles := c.Query("include_files") == "true"

	var groups interface{}
	var err error

	if includeFiles {
		groups, err = h.duplicateService.GetDuplicateGroupsWithFiles(c.Request.Context(), uid, limit)
	} else {
		groups, err = h.duplicateService.GetDuplicateGroups(c.Request.Context(), uid)
	}

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get duplicate groups", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"groups": groups})
}

// GetDuplicateGroupFiles returns files in a specific duplicate group
func (h *DuplicateHandler) GetDuplicateGroupFiles(c *gin.Context) {
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

	sha256 := c.Param("sha256")
	if sha256 == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "SHA256 hash is required"})
		return
	}

	files, err := h.duplicateService.GetDuplicateGroupFiles(c.Request.Context(), uid, sha256)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get duplicate files", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"files": files})
}

type DeleteDuplicatesRequest struct {
	FileIDs []uint `json:"file_ids" binding:"required"`
}

// DeleteDuplicateFiles deletes specified duplicate files
func (h *DuplicateHandler) DeleteDuplicateFiles(c *gin.Context) {
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

	var req DeleteDuplicatesRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request format", "details": err.Error()})
		return
	}

	if err := h.duplicateService.DeleteDuplicateFiles(c.Request.Context(), uid, req.FileIDs); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to delete files", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Files deleted successfully"})
}

// GetLargeFiles returns large files above a size threshold
func (h *DuplicateHandler) GetLargeFiles(c *gin.Context) {
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

	// Parse min_size parameter (default 100MB)
	minSize := int64(100 * 1024 * 1024) // 100MB default
	if minSizeStr := c.Query("min_size"); minSizeStr != "" {
		if ms, err := strconv.ParseInt(minSizeStr, 10, 64); err == nil && ms > 0 {
			minSize = ms
		}
	}

	// Parse limit parameter
	limit := 100 // Default limit
	if limitStr := c.Query("limit"); limitStr != "" {
		if l, err := strconv.Atoi(limitStr); err == nil && l > 0 {
			limit = l
		}
	}

	files, err := h.duplicateService.GetLargeFiles(c.Request.Context(), uid, minSize, limit)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get large files", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"files": files, "min_size": minSize})
}

// AnalyzeDuplicates provides detailed duplicate analysis
func (h *DuplicateHandler) AnalyzeDuplicates(c *gin.Context) {
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

	analysis, err := h.duplicateService.AnalyzeDuplicates(c.Request.Context(), uid)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to analyze duplicates", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, analysis)
}
