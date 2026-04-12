# Badge reactivo de notificaciones en BottomNav — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Mostrar un badge numérico en la pestaña de Transacciones del BottomNav cuando el usuario tiene transacciones PENDIENTES o COMPLETADAS sin valorar, usando un Flow reactivo que se actualiza automáticamente.

**Architecture:** Nueva query en `TransaccionDao` con subquery sobre `valoracion` devuelve un `Flow<Int>` con el conteo. Se propaga por `TransaccionRepository` → `MainViewModel` (nuevo, con `stateIn`) → `MainActivity` que observa y actualiza el badge del `BottomNavigationView`. Toda la lógica del badge vive dentro del bloque de sesión activa.

**Tech Stack:** Room (query con subquery + Flow reactivo), ViewModel + StateFlow, BottomNavigationView badges (Material Components), Coroutines (lifecycleScope + repeatOnLifecycle)

---

## Estructura de archivos

| Acción    | Archivo                                                                    | Responsabilidad                                    |
|-----------|----------------------------------------------------------------------------|----------------------------------------------------|
| Modificar | `app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt:83`     | Nueva query `getConteoNotificaciones`              |
| Modificar | `app/src/main/java/com/example/vecindapp/domain/repository/TransaccionRepository.kt:58` | Nueva firma `getConteoNotificaciones`  |
| Modificar | `app/src/main/java/com/example/vecindapp/data/repository/TransaccionRepositoryImpl.kt:39` | Implementación delegando al DAO      |
| Crear     | `app/src/main/java/com/example/vecindapp/MainViewModel.kt`                 | ViewModel con StateFlow de notificaciones          |
| Modificar | `app/src/main/java/com/example/vecindapp/MainActivity.kt:35-81`            | Observar notificaciones y pintar badge             |

---

### Task 1: Añadir query de conteo en TransaccionDao

**Files:**
- Modificar: `app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt:83` (añadir al final del interface)

- [ ] **Step 1: Añadir la función `getConteoNotificaciones` al final del DAO**

Añadir justo antes del cierre `}` del interface (después de la línea 83):

```kotlin
    /**
     * Cuenta las transacciones que requieren atención del usuario:
     * - Estado PENDIENTE (por aceptar/cancelar).
     * - Estado COMPLETADA sin valoración del usuario actual.
     *
     * Devuelve un [Flow] reactivo que se actualiza automáticamente
     * cuando cambian las tablas `transaccion` o `valoracion`.
     *
     * @param usuarioId ID del usuario activo.
     * @return [Flow] con el número de transacciones pendientes de atención.
     */
    @Query("""
        SELECT COUNT(*) FROM transaccion t
        WHERE (t.id_comprador_fk = :usuarioId OR t.id_vendedor_fk = :usuarioId)
        AND (
            t.estado = 'PENDIENTE'
            OR (t.estado = 'COMPLETADA' AND NOT EXISTS (
                SELECT 1 FROM valoracion v
                WHERE v.id_transaccion_fk = t.id_transaccion
                AND v.id_valorador_fk = :usuarioId
            ))
        )
    """)
    fun getConteoNotificaciones(usuarioId: Int): Flow<Int>
```

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt
git commit -m "feat(dao): añadir query reactiva getConteoNotificaciones"
```

---

### Task 2: Propagar la nueva función por el Repository

**Files:**
- Modificar: `app/src/main/java/com/example/vecindapp/domain/repository/TransaccionRepository.kt:58` (añadir al final del interface)
- Modificar: `app/src/main/java/com/example/vecindapp/data/repository/TransaccionRepositoryImpl.kt:39` (añadir al final de la clase)

- [ ] **Step 1: Añadir firma en TransaccionRepository.kt**

Añadir justo antes del cierre `}` del interface (después de la línea 57):

```kotlin

    /**
     * Cuenta las transacciones que requieren atención del usuario:
     * pendientes de gestionar o completadas sin valorar.
     *
     * @param usuarioId ID del usuario activo.
     * @return [Flow] reactivo con el conteo de notificaciones.
     */
    fun getConteoNotificaciones(usuarioId: Int): Flow<Int>
```

- [ ] **Step 2: Añadir implementación en TransaccionRepositoryImpl.kt**

Añadir justo antes del cierre `}` de la clase (después de la línea 38):

```kotlin

    override fun getConteoNotificaciones(usuarioId: Int): Flow<Int> =
        transaccionDao.getConteoNotificaciones(usuarioId)
```

- [ ] **Step 3: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/domain/repository/TransaccionRepository.kt app/src/main/java/com/example/vecindapp/data/repository/TransaccionRepositoryImpl.kt
git commit -m "feat(repository): propagar getConteoNotificaciones al contrato y su implementación"
```

---

### Task 3: Crear MainViewModel

**Files:**
- Crear: `app/src/main/java/com/example/vecindapp/MainViewModel.kt`

- [ ] **Step 1: Crear el archivo MainViewModel.kt**

```kotlin
package com.example.vecindapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel principal asociado a [MainActivity].
 *
 * Expone un [StateFlow] reactivo con el número de transacciones que
 * requieren atención del usuario (pendientes + completadas sin valorar),
 * utilizado para mostrar un badge en la pestaña de Transacciones del BottomNav.
 *
 * @property transaccionRepository Repositorio de transacciones.
 * @property usuarioId             ID del usuario con sesión activa.
 *
 * @see MainActivity
 */
class MainViewModel(
    transaccionRepository: TransaccionRepository,
    private val usuarioId: Int
) : ViewModel() {

    /**
     * Conteo reactivo de transacciones pendientes de atención.
     *
     * Se actualiza automáticamente cuando cambian las tablas `transaccion`
     * o `valoracion` en Room. Usa [SharingStarted.WhileSubscribed] para
     * cancelar la suscripción 5 s después de que la UI deje de observar.
     */
    val notificaciones: StateFlow<Int> = transaccionRepository
        .getConteoNotificaciones(usuarioId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Factory para inyección manual de dependencias.
     *
     * @property transaccionRepository Repositorio de transacciones.
     * @property usuarioId             ID del usuario con sesión activa.
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val usuarioId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(transaccionRepository, usuarioId) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
```

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/MainViewModel.kt
git commit -m "feat(viewmodel): crear MainViewModel con StateFlow de notificaciones"
```

---

### Task 4: Integrar badge en MainActivity

**Files:**
- Modificar: `app/src/main/java/com/example/vecindapp/MainActivity.kt:35-81`

- [ ] **Step 1: Añadir imports necesarios**

Añadir estos imports en la cabecera del archivo (después de los imports existentes, antes de la línea `class MainActivity`):

```kotlin
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
```

- [ ] **Step 2: Añadir instanciación del ViewModel y observación del badge dentro del bloque de sesión**

En el método `configurarNavegacion`, dentro del bloque `if (sesion.haySesion() && savedInstanceState == null)` (línea 77), reemplazar:

```kotlin
        if (sesion.haySesion() && savedInstanceState == null) {
            navController.navigate(R.id.action_login_to_escaparate)
        }
```

Por:

```kotlin
        if (sesion.haySesion() && savedInstanceState == null) {
            navController.navigate(R.id.action_login_to_escaparate)

            val app = application as VecindAppApplication
            val viewModel: MainViewModel by viewModels {
                MainViewModel.Factory(app.transaccionRepository, sesion.obtenerUsuarioId())
            }

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

- [ ] **Step 3: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit final**

```bash
git add app/src/main/java/com/example/vecindapp/MainActivity.kt
git commit -m "feat(notificaciones): badge reactivo en transacciones para pendientes y sin valorar"
```

---

## Nota sobre `savedInstanceState == null`

El bloque `if (sesion.haySesion() && savedInstanceState == null)` evita que el badge se re-observe tras una recreación de la Activity (rotación). Para un TFG con orientación fija (portrait) esto no es problema. Si en el futuro se permite rotación, se debería separar la navegación (protegida por `savedInstanceState == null`) de la observación del badge (solo protegida por `sesion.haySesion()`).
