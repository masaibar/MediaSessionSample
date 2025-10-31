# CLAUDE.md
 
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
[Most Important] すべてのやり取りは日本語で行うこと

## Project Overview

This is an Android sample application demonstrating Media3 MediaSession implementation for video playback with HLS streaming support. The project showcases both View-based and Compose-based implementations, with background playback capabilities.

Reference: https://developer.android.com/media/media3?hl=ja

## Build Commands

### Build the project
```bash
./gradlew build
```

### Run tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Install on device
```bash
./gradlew installDebug
```

### Clean build
```bash
./gradlew clean
```

## Project Architecture

### Module Structure
- Single-module Android application (`app`)
- Package: `com.masaibar.mediasessionsample`
- Min SDK: 24, Target SDK: 34

### Key Components

#### PlayerService (MediaSessionService)
- Core background service implementing `MediaSessionService`
- Manages ExoPlayer instance for HLS video playback
- Handles custom commands via `MediaSession.Callback`:
  - `PlayHls`: Initiates HLS stream playback
  - `EnteredInForeground`: Tracks when UI is visible
  - `EnteredInBackground`: Tracks when UI is backgrounded
- Implements video end detection using `setPauseAtEndOfMediaItems(true)` + `Player.Listener`
- Configured as foreground service with `mediaPlayback` type

#### Custom Command Pattern
Two sealed interfaces for bidirectional communication:

1. **MediaControllerCommand** (Activity → Service)
   - `PlayHls(url)`: Request playback of HLS URL
   - `EnteredInForeground`: Notify service UI is visible
   - `EnteredInBackground`: Notify service UI is hidden
   - Extension function: `MediaController.notify(command)`

2. **MediaSessionCommand** (Service → Activity)
   - `OnVideoEnded`: Notify activity when video completes
   - Extension function: `MediaSession.notify(context, command)`

#### Activity Implementations

**PlayerActivity** (Simple View-based)
- Direct ExoPlayer usage without MediaSession
- Suitable for in-app only playback
- Uses View Binding with `PlayerView`

**BackgroundPlayerActivity** (View-based with MediaSession)
- Connects to `PlayerService` via `MediaController`
- Manages foreground/background state notifications
- Uses View Binding with `PlayerView`

**BackgroundComposePlayerActivity** (Compose-based with MediaSession)
- Jetpack Compose UI implementation
- Uses `ComposePlayerViewModel` for state management
- Handles video end events to finish activity
- Manages `MediaController` lifecycle in Compose

#### Compose Components

**ComposePlayerScreen**
- Wraps `PlayerView` in AndroidView for Compose
- Manages MediaController connection and lifecycle
- Implements custom command listener for `OnVideoEnded`
- Uses `DisposableEffect` for proper cleanup

**ComposePlayerViewModel**
- Simple ViewModel managing MediaItem state
- Hardcoded test HLS URL (same as other activities)

### Key Technical Details

#### ExoPlayer Configuration
- `setHandleAudioBecomingNoisy(true)`: Auto-pause when headphones disconnect
- `setPauseAtEndOfMediaItems(true)`: Pause instead of looping
- HLS streaming via `HlsMediaSource` and `DefaultHttpDataSource`

#### Lifecycle Management
- Service implements `LifecycleOwner` via `ServiceLifecycleDispatcher`
- Proper cleanup in `onDestroy()` for players and sessions
- MediaController release in Activity lifecycle methods

#### Memory Leak Detection
- LeakCanary integrated for debug builds (see `gradle/libs.versions.toml:13`)

### Test HLS URL
All implementations use: `https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8`

## Key Dependencies

- **Media3**: androidx.media3:media3-exoplayer, media3-session, media3-ui, media3-exoplayer-hls (1.4.1)
- **Compose**: BOM 2024.09.00 with Material3
- **Lifecycle**: 2.8.5 (runtime-ktx, runtime-compose, service)
- **LeakCanary**: 2.14 (debug only)

## Common Development Tasks

### Adding New MediaController Commands
1. Add sealed interface member to `MediaControllerCommand` or `MediaSessionCommand`
2. Define action string constant and bundle parameters
3. Handle in `MediaSession.Callback.onCustomCommand()` (PlayerService)
4. Add listener implementation in MediaController (Activity/Composable)

### Modifying Video Source
Update HLS URL in:
- `PlayerActivity.playStart():48`
- `BackgroundPlayerActivity.onCreate():51`
- `ComposePlayerViewModel.init():20`
- `ComposePlayerScreen.LaunchedEffect():100`

### Testing Background Playback
1. Launch app and select "Open Background Player" or "Open Background Compose Player"
2. Press home button to background the app
3. Playback should continue with notification controls
4. Verify foreground/background state tracking in PlayerService
