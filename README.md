# StudyMate - Android Task Manager App

A comprehensive task management application built with Android (Kotlin) using Firebase Authentication, Realtime Database, and Cloud Firestore.

## Features

### Core Features
-  User Authentication (Firebase Auth)
-  Task Management (Create, Read, Update, Delete)
-  Real-time synchronization across devices
-  Offline support with automatic sync
-  Task categorization and prioritization
-  Due date and reminder system
-  Task completion tracking with progress indicators

### Advanced Features
-  Smart notifications for overdue and due tasks
-  Task statistics and completion analytics
-  Modern Material Design UI with animations
-  Dark mode support (configurable)
-  Responsive design for all screen sizes
-  Background sync and data persistence
-  Export/Import functionality
-  Search and filter tasks

## Technologies Used

- **Language**: Kotlin
- **Architecture**: MVVM Pattern
- **Database**: Firebase Realtime Database + Cloud Firestore
- **Authentication**: Firebase Authentication
- **UI**: Material Design 3 Components
- **Notifications**: Android Notification API with AlarmManager
- **Storage**: SharedPreferences + Firebase Cloud Storage

## Setup Instructions

### Prerequisites
1. Android Studio Arctic Fox or later
2. Kotlin 1.8.0 or later
3. Android SDK 21+ (Android 5.0+)
4. Google account for Firebase setup

### Firebase Setup

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Create a project"
   - Enter project name: "TaskMaster Pro"
   - Enable Google Analytics (optional)

2. **Add Android App**
   - Click "Add app" → Android
   - Package name: `com.example.basictaskmanagerapp`
   - App nickname: "TaskMaster Pro"
   - Debug signing certificate SHA-1 (optional for testing)

3. **Download Configuration**
   - Download `google-services.json`
   - Place it in `app/` directory

4. **Enable Authentication**
   - Go to Authentication → Sign-in method
   - Enable Email/Password authentication
   - Enable Google Sign-In (optional)

5. **Setup Realtime Database**
   - Go to Realtime Database
   - Create database in test mode
   - Choose region closest to your users
   - Update security rules:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

6. **Setup Cloud Firestore**
   - Go to Firestore Database
   - Create database in test mode
   - Update security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Project Setup

1. **Clone Repository**
```bash
git clone https://github.com/pratish444/TaskManager-App
cd TaskMasterPro
```

2. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. **Add Dependencies**
   
   In `app/build.gradle`:

```gradle
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // Material Design
    implementation 'com.google.android.material:material:1.11.0'
    
    // Architecture Components
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-database'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.firebase:firebase-messaging'
    
    // Google Services
    implementation 'com.google.android.gms:play-services-auth:20.7.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

4. **Add Google Services Plugin**
   
   In `app/build.gradle` (top):

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
}
```

   In project-level `build.gradle`:

```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

### File Structure

```
app/src/main/
├── java/com/example/basictaskmanagerapp/
│   ├── DataModel/
│   │   └── Task.kt
│   ├── database/
│   │   └── DatabaseHelper.kt
│   ├── services/
│   │   ├── TaskSyncService.kt
│   │   ├── BackupService.kt
│   │   └── TaskFirebaseMessagingService.kt
│   ├── utils/
│   │   ├── NotificationHelper.kt
│   │   ├── AlarmHelper.kt
│   │   ├── TaskActionReceiver.kt
│   │   └── BootReceiver.kt
│   ├── LoginActivity.kt
│   ├── SignupActivity.kt
│   ├── MainActivity.kt
│   ├── AddTaskActivity.kt
│   ├── TaskAdapter.kt
│   └── TaskManagerApplication.kt
├── res/
│   ├── layout/
│   │   ├── activity_login.xml
│   │   ├── activity_signup.xml
│   │   ├── activity_main.xml
│   │   ├── activity_add_task.xml
│   │   └── item_task.xml
│   ├── drawable/
│   │   ├── [various vector drawables and shapes]
│   ├── values/
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── xml/
│       ├── network_security_config.xml
│       ├── file_paths.xml
│       ├── data_extraction_rules.xml
│       └── backup_rules.xml
└── AndroidManifest.xml
```

### Build and Run

1. **Sync Project**
   - Tools → Sync Project with Gradle Files

2. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

3. **Run App**
   - Connect Android device or start emulator
   - Click Run button or press Shift + F10

## Configuration

### Notifications
- The app requests notification permissions on Android 13+
- Notifications are automatically scheduled for due and overdue tasks
- Users can snooze reminders or mark tasks as complete directly from notifications

### Offline Support
- All data is cached locally using Firebase offline persistence
- Changes sync automatically when connection is restored
- Users can work completely offline

### Backup & Restore
- User data is automatically backed up to Firebase
- Manual export/import functionality available
- Data is encrypted and securely stored


## Deployment

### Release Build
1. Create keystore for signing
2. Configure signing in `app/build.gradle`
3. Build release APK:
```bash
./gradlew assembleRelease
```

## Security Considerations

- All Firebase security rules are properly configured
- User data is isolated per user account
- Network traffic is secured with HTTPS
- No sensitive data stored in plain text
- Proper input validation and sanitization

## Performance Optimization

- Efficient RecyclerView with ViewHolder pattern
- Image loading optimization
- Minimal memory footprint
- Battery-efficient background processing
- Proper lifecycle management

## Troubleshooting

### Common Issues

1. **Firebase Connection Failed**
   - Check `google-services.json` is in correct location
   - Verify package name matches Firebase configuration
   - Ensure internet connectivity

2. **Notifications Not Working**
   - Check notification permissions
   - Verify notification channels are created
   - Test on physical device (emulator limitations)

3. **Build Errors**
   - Clean and rebuild project
   - Check all dependencies are up to date
   - Verify Kotlin and Gradle versions

4. **Data Not Syncing**
   - Check Firebase rules
   - Verify user authentication
   - Test network connectivity

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please contact [pratishk444@gmail.com] or create an issue in the repository.

---

**TaskMaster Pro** - Organize your life, achieve your goals! 🎯
