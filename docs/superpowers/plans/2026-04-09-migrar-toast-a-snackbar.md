# Migrar Toast a Snackbar — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sustituir todos los `Toast.makeText` de la app por `Snackbar.make` de Material Design para una experiencia visual consistente y moderna.

**Architecture:** Solo cambios en Fragments (capa UI). No se tocan ViewModels, Repositories, lógica de negocio, navegación ni TTS. Los ViewModels ya exponen errores/mensajes vía StateFlow que los Fragments recogen — el cambio es solo en cómo se muestra el feedback al usuario.

**Tech Stack:** Kotlin, `com.google.android.material.snackbar.Snackbar`, Navigation Component.

---

## Inventario de Toast.makeText (15 ocurrencias en 7 archivos)

| # | Archivo | Línea | Mensaje | Tipo | Duración propuesta |
|---|---------|-------|---------|------|-------------------|
| 1 | `LoginFragment.kt` | 97 | error dinámico (ViewModel) | Error | LENGTH_LONG |
| 2 | `RegistroFragment.kt` | 108 | `R.string.registro_exitoso` | Confirmación | LENGTH_SHORT |
| 3 | `RegistroFragment.kt` | 125 | error dinámico (ViewModel) | Error | LENGTH_LONG |
| 4 | `CrearServicioFragment.kt` | 141 | `R.string.servicio_guardado` | Confirmación | LENGTH_SHORT |
| 5 | `CrearServicioFragment.kt` | 154 | error dinámico (ViewModel) | Error | LENGTH_LONG |
| 6 | `DetalleServicioFragment.kt` | 265 | `R.string.error_titulo_vacio` | Error (validación en dialog) | **MANTENER TOAST** |
| 7 | `DetalleServicioFragment.kt` | 315 | `R.string.servicio_solicitado` | Confirmación | LENGTH_SHORT |
| 8 | `DetalleServicioFragment.kt` | 335 | `R.string.solicitud_cancelada` | Confirmación | LENGTH_SHORT |
| 9 | `DetalleServicioFragment.kt` | 355 | `R.string.servicio_eliminado` | Confirmación | LENGTH_SHORT |
| 10 | `DetalleServicioFragment.kt` | 375 | `R.string.servicio_actualizado` | Confirmación | LENGTH_SHORT |
| 11 | `DetalleServicioFragment.kt` | 395 | error dinámico (ViewModel) | Error | LENGTH_LONG |
| 12 | `TransaccionFragment.kt` | 164 | mensaje dinámico (ViewModel) | Feedback | LENGTH_SHORT |
| 13 | `HistorialFragment.kt` | 145 | "Sin valoración" (hardcoded) | Info | LENGTH_SHORT |
| 14 | `ValoracionBottomSheetFragment.kt` | 205 | `R.string.valoracion_enviada` | Confirmación | LENGTH_SHORT |
| 15 | `ValoracionBottomSheetFragment.kt` | 222 | error dinámico (ViewModel) | Error | LENGTH_LONG |

### Casos especiales

**#6 — `DetalleServicioFragment.kt` línea 265: MANTENER COMO TOAST.**
Este Toast se dispara dentro del callback `setPositiveButton` de un `AlertDialog` de edición. En ese momento, el dialog acaba de cerrarse y la vista del fragment podría no ser la mejor ancla para un Snackbar que aparece "detrás" del dialog. Además, es una validación inline del dialog, no del flujo principal. Se mantiene como Toast por coherencia UX.

**#14 y #15 — `ValoracionBottomSheetFragment.kt`: Snackbar viable.**
Es un `BottomSheetDialogFragment` que tiene su propia vista (`view`). El Snackbar se anclará a la vista del BottomSheet. No se usa `setAnchorView(bottomNav)` aquí porque el BottomSheet está encima de todo.

**#2 — `RegistroFragment.kt` línea 108: Snackbar antes de navegación.**
Este Toast se muestra y luego navega al escaparate. El Snackbar se verá brevemente antes de la navegación — aceptable porque es una confirmación rápida de éxito. Alternativamente se podría eliminar el Snackbar y confiar en la navegación como feedback, pero el prompt pide mantener los mismos mensajes.

**#4, #7, #9 — Toast + `popBackStack()`/`navigate()`:** Similar al #2 — el Snackbar se mostrará brevemente antes de navegar. Esto es un patrón común y aceptado en Material Design.

### BottomNavigationView

El `BottomNavigationView` tiene `android:id="@+id/bottomNav"` en `activity_main.xml`. Los fragments que son tabs principales (escaparate, transacciones, historial, perfil) tienen el bottomNav visible. Los fragments secundarios (crearServicio, detalleServicio) también lo ven (se oculta solo en login/registro).

**Decisión:** Usar `.setAnchorView(activity?.findViewById(R.id.bottomNav))` en los fragments donde el bottomNav es visible. Crear un **método de extensión** `Fragment.mostrarSnackbar()` para centralizar la lógica y evitar repetir el patrón 14 veces.

---

## File Structure

| Archivo | Acción |
|---------|--------|
| `app/src/main/java/.../ui/common/SnackbarUtils.kt` | **Crear**: extensión `Fragment.mostrarSnackbar()` |
| `app/src/main/java/.../ui/auth/LoginFragment.kt` | Modificar: 1 Toast → Snackbar |
| `app/src/main/java/.../ui/auth/RegistroFragment.kt` | Modificar: 2 Toast → Snackbar |
| `app/src/main/java/.../ui/servicio/CrearServicioFragment.kt` | Modificar: 2 Toast → Snackbar |
| `app/src/main/java/.../ui/servicio/Detalleserviciofragment.kt` | Modificar: 5 Toast → Snackbar, 1 Toast se mantiene |
| `app/src/main/java/.../ui/transaccion/TransaccionFragment.kt` | Modificar: 1 Toast → Snackbar |
| `app/src/main/java/.../ui/historial/HistorialFragment.kt` | Modificar: 1 Toast → Snackbar |
| `app/src/main/java/.../ui/valoracion/ValoracionBottomSheetFragment.kt` | Modificar: 2 Toast → Snackbar |

---

### Task 1: Crear extensión Fragment.mostrarSnackbar()

**Files:**
- Create: `app/src/main/java/com/example/vecindapp/ui/common/SnackbarUtils.kt`

**Estrategia:** Centralizar la creación de Snackbar en una función de extensión para Fragment. Esto evita repetir `Snackbar.make(...).setAnchorView(...).show()` en cada sitio. El método intenta buscar el `bottomNav` y lo usa como anchor si está visible.

- [ ] **Step 1: Crear SnackbarUtils.kt**

```kotlin
package com.example.vecindapp.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import com.example.vecindapp.R
import com.google.android.material.snackbar.Snackbar

/**
 * Muestra un [Snackbar] anclado a la vista del fragment.
 *
 * Si el [BottomNavigationView] (`R.id.bottomNav`) está visible,
 * el Snackbar flota encima de él para no quedar oculto.
 *
 * @param mensaje   Texto a mostrar.
 * @param duracion  [Snackbar.LENGTH_SHORT] o [Snackbar.LENGTH_LONG].
 */
fun Fragment.mostrarSnackbar(
    mensaje: String,
    duracion: Int = Snackbar.LENGTH_SHORT
) {
    val vista = view ?: return
    val snackbar = Snackbar.make(vista, mensaje, duracion)
    // Anclar encima del BottomNav si existe y está visible
    activity?.findViewById<View>(R.id.bottomNav)?.let { bottomNav ->
        if (bottomNav.visibility == View.VISIBLE) {
            snackbar.setAnchorView(bottomNav)
        }
    }
    snackbar.show()
}

/**
 * Sobrecarga que acepta un recurso de string.
 */
fun Fragment.mostrarSnackbar(
    mensajeResId: Int,
    duracion: Int = Snackbar.LENGTH_SHORT
) {
    mostrarSnackbar(getString(mensajeResId), duracion)
}
```

- [ ] **Step 2: Verificar que compila**

```bash
./gradlew compileDebugKotlin
```

---

### Task 2: Migrar LoginFragment y RegistroFragment

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/auth/LoginFragment.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/auth/RegistroFragment.kt`

- [ ] **Step 1: LoginFragment.kt — 1 ocurrencia**

Línea 97 — `observarErrores()`:
```kotlin
// ANTES:
Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
```

Añadir import:
```kotlin
import com.example.vecindapp.ui.common.mostrarSnackbar
import com.google.android.material.snackbar.Snackbar
```

Eliminar import `android.widget.Toast` (verificar que no se usa en otro sitio del archivo).

**Nota:** Login y Registro ocultan el bottomNav (MainActivity lo setea a `GONE`). La extensión `mostrarSnackbar` ya maneja esto: si `bottomNav.visibility != VISIBLE`, no setea anchor. El Snackbar aparecerá en la parte inferior de la pantalla, que es el comportamiento correcto.

- [ ] **Step 2: RegistroFragment.kt — 2 ocurrencias**

Línea 108 — `observarResultado()`:
```kotlin
// ANTES:
Toast.makeText(requireContext(), R.string.registro_exitoso, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(R.string.registro_exitoso)
```

Línea 125 — `observarErrores()`:
```kotlin
// ANTES:
Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
```

Añadir imports, eliminar import Toast.

- [ ] **Step 3: Build parcial**

```bash
./gradlew compileDebugKotlin
```

---

### Task 3: Migrar CrearServicioFragment

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/CrearServicioFragment.kt`

- [ ] **Step 1: 2 ocurrencias**

Línea 141 — `observarResultado()` (éxito):
```kotlin
// ANTES:
Toast.makeText(requireContext(), getString(R.string.servicio_guardado), Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(R.string.servicio_guardado)
```

Línea 154 — `observarResultado()` (error):
```kotlin
// ANTES:
Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
```

Actualizar KDoc del método `observarResultado()`:
```kotlin
/**
 * Observa los StateFlows del ViewModel para reaccionar al resultado.
 *
 * - Si [guardado] es `true` → muestra Snackbar de éxito y navega atrás.
 * - Si [error] tiene mensaje → muestra Snackbar con el error.
 */
```

Añadir imports, eliminar import Toast.

---

### Task 4: Migrar DetalleServicioFragment (5 de 6, 1 se mantiene)

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt`

- [ ] **Step 1: Ocurrencia #6 — MANTENER COMO TOAST**

Línea 265 — `mostrarDialogoEditar()` — validación dentro de AlertDialog:
```kotlin
// SE MANTIENE como Toast — está dentro del callback de un AlertDialog
Toast.makeText(requireContext(), R.string.error_titulo_vacio, Toast.LENGTH_SHORT).show()
```

**No cambiar.** Documentar con comentario:
```kotlin
// Toast intencional: validación inline del AlertDialog (Snackbar no es viable aquí)
Toast.makeText(requireContext(), R.string.error_titulo_vacio, Toast.LENGTH_SHORT).show()
```

- [ ] **Step 2: Las otras 5 ocurrencias**

Línea 315 — `observarSolicitud()`:
```kotlin
mostrarSnackbar(R.string.servicio_solicitado)
```

Línea 335 — `observarCancelacion()`:
```kotlin
mostrarSnackbar(R.string.solicitud_cancelada)
```

Línea 355 — `observarEliminacion()`:
```kotlin
mostrarSnackbar(R.string.servicio_eliminado)
```

Línea 375 — `observarActualizacion()`:
```kotlin
mostrarSnackbar(R.string.servicio_actualizado)
```

Línea 395 — `observarErrores()`:
```kotlin
mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
```

Añadir imports de `mostrarSnackbar` y `Snackbar`. **Mantener** import `Toast` porque la ocurrencia #6 sigue usándolo.

---

### Task 5: Migrar TransaccionFragment y HistorialFragment

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionFragment.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/historial/HistorialFragment.kt`

- [ ] **Step 1: TransaccionFragment.kt — 1 ocurrencia**

Línea 164 — `observarMensajes()`:
```kotlin
// ANTES:
Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(mensaje)
```

Añadir imports, eliminar import Toast.

- [ ] **Step 2: HistorialFragment.kt — 1 ocurrencia**

Línea 145 — `configurarRecyclerView()` (sin valoración):
```kotlin
// ANTES:
Toast.makeText(requireContext(), "Sin valoración", Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar("Sin valoración")
```

**Nota:** Este string está hardcodeado. No se extrae a strings.xml en esta tarea (no está en el scope). Se migra tal cual.

Añadir imports, eliminar import Toast.

---

### Task 6: Migrar ValoracionBottomSheetFragment

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/valoracion/ValoracionBottomSheetFragment.kt`

- [ ] **Step 1: 2 ocurrencias**

Línea 205 — `observarResultado()` (éxito):
```kotlin
// ANTES:
Toast.makeText(requireContext(), R.string.valoracion_enviada, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(R.string.valoracion_enviada)
```

Línea 222 — `observarErrores()`:
```kotlin
// ANTES:
Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

// DESPUÉS:
mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
```

**Nota sobre BottomSheet:** La extensión `mostrarSnackbar` usa `view` del fragment. En un `BottomSheetDialogFragment`, `view` es la vista del BottomSheet. El Snackbar aparecerá dentro del BottomSheet. El `bottomNav` no será encontrado por `activity?.findViewById` dentro del dialog, así que no se seteará anchor — comportamiento correcto.

Añadir imports, eliminar import Toast.

---

### Task 7: Build final y commit

- [ ] **Step 1: Build completo**

```bash
./gradlew assembleDebug
```

- [ ] **Step 2: Verificar que no quedan Toasts inesperados**

```bash
grep -r "Toast.makeText" app/src/main/java/ --include="*.kt"
```

Resultado esperado: solo 1 ocurrencia en `Detalleserviciofragment.kt` (la #6 que se mantiene intencionalmente).

- [ ] **Step 3: Verificar imports limpios**

```bash
grep -r "import android.widget.Toast" app/src/main/java/ --include="*.kt"
```

Resultado esperado: solo `Detalleserviciofragment.kt` (necesita Toast para la ocurrencia #6).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/common/SnackbarUtils.kt \
        app/src/main/java/com/example/vecindapp/ui/auth/LoginFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/auth/RegistroFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/servicio/CrearServicioFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt \
        app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/historial/HistorialFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/valoracion/ValoracionBottomSheetFragment.kt
git commit -m "refactor: migrar Toast a Snackbar en toda la app"
```

---

## Resumen de decisiones

| Decisión | Justificación |
|----------|---------------|
| **Extensión `Fragment.mostrarSnackbar()`** | Centraliza la lógica de anchor + duración. Evita repetir 14 veces el mismo patrón. Ubicada en `ui/common/` junto a `TtsHelper.kt`. |
| **`setAnchorView(bottomNav)` condicional** | Solo si el bottomNav existe y está visible. Login/Registro no lo muestran; BottomSheets no lo ven. |
| **Toast #6 se mantiene** | Está dentro de un callback de AlertDialog. El dialog se cierra al pulsar el botón positivo, y el Snackbar necesita una vista estable como ancla. Un Toast es más apropiado aquí. |
| **LENGTH_LONG para errores** | Los errores requieren más tiempo de lectura. Las confirmaciones rápidas usan LENGTH_SHORT. |
| **No extraer "Sin valoración" a strings.xml** | Fuera del scope de esta tarea. Se migra el Toast tal cual. |
