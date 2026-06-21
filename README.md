# QMobilityProduct

A Kotlin Multiplatform project targeting Android and iOS that displays products from the [DummyJSON API](https://dummyjson.com) with search, pagination, and local favourites.

## Architecture

The project follows a **Clean Architecture** pattern with three layers — data, domain, and presentation — all wired together with **Koin** dependency injection.

- **`/androidApp`** — Android entry point. Contains Jetpack Compose UI screens (`ListScreen`, `DetailScreen`, `FavouritesScreen`) and navigation via `NavHost`.
- **`/sharedLogic`** — Shared KMP module containing all business logic, networking, and local storage. Platform-specific code (database drivers) lives in `androidMain` and `iosMain` source sets.

## Domain Layer

**Use cases** encapsulate business logic. `ToggleFavouriteUseCase` toggles favourites and broadcasts changes via a `SharedFlow`.

**Repositories** define data contracts as interfaces in the domain layer. `ProductRepository` abstracts remote API access (product listing, search, detail). `FavouriteRepository` abstracts local persistence (add/remove/query favourites).

## Presentation Layer

The pagination and search logic each have their own **coordinator** (`PaginationCoordinator`, `SearchCoordinator`) to keep the ViewModel light. Each coordinator owns its own state `PaginationCoordinator` manages next skip value and guards against parallel in-flight requests via an atomic boolean. `SearchCoordinator` handles query debouncing and API calls. `ListViewModel` uses `combine` to merge both coordinator states into a single `ListUiState` flow, acting as a thin mapping layer between coordinators and the UI.

## Data Layer

Remote data comes from Ktor HTTP client hitting the DummyJSON REST API. Local favourites are persisted in a **SQLDelight** database with platform-specific drivers (`AndroidSqliteDriver` / `NativeSqliteDriver`) provided via the `DatabaseDriverFactory` interface.

## iOS Interop

The [SKIE](https://skie.touchlab.co/) plugin is used to improve Kotlin-to-Swift interop, providing idiomatic Swift APIs for Kotlin sealed classes, coroutines, and flows.

## Install Android App

You can install the debug APK to your connected device by running `./gradlew installDebug`.

## Demo

https://github.com/user-attachments/assets/34cbeb43-04d7-428d-8245-e54a8c0f97db




