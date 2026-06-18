# Rustyn Sentinel

Rustyn Sentinel is an advanced, offline-first, privacy-focused call screening application for Android. It intercepts incoming calls, matching them against powerful rule-based filters including exact matches, prefixes, and mid-string wildcards. 

## Features
- **100% Offline Architecture**: Zero tracking, zero cloud data uploads. Your logs and rules never leave your device.
- **Pattern Matching Engine**: Create robust rules with exact, prefix (`140*`), or wildcard (`*Loan*`) matchers.
- **Time-Bound Schedules**: Restrict rules to only block calls during specific hours (e.g., block all unknown numbers at night).
- **Strict Mode (Contacts Only)**: One-tap toggle to block any call from a number not saved in your native Android Contacts list.
- **Smart Analytics**: Real-time visual dashboard showcasing your screened calls over the last 7 days.
- **Native Call Log Suggestions**: Rustyn Sentinel can securely scan your missed and rejected calls to suggest recurring numbers for blocking.
- **Interactive Notifications**: One-tap actions to Call Back, Add to Contacts, or Unblock a number directly from the intercepted call notification.
- **JSON Import/Export**: Fully export your blocklist configuration to a local JSON file for safekeeping, and import it at any time.

## Tech Stack
- **Kotlin & Jetpack Compose**: Fully modern, declarative UI with glassmorphic design elements.
- **Room Database**: Fast, offline SQLite data persistence.
- **Dagger Hilt**: Dependency Injection.
- **Android CallScreeningService API**: Native system integration for zero-ring silent call blocking.

## Setup Instructions
1. Clone the repository.
2. Open the project in Android Studio (Giraffe or newer recommended).
3. Build and Run the project on an Android 10+ (API 29+) device.
4. When launching the app for the first time, you must grant the **Call Screening Role** and **Contacts Permission** for the engine to work correctly.

## Privacy Guarantee
This application does not contain internet permissions in its `AndroidManifest.xml`. It cannot physically upload your data.
