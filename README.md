# 📱 Rivava TrackFi - Android Application

Native, modern Android financial intelligence and expense management application built using **Kotlin**, **Jetpack Compose**, and **Clean Architecture**.

---

## 🌟 Key Features

- 📊 **Smart Dashboard & Analytics:** Real-time expense breakdown, monthly budgeting, and category insights.
- 📩 **Automatic SMS Expense Tracker:** On-device SMS parsing for banking transactions and credit/debit alerts.
- 💼 **Investment & Portfolio Tracking:** Live market insights powered by Alpha Vantage & Finnhub APIs.
- 🤖 **AI Financial Review:** Automated smart tips and budget optimization suggestions.
- ⭐ **Elite Membership & Advisory:** Integrated payment gateway, booking advisory sessions, and premium financial tools.
- 🔐 **Biometric & Cloud Sync:** Biometric authentication, offline-first Room database, and Firebase Cloud Firestore synchronisation.
- 🎨 **Modern Jetpack Compose UI:** Fluid dark/light theme, custom financial charts, and glassmorphic micro-animations.

---

## 🏛 Tech Stack & Architecture

- **Language:** Kotlin (1.9.22)
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** Clean Architecture + MVVM (Data, Domain, UI layers)
- **Dependency Injection:** Dagger Hilt
- **Local Persistence:** Room Database + DataStore Preferences
- **Network / API:** Retrofit 2 + OkHttp + Moshi / Gson
- **Async & Reactive:** Kotlin Coroutines + StateFlow / SharedFlow
- **Firebase Services:** Firebase Auth, Cloud Firestore, Cloud Messaging (FCM)
- **Target SDK:** 35 (Android 15) | **Min SDK:** 24 (Android 7.0)

---

## 📂 Project Structure

```
mobile/
├── app/
│   ├── src/main/java/com/rivavafi/universal/
│   │   ├── data/             # Repositories, Room DB, DAO, Remote API clients
│   │   ├── di/               # Hilt Dependency Injection Modules
│   │   ├── domain/           # Models, UseCases, Business Logic
│   │   ├── sms/              # SMS Parsing & Banking Detection Engine
│   │   ├── ui/               # Jetpack Compose Screens & ViewModels
│   │   │   ├── add/          # Add Expense / Transaction Screen
│   │   │   ├── aireview/     # AI Review & Insights
│   │   │   ├── analytics/    # Deep Financial Analytics & Charts
│   │   │   ├── auth/         # Phone OTP & Google Login
│   │   │   ├── calculator/   # Financial & Investment Calculators
│   │   │   ├── elite/        # Elite Membership & Advisory Booking
│   │   │   ├── history/      # Transaction History & Filter
│   │   │   ├── home/         # Main Home Dashboard
│   │   │   ├── onboarding/   # App Onboarding flow
│   │   │   ├── portfolio/    # Stock & Crypto Portfolio
│   │   │   ├── profile/      # User Profile & KYC
│   │   │   ├── settings/     # Security, Preferences & Sync
│   │   │   └── theme/        # Color, Typography & Shape Design System
│   │   └── utils/            # Extensions, Date Formatters, Key Validators
│   ├── src/main/res/         # Drawables, Strings, Icons, XML Configs
│   ├── build.gradle.kts      # Application-level Gradle Build Script
│   └── google-services.json  # Firebase Configuration File
├── gradle/                   # Gradle Wrapper Files
├── build.gradle.kts          # Root-level Gradle Build Script
├── settings.gradle.kts       # Repository & Plugin Management
└── local.properties          # Local SDK & Secret API Keys (Git ignored)
```

---

## ⚙️ Setup & Configuration

### 1. Prerequisites
- **Android Studio:** Hedgehog | Iguana | Jellyfish | Koala or newer
- **JDK:** Java 17 or Java 21 (configured in Android Studio Gradle settings)
- **Android SDK:** API 35 installed

### 2. Configure `local.properties`
Create or edit `local.properties` inside the `mobile/` directory:

```properties
sdk.dir=/path/to/your/Android/Sdk

# Market Data API Keys (Optional for live stock data)
alphavantage.apikey=your_alpha_vantage_key
finnhub.apikey=your_finnhub_key
resend.apikey=your_resend_api_key

# Release Signing Keys (Optional for local debug builds)
RELEASE_STORE_PASSWORD=your_store_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```

### 3. Firebase Setup
Ensure [`mobile/app/google-services.json`](file:///e:/Rivavatrackfi-app/mobile/app/google-services.json) is present with your Firebase project credentials.

---

## 🚀 Build & Run Commands

From the `mobile/` directory:

### Debug Build
```bash
# On Linux / macOS
./gradlew assembleDebug

# On Windows (PowerShell / Command Prompt)
.\gradlew.bat assembleDebug
```

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Generate Release APK / Bundle
```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

---

## 🔑 Release Signing & GitHub Actions Setup

To upload Android App Bundles (AAB) or APKs to Google Play Console, release builds must be digitally signed with a JKS keystore.

### 1. Keystore Generation (if you do not already have one)
If your app is already published or you have an existing upload key, **use your existing keystore**. Do NOT generate a new one.

If creating a new release keystore for the first time:
```bash
keytool -genkeypair -v -keystore release-key.jks -alias your_key_alias -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Base64 Encode Keystore for GitHub Actions Secrets
Convert the JKS keystore file to Base64 format:

- **Linux / macOS:**
  ```bash
  base64 -w 0 release-key.jks > keystore_base64.txt
  ```
- **Windows (PowerShell):**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.jks")) | Out-File -Encoding ascii keystore_base64.txt
  ```

### 3. Add GitHub Repository Secrets
Navigate to **Settings -> Secrets and variables -> Actions** in your GitHub repository and add the following repository secrets:

| Secret Name | Description |
| ----------- | ----------- |
| `RELEASE_KEYSTORE_BASE64` | The complete Base64 encoded string of your `release-key.jks` file. |
| `RELEASE_STORE_PASSWORD` | Password for your keystore file. |
| `RELEASE_KEY_ALIAS` | Key alias name inside the keystore. |
| `RELEASE_KEY_PASSWORD` | Password for the key alias. |

The GitHub Actions workflow (`android-release.yml`) automatically decodes the keystore secret, signs the AABs (`Rivava-Play.aab`, `Rivava-FullSMS.aab`) and APKs (`Rivava-Play.apk`, `Rivava-FullSMS.apk`), and publishes them as downloadable build artifacts compatible with Google Play Console.
