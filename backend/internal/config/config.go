package config

import (
	"github.com/spf13/viper"
)

type Config struct {
	Environment    string `mapstructure:"ENVIRONMENT"`
	Port           string `mapstructure:"PORT"`
	DatabaseURL    string `mapstructure:"DATABASE_URL"`
	RedisURL       string `mapstructure:"REDIS_URL"`
	JWTSecret      string `mapstructure:"JWT_SECRET"`
	AllowedOrigins string `mapstructure:"ALLOWED_ORIGINS"`
}

func Load() (*Config, error) {
	viper.SetDefault("ENVIRONMENT", "development")
	viper.SetDefault("PORT", "8080")
	viper.SetDefault("DATABASE_URL", "postgres://purespace:purespace@localhost:5432/purespace?sslmode=disable")
	viper.SetDefault("REDIS_URL", "localhost:6379")
	viper.SetDefault("ALLOWED_ORIGINS", "*")

	viper.AutomaticEnv()

	// Try to read from .env file
	viper.SetConfigName(".env")
	viper.SetConfigType("env")
	viper.AddConfigPath(".")
	viper.AddConfigPath("./deploy")
	
	// Don't error if .env file doesn't exist
	_ = viper.ReadInConfig()

	var config Config
	if err := viper.Unmarshal(&config); err != nil {
		return nil, err
	}

	return &config, nil
}
