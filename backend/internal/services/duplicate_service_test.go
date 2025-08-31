package services

import (
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"purespace-backend/internal/models"
)

func setupTestDB(t *testing.T) *gorm.DB {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	require.NoError(t, err)

	err = db.AutoMigrate(&models.User{}, &models.File{}, &models.Report{}, &models.Subscription{})
	require.NoError(t, err)

	return db
}

func TestDuplicateService_GetDuplicateGroups(t *testing.T) {
	db := setupTestDB(t)
	service := NewDuplicateService(db)
	userID := uuid.New()

	// Create test files with duplicates
	files := []models.File{
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "file1.jpg",
			Path:     "/path/file1.jpg",
			Size:     1024,
			MimeType: "image/jpeg",
			Hash:     "hash1",
		},
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "file2.jpg",
			Path:     "/path/file2.jpg",
			Size:     1024,
			MimeType: "image/jpeg",
			Hash:     "hash1", // Same hash = duplicate
		},
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "file3.jpg",
			Path:     "/path/file3.jpg",
			Size:     2048,
			MimeType: "image/jpeg",
			Hash:     "hash2", // Unique hash
		},
	}

	for _, file := range files {
		err := db.Create(&file).Error
		require.NoError(t, err)
	}

	groups, err := service.GetDuplicateGroups(userID)
	require.NoError(t, err)

	assert.Len(t, groups, 1)
	assert.Equal(t, "hash1", groups[0].Hash)
	assert.Equal(t, 2, groups[0].Count)
	assert.Equal(t, int64(2048), groups[0].TotalSize) // 1024 * 2
}

func TestDuplicateService_GetDuplicateGroupFiles(t *testing.T) {
	db := setupTestDB(t)
	service := NewDuplicateService(db)
	userID := uuid.New()
	hash := "test-hash"

	// Create test files with same hash
	files := []models.File{
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "duplicate1.jpg",
			Path:     "/path/duplicate1.jpg",
			Size:     1024,
			MimeType: "image/jpeg",
			Hash:     hash,
		},
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "duplicate2.jpg",
			Path:     "/path/duplicate2.jpg",
			Size:     1024,
			MimeType: "image/jpeg",
			Hash:     hash,
		},
	}

	for _, file := range files {
		err := db.Create(&file).Error
		require.NoError(t, err)
	}

	duplicateFiles, err := service.GetDuplicateGroupFiles(userID, hash)
	require.NoError(t, err)

	assert.Len(t, duplicateFiles, 2)
	assert.Equal(t, hash, duplicateFiles[0].Hash)
	assert.Equal(t, hash, duplicateFiles[1].Hash)
}

func TestDuplicateService_GetLargeFiles(t *testing.T) {
	db := setupTestDB(t)
	service := NewDuplicateService(db)
	userID := uuid.New()

	// Create test files with different sizes
	files := []models.File{
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "small.jpg",
			Path:     "/path/small.jpg",
			Size:     1024, // 1KB
			MimeType: "image/jpeg",
			Hash:     "hash1",
		},
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "large.mp4",
			Path:     "/path/large.mp4",
			Size:     104857600, // 100MB
			MimeType: "video/mp4",
			Hash:     "hash2",
		},
	}

	for _, file := range files {
		err := db.Create(&file).Error
		require.NoError(t, err)
	}

	largeFiles, err := service.GetLargeFiles(userID, 50*1024*1024) // 50MB threshold
	require.NoError(t, err)

	assert.Len(t, largeFiles, 1)
	assert.Equal(t, "large.mp4", largeFiles[0].Name)
	assert.Equal(t, int64(104857600), largeFiles[0].Size)
}

func TestDuplicateService_AnalyzeDuplicates(t *testing.T) {
	db := setupTestDB(t)
	service := NewDuplicateService(db)
	userID := uuid.New()

	// Create test files
	files := []models.File{
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "file1.jpg",
			Path:     "/path/file1.jpg",
			Size:     1024,
			MimeType: "image/jpeg",
			Hash:     "hash1",
		},
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "file2.jpg",
			Path:     "/path/file2.jpg",
			Size:     1024,
			MimeType: "image/jpeg",
			Hash:     "hash1", // Duplicate
		},
		{
			ID:       uuid.New(),
			UserID:   userID,
			Name:     "file3.jpg",
			Path:     "/path/file3.jpg",
			Size:     2048,
			MimeType: "image/jpeg",
			Hash:     "hash2", // Unique
		},
	}

	for _, file := range files {
		err := db.Create(&file).Error
		require.NoError(t, err)
	}

	analysis, err := service.AnalyzeDuplicates(userID)
	require.NoError(t, err)

	assert.Equal(t, 1, analysis.DuplicateGroups)
	assert.Equal(t, 2, analysis.DuplicateFiles)
	assert.Equal(t, int64(1024), analysis.WastedSpace) // Size of one duplicate file
	assert.Equal(t, 3, analysis.TotalFiles)
	assert.Equal(t, int64(4096), analysis.TotalSize) // 1024 + 1024 + 2048
}

func TestDuplicateService_DeleteFiles(t *testing.T) {
	db := setupTestDB(t)
	service := NewDuplicateService(db)
	userID := uuid.New()

	// Create test file
	file := models.File{
		ID:       uuid.New(),
		UserID:   userID,
		Name:     "test.jpg",
		Path:     "/path/test.jpg",
		Size:     1024,
		MimeType: "image/jpeg",
		Hash:     "test-hash",
		IsDeleted: false,
	}

	err := db.Create(&file).Error
	require.NoError(t, err)

	// Delete the file
	err = service.DeleteFiles(userID, []uuid.UUID{file.ID})
	require.NoError(t, err)

	// Verify file is marked as deleted
	var updatedFile models.File
	err = db.First(&updatedFile, file.ID).Error
	require.NoError(t, err)
	assert.True(t, updatedFile.IsDeleted)
}
