# PureSpace – Smart Storage Optimization App

[![Android CI](https://github.com/your-username/PureSpace/actions/workflows/android-ci.yml/badge.svg)](https://github.com/your-username/PureSpace/actions/workflows/android-ci.yml)
[![Backend CI](https://github.com/your-username/PureSpace/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/your-username/PureSpace/actions/workflows/backend-ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

PureSpace is a comprehensive storage optimization solution that helps users reclaim valuable storage space on their Android devices through intelligent duplicate detection, large file analysis, and smart cleanup recommendations.

## 🚀 Features

### Core Features
- **Smart Duplicate Detection**: Advanced algorithms to find exact and similar duplicates
- **Large File Analysis**: Identify space-consuming files across your device
- **Storage Analytics**: Detailed insights into storage usage patterns
- **Safe Cleanup**: Intelligent recommendations with undo functionality
- **Multi-Device Sync**: Cloud synchronization across multiple devices

### Premium Features
- **Unlimited Scanning**: No limits on duplicate detection and analysis
- **Advanced AI Algorithms**: Similarity-based duplicate detection
- **Automatic Scheduled Scans**: Set up daily, weekly, or monthly scans
- **Priority Support**: Get help when you need it
- **Export Reports**: Detailed cleanup and analysis reports
- **Ad-Free Experience**: Enjoy PureSpace without advertisements

## 🏗️ Architecture

PureSpace follows a modern, scalable architecture with:

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **Dependency Injection**: Hilt
- **Background Processing**: WorkManager
- **Authentication**: Firebase Auth + Custom JWT
- **Billing**: Google Play Billing

### Backend API
- **Language**: Go 1.22
- **Framework**: Gin Web Framework
- **Database**: PostgreSQL with GORM
- **Caching**: Redis
- **Authentication**: JWT with Google OAuth verification
- **Logging**: Structured logging with Zap
- **Deployment**: Docker + Docker Compose

## 📱 Screenshots

[Add screenshots here when available]

## 🛠️ Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Go 1.22+
- Docker and Docker Compose
- PostgreSQL 15+
- Redis 7+

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/PureSpace.git
   cd PureSpace
   ```

2. **Set up environment variables**
   ```bash
   cd backend
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start services with Docker Compose**
   ```bash
   docker-compose up -d postgres redis
   ```

4. **Run the backend**
   ```bash
   cd backend
   go mod tidy
   go run cmd/api/main.go
   ```

### Android Setup

1. **Open Android Studio**
   - Open the `android-app` directory in Android Studio

2. **Configure Firebase**
   - Add your `google-services.json` file to `android-app/app/`
   - Update Firebase configuration as needed

3. **Build and run**
   - Sync project with Gradle files
   - Run the app on an emulator or device

## 🧪 Testing

### Backend Tests
```bash
cd backend
go test -v ./...
```

### Android Tests
```bash
cd android-app
./gradlew test
./gradlew connectedAndroidTest
```

## 🚀 Deployment

### Backend Deployment
The backend is containerized and can be deployed using Docker:

```bash
docker build -t purespace-backend ./backend
docker run -p 8080:8080 purespace-backend
```

### Android Release
Build release APK/AAB:

```bash
cd android-app
./gradlew assembleRelease
./gradlew bundleRelease
```

## 📊 API Documentation

### Authentication Endpoints
- `POST /api/v1/auth/login` - Google OAuth login
- `POST /api/v1/auth/logout` - User logout
- `GET /api/v1/profile` - Get user profile

### File Management
- `POST /api/v1/files/metadata` - Upload file metadata
- `GET /api/v1/files` - Get user files
- `GET /api/v1/files/stats` - Get storage statistics

### Duplicate Detection
- `GET /api/v1/duplicates/groups` - Get duplicate groups
- `GET /api/v1/duplicates/detect` - Advanced duplicate detection
- `DELETE /api/v1/duplicates/files` - Delete duplicate files

### Subscription Management
- `POST /api/v1/subscriptions/verify` - Verify purchase
- `GET /api/v1/subscriptions/status` - Get subscription status
- `DELETE /api/v1/subscriptions/cancel` - Cancel subscription

## 🔒 Privacy & Security

PureSpace prioritizes user privacy and security:

- **No File Content Upload**: Only metadata (hashes, sizes) is synced
- **Local Processing**: File scanning happens on-device
- **Encrypted Storage**: Sensitive data is encrypted at rest
- **Secure Authentication**: JWT tokens with Google OAuth verification
- **GDPR Compliant**: Full user data control and deletion rights

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📞 Support

- **Documentation**: [Wiki](https://github.com/your-username/PureSpace/wiki)
- **Issues**: [GitHub Issues](https://github.com/your-username/PureSpace/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/PureSpace/discussions)
- **Email**: support@purespace.app

## 🗺️ Roadmap

### Upcoming Features
- [ ] Machine learning-based duplicate detection
- [ ] Integration with cloud storage providers
- [ ] Advanced file organization suggestions
- [ ] Multi-language support (Hindi, Tamil, Telugu, etc.)
- [ ] Desktop companion app

### Version History
- **v1.0.0** - Initial release with core features
- **v1.1.0** - Premium features and billing integration
- **v1.2.0** - Advanced duplicate detection algorithms
- **v2.0.0** - Multi-device sync and cloud features

## 🙏 Acknowledgments

- Inspired by [twpayne/find-duplicates](https://github.com/twpayne/find-duplicates)
- Built with love using modern Android and Go technologies
- Special thanks to the open-source community

---

**Find. Clean. Breathe easy.**

A production-ready mobile + backend system that scans device storage, detects duplicate/junk/large/unused files, and provides actionable storage optimization insights with enterprise-level scalability.

## 🎯 Features

- **Smart Storage Scanner**: Scan files (images, videos, documents, audio, APKs)
- **Duplicate & Junk Cleaner**: Detect duplicates using SHA-256 hashing
- **Optimization Tools**: Delete, Archive, Move to Cloud (Google Drive/Dropbox)
- **Reports & Insights**: Dashboard with storage analytics and optimization history
- **Premium Features**: Scheduled scans, cloud sync, advanced cleanup suggestions
- **Multi-language Support**: English + Indian regional languages

## 🏗 Architecture

### Android App
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture (ui/domain/data layers)
- **DI**: Hilt
- **Local DB**: Room
- **Networking**: Retrofit + OkHttp
- **Background**: WorkManager
- **Auth**: Firebase Auth
- **Billing**: Google Play Billing v6

### Backend
- **Language**: Go 1.22+
- **Framework**: Gin
- **Database**: PostgreSQL
- **Cache**: Redis
- **Auth**: JWT
- **Deployment**: Docker + Docker Compose

## 📁 Project Structure

```
purespace/
├── android-app/          # Android Kotlin app
│   ├── app/
│   └── build.gradle.kts
├── backend/              # Go backend service
│   ├── cmd/api/
│   ├── internal/
│   └── go.mod
├── deploy/               # Docker & deployment
│   ├── docker-compose.yml
│   └── Dockerfile.api
├── docs/                 # Documentation
└── .github/workflows/    # CI/CD pipelines
```

## 🚀 Quick Start

### Prerequisites
- Android Studio (latest)
- JDK 17+
- Go 1.22+
- Docker & Docker Compose
- PostgreSQL 16+
- Redis 7+

### Backend Setup

1. **Start services with Docker Compose**:
```bash
cd deploy
docker-compose up -d
```

2. **Run backend locally**:
```bash
cd backend
go mod tidy
go run cmd/api/main.go
```

The API will be available at `http://localhost:8080`

### Android Setup

1. **Open in Android Studio**:
```bash
cd android-app
# Open in Android Studio
```

2. **Configure Firebase**:
   - Add your `google-services.json` to `app/`
   - Update Firebase project settings

3. **Build and run**:
```bash
./gradlew assembleDebug
```

## 🔧 Configuration

### Environment Variables

Create `deploy/.env` from `deploy/.env.example`:

```env
DB_DSN=postgres://purespace:purespace@localhost:5432/purespace?sslmode=disable
REDIS_ADDR=localhost:6379
JWT_SECRET=your-jwt-secret-here
PORT=8080
ALLOWED_ORIGINS=*
```

### Android Configuration

Add to `android-app/local.properties`:

```properties
BACKEND_URL="http://10.0.2.2:8080"
FIREBASE_PROJECT_ID="your-firebase-project"
```

## 📱 App Features

### Core Functionality
- **File Scanning**: MediaStore API integration with SHA-256 hashing
- **Duplicate Detection**: Group files by hash with preview thumbnails
- **Large Files**: Sort and filter by size thresholds
- **Cleanup Actions**: SAF-based delete/move with user confirmation
- **Background Sync**: WorkManager for scheduled operations

### Premium Features (Google Play Billing)
- Automatic scheduled scans
- Cloud sync of reports & optimization history
- Ad-free experience
- Advanced cleanup suggestions

### Security & Privacy
- No file contents uploaded (metadata only)
- Scoped Storage compliance
- JWT-based API authentication
- TLS encryption for all network calls

## 🧪 Testing

### Android Tests
```bash
cd android-app
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

### Backend Tests
```bash
cd backend
go test ./...                     # Unit tests
go test ./test/integration/...    # Integration tests
```

## 🚢 Deployment

### Production Backend
```bash
cd deploy
docker-compose -f docker-compose.prod.yml up -d
```

### Android Release
```bash
cd android-app
./gradlew assembleRelease
```

## 📊 API Documentation

API documentation is available at `/v1/swagger/` when running the backend.

Key endpoints:
- `POST /v1/auth/login` - Authentication
- `POST /v1/upload-metadata` - Upload file metadata
- `GET /v1/duplicates` - Get duplicate groups
- `GET /v1/stats` - Storage statistics
- `GET /v1/history` - Cleanup history

## 🔒 Privacy & Security

- **Data Collection**: Only file metadata (hash, size, mime type, path)
- **Storage**: Local-first with optional cloud sync
- **Permissions**: Minimal required permissions (MediaStore + SAF)
- **Compliance**: Google Play Data Safety requirements

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For issues and support:
- Create an issue on GitHub
- Check the documentation in `docs/`
- Review the troubleshooting guide

---

**Built with ❤️ for efficient storage management**
