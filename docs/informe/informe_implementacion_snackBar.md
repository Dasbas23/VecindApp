# Informe de implementación: Snackbar en `ValoracionBottomSheetFragment`

## Contexto

Tras la migración de `Toast` a `Snackbar` en toda la app, el Snackbar del
`ValoracionBottomSheetFragment` dejó de mostrarse correctamente porque quedaba
oculto detrás del propio `BottomSheetDialogFragment`. El objetivo final fue:

1. Que el Snackbar se muestre **dentro** del bottom sheet (no detrás ni por encima).
2. Que quede anclado por encima del botón `btnEnviarValoracion` para no taparlo.
3. Que la duración del Snackbar de errores sea `LENGTH_SHORT`.
4. Que el mensaje de error del `ValoracionViewModel` se alimente de `strings.xml`
   en lugar de strings hardcodeados.

---

## Archivos modificados

1. `app/src/main/res/layout/bottom_sheet_valoracion.xml`
2. `app/src/main/java/com/example/vecindapp/ui/common/SnackbarUtils.kt`
3. `app/src/main/java/com/example/vecindapp/ui/valoracion/ValoracionBottomSheetFragment.kt`
4. `app/src/main/java/com/example/vecindapp/ui/valoracion/ValoracionViewModel.kt`
5. `app/src/main/res/values/strings.xml`

---

## 1. `bottom_sheet_valoracion.xml`

**Motivo:** `Snackbar.make(view, ...)` sube por la jerarquía buscando el primer
`CoordinatorLayout`. Si no existe dentro del sheet, lo encuentra en el
`CoordinatorLayout` interno del diálogo, que está al fondo de la pantalla y
queda tapado por el propio sheet. Solución: envolver el `ScrollView` raíz en
un `CoordinatorLayout` para que el Snackbar se adjunte **dentro** del contenido
del sheet.

**Inicio del archivo — antes:**
```xml
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```

**Inicio del archivo — después:**
```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/coordinatorValoracion"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

<ScrollView
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```

**Final del archivo — antes:**
```xml
    </LinearLayout>

</ScrollView>
```

**Final del archivo — después:**
```xml
    </LinearLayout>

</ScrollView>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

---

## 2. `SnackbarUtils.kt`

**Motivo:** dos cambios en la utilidad:

- Si el fragment es un `DialogFragment`, **no** anclar al `BottomNav` de la
  Activity (no está visible desde el diálogo). La `view` del fragment ya es el
  `CoordinatorLayout` interno del sheet, así que `Snackbar.make` lo usa como
  padre y el Snackbar aparece dentro del sheet.
- Añadir un parámetro opcional `anchorView: View?` que tiene prioridad sobre
  el anclaje automático, para poder anclar a cualquier vista (por ejemplo el
  botón Enviar).

**Archivo completo resultante:**
```kotlin
package com.example.vecindapp.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import com.example.vecindapp.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment

/**
 * Muestra un Snackbar anclado a la vista del fragment.
 * Si el BottomNavigationView está visible en la Activity, el Snackbar flota encima.
 */
fun Fragment.mostrarSnackbar(
    mensaje: String,
    duracion: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) {
    val vista = view ?: return

    // En un DialogFragment usamos la propia vista del fragment (el CoordinatorLayout interno
    // del layout del sheet) para que el Snackbar se muestre dentro del sheet.
    val snackbar = Snackbar.make(vista, mensaje, duracion)

    when {
        // Ancla explícita tiene prioridad.
        anchorView != null -> snackbar.anchorView = anchorView
        // Solo anclamos al BottomNav si NO estamos en un diálogo.
        this !is DialogFragment -> {
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.let { bottomNav ->
                if (bottomNav.isVisible) {
                    snackbar.anchorView = bottomNav
                }
            }
        }
    }

    snackbar.show()
}

/**
 * Sobrecarga para usar strings.xml directamente (DRY).
 */
fun Fragment.mostrarSnackbar(
    mensajeResId: Int,
    duracion: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) {
    mostrarSnackbar(getString(mensajeResId), duracion, anchorView)
}
```

---

## 3. `ValoracionBottomSheetFragment.kt`

**Dos cambios:**

### 3.1. `observarResultado()`

**Motivo:** el `dismiss()` se ejecutaba inmediatamente después de mostrar el
Snackbar. Como el Snackbar estaba adjunto al `CoordinatorLayout` interno del
sheet, al cerrar el diálogo el Snackbar se destruía antes de renderizarse.
Solución: mostrar el Snackbar de confirmación desde el `parentFragment`, que
sigue vivo tras el `dismiss()`.

**Antes:**
```kotlin
viewModel.guardada.collect { guardada ->
    if (guardada) {
        mostrarSnackbar(R.string.valoracion_enviada)
        dismiss()
    }
}
```

**Después:**
```kotlin
viewModel.guardada.collect { guardada ->
    if (guardada) {
        // El Snackbar se muestra desde el fragment padre porque al hacer dismiss()
        // el bottom sheet (y su Snackbar adjunto) se destruyen antes de renderizarse.
        parentFragment?.mostrarSnackbar(R.string.valoracion_enviada)
        dismiss()
    }
}
```

### 3.2. `observarErrores()`

**Motivo:** pasa a recibir un `Int?` (ID de recurso) en lugar de `String?`,
usa duración `SHORT` y ancla el Snackbar al botón `btnEnviarValoracion` para
no tapar el botón.

**Antes:**
```kotlin
private fun observarErrores() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.error.collect { mensaje ->
                if (mensaje != null) {
                    mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
                    viewModel.limpiarError()
                }
            }
        }
    }
}
```

**Después:**
```kotlin
private fun observarErrores() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.error.collect { mensajeResId ->
                if (mensajeResId != null) {
                    val btnEnviar = view?.findViewById<MaterialButton>(R.id.btnEnviarValoracion)
                    mostrarSnackbar(
                        mensajeResId = mensajeResId,
                        duracion = Snackbar.LENGTH_SHORT,
                        anchorView = btnEnviar
                    )
                    viewModel.limpiarError()
                }
            }
        }
    }
}
```

---

## 4. `ValoracionViewModel.kt`

**Motivo:** el `_error` pasa de `String?` hardcodeado a `Int?` con IDs de
recurso de `strings.xml`. Beneficios: sin literales en el ViewModel,
internacionalización lista.

### 4.1. Imports

**Añadido:**
```kotlin
import com.example.vecindapp.R
```

### 4.2. Declaración de `_error`

**Antes:**
```kotlin
/** Mensaje de error. */
private val _error = MutableStateFlow<String?>(null)
val error: StateFlow<String?> = _error
```

**Después:**
```kotlin
/** Mensaje de error como ID de recurso de strings.xml. */
private val _error = MutableStateFlow<Int?>(null)
val error: StateFlow<Int?> = _error
```

### 4.3. Asignaciones dentro de `guardarValoracion()`

**Antes:**
```kotlin
if (pictogramas.isEmpty()) {
    _error.value = "Selecciona al menos un pictograma"
    return
}
...
} catch (e: Exception) {
    e.printStackTrace()
    _error.value = "Error al guardar la valoración"
}
```

**Después:**
```kotlin
if (pictogramas.isEmpty()) {
    _error.value = R.string.valoracion_error_sin_pictograma
    return
}
...
} catch (e: Exception) {
    e.printStackTrace()
    _error.value = R.string.valoracion_error_guardar
}
```

---

## 5. `strings.xml`

**Motivo:** añadir los dos nuevos mensajes de error que consume el ViewModel.

**Antes (línea 109):**
```xml
<string name="valoracion_enviada">¡Gracias por tu valoración!</string>
```

**Después:**
```xml
<string name="valoracion_enviada">¡Gracias por tu valoración!</string>
<string name="valoracion_error_sin_pictograma">Selecciona al menos un pictograma</string>
<string name="valoracion_error_guardar">Error al guardar la valoración</string>
```

---

## 6. Ajuste del tamaño por defecto del bottom sheet

**Motivo:** por defecto Material abre el `BottomSheetDialogFragment` ocupando
aproximadamente el 50% de la pantalla. Se quiere forzar que ocupe un
porcentaje mayor al abrirse (en este caso, el **85%**).

**Dónde se hace:** en `ValoracionBottomSheetFragment.kt`, sobreescribiendo
`onStart()`. No se puede hacer en `onCreate()` porque en ese momento el
diálogo y su vista interna aún no existen; `onStart()` es el primer punto
del ciclo de vida en el que el `BottomSheetBehavior` ya está disponible.

### 6.1. Import añadido

```kotlin
import com.google.android.material.bottomsheet.BottomSheetBehavior
```

### 6.2. Nuevo método `onStart()`

```kotlin
/**
 * Fuerza que el bottom sheet ocupe el 85% de la altura de pantalla al abrirse,
 * en lugar del ~50% por defecto de Material.
 */
override fun onStart() {
    super.onStart()
    val sheet = dialog?.findViewById<View>(
        com.google.android.material.R.id.design_bottom_sheet
    ) ?: return
    val alturaObjetivo = (resources.displayMetrics.heightPixels * 0.85).toInt()
    val behavior = BottomSheetBehavior.from(sheet)
    behavior.peekHeight = alturaObjetivo
    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
}
```

### Explicación línea a línea

1. `dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)`
   obtiene la vista interna que Material usa como contenedor del sheet.
2. `resources.displayMetrics.heightPixels * 0.85` calcula el 85% del alto
   de la pantalla en píxeles. Cambiando ese `0.85` se ajusta la proporción
   (`0.60` → 60%, `0.40` → 40%, etc.).
3. `BottomSheetBehavior.from(sheet)` accede al comportamiento del sheet.
4. `behavior.peekHeight = alturaObjetivo` fija la altura "asomada" en el
   estado colapsado.
5. `behavior.state = BottomSheetBehavior.STATE_COLLAPSED` asegura que el
   sheet abra en ese estado con la altura recién calculada.

El usuario puede seguir arrastrando hacia arriba para expandirlo más allá
de ese 85%, como hasta ahora.

---

## Resumen conceptual (para replicar en otros BottomSheets)

Para que un `Snackbar` se muestre correctamente dentro de un
`BottomSheetDialogFragment`:

1. **Layout del sheet**: la raíz debe ser un `CoordinatorLayout` (envolver el
   contenido actual). Sin esto, `Snackbar.make` se adjunta al Coordinator
   interno del diálogo y queda tapado por el sheet.
2. **Utilidad `mostrarSnackbar`**: no anclar al `BottomNav` cuando el fragment
   es un `DialogFragment`, ya que no está visible desde el diálogo.
3. **Snackbar de éxito antes de `dismiss()`**: mostrarlo desde
   `parentFragment?.mostrarSnackbar(...)` porque al cerrar el sheet el
   Snackbar interno se destruye antes de renderizarse.
4. **Anclar a otra vista** (p. ej. el botón de acción): usar el parámetro
   `anchorView` de `mostrarSnackbar`.
5. **Errores en el ViewModel**: emitir `Int?` con ID de recurso
   (`R.string.*`) en lugar de strings hardcodeados, para mantener el ViewModel
   libre de contexto Android y soportar i18n.
