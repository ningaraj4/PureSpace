package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/purespace/backend/internal/models"
	"github.com/purespace/backend/internal/services"
	"gorm.io/gorm"
)

type AuthHandler struct {
	db          *gorm.DB
	authService *services.AuthService
}

func NewAuthHandler(db *gorm.DB, authService *services.AuthService) *AuthHandler {
	return &AuthHandler{
		db:          db,
		authService: authService,
	}
}

type LoginRequest struct {
	IDToken string `json:"id_token" binding:"required"`
}

type LoginResponse struct {
	Token string      `json:"token"`
	User  models.User `json:"user"`
}

// Login handles Google OAuth login
func (h *AuthHandler) Login(c *gin.Context) {
	var req LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request format"})
		return
	}

	// Verify Google token
	tokenInfo, err := h.authService.VerifyGoogleToken(c.Request.Context(), req.IDToken)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid Google token"})
		return
	}

	// Find or create user
	var user models.User
	result := h.db.Where("email = ?", tokenInfo.Email).First(&user)
	
	if result.Error == gorm.ErrRecordNotFound {
		// Create new user
		user = models.User{
			Email:    tokenInfo.Email,
			Provider: "google",
		}
		
		if err := h.db.Create(&user).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create user"})
			return
		}
	} else if result.Error != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Database error"})
		return
	}

	// Generate JWT
	token, err := h.authService.GenerateJWT(user.ID, user.Email)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
		return
	}

	c.JSON(http.StatusOK, LoginResponse{
		Token: token,
		User:  user,
	})
}

// Profile returns the current user's profile
func (h *AuthHandler) Profile(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var user models.User
	if err := h.db.First(&user, userID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "User not found"})
		return
	}

	c.JSON(http.StatusOK, user)
}

// Logout handles user logout (token invalidation would be handled client-side)
func (h *AuthHandler) Logout(c *gin.Context) {
	// In a stateless JWT system, logout is typically handled client-side
	// by removing the token. For enhanced security, you could maintain
	// a blacklist of tokens in Redis.
	c.JSON(http.StatusOK, gin.H{"message": "Logged out successfully"})
}
