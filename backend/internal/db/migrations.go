package db

import (
	"github.com/purespace/backend/internal/models"
	"gorm.io/gorm"
)

func autoMigrate(db *gorm.DB) error {
	return db.AutoMigrate(
		&models.User{},
		&models.File{},
		&models.Report{},
		&models.Subscription{},
	)
}
