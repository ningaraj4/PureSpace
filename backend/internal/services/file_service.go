package services

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/purespace/backend/internal/models"
	"github.com/redis/go-redis/v9"
	"gorm.io/gorm"
)

type FileService struct {
	db    *gorm.DB
	redis *redis.Client
}

func NewFileService(db *gorm.DB, redis *redis.Client) *FileService {
	return &FileService{
		db:    db,
		redis: redis,
	}
}

type UploadMetadataRequest struct {
	DeviceID string     `json:"device_id" binding:"required"`
	Files    []FileItem `json:"files" binding:"required"`
}

type FileItem struct {
	SHA256   string `json:"sha256" binding:"required"`
	Size     int64  `json:"size" binding:"required"`
	Mime     string `json:"mime"`
	PathTail string `json:"path_tail" binding:"required"`
}

// UploadMetadata processes and stores file metadata
func (s *FileService) UploadMetadata(ctx context.Context, userID uuid.UUID, req *UploadMetadataRequest) error {
	// Validate request
	if len(req.Files) == 0 {
		return fmt.Errorf("no files provided")
	}

	if len(req.Files) > 1000 {
		return fmt.Errorf("too many files in single request (max 1000)")
	}

	// Process files in batches
	const batchSize = 100
	for i := 0; i < len(req.Files); i += batchSize {
		end := i + batchSize
		if end > len(req.Files) {
			end = len(req.Files)
		}

		batch := req.Files[i:end]
		if err := s.processBatch(ctx, userID, req.DeviceID, batch); err != nil {
			return fmt.Errorf("failed to process batch: %w", err)
		}
	}

	// Invalidate cache
	s.invalidateUserCache(ctx, userID)

	return nil
}

func (s *FileService) processBatch(ctx context.Context, userID uuid.UUID, deviceID string, files []FileItem) error {
	var dbFiles []models.File

	for _, file := range files {
		// Validate SHA256 format
		if len(file.SHA256) != 64 {
			continue // Skip invalid hashes
		}

		dbFile := models.File{
			UserID:   userID,
			DeviceID: deviceID,
			PathTail: file.PathTail,
			Mime:     file.Mime,
			Size:     file.Size,
			SHA256:   file.SHA256,
		}

		dbFiles = append(dbFiles, dbFile)
	}

	if len(dbFiles) == 0 {
		return nil
	}

	// Upsert files (insert or update on conflict)
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		for _, file := range dbFiles {
			result := tx.Where("user_id = ? AND device_id = ? AND sha256 = ? AND path_tail = ?",
				file.UserID, file.DeviceID, file.SHA256, file.PathTail).
				FirstOrCreate(&file)

			if result.Error != nil {
				return result.Error
			}
		}
		return nil
	})
}

// GetFilesByUser returns all files for a user
func (s *FileService) GetFilesByUser(ctx context.Context, userID uuid.UUID) ([]models.File, error) {
	var files []models.File
	
	result := s.db.WithContext(ctx).
		Where("user_id = ?", userID).
		Order("created_at DESC").
		Find(&files)

	if result.Error != nil {
		return nil, fmt.Errorf("failed to get files: %w", result.Error)
	}

	return files, nil
}

// GetFileStats returns file statistics for a user
func (s *FileService) GetFileStats(ctx context.Context, userID uuid.UUID) (*models.Stats, error) {
	// Try cache first
	cacheKey := fmt.Sprintf("stats:%s", userID.String())
	cached, err := s.redis.Get(ctx, cacheKey).Result()
	if err == nil {
		var stats models.Stats
		if json.Unmarshal([]byte(cached), &stats) == nil {
			return &stats, nil
		}
	}

	// Calculate stats from database
	var totalFiles int64
	var duplicateBytes int64

	// Total files
	s.db.WithContext(ctx).Model(&models.File{}).Where("user_id = ?", userID).Count(&totalFiles)

	// Duplicate bytes calculation
	var duplicateGroups []struct {
		SHA256    string
		Count     int64
		TotalSize int64
	}

	s.db.WithContext(ctx).Model(&models.File{}).
		Select("sha256, COUNT(*) as count, SUM(size) as total_size").
		Where("user_id = ?", userID).
		Group("sha256").
		Having("COUNT(*) > 1").
		Scan(&duplicateGroups)

	for _, group := range duplicateGroups {
		// Potential savings = total size - size of one file
		if group.Count > 1 {
			avgSize := group.TotalSize / group.Count
			duplicateBytes += group.TotalSize - avgSize
		}
	}

	stats := &models.Stats{
		TotalFiles:       int(totalFiles),
		DuplicateBytes:   duplicateBytes,
		PotentialSavings: duplicateBytes,
	}

	// Cache for 5 minutes
	if statsJSON, err := json.Marshal(stats); err == nil {
		s.redis.Set(ctx, cacheKey, statsJSON, 5*time.Minute)
	}

	return stats, nil
}

func (s *FileService) invalidateUserCache(ctx context.Context, userID uuid.UUID) {
	cacheKey := fmt.Sprintf("stats:%s", userID.String())
	s.redis.Del(ctx, cacheKey)
}
