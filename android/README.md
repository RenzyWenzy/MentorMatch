# MentorMatch — Android (Kotlin + Jetpack Compose)

A native Android client for the MentorMatch Spring Boot backend
(`AuthController`, `UserController`, JWT auth via `JwtAuthenticationFilter`).

## What's included

- **Retrofit + Moshi** networking layer (`data/api`) matching the backend's
  DTOs exactly: `LoginRequest`, `RegistrationRequest`, `AuthResponse`,
  `UserResponse` (from `UserResponseRequest`), `ApiError`.
- **JWT persistence** via Jetpack DataStore (`data/local/TokenManager`),
  automatically attached to every request as `Authorization: Bearer <token>`
  by `AuthInterceptor` — mirroring what `JwtAuthenticationFilter` expects.
- **Repositories** (`data/repository`) wrapping the API calls in `Result`,
  surfacing the backend's `ApiError.message` / `fieldErrors` on failure
  (same errors `GlobalExceptionHandler` produces: validation failures,
  duplicate email, bad credentials, etc).
- **Jetpack Compose UI**: Login screen, Registration screen (with a role
  picker that conditionally shows student vs. mentor fields, since `ADMIN`
  can't self-register per `UserServiceImpl`), and a role-based Dashboard
  screen that calls `/api/users/dashboard/{student|mentor|admin}`.
- **Navigation Compose** graph routing to the right dashboard based on the
  role returned in `AuthResponse.user.role`.

## Before you run it

Open `data/api/NetworkModule.kt` and set `BASE_URL` for your setup:

- **Emulator**, backend running on your dev machine at `localhost:8080` →
  keep the default `http://10.0.2.2:8080/` (10.0.2.2 is the emulator's
  alias for the host machine).
- **Physical device** → use your machine's LAN IP, e.g.
  `http://192.168.1.23:8080/`, and make sure the phone is on the same
  network and your firewall allows the connection.
- For production, switch to `https://` and drop
  `android:usesCleartextTraffic="true"` from `AndroidManifest.xml`.

Also make sure the backend's CORS config (`SecurityConfig.corsConfigurationSource`)
allows whatever origin your build uses if you ever add a WebView — native
HTTP calls via Retrofit aren't subject to browser CORS, so this only matters
if you extend the app with in-app web content.

## Project layout

```
app/src/main/java/edu/cit/estillore/mentormatch/
├── AppContainer.kt              # manual DI: TokenManager, APIs, repositories
├── MainActivity.kt
├── data/
│   ├── model/                   # Kotlin data classes mirroring the Java DTOs
│   ├── api/                     # Retrofit interfaces + OkHttp/Moshi setup
│   ├── local/TokenManager.kt     # DataStore-backed JWT storage
│   └── repository/              # AuthRepository, UserRepository
└── ui/
    ├── auth/                    # AuthViewModel, LoginScreen, RegisterScreen
    ├── dashboard/                # DashboardViewModel, DashboardScreen
    └── nav/NavGraph.kt           # Navigation Compose graph
```

## Opening the project

1. Open this folder in Android Studio (Koala or newer).
2. Let Gradle sync — it will fetch the Compose BOM, Retrofit, Moshi,
   Navigation Compose, and DataStore dependencies declared in
   `app/build.gradle.kts`.
3. Set `BASE_URL` as described above.
4. Run the backend (`./mvnw spring-boot:run` or your IDE's run config),
   then run the app on an emulator or device.

## Notes on parity with the backend

- Registration sends only the fields relevant to the chosen role (student
  number/program, or expertise/department) — matching how
  `UserServiceImpl.registerUser` validates conditionally.
- Login/registration errors are parsed from the same `ApiError` shape the
  backend returns, including per-field validation messages from
  `MethodArgumentNotValidException`.
- The dashboard screen calls the role-specific endpoint
  (`/api/users/dashboard/student|mentor|admin`), which is also enforced
  server-side by `SecurityConfig` — so even if the app's local role state
  were ever wrong, the backend still rejects a mismatched request.
