# PureSpace Backend API

A Go-based REST API for the PureSpace storage optimization app, providing file metadata management, duplicate detection, and user authentication.

## Features

- **Authentication**: Google OAuth integration with JWT tokens
- **File Management**: Upload and manage file metadata
- **Duplicate Detection**: Identify duplicate files by SHA-256 hash
- **Statistics**: Storage analytics and insights
- **Scalable**: PostgreSQL + Redis for production workloads

## Tech Stack

- **Framework**: Gin (Go web framework)
- **Database**: PostgreSQL with GORM ORM
- **Cache**: Redis for session and statistics caching
- **Authentication**: JWT tokens with Google OAuth
- **Logging**: Zap structured logging
- **Configuration**: Viper for environment management

## Quick Start

### Prerequisites

- Go 1.22+
- PostgreSQL 15+
- Redis 7+
- Docker & Docker Compose (optional)

### Local Development

1. **Clone and setup**:
```bash
cd backend
cp .env.example .env
# Edit .env with your configuration
```

2. **Install dependencies**:
```bash
go mod tidy
```

3. **Run with Docker Compose** (recommended):
```bash
# From project root
docker-compose up -d postgres redis
go run cmd/api/main.go
```

4. **Or run everything with Docker**:
```bash
# From project root
docker-compose up --build
```

### API Endpoints

#### Authentication
- `POST /api/v1/auth/login` - Google OAuth login
- `POST /api/v1/auth/logout` - Logout
- `GET /api/v1/profile` - Get user profile (protected)

#### File Operations
- `POST /api/v1/files/metadata` - Upload file metadata (protected)
- `GET /api/v1/files` - Get user files (protected)
- `GET /api/v1/files/stats` - Get storage statistics (protected)

#### Duplicate Detection
- `GET /api/v1/duplicates/groups` - Get duplicate groups (protected)
- `GET /api/v1/duplicates/groups/:sha256/files` - Get files in duplicate group (protected)
- `DELETE /api/v1/duplicates/files` - Delete duplicate files (protected)
- `GET /api/v1/duplicates/analyze` - Analyze duplicates (protected)

#### Large Files
- `GET /api/v1/large-files` - Get large files above threshold (protected)

#### Health Check
- `GET /health` - Service health status

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `ENVIRONMENT` | Runtime environment | `development` |
| `PORT` | Server port | `8080` |
| `DATABASE_URL` | PostgreSQL connection string | `postgres://purespace:purespace@localhost:5432/purespace?sslmode=disable` |
| `REDIS_URL` | Redis connection string | `localhost:6379` |
| `JWT_SECRET` | JWT signing secret | Required |
| `ALLOWED_ORIGINS` | CORS allowed origins | `*` |

### Database Schema

The API automatically creates the following tables:
- `users` - User accounts and profiles
- `files` - File metadata and hashes
- `reports` - Cleanup operation history
- `subscriptions` - User subscription status

### Development

**Run tests**:
```bash
go test ./...
```

**Build binary**:
```bash
go build -o bin/api cmd/api/main.go
```

**Generate API docs** (if Swagger is configured):
```bash
swag init -g cmd/api/main.go
```

### Deployment

The backend is containerized and ready for production deployment:

1. **Build and push Docker image**:
```bash
docker build -t purespace-backend .
docker tag purespace-backend your-registry/purespace-backend:latest
docker push your-registry/purespace-backend:latest
```

2. **Deploy with Docker Compose**:
```bash
# Update docker-compose.yml with production settings
docker-compose -f docker-compose.prod.yml up -d
```

### Security Considerations

- JWT tokens expire after 24 hours
- Google OAuth tokens are verified server-side
- CORS is configurable per environment
- Database connections use connection pooling
- All endpoints use HTTPS in production
- File metadata only (no raw file content) is stored

### Contributing

1. Follow Go best practices and conventions
2. Add tests for new features
3. Update API documentation
4. Use structured logging with appropriate levels
5. Handle errors gracefully with proper HTTP status codes
