package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
)

func CORSMiddleware(allowedOrigins string) gin.HandlerFunc {
	return func(c *gin.Context) {
		origin := c.Request.Header.Get("Origin")
		
		// Check if origin is allowed
		if allowedOrigins == "*" || isOriginAllowed(origin, allowedOrigins) {
			c.Header("Access-Control-Allow-Origin", origin)
		}
		
		c.Header("Access-Control-Allow-Credentials", "true")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
		c.Header("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT, DELETE, PATCH")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}

		c.Next()
	}
}

func isOriginAllowed(origin, allowedOrigins string) bool {
	if origin == "" {
		return false
	}
	
	origins := strings.Split(allowedOrigins, ",")
	for _, allowed := range origins {
		allowed = strings.TrimSpace(allowed)
		if allowed == origin || allowed == "*" {
			return true
		}
	}
	
	return false
}
