# Campus Plug SA — Architecture & System Foundation Documentation
**Phase 1: Project Foundation & Engineering Blueprint**
*Target Platform: Android (API 24 to 36)*
*Language & Toolkit: Kotlin 2.2 / Jetpack Compose / Material 3*

---

## 1. Executive Summary & Vision

Campus Plug SA is a unified digital platform engineered specifically for South African students across 26 public universities and national TVET college campuses. The platform connects students to essential campus services, verified student accommodation, peer-to-peer student marketplaces, study vaults, academic resources, and career opportunities.

This Phase 1 release establishes a clean, enterprise-grade, scalable, and secure Android foundation, ensuring the application feels like a modern tech startup rather than a basic prototype.

---

## 2. Layered Clean Architecture & Modularity

The application strictly adheres to Clean Architecture principles combined with MVVM (Model-View-ViewModel) and unidirectional data flow (UDF):

```
┌───────────────────────────────────────────────────────────────┐
│                     Presentation Layer                        │
│  (Jetpack Compose UI, Material 3, Navigation, ViewModels)     │
└───────────────────────────────┬───────────────────────────────┘
                                │ Observes UiState & StateFlow
┌───────────────────────────────▼───────────────────────────────┐
│                        Domain Layer                           │
│  (Data Models, Entities, Campus Enums, Common Use Cases)      │
└───────────────────────────────▲───────────────────────────────┘
                                │ Exposes Result<T> / Flow<T>
┌───────────────────────────────┴───────────────────────────────┐
│                         Data Layer                            │
│  (Firebase Auth, Cloud Firestore, Cloud Storage, Repositories)│
└───────────────────────────────┬───────────────────────────────┘
                                │ Diagnostic Telemetry
┌───────────────────────────────▼───────────────────────────────┐
│                         Core Layer                            │
│  (AppLogger, AppError Hierarchy, Dispatchers, Network Monitor)│
└───────────────────────────────────────────────────────────────┘
```

### Directory Structure & Responsibilities

```
app/src/main/java/com/example/
├── CampusPlugApplication.kt        # Application class, uncaught crash hook, logger init
├── MainActivity.kt                 # Single Activity, edge-to-edge, root navigation host
├── core/
│   ├── common/
│   │   ├── Result.kt               # Functional sealed Result<T> monad
│   │   ├── UiState.kt              # Standard Compose UI states (Idle, Loading, Success, Error)
│   │   └── DispatchersProvider.kt  # Coroutines dispatchers abstraction for testability
│   ├── error/
│   │   └── AppError.kt             # Domain error hierarchy (Network, Auth, Firestore, Storage)
│   └── logging/
│       └── AppLogger.kt            # Structured application logging with diagnostic events
├── data/
│   ├── firebase/
│   │   ├── FirebaseConfig.kt       # Live readiness evaluator & diagnostic telemetry
│   │   ├── AuthRepository.kt       # Interface & FirebaseAuth with Jetpack Credential Manager
│   │   ├── FirestoreService.kt     # Custom DB ID instance provider & 8-pillar error handling
│   │   └── StorageRepository.kt    # Storage interface & FirebaseStorage for student docs
│   └── model/
│       ├── SouthAfricanCampus.kt   # Enum of all SA universities & TVET system
│       ├── UserProfile.kt          # Firestore entity (@DocumentId, @ServerTimestamp)
│       ├── CommunityCategory.kt    # Campus Pulse, Study Vault, Res & Pads, Market, Gigs
│       └── PlugPost.kt             # Scalable entity model for user plugs & listings
├── navigation/
│   ├── Screen.kt                   # Sealed navigation routes & bottom nav items
│   └── CampusPlugNavigation.kt     # Top-level NavHost with animated transitions & M3 NavigationBar
└── ui/
    ├── components/
    │   ├── CampusPlugButton.kt     # Accessible M3 buttons with loading & 48dp touch targets
    │   ├── CampusPlugCard.kt       # M3 surface cards with subtle borders & elevation
    │   ├── CampusPlugTopBar.kt     # Branded header with campus badge & diagnostics dot
    │   ├── StatusBadge.kt          # Verified student badge, campus tag & status pills
    │   ├── EmptyStateView.kt       # Friendly South African student empty state
    │   └── ErrorBanner.kt          # Dismissible error banner with retry trigger
    ├── screens/
    │   ├── auth/                   # Onboarding & Google Sign-In via Credential Manager
    │   ├── dashboard/              # Active campus switcher, quick plugs, foundation status
    │   ├── campuses/               # Directory of South African higher education institutions
    │   ├── marketplace/            # Peer-to-peer student commerce foundation
    │   ├── housing/                # Student res & accommodation foundation
    │   └── readiness/              # Live Phase 1 System Diagnostics Dashboard
    └── theme/
        ├── Color.kt                # Vibrant SA palette: African Indigo, Emerald Green, Gold
        ├── Type.kt                 # Geometric, clean typography scale (SP scaling)
        └── Theme.kt                # M3 dynamic/branded theme with system bar insets
```

---

## 3. Technology Stack & Dependency Configuration

* **Runtime:** Android API 24 (minSdk) to API 36 (targetSdk / compileSdk)
* **Application ID:** `com.aistudio.campusplug.zbxy`
* **Language:** Kotlin 2.2.10
* **UI Toolkit:** Jetpack Compose (BOM 2024.09.00) with Material Design 3
* **Navigation:** `androidx.navigation:navigation-compose:2.8.9`
* **Image Loading:** `io.coil-kt:coil-compose:2.7.0`
* **Firebase Backend (via BoM 34.17.0):**
  * `com.google.firebase:firebase-auth`
  * `com.google.firebase:firebase-firestore`
  * `com.google.firebase:firebase-storage`
* **Authentication Engine:**
  * `androidx.credentials:credentials:1.5.0`
  * `androidx.credentials:credentials-play-services-auth:1.5.0`
  * `com.google.android.libraries.identity.googleid:googleid:1.1.1`
* **Build Optimization:**
  * Unused Room and Moshi compiler plugins commented out to minimize APK footprint and build duration.
  * Release ProGuard rules configured in `proguard-rules.pro` to preserve Firestore `@ServerTimestamp` and `@DocumentId` reflection.

---

## 4. Firebase Backend Integration Readiness

### Authenticated Identity Architecture
* Standardized on **Google Sign-In exclusively** via Android Jetpack `CredentialManager` (`GetSignInWithGoogleOption`).
* Zero fake authentication: No hardcoded sample logins or simulated success buttons.
* Strict cancellation safety: Catches `GetCredentialCancellationException` independently to avoid false UI errors.
* Android API 36 bottom-sheet responsiveness ensured by issuing exactly one credential option per request.

### Cloud Firestore Architecture
* Custom Database ID enforcement via `R.string.firestore_database_id` (defaults gracefully before provisioning).
* Zero-trust security rule architecture designed for `/users/{userId}` and `/campuses/{campusCode}/plugs/{plugId}`.
* 8-pillar structured exception handling pipeline implemented in `FirestoreService.handleFirestoreError`.
* Kotlin data models utilize `@DocumentId` and `@ServerTimestamp`.

### Firebase Cloud Storage Architecture
* Prepared repository contract (`StorageRepository` / `FirebaseStorageRepository`) for:
  1. Student proof-of-registration documents (`student_verifications/{userId}/...`)
  2. Accommodation property photos (`accommodations/{landlordId}/...`)
  3. Student marketplace listings (`marketplace/{sellerId}/...`)

### Keystore & Signing Identity
* Extracted debug signing identity:
  * **SHA-1:** `CE:73:F5:9E:76:F2:15:3E:10:73:F3:2C:A5:2A:CB:BC:EC:78:91:3B`
  * **SHA-256:** `12:D3:3B:3A:5A:63:5F:53:6D:5E:E6:77:7B:10:85:83:7F:66:80:1B:D7:9A:3E:3F:32:46:1A:6A:CF:A4:DC:1C`
  * **Package Name:** `com.aistudio.campusplug.zbxy`

---

## 5. Security & Error Handling Strategy

1. **Centralized Logging (`AppLogger`):**
   * Debug-only verbose telemetry; sanitized breadcrumbs in release.
   * Catches uncaught thread exceptions via global `Thread.UncaughtExceptionHandler`.
2. **Type-Safe Domain Errors (`AppError`):**
   * Network offline detection
   * Authentication cancellations & token expirations
   * Firestore `PERMISSION_DENIED`, `NOT_FOUND`, `RESOURCE_EXHAUSTED` mappings
   * User-friendly messaging with retry affordances (`ErrorBanner`, `EmptyStateView`)
3. **Google Play Policy Compliance:**
   * Least-privilege permissions (`INTERNET`, `ACCESS_NETWORK_STATE`).
   * No broad storage permissions requested.

---

## 6. Git Version Control & Release Readiness

* Project version control initialized with clean `.gitignore`.
* Adaptive launcher icon created using Material You guidelines with density-specific PNGs (mdpi to xxxhdpi) and circular round icon mask.
* Android ProGuard rules configured to safeguard reflection-based deserialization.
