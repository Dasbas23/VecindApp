# CLAUDE.md

## Project Overview

VecindApp — Banco de Tiempo Vecinal Accesible.
Android native app (Kotlin) for neighbourhood time banking.
TFG DAM · ILERNA · 2S2526

## Architecture

- **Pattern**: MVVM + Clean Architecture
- **Language**: Kotlin
- **UI**: XML layouts + Fragments (NO Jetpack Compose)
- **Navigation**: Navigation Component + Bottom Navigation Bar (4 tabs)
- **Database**: Room/SQLite (offline-first, no remote server)
- **DI**: Manual with ViewModelProvider.Factory + VecindAppApplication as container
- **Min SDK**: 26 · **Target SDK**: 36

## Project Structure
```
app/src/main/java/com/example/vecindapp/
├── VecindAppApplication.kt  → Singleton DI container (DB + Repos)
├── MainActivity.kt          → Single Activity (NavHost + BottomNav)
├── data/
│   ├── db/              → AppDatabase, DAOs, Converters, SeedDatabaseCallback
│   ├── entities/        → Room entities (Usuario, Servicio, Transaccion, Valoracion)
│   └── repository/      → Repository implementations (*Impl)
├── domain/
│   ├── model/           → Enums (NivelVecino, CategoriaServicio, EstadoServicio, EstadoTransaccion)
│   ├── repository/      → Repository interfaces (contracts)
│   └── usecase/         → Use cases (business logic) [pendiente]
├── ui/
│   ├── escaparate/      → EscaparateFragment + ViewModel + ServicioAdapter
│   ├── servicio/        → CrearServicioFragment + DetalleServicioFragment + ViewModels
│   ├── transaccion/     → TransaccionFragment + ViewModel + Adapter + TransaccionUI
│   ├── perfil/          → PerfilFragment + ViewModel (reuses ServicioAdapter)
│   └── historial/       → HistorialFragment + ViewModel + Adapter + MPAndroidChart
└── worker/              → WorkManager (local notifications) [pendiente]
```

## Key Libraries

- Room 2.8.4 (persistence)
- Navigation Component 2.9.7
- ViewModel + LiveData 2.10.0
- MPAndroidChart v3.1.0 (bar charts in Historial)
- Glide 4.16.0 (avatar images) [pendiente integrar]
- WorkManager 2.9.0 (background tasks) [pendiente]
- Android TTS API (native, no external dependency) [pendiente]
- ARASAAC pictograms (bundled as local assets) [pendiente]

## Database

4 Room entities: USUARIO, SERVICIO, TRANSACCION, VALORACION.
SeedDatabaseCallback creates 2 test users + 1 test service on first run.
Converters handle enum ↔ String conversions for Room.
Use fallbackToDestructiveMigration(false) during development.

## Current Sprint Status

| Sprint   | Status     | Content                                                   |
|----------|------------|-----------------------------------------------------------|
| Sprint 1 | Done       | Base structure and diagrams                               |
| Sprint 2 | Done       | Room DB + CRUD services + escaparate                      |
| Sprint 3 | Done       | Transactions + Profile + History (MPAndroidChart)         |
| Sprint 4 | In progress| User selection + Ratings with ARASAAC pictograms          |
| Sprint 5 | Pending    | Accessibility, TTS, Glide, WorkManager, visual polish     |

## Navigation (nav_graph.xml)

- startDestination: escaparateFragment
- BottomNav tabs: Escaparate → Transacciones → Historial → Perfil
- Secondary: crearServicioFragment, detalleServicioFragment (with arg servicioId)

## Known TODOs

- usuarioActualId is hardcoded to 1 in all ViewModels → replace with SharedPreferences
- Glide not yet integrated for avatar/pictogram loading
- ARASAAC pictograms not yet bundled as assets
- TTS not yet implemented
- WorkManager notifications not yet implemented
- Diagram de clases needs updating with new methods

## Conventions

- Write all UI in XML, never use Compose
- Respond in Spanish unless asked otherwise
- Follow Kotlin coding conventions
- Use coroutines with viewModelScope in ViewModels
- Repository pattern: interface in domain/, implementation in data/
- Dependency injection: manual with ViewModelProvider.Factory (no Hilt/Dagger)
- Commit messages in Spanish
- KDoc documentation on all public classes

## Build Commands

- Build: `./gradlew build`
- Install: `./gradlew installDebug`
- Test: `./gradlew test`
- Clean: `./gradlew clean`