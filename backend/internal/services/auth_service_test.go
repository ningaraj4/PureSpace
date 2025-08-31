package services

import (
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestAuthService_GenerateJWT(t *testing.T) {
	authService := NewAuthService("test-secret-key")
	userID := uuid.New()
	email := "test@example.com"

	token, err := authService.GenerateJWT(userID, email)
	require.NoError(t, err)
	assert.NotEmpty(t, token)

	// Verify token can be parsed
	parsedToken, err := jwt.ParseWithClaims(token, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		return []byte("test-secret-key"), nil
	})
	require.NoError(t, err)
	assert.True(t, parsedToken.Valid)

	claims, ok := parsedToken.Claims.(*Claims)
	require.True(t, ok)
	assert.Equal(t, userID, claims.UserID)
	assert.Equal(t, email, claims.Email)
	assert.Equal(t, "purespace-api", claims.Issuer)
}

func TestAuthService_ValidateJWT(t *testing.T) {
	authService := NewAuthService("test-secret-key")
	userID := uuid.New()
	email := "test@example.com"

	// Generate a valid token
	token, err := authService.GenerateJWT(userID, email)
	require.NoError(t, err)

	// Validate the token
	claims, err := authService.ValidateJWT(token)
	require.NoError(t, err)
	assert.Equal(t, userID, claims.UserID)
	assert.Equal(t, email, claims.Email)
}

func TestAuthService_ValidateJWT_InvalidToken(t *testing.T) {
	authService := NewAuthService("test-secret-key")

	// Test with invalid token
	_, err := authService.ValidateJWT("invalid-token")
	assert.Error(t, err)

	// Test with token signed with different secret
	otherAuthService := NewAuthService("different-secret")
	userID := uuid.New()
	email := "test@example.com"
	
	token, err := otherAuthService.GenerateJWT(userID, email)
	require.NoError(t, err)

	_, err = authService.ValidateJWT(token)
	assert.Error(t, err)
}

func TestAuthService_ValidateJWT_ExpiredToken(t *testing.T) {
	authService := NewAuthService("test-secret-key")
	userID := uuid.New()
	email := "test@example.com"

	// Create an expired token manually
	claims := Claims{
		UserID: userID,
		Email:  email,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(-1 * time.Hour)), // Expired 1 hour ago
			IssuedAt:  jwt.NewNumericDate(time.Now().Add(-2 * time.Hour)),
			NotBefore: jwt.NewNumericDate(time.Now().Add(-2 * time.Hour)),
			Issuer:    "purespace-api",
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString([]byte("test-secret-key"))
	require.NoError(t, err)

	// Validate the expired token
	_, err = authService.ValidateJWT(tokenString)
	assert.Error(t, err)
}

func TestAuthService_ExtractBearerToken(t *testing.T) {
	authService := NewAuthService("test-secret-key")

	tests := []struct {
		name           string
		authHeader     string
		expectedToken  string
		expectedError  bool
	}{
		{
			name:           "Valid bearer token",
			authHeader:     "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
			expectedToken:  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
			expectedError:  false,
		},
		{
			name:           "Missing Bearer prefix",
			authHeader:     "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
			expectedToken:  "",
			expectedError:  true,
		},
		{
			name:           "Empty header",
			authHeader:     "",
			expectedToken:  "",
			expectedError:  true,
		},
		{
			name:           "Only Bearer without token",
			authHeader:     "Bearer",
			expectedToken:  "",
			expectedError:  true,
		},
		{
			name:           "Bearer with extra spaces",
			authHeader:     "Bearer   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
			expectedToken:  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
			expectedError:  false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			token, err := authService.ExtractBearerToken(tt.authHeader)
			
			if tt.expectedError {
				assert.Error(t, err)
				assert.Empty(t, token)
			} else {
				assert.NoError(t, err)
				assert.Equal(t, tt.expectedToken, token)
			}
		})
	}
}
