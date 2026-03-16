# CLAUDE.md

## Project Overview

VecindApp — Banco de Tiempo Vecinal Accesible.
Android native app (Kotlin) for neighbourhood time banking.
TFG DAM · ILERNA · 2S2526

## Architecture

- **Pattern**: MVVM + Clean Architecture
- **Language**: Kotlin
- **UI**: XML layouts + Fragments (NO Jetpack Compose)
- **Navigation**: Navigation Component + Bottom Navigation Bar
- **Database**: Room/SQLite (offline-first, no remote server)
- **Min SDK**: 26 · **Target SDK**: 36

## Project Structure
```
app/src/main/java/com/example/vecindapp/
├── data/
│   ├── db/              → AppDatabase, DAOs
│   ├── entities/        → Room entities (Usuario, Servicio, Transaccion, Valoracion)
│   └── repository/      → Repository implementations
├── domain/
│   ├── model/           → Domain models
│   ├── repository/      → Repository interfaces
│   └── usecase/         → Use cases (business logic)
├── ui/
│   ├── escaparate/      → Fragment + ViewModel (service catalog)
│   ├── servicio/        → Fragment + ViewModel (detail + publish)
│   ├── transaccion/     → Fragment + ViewModel (accept + review)
│   ├── perfil/          → Fragment + ViewModel (profile)
│   └── historial/       → Fragment + ViewModel (chart)
└── worker/              → WorkManager (local notifications)
```

## Key Libraries

- Room 2.8.4 (persistence)
- Navigation Component 2.9.7
- ViewModel + LiveData 2.10.0
- MPAndroidChart v3.1.0 (bar charts)
- Glide 4.16.0 (avatar images)
- WorkManager 2.9.0 (background tasks)
- Android TTS API (native, no external dependency)
- ARASAAC pictograms (bundled as local assets)

## Database

4 Room entities: USUARIO, SERVICIO, TRANSACCION, VALORACION.
Transactions use @Transaction for atomicity.
Use fallbackToDestructiveMigration() during development.

## Conventions

- Write all UI in XML, never use Compose
- Respond in Spanish unless asked otherwise
- Follow Kotlin coding conventions
- Use coroutines with viewModelScope in ViewModels
- Repository pattern: interface in domain/, implementation in data/
- Dependency injection: manual with ViewModelProvider.Factory (no Hilt/Dagger)
- Commit messages in Spanish

## Build Commands

- Build: `./gradlew build`
- Install: `./gradlew installDebug`
- Test: `./gradlew test`
- Clean: `./gradlew clean`