package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/purespace/backend/internal/config"
	"github.com/purespace/backend/internal/db"
	"github.com/purespace/backend/internal/http/handlers"
	"github.com/purespace/backend/internal/http/middleware"
	"github.com/purespace/backend/internal/services"
	"github.com/purespace/backend/pkg/logger"
	"go.uber.org/zap"
)

func main() {
	// Initialize logger
	logger, err := zap.NewProduction()
	if err != nil {
		log.Fatal("Failed to initialize logger:", err)
	}
	defer logger.Sync()

	// Load configuration
	cfg, err := config.Load()
	if err != nil {
		logger.Fatal("Failed to load config", zap.Error(err))
	}

	// Initialize database
	database, err := db.New(cfg.DatabaseURL)
	if err != nil {
		logger.Fatal("Failed to connect to database", zap.Error(err))
	}

	// Initialize Redis
	redisClient, err := db.NewRedis(cfg.RedisURL)
	if err != nil {
		logger.Fatal("Failed to connect to Redis", zap.Error(err))
	}

	// Initialize services
	authService := services.NewAuthService(cfg.JWTSecret)
	fileService := services.NewFileService(database, redisClient)
	duplicateService := services.NewDuplicateService(database)
	duplicateDetector := services.NewDuplicateDetector(database)

	// Initialize handlers
	authHandler := handlers.NewAuthHandler(authService)
	fileHandler := handlers.NewFileHandler(fileService)
	duplicateHandler := handlers.NewDuplicateHandler(duplicateService)
	duplicateAdvancedHandler := handlers.NewDuplicateAdvancedHandler(duplicateDetector)
	subscriptionHandler := handlers.NewSubscriptionHandler(db)

	// Setup router
	router := setupRouter(cfg, logger, authService, authHandler, fileHandler, duplicateHandler, duplicateAdvancedHandler, subscriptionHandler)

	// Start server
	srv := &http.Server{
		Addr:    ":" + cfg.Port,
		Handler: router,
	}

	// Graceful shutdown
	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Fatal("Failed to start server", zap.Error(err))
		}
	}()

	logger.Info("Server started", zap.String("port", cfg.Port))

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	logger.Info("Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		logger.Fatal("Server forced to shutdown", zap.Error(err))
	}

	logger.Info("Server exited")
}

func setupRouter(cfg *config.Config, logger *zap.Logger, authService *services.AuthService, authHandler *handlers.AuthHandler, fileHandler *handlers.FileHandler, duplicateHandler *handlers.DuplicateHandler, duplicateAdvancedHandler *handlers.DuplicateAdvancedHandler, subscriptionHandler *handlers.SubscriptionHandler) *gin.Engine {
	if cfg.Environment == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	router := gin.New()

	// Middleware
	router.Use(middleware.LoggingMiddleware(logger))
	router.Use(middleware.RequestIDMiddleware())
	router.Use(gin.Recovery())
	router.Use(middleware.CORSMiddleware(cfg.AllowedOrigins))

	// Health check
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	// API routes
	api := router.Group("/api/v1")

	// Auth routes (no auth required)
	auth := api.Group("/auth")
	{
		auth.POST("/login", authHandler.Login)
		auth.POST("/logout", authHandler.Logout)
	}

	// Protected routes
	protected := api.Group("/")
	protected.Use(middleware.AuthMiddleware(authService))
	{
		// User profile
		protected.GET("/profile", authHandler.Profile)

		// File operations
		files := protected.Group("/files")
		{
			files.POST("/metadata", fileHandler.UploadMetadata)
			files.GET("/", fileHandler.GetFiles)
			files.GET("/stats", fileHandler.GetStats)
		}

		// Duplicate operations
		duplicates := protected.Group("/duplicates")
		{
			duplicates.GET("/groups", duplicateHandler.GetDuplicateGroups)
			duplicates.GET("/groups/:sha256/files", duplicateHandler.GetDuplicateGroupFiles)
			duplicates.DELETE("/files", duplicateHandler.DeleteDuplicateFiles)
			duplicates.GET("/analyze", duplicateHandler.AnalyzeDuplicates)
			
			// Advanced duplicate detection
			duplicates.GET("/detect", duplicateAdvancedHandler.DetectDuplicatesAdvanced)
			duplicates.GET("/clusters/:cluster_id", duplicateAdvancedHandler.GetDuplicateCluster)
			duplicates.GET("/compare-strategies", duplicateAdvancedHandler.CompareDuplicateStrategies)
		}

		// Large files
		protected.GET("/large-files", duplicateHandler.GetLargeFiles)
		
		// Subscription operations
		subscriptions := protected.Group("/subscriptions")
		{
			subscriptions.POST("/verify", subscriptionHandler.VerifyPurchase)
			subscriptions.GET("/status", subscriptionHandler.GetSubscription)
			subscriptions.DELETE("/cancel", subscriptionHandler.CancelSubscription)
		}
	}

	return router
}
