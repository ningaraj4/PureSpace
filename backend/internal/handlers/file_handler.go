package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/purespace/backend/internal/services"
)

type FileHandler struct {
	fileService *services.FileService
}

func NewFileHandler(fileService *services.FileService) *FileHandler {
	return &FileHandler{
		fileService: fileService,
	}
}

// UploadMetadata handles file metadata upload
func (h *FileHandler) UploadMetadata(c *gin.Context) {
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

	var req services.UploadMetadataRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request format", "details": err.Error()})
		return
	}

	if err := h.fileService.UploadMetadata(c.Request.Context(), uid, &req); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to upload metadata", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Metadata uploaded successfully"})
}

// GetFiles returns all files for the authenticated user
func (h *FileHandler) GetFiles(c *gin.Context) {
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

	files, err := h.fileService.GetFilesByUser(c.Request.Context(), uid)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get files", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"files": files})
}

// GetStats returns file statistics for the authenticated user
func (h *FileHandler) GetStats(c *gin.Context) {
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

	stats, err := h.fileService.GetFileStats(c.Request.Context(), uid)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get stats", "details": err.Error()})
		return
	}

	c.JSON(http.StatusOK, stats)
}
