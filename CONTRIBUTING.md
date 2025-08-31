# Contributing to PureSpace

Thank you for your interest in contributing to PureSpace! We welcome contributions from the community to help make storage optimization better for everyone.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct:
- Be respectful and inclusive
- Focus on constructive feedback
- Help create a welcoming environment for all contributors
- Report any unacceptable behavior to the maintainers

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Go 1.22+
- Git
- Docker and Docker Compose (for backend development)

### Setting Up Development Environment

1. **Fork and Clone**
   ```bash
   git clone https://github.com/your-username/PureSpace.git
   cd PureSpace
   ```

2. **Backend Setup**
   ```bash
   cd backend
   cp .env.example .env
   # Edit .env with your configuration
   docker-compose up -d postgres redis
   go mod tidy
   go run cmd/api/main.go
   ```

3. **Android Setup**
   - Open `android-app` in Android Studio
   - Add your `google-services.json` file
   - Sync and build the project

## How to Contribute

### Reporting Issues
- Use GitHub Issues to report bugs or request features
- Search existing issues before creating new ones
- Provide detailed information including:
  - Steps to reproduce
  - Expected vs actual behavior
  - Device/environment details
  - Screenshots if applicable

### Submitting Changes

1. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Follow the coding standards below
   - Add tests for new functionality
   - Update documentation as needed

3. **Test Your Changes**
   ```bash
   # Backend tests
   cd backend && go test ./...
   
   # Android tests
   cd android-app && ./gradlew test
   ```

4. **Commit and Push**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   git push origin feature/your-feature-name
   ```

5. **Create Pull Request**
   - Use the PR template
   - Link related issues
   - Provide clear description of changes

## Coding Standards

### Android (Kotlin)
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Follow MVVM architecture patterns
- Use Jetpack Compose best practices

### Backend (Go)
- Follow [Go Code Review Comments](https://github.com/golang/go/wiki/CodeReviewComments)
- Use `gofmt` and `golint`
- Add godoc comments for exported functions
- Follow RESTful API design principles
- Use structured logging with Zap

### General Guidelines
- Write self-documenting code
- Keep functions small and focused
- Use descriptive commit messages
- Add unit tests for new features
- Update documentation for API changes

## Commit Message Format

Use conventional commits format:
```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Examples:
```
feat(android): add premium billing integration
fix(backend): resolve duplicate detection race condition
docs(readme): update installation instructions
```

## Testing Guidelines

### Unit Tests
- Write tests for all new functionality
- Aim for >80% code coverage
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

### Integration Tests
- Test API endpoints with real database
- Test Android UI components
- Verify cross-component interactions

### Manual Testing
- Test on different Android versions
- Verify UI on various screen sizes
- Test offline functionality
- Validate premium features

## Documentation

### Code Documentation
- Add inline comments for complex logic
- Document public APIs thoroughly
- Include usage examples
- Keep documentation up to date

### User Documentation
- Update README for new features
- Add screenshots for UI changes
- Document configuration options
- Provide troubleshooting guides

## Review Process

### Pull Request Reviews
- All PRs require at least one review
- Address reviewer feedback promptly
- Keep PRs focused and reasonably sized
- Ensure CI checks pass

### Review Criteria
- Code quality and style
- Test coverage
- Documentation completeness
- Performance impact
- Security considerations

## Release Process

### Versioning
We use [Semantic Versioning](https://semver.org/):
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes (backward compatible)

### Release Checklist
- [ ] Update version numbers
- [ ] Update CHANGELOG.md
- [ ] Run full test suite
- [ ] Create release notes
- [ ] Tag the release
- [ ] Deploy to production

## Community

### Communication Channels
- GitHub Issues: Bug reports and feature requests
- GitHub Discussions: General questions and ideas
- Email: contact@purespace.app

### Getting Help
- Check existing documentation
- Search GitHub Issues
- Ask in GitHub Discussions
- Contact maintainers directly

## Recognition

Contributors are recognized in:
- CONTRIBUTORS.md file
- Release notes
- App credits section

## License

By contributing to PureSpace, you agree that your contributions will be licensed under the MIT License.

---

Thank you for helping make PureSpace better! 🚀
