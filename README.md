# QMobilityProduct

A Kotlin Multiplatform project targeting Android and iOS that displays products from the [DummyJSON API](https://dummyjson.com) with search, pagination, and local favourites.

## Architecture

The project follows a **Clean Architecture** pattern with three layers — data, domain, and presentation — all wired together with **Koin** dependency injection.

- **`/androidApp`** — Android entry point. Contains Jetpack Compose UI screens (`ListScreen`, `DetailScreen`, `FavouritesScreen`) and navigation via `NavHost`.
- **`/sharedLogic`** — Shared KMP module containing all business logic, networking, and local storage. Platform-specific code (database drivers) lives in `androidMain` and `iosMain` source sets.

## Domain Layer

**Use cases** encapsulate single pieces of business logic. `PaginatedProductsUseCase` handles paginated product loading, `SearchProductsUseCase` debounces and executes search queries, and `FavouriteUseCase` toggles favourites and broadcasts changes via a `SharedFlow` so multiple ViewModels can react.

**Repositories** define data contracts as interfaces in the domain layer. `ProductRepository` abstracts remote API access (product listing, search, detail). `FavouriteRepository` abstracts local persistence (add/remove/query favourites).

## Data Layer

Remote data comes from Ktor HTTP client hitting the DummyJSON REST API. Local favourites are persisted in a **SQLDelight** database with platform-specific drivers (`AndroidSqliteDriver` / `NativeSqliteDriver`) provided via the `DatabaseDriverFactory` interface.

## iOS Interop

The [SKIE](https://skie.touchlab.co/) plugin is used to improve Kotlin-to-Swift interop, providing idiomatic Swift APIs for Kotlin sealed classes, coroutines, and flows.

## Demo

https://github.com/user-attachments/assets/34cbeb43-04d7-428d-8245-e54a8c0f97db




