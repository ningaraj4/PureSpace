package handlers

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	"purespace-backend/internal/models"
)

type SubscriptionHandler struct {
	db *gorm.DB
}

func NewSubscriptionHandler(db *gorm.DB) *SubscriptionHandler {
	return &SubscriptionHandler{db: db}
}

type VerifyPurchaseRequest struct {
	PurchaseToken string `json:"purchase_token" binding:"required"`
	ProductID     string `json:"product_id" binding:"required"`
	OrderID       string `json:"order_id"`
}

type SubscriptionResponse struct {
	ID           uuid.UUID `json:"id"`
	UserID       uuid.UUID `json:"user_id"`
	ProductID    string    `json:"product_id"`
	Status       string    `json:"status"`
	ExpiresAt    *time.Time `json:"expires_at,omitempty"`
	PurchaseToken string   `json:"purchase_token"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

// VerifyPurchase verifies a purchase with Google Play and creates/updates subscription
func (h *SubscriptionHandler) VerifyPurchase(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var req VerifyPurchaseRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// TODO: Implement actual Google Play verification
	// For now, we'll create a subscription record assuming the purchase is valid
	
	// Check if subscription already exists
	var existingSub models.Subscription
	err := h.db.Where("user_id = ? AND purchase_token = ?", userID, req.PurchaseToken).First(&existingSub).Error
	
	if err == nil {
		// Subscription already exists, return it
		response := SubscriptionResponse{
			ID:           existingSub.ID,
			UserID:       existingSub.UserID,
			ProductID:    existingSub.ProductID,
			Status:       existingSub.Status,
			ExpiresAt:    existingSub.ExpiresAt,
			PurchaseToken: existingSub.PurchaseToken,
			CreatedAt:    existingSub.CreatedAt,
			UpdatedAt:    existingSub.UpdatedAt,
		}
		c.JSON(http.StatusOK, response)
		return
	}

	// Create new subscription
	subscription := models.Subscription{
		ID:           uuid.New(),
		UserID:       userID.(uuid.UUID),
		ProductID:    req.ProductID,
		Status:       "active",
		PurchaseToken: req.PurchaseToken,
		OrderID:      req.OrderID,
	}

	// Set expiration based on product type
	switch req.ProductID {
	case "premium_monthly":
		expiresAt := time.Now().AddDate(0, 1, 0) // 1 month
		subscription.ExpiresAt = &expiresAt
	case "premium_yearly":
		expiresAt := time.Now().AddDate(1, 0, 0) // 1 year
		subscription.ExpiresAt = &expiresAt
	case "premium_lifetime":
		// No expiration for lifetime
		subscription.ExpiresAt = nil
	}

	if err := h.db.Create(&subscription).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create subscription"})
		return
	}

	response := SubscriptionResponse{
		ID:           subscription.ID,
		UserID:       subscription.UserID,
		ProductID:    subscription.ProductID,
		Status:       subscription.Status,
		ExpiresAt:    subscription.ExpiresAt,
		PurchaseToken: subscription.PurchaseToken,
		CreatedAt:    subscription.CreatedAt,
		UpdatedAt:    subscription.UpdatedAt,
	}

	c.JSON(http.StatusCreated, response)
}

// GetSubscription returns the user's current subscription status
func (h *SubscriptionHandler) GetSubscription(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var subscription models.Subscription
	err := h.db.Where("user_id = ? AND status = ?", userID, "active").
		Order("created_at DESC").
		First(&subscription).Error

	if err != nil {
		if err == gorm.ErrRecordNotFound {
			c.JSON(http.StatusOK, gin.H{
				"has_premium": false,
				"subscription": nil,
			})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch subscription"})
		return
	}

	// Check if subscription is expired
	isPremium := true
	if subscription.ExpiresAt != nil && subscription.ExpiresAt.Before(time.Now()) {
		isPremium = false
		// Update subscription status to expired
		subscription.Status = "expired"
		h.db.Save(&subscription)
	}

	response := SubscriptionResponse{
		ID:           subscription.ID,
		UserID:       subscription.UserID,
		ProductID:    subscription.ProductID,
		Status:       subscription.Status,
		ExpiresAt:    subscription.ExpiresAt,
		PurchaseToken: subscription.PurchaseToken,
		CreatedAt:    subscription.CreatedAt,
		UpdatedAt:    subscription.UpdatedAt,
	}

	c.JSON(http.StatusOK, gin.H{
		"has_premium": isPremium,
		"subscription": response,
	})
}

// CancelSubscription cancels a user's subscription
func (h *SubscriptionHandler) CancelSubscription(c *gin.Context) {
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var subscription models.Subscription
	err := h.db.Where("user_id = ? AND status = ?", userID, "active").
		First(&subscription).Error

	if err != nil {
		if err == gorm.ErrRecordNotFound {
			c.JSON(http.StatusNotFound, gin.H{"error": "No active subscription found"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch subscription"})
		return
	}

	// Update subscription status
	subscription.Status = "cancelled"
	if err := h.db.Save(&subscription).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to cancel subscription"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Subscription cancelled successfully"})
}
