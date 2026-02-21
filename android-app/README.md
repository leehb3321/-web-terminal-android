# Web Terminal Android App

Android client for Web Terminal.

## Features

- Connect to any Web Terminal server
- Save server address and password
- Full terminal access via WebView
- Mobile-optimized keyboard helper
- Fullscreen mode

## Requirements

- Android 7.0 (API 24) or higher
- Android Studio Hedgehog (2023.1.1) or later
- Gradle 8.2+

## Build

### Using Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `android-app` folder and select it
4. Wait for Gradle sync to complete
5. Build > Build APK(s) or Run > Run 'app'

### Using Command Line

```bash
cd android-app
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

## Usage

1. Enter server address (e.g., `192.168.1.100:3000` or `https://terminal.example.com`)
2. Enter password
3. Tap "Connect"
4. Use the terminal as you would in a browser

## Screenshots

| Login Screen | Terminal |
|--------------|----------|
| ![Login](docs/login.png) | ![Terminal](docs/terminal.png) |

## Configuration

- **Server Address**: Full URL or IP:port of your Web Terminal server
- **Password**: The password configured on the server
- **Remember Password**: Toggle to save credentials for automatic login

## Security Notes

- Passwords are stored in Android's encrypted SharedPreferences
- Only connections to the configured server are allowed
- External URLs are blocked in WebView
- Supports both HTTP and HTTPS connections

## License

MIT
