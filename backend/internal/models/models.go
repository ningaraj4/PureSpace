package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// User represents a user in the system
type User struct {
	ID        uuid.UUID `json:"id" gorm:"type:uuid;primary_key;default:gen_random_uuid()"`
	Email     string    `json:"email" gorm:"uniqueIndex;not null"`
	Provider  string    `json:"provider" gorm:"not null"` // google, passwordless
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// File represents a file metadata entry
type File struct {
	ID       uint      `json:"id" gorm:"primaryKey"`
	UserID   uuid.UUID `json:"user_id" gorm:"type:uuid;not null;index"`
	DeviceID string    `json:"device_id" gorm:"not null;index"`
	PathTail string    `json:"path_tail" gorm:"not null"`
	Mime     string    `json:"mime"`
	Size     int64     `json:"size" gorm:"not null"`
	SHA256   string    `json:"sha256" gorm:"type:char(64);not null;index"`
	
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	
	// Relationships
	User User `json:"-" gorm:"foreignKey:UserID"`
}

// Report represents a cleanup report
type Report struct {
	ID           uint      `json:"id" gorm:"primaryKey"`
	UserID       uuid.UUID `json:"user_id" gorm:"type:uuid;not null;index"`
	BytesSaved   int64     `json:"bytes_saved" gorm:"not null"`
	ItemsDeleted int       `json:"items_deleted" gorm:"not null"`
	StartedAt    time.Time `json:"started_at" gorm:"not null"`
	CompletedAt  time.Time `json:"completed_at" gorm:"not null"`
	
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	
	// Relationships
	User User `json:"-" gorm:"foreignKey:UserID"`
}

// Subscription represents a user's subscription status
type Subscription struct {
	UserID    uuid.UUID  `json:"user_id" gorm:"type:uuid;primary_key"`
	Tier      string     `json:"tier" gorm:"not null"` // free, premium
	ExpiresAt *time.Time `json:"expires_at"`
	
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	
	// Relationships
	User User `json:"-" gorm:"foreignKey:UserID"`
}

// BeforeCreate sets UUID for User
func (u *User) BeforeCreate(tx *gorm.DB) error {
	if u.ID == uuid.Nil {
		u.ID = uuid.New()
	}
	return nil
}

// DuplicateGroup represents a group of duplicate files
type DuplicateGroup struct {
	SHA256    string `json:"sha256"`
	Count     int    `json:"count"`
	TotalSize int64  `json:"total_size"`
	Files     []File `json:"files,omitempty"`
}

// Stats represents storage statistics
type Stats struct {
	TotalFiles       int   `json:"total_files"`
	DuplicateBytes   int64 `json:"duplicate_bytes"`
	PotentialSavings int64 `json:"potential_savings"`
}
