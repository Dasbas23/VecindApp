# Badge State Bleed — MainViewModel reactivo — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Desacoplar el `usuarioId` del constructor del `MainViewModel` para que el badge de notificaciones sea reactivo al cambio de usuario (login/logout/registro), sin necesidad de destruir la Activity.

**Architecture:** Reemplazar el `usuarioId` estático del constructor por un `MutableStateFlow<Int>` interno con `flatMapLatest` para resubscribirse automáticamente a `getConteoNotificaciones(nuevoId)` cuando cambie el usuario. La Activity llama a `setUsuarioId()` en cada transición de sesión.

**Tech Stack:** Kotlin, StateFlow, flatMapLatest, Room, MVVM

---

## Diagnóstico del problema

### Root cause

`MainViewModel` recibe `usuarioId` como parámetro del constructor via Factory. En la arquitectura Single-Activity:

```
Login (user A) → iniciarBadge() → Factory(repoId=A) → ViewModel creado → observa user A ✓
Logout → navigate(login) → bottomNav hidden → ViewModel SIGUE VIVO con user A
Login (user B) → iniciarBadge() → badgeIniciado=true → RETURN → ViewModel sigue con user A ✗
```

El flag `badgeIniciado` impide re-inicializar, y el Factory no se vuelve a ejecutar porque el ViewModel ya existe en el ViewModelStore de la Activity.

### Solución

```
Login (user A) → iniciarBadge() → Factory(repo) → ViewModel creado → setUsuarioId(A) → flatMapLatest → observa user A ✓
Logout → setUsuarioId(-1) → flatMapLatest → emite 0 → badge limpio
Login (user B) → iniciarBadge() → setUsuarioId(B) → flatMapLatest → observa user B ✓
```

---

## Ficheros que cambian

| Fichero | Cambio |
|---------|--------|
| `MainViewModel.kt` | Eliminar `usuarioId` del constructor/Factory, añadir `_usuarioId` StateFlow + `setUsuarioId()`, refactorizar `notificaciones` con `flatMapLatest` |
| `MainActivity.kt` | Almacenar ViewModel como propiedad, llamar `setUsuarioId()` en cada `iniciarBadge()`, simplificar Factory |
| `PerfilFragment.kt` | Llamar `setUsuarioId(SIN_SESION)` al cerrar sesión |

## Ficheros que NO cambian

| Fichero | Razón |
|---------|-------|
| `TransaccionDao.kt` | La query `getConteoNotificaciones` no cambia — recibe un `Int` como antes |
| `TransaccionRepository.kt` / `Impl` | Pass-through del DAO, sin cambios |
| `LoginFragment.kt` / `RegistroFragment.kt` | Solo guardan sesión en SharedPreferences — el badge se actualiza reactivamente via `iniciarBadge()` en el destination listener |
| `SesionUsuario.kt` | Solo es un helper de SharedPreferences, no cambia |

---

## Task 1: Refactorizar MainViewModel con flatMapLatest

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/MainViewModel.kt`

- [ ] **Step 1: Reescribir MainViewModel completo**

Reemplazar el contenido actual del fichero por:

```kotlin
package com.example.vecindapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.SesionUsuario
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel principal asociado a [MainActivity].
 *
 * Expone un [StateFlow] reactivo con el número de transacciones que
 * requieren atención del usuario (pendientes + completadas sin valorar),
 * utilizado para mostrar un badge en la pestaña de Transacciones del BottomNav.
 *
 * ## Reactividad al cambio de usuario
 * El `usuarioId` no se fija en el constructor: se actualiza dinámicamente
 * con [setUsuarioId]. Internamente, [notificaciones] usa [flatMapLatest]
 * para cancelar la suscripción anterior y recolectar los datos del nuevo
 * usuario cada vez que el ID cambia. Esto resuelve el problema de
 * "state bleed" en la arquitectura Single-Activity (la Activity no se
 * destruye al cambiar de sesión).
 *
 * @property transaccionRepository Repositorio de transacciones.
 *
 * @see MainActivity
 */
class MainViewModel(
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {

    /**
     * ID del usuario con sesión activa. Valor inicial: [SesionUsuario.SIN_SESION].
     * Al cambiar, [flatMapLatest] cancela la recolección anterior y empieza
     * a recolectar [TransaccionRepository.getConteoNotificaciones] del nuevo ID.
     */
    private val _usuarioId = MutableStateFlow(SesionUsuario.SIN_SESION)

    /**
     * Conteo reactivo de transacciones pendientes de atención.
     *
     * Se actualiza automáticamente cuando cambian las tablas `transaccion`
     * o `valoracion` en Room, y también cuando cambia el usuario activo
     * (via [setUsuarioId]). Usa [SharingStarted.WhileSubscribed] para
     * cancelar la suscripción 5 s después de que la UI deje de observar.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notificaciones: StateFlow<Int> = _usuarioId
        .flatMapLatest { id ->
            if (id > 0) transaccionRepository.getConteoNotificaciones(id)
            else flowOf(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Actualiza el usuario activo. Llamar tras login, registro
     * o al detectar sesión existente. Pasar [SesionUsuario.SIN_SESION]
     * al cerrar sesión para limpiar el badge.
     *
     * @param id ID del usuario (o [SesionUsuario.SIN_SESION] para limpiar).
     */
    fun setUsuarioId(id: Int) {
        _usuarioId.value = id
    }

    /**
     * Factory simplificada — ya no recibe `usuarioId`.
     *
     * @property transaccionRepository Repositorio de transacciones.
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(transaccionRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
```

Cambios respecto al original:
- Constructor: `(transaccionRepository, usuarioId)` → `(transaccionRepository)` — eliminado `usuarioId`
- Nuevo: `_usuarioId` MutableStateFlow con valor inicial `SIN_SESION`
- Nuevo: `fun setUsuarioId(id: Int)`
- `notificaciones`: `repo.getConteoNotificaciones(usuarioId)` → `_usuarioId.flatMapLatest { ... }`
- Factory: `(transaccionRepository, usuarioId)` → `(transaccionRepository)` — eliminado `usuarioId`
- Nuevos imports: `MutableStateFlow`, `flatMapLatest`, `flowOf`, `ExperimentalCoroutinesApi`, `SesionUsuario`

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD FAILURE — `MainActivity.kt` todavía pasa `usuarioId` al Factory. Es esperado, se corrige en Task 2.

- [ ] **Step 3: Commit (parcial, se completará en Task 2)**

No hacer commit aún — el código no compila. Pasar directamente a Task 2.

---

## Task 2: Actualizar MainActivity.iniciarBadge()

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/MainActivity.kt`

- [ ] **Step 1: Añadir propiedad `mainViewModel` y refactorizar `iniciarBadge()`**

En `MainActivity.kt`, reemplazar:

```kotlin
class MainActivity : AppCompatActivity() {

    /** Evita suscribirse al badge más de una vez por sesión. */
    private var badgeIniciado = false
```

Por:

```kotlin
class MainActivity : AppCompatActivity() {

    /** Evita suscribirse al badge más de una vez por sesión. */
    private var badgeIniciado = false

    /** ViewModel principal — se inicializa en [iniciarBadge]. */
    private lateinit var mainViewModel: MainViewModel
```

- [ ] **Step 2: Reescribir `iniciarBadge()`**

Reemplazar el método `iniciarBadge()` completo:

```kotlin
    private fun iniciarBadge(bottomNav: BottomNavigationView) {
        if (badgeIniciado) return
        val sesion = SesionUsuario(this)
        if (!sesion.haySesion()) return
        badgeIniciado = true

        val app = application as VecindAppApplication
        val viewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(app.transaccionRepository, sesion.obtenerUsuarioId())
        )[MainViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificaciones.collect { conteo ->
                    val badge = bottomNav.getOrCreateBadge(R.id.transaccionFragment)
                    if (conteo > 0) {
                        badge.isVisible = true
                        badge.number = conteo
                    } else {
                        badge.isVisible = false
                    }
                }
            }
        }
    }
```

Por:

```kotlin
    /**
     * Inicia la observación reactiva del badge de notificaciones.
     *
     * Se ejecuta en cada navegación fuera de login/registro.
     * - La primera vez: crea el ViewModel y arranca el collector.
     * - Las siguientes: solo actualiza el [MainViewModel.setUsuarioId]
     *   para que [flatMapLatest] resubscriba al usuario correcto.
     */
    private fun iniciarBadge(bottomNav: BottomNavigationView) {
        val sesion = SesionUsuario(this)
        if (!sesion.haySesion()) return

        // Inicializar ViewModel una sola vez (sin usuarioId en Factory)
        if (!::mainViewModel.isInitialized) {
            val app = application as VecindAppApplication
            mainViewModel = ViewModelProvider(
                this,
                MainViewModel.Factory(app.transaccionRepository)
            )[MainViewModel::class.java]
        }

        // Siempre actualizar el ID — flatMapLatest resubscribe automáticamente
        mainViewModel.setUsuarioId(sesion.obtenerUsuarioId())

        // Arrancar collector una sola vez
        if (!badgeIniciado) {
            badgeIniciado = true
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mainViewModel.notificaciones.collect { conteo ->
                        val badge = bottomNav.getOrCreateBadge(R.id.transaccionFragment)
                        if (conteo > 0) {
                            badge.isVisible = true
                            badge.number = conteo
                        } else {
                            badge.isVisible = false
                        }
                    }
                }
            }
        }
    }
```

Cambios clave:
- El guard `if (badgeIniciado) return` se mueve ABAJO — ahora solo protege el arranque del collector
- `setUsuarioId()` se llama SIEMPRE (antes del guard del collector)
- Factory ya no recibe `sesion.obtenerUsuarioId()`
- El ViewModel se almacena en `mainViewModel` para que PerfilFragment pueda acceder

- [ ] **Step 3: Eliminar import no usado de ViewModelProvider (si lo hay)**

Verificar que `import androidx.lifecycle.ViewModelProvider` sigue existiendo (se usa). No hay imports que eliminar.

- [ ] **Step 4: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit Tasks 1 y 2 juntas**

```bash
git add app/src/main/java/com/example/vecindapp/MainViewModel.kt \
       app/src/main/java/com/example/vecindapp/MainActivity.kt
git commit -m "fix(badge): hacer MainViewModel reactivo al cambio de usuario

Desacoplar usuarioId del constructor del ViewModel. Usar flatMapLatest
sobre un MutableStateFlow interno para resubscribirse cuando cambia
el usuario activo, resolviendo el state bleed en Single-Activity."
```

---

## Task 3: Limpiar usuarioId al cerrar sesión en PerfilFragment

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt:94-104`

- [ ] **Step 1: Obtener MainViewModel y resetear ID en el bloque de logout**

En `PerfilFragment.kt`, localizar el bloque de logout (líneas 94-104):

```kotlin
        val btnCerrarSesion = view.findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            SesionUsuario(requireContext()).cerrarSesion()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)  // Limpia TODA la pila
                    .build()
            )
        }
```

Reemplazar por:

```kotlin
        val btnCerrarSesion = view.findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            // Limpiar badge antes de cerrar sesión
            val app = requireActivity().application as VecindAppApplication
            ViewModelProvider(
                requireActivity(),
                MainViewModel.Factory(app.transaccionRepository)
            )[MainViewModel::class.java].setUsuarioId(SesionUsuario.SIN_SESION)

            SesionUsuario(requireContext()).cerrarSesion()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
```

- [ ] **Step 2: Añadir imports necesarios**

Añadir al inicio de `PerfilFragment.kt`:

```kotlin
import androidx.lifecycle.ViewModelProvider
import com.example.vecindapp.MainViewModel
```

Verificar que `VecindAppApplication` ya está importado (debería estarlo).

- [ ] **Step 3: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt
git commit -m "fix(perfil): limpiar badge al cerrar sesión

Resetear el usuarioId del MainViewModel a SIN_SESION antes de
navegar al login, evitando que el badge muestre datos residuales."
```

---

## Task 4: Verificación funcional

- [ ] **Step 1: Build completo**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Ejecutar tests**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verificación manual en emulador**

Instalar con `./gradlew installDebug` y verificar estos escenarios:

| # | Escenario | Pasos | Resultado esperado |
|---|-----------|-------|--------------------|
| 1 | Login normal | Login con user A → ir a Transacciones | Badge muestra conteo correcto de user A |
| 2 | Cambio de usuario | Logout → Login con user B | Badge muestra conteo de user B (no de A) |
| 3 | Registro nuevo | Logout → Registrar user C | Badge = 0 (usuario nuevo, sin transacciones) |
| 4 | Solicitar servicio | User A solicita servicio de user B | Badge de A NO sube (la solicitud no es notificación para A) |
| 5 | Persistencia | Login → cerrar app → reabrir | Badge mantiene el conteo correcto |
| 6 | Logout limpio | Con badge visible → Logout | Badge no visible en login; al re-login, badge correcto |

---

## Resumen de cambios por fichero

| Fichero | Cambio | LOC aprox |
|---------|--------|-----------|
| `MainViewModel.kt` | Reescritura: `flatMapLatest` + `setUsuarioId()`, Factory simplificada | ~50 netas |
| `MainActivity.kt` | Propiedad `mainViewModel`, refactorizar `iniciarBadge()` | ~10 netas |
| `PerfilFragment.kt` | `setUsuarioId(SIN_SESION)` al cerrar sesión + 2 imports | ~5 netas |
