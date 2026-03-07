# MagicAI Android App

Complete Android application for MagicAI platform built with Kotlin + Jetpack Compose.

## Features

- **AI Chat** - Real-time streaming chat with AI assistant
- **AI Writer** - 50+ content generation templates
- **AI Image Generator** - Generate images with DALL-E
- **Voice Over (TTS)** - Text to speech with multiple voices
- **Documents** - Manage all generated content
- **Support** - Create and manage support tickets
- **Subscriptions** - View and manage subscription plans
- **Authentication** - Email/password + Google Sign-In

## Setup

### 1. Configure Base URL

In `app/build.gradle.kts`, update:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://your-actual-domain.com/\"")
```

### 2. Configure Google Sign-In

In Google Cloud Console, create OAuth 2.0 credentials and update:
```kotlin
buildConfigField("String", "GOOGLE_CLIENT_ID", "\"your-google-client-id\"")
```

### 3. Configure RevenueCat (Optional)

For in-app purchases, update:
```kotlin
buildConfigField("String", "REVENUECAT_API_KEY", "\"your-revenuecat-key\"")
```

### 4. Add google-services.json

Place your `google-services.json` from Firebase Console in the `app/` folder.

### 5. Build & Run

```bash
./gradlew assembleDebug
```

## Architecture

- **MVVM** - ViewModel + StateFlow
- **Hilt** - Dependency Injection
- **Retrofit + OkHttp** - API calls with streaming support
- **Room** - Local caching
- **Jetpack Compose** - Modern UI toolkit
- **Navigation Compose** - Type-safe navigation

## API Integration

The app connects to your Laravel backend at `BASE_URL`. All API endpoints from `routes/api.php` are implemented:

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/aichat/chat-send` - Streaming AI chat
- `POST /api/aiwriter/generate-output` - Content generation
- `POST /api/aiimage/generate-image` - Image generation
- `POST /api/aivoiceover/generate` - Voice over
- And 30+ more endpoints

## Project Structure

```
app/src/main/java/com/magicai/app/
├── data/
│   ├── api/          # Retrofit API service
│   ├── models/       # Data models
│   ├── local/        # DataStore / Room
│   └── repository/   # Data repositories
├── di/               # Hilt modules
├── ui/
│   ├── screens/      # All UI screens
│   ├── components/   # Reusable UI components
│   ├── navigation/   # Navigation setup
│   └── theme/        # Material3 theme
├── viewmodel/        # ViewModels
└── utils/            # Utilities
```
