# VecindApp — Banco de Tiempo Vecinal Accesible

> Aplicación Android nativa para intercambiar servicios entre vecinos
> usando horas como moneda, con accesibilidad cognitiva mediante
> pictogramas ARASAAC y síntesis de voz (TTS).

**TFG · DAM · ILERNA · 2S2526**

---

## Estado del Proyecto

| Sprint   | Estado     | Contenido                                                    |
|----------|------------|--------------------------------------------------------------|
| Sprint 1 | Completado | Estructura base y diagramas                                  |
| Sprint 2 | Completado | BBDD Room + CRUD servicios + escaparate                      |
| Sprint 3 | Completado | Transacciones + Perfil + Historial (MPAndroidChart)          |
| Sprint 4 | Completado | Selección de usuario + Valoraciones con pictogramas ARASAAC  |
| Sprint 5 | Completado | Accesibilidad + TTS                                          |
| Sprint 6 | Completado | WorkManager y pulido visual                                  |

---

## Descripción

VecindApp es una App Android que funciona como un banco de tiempo vecinal donde los usuarios
publican servicios que ofrecen (pasear al perro, dar clases, hacer recados...)
y los "pagan" con horas en lugar de dinero. La app opera offline y guarda los datos en local con
Room/SQLite. Incluye accesibilidad mediante Text-To-Speech y una interfaz accesible. Prioriza la
accesibilidad cognitiva incorporando pictogramas del sistema ARASAAC
y lectura en voz alta mediante el motor TTS nativo de Android.

## Arquitectura

El proyecto sigue el patrón **MVVM + Clean Architecture** organizado en 5 capas:

```
app/src/main/java/com/example/vecindapp/
├── VecindAppApplication.kt  → Singleton DI container (DB + Repos)
├── MainActivity.kt          → Single Activity (NavHost + BottomNav)
├── MainViewModel.kt         → Badge de notificaciones reactivo (LiveData)
├── data/
│   ├── db/              → AppDatabase, DAOs (Usuario/Servicio/Transaccion/Valoracion), Converters, SeedDatabaseCallback
│   ├── entities/        → Room entities (Usuario, Servicio, Transaccion, Valoracion)
│   ├── repository/      → Repository implementations (*Impl)
│   └── SesionUsuario    → Helper singleton para gestionar la sesión activa
├── domain/
│   ├── model/           → Enums (NivelVecino, CategoriaServicio, EstadoServicio, EstadoTransaccion, Barrio)
│   ├── repository/      → Repository interfaces (contracts)
├── ui/
|   ├── auth/            → LoginFragment, RegistroFragment + ViewModels
|   ├── common/          → TtsHelper.kt, CategoriaMapper.kt, SnackbarUtils.kt
│   ├── escaparate/      → EscaparateFragment + ViewModel + ServicioAdapter
│   ├── servicio/        → CrearServicioFragment + DetalleServicioFragment + ViewModels
│   ├── transaccion/     → TransaccionFragment + ViewModel + Adapter + TransaccionUI
│   ├── perfil/          → PerfilFragment + ViewModel 
│   ├── historial/       → HistorialFragment + ViewModel + Adapter + MPAndroidChart
|   └── valoracion/      → ValoracionBottomSheetFragment + ViewModel + PictogramaMapper (ARASAAC) + DetalleValoracionBottomSheet 
└── worker/              → WorkManager (local notifications) [pendiente]
```

## Stack tecnológico

| Categoría       | Tecnología                          |
|-----------------|-------------------------------------|
| Lenguaje        | Kotlin                              |
| UI              | XML Layouts + Fragments             |
| Base de datos   | Room 2.8.4 / SQLite (offline-first) |
| Navegación      | Navigation Component 2.9.7          |
| Arquitectura    | ViewModel 2.10.0 + LiveData         |
| Gráficos        | MPAndroidChart v3.1.0               |
| Background      | WorkManager 2.9.0                   |
| Accesibilidad   | Android TTS API + ARASAAC           |
| Procesador      | KSP (Kotlin Symbol Processing)      |

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36

## Base de datos

4 entidades Room con relaciones Foreign Key:

- **Usuario** — Vecinos registrados con saldo de horas y nivel.
- **Servicio** — Servicios publicados en el escaparate.
- **Transaccion** — Intercambios de horas entre comprador y vendedor.
- **Valoracion** — Reseñas con pictogramas ARASAAC tras cada transacción.

## Cómo compilar

```bash
# Compilar el proyecto
./gradlew build

# Instalar en dispositivo/emulador conectado
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Limpiar build
./gradlew clean
```

## Convenciones del proyecto

- Interfaz gráfica en **XML**, nunca Jetpack Compose.
- Corrutinas con `viewModelScope` en los ViewModels.
- Patrón Repository: interfaz en `domain/`, implementación en `data/`.
- Inyección de dependencias manual con `ViewModelProvider.Factory`.
- Commits y documentación en **español**.
- Documentación KDoc en todas las clases públicas.

## Ramas

| Rama                       | Contenido                                          |
|----------------------------|----------------------------------------------------|
| `desarrollo`               | Rama principal de integración                      |
| `sprint2/bbdd+escaparate`  | BBDD Room + CRUD servicios + escaparate            |
| `sprint3/transacciones`    | Transacciones + Perfil + Historial                 |
| `sprint4/ usuarios+valorac`| Login sencillo de usuarius + sistema valoraciones  |
| `sprint5/ accesibilidad `  | TTS en escaparate + servicio + valoración + perfil |
| `sprint6/ worker+ui&ux `   | Mejoras diseño UX/UI + ~~Workmanager(tareas 2º plano)~~|


## | Utilidades

`[alias Git]`

#### === BÁSICOS ===

- **`s`** = `status -sb` #[ok] **(no se si me gusta mas git status normal pero con colores)**
- **`a`** = `add .` #[ok] **(add normal)**
- **`c`** = `commit -m` #[ok] **(commit normal)**
- **`ac`** = `!git add . && git commit -m` #[ok] **(Añade todos los fichero modificados y hace commit con mensaje)** 
- **`p`** = `push` #[ok] **(Push a remote)**
- **`pl`** = `pull` #[ok] **(Pull desde remote)**

#### === RAMAS ===

- **`co`** = `checkout` #[ok] **(Cambia a la rama seleccionada, hay que darle un argumento hacia que rama se quiere cambiar)**
- **`cob` = `checkout -b` #[ok] **(Crea una rama nueva y salta a ella)**
- **`br`** = `branch` #[ok] **(Nueva rama)**
- **`bra`** = `branch -a` #[ok] **(Lista todas las ramas: locales + remotas)**
- **`brd`** = `branch -d` #[ok] **(Elimina la rama que digas como argumento en local)**
- **`brr`** = `push origin --delete` #[ok] **(Borra una rama del remoto)**
- **`merged`** = `branch --merged` #[ok] **(Lista ramas ya mergeadas que puedes borrar tranquilamente. Para limpiar ramas muertas)**
- **`mergeff`** = `merge --no-ff` #[ok] **(Merge forzando commit de merge. Para cerrar sprints con marca visible en el historial. Indicar rama a mergear en la que estas. Requieres mensaje "-m")**

#### === HISTORIAL ===

- **`lg`** = `log --oneline --graph --all --decorate --color` #[ok] **(A diferencia con ll es más compacto)**
- **`ll`** = `log --graph --pretty=format:'%C(yellow)%h%Creset %C(cyan)%an%Creset %C(white)%s%Creset %C(green)(%cr)%Creset%C(auto)%d%Creset' --all` #[ok] **(Te muestra el graph en bonito con el formato amarillo: commits, azul:usuario, blanco: que se ha hecho, Verde:hace cuanto tiempo)**
- **`last`** = `log -1 --stat` #[ok] **(Muestra el último commit)**
- **`preview`** = `!git log $(git rev-parse --abbrev-ref @{upstream} 2>/dev/null || echo desarrollo)..HEAD --oneline` #[ok] **(Antes de mergear a desarrollo, muestra qué commits tiene tu rama que desarrollo no tiene. Retrovisor antes de cerrar sprint)**

#### === DESHACER ===

- **`undo`** = `reset HEAD~1 --mixed` #[ok] **(Deshace el commit pero deja el add y los archivos sin tocar. Sirve para editar un mensaje mal puesto o añadir más archivos.)**
- **`unstage`** = `reset HEAD --` #[ok] **(Lo contrario de add. Saca el archivo del staging)**
- **`discard`** = `restore --` #[ok] **(Deja el archivo como estaba al principio del commit. Sirve para hacer experimentos y si no funciona dejarlo como estaba al comienzo)**
- **`oops`** = `commit --amend --no-edit` #[ok] **(Se te olvidó un archivo en el commit? git add archivo.kt > git oops y se mete en el último commit sin cambiar el mensaje)**
- **`nuke`** = `!git reset --hard HEAD && git clean -fd` #[ok] **(Resetea la rama en la que estás. CUIDADO: borra TODO lo no commiteado)**

#### === STASH ===

- **`ss`** = `stash push -m` #[ok] **(!Necesita mensaje! Hace un stash. Congela los cambios y no se los lleva contigo si haces checkout)**
- **`sp`** = `stash pop` #[ok] **(Recupera los cambios del último stash y lo borra de la lista)**
- **`sl`** = `stash list` #[ok] **(Muestra la lista de stashes)**

#### === INFO ===

- **`who`** = `shortlog -sne` #[ok] **(Muestra quien soy y cuantos commits tienes)**
- **`here`** = `rev-parse --abbrev-ref HEAD` #[ok] **(Muestra donde esta el HEAD en local)**
- **`alias`** = `config --get-regexp alias` #[ok] **(Muestra la lista de alias disponibles)**
- **`f`** = `fetch origin` #[ok] **(Descarga info del remoto sin tocar tu código. Como mirar el correo sin abrir los sobres)**
- **`changes`** = `diff --stat` #[ok] **(Resumen de archivos tocados y líneas cambiadas antes de commitear)**
- **`count`** = `rev-list --count HEAD` #[ok] **(Cuántos commits lleva el repo en total)**
- **`lastweek`** = `log --oneline --since='1 week ago'` #[ok] **(Commits de la última semana. Útil para memorias y seguimiento)**
- **`graph`** = `log --oneline --graph --all -20` #[ok] **(Como lg pero solo los últimos 20 commits)**
- **`d`** = `diff` #[ok] **(Ver cambios en detalle de archivos NO stageados)**
- **`ds`** = `diff --staged` #[ok] **(Ver cambios en detalle de archivos YA stageados, listos para commit)**
- **`dw`** = `diff --word-diff` #[ok] **(Como d pero resalta palabra por palabra en vez de línea entera. Ideal para textos o strings largos)**

#### === COMPARAR CON REMOTO (después de git f) ===

- **`incoming`** = `!git log HEAD..@{upstream} --oneline` #[ok] **(Qué commits tiene el remoto que yo no tengo)**
- **`peek`** = `!git diff --stat HEAD @{upstream}` #[ok] **(Qué archivos cambiarían si hago pull)**
- **`peekfull `** = `!git diff HEAD @{upstream}` #[ok] **(Ver los cambios en detalle antes de pull)**
- **`peekword`** = `!git diff --word-diff HEAD @{upstream}` #[ok] **(Ver cambios palabra a palabra antes de pull)**

#### === COMPARAR CON REMOTO (antes de hacer push) === 

- **`outgoing`** = `!git diff --stat @{upstream} HEAD` #[ok] **(Qué archivos cambiarían si hago push)**
- **`outfull`** = `!git diff @{upstream} HEAD` #[ok] **(Ver los cambios en detalle antes de push)**
- **`outword`** = `!git diff --word-diff @{upstream} HEAD` #[ok] **(Ver cambios palabra a palabra antes de push)**

## Autor

Marius Ion — ILERNA 2S2526

## Licencia

Proyecto académico (TFG). Todos los derechos reservados.
Pictogramas: [ARASAAC](http://www.arasaac.org) — Licencia Creative Commons BY-NC-SA.