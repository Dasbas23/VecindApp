
# TTS VecindApp — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrar Text-To-Speech nativo de Android en las pantallas principales de VecindApp para accesibilidad auditiva.

**Architecture:** Un `TtsHelper` ligado al ciclo de vida del Fragment via `DefaultLifecycleObserver` gestiona el motor TTS; se instancia en cada Fragment que lo necesita y se destruye automáticamente al salir de la pantalla. Un `PictogramaMapper` centraliza el mapeo `tag → descripción/drawable`, eliminando la duplicación entre `ValoracionBottomSheetFragment` y `DetalleValoracionBottomSheet`.

**Tech Stack:** `android.speech.tts.TextToSpeech` (nativo), `androidx.lifecycle.DefaultLifecycleObserver`, Kotlin, XML layouts, `FloatingActionButton` de Material Design.

---

## Mapa de ficheros

| Fichero | Acción |
|---|---|
| `app/src/main/res/drawable/ic_volume_up.xml` | **Crear** — icono vector altavoz |
| `app/src/main/res/values/strings.xml` | **Modificar** — añadir 2 strings TTS |
| `app/src/main/java/…/ui/common/TtsHelper.kt` | **Crear** — wrapper TTS + lifecycle |
| `app/src/main/java/…/ui/valoracion/PictogramaMapper.kt` | **Crear** — mapeo centralizado de pictogramas |
| `app/src/main/java/…/ui/valoracion/DetalleValoracionBottomSheet.kt` | **Modificar** — delegar en PictogramaMapper |
| `app/src/main/res/layout/bottom_sheet_valoracion.xml` | **Modificar** — añadir ImageButton TTS junto al título |
| `app/src/main/java/…/ui/valoracion/ValoracionBottomSheetFragment.kt` | **Modificar** — TtsHelper + ibTts |
| `app/src/main/res/layout/fragment_escaparate.xml` | **Modificar** — añadir fabTts abajo-izquierda |
| `app/src/main/java/…/ui/escaparate/ServicioAdapter.kt` | **Modificar** — añadir onLongClick opcional |
| `app/src/main/java/…/ui/escaparate/EscaparateFragment.kt` | **Modificar** — TtsHelper + fabTts + longPress |
| `app/src/main/res/layout/fragment_detalle_servicio.xml` | **Modificar** — envolver en CoordinatorLayout + fabTts |
| `app/src/main/java/…/ui/servicio/DetalleServicioFragment.kt` | **Modificar** — TtsHelper + fabTts |
| `app/src/main/res/layout/fragment_perfil.xml` | **Modificar** — añadir fabTts abajo-izquierda |
| `app/src/main/java/…/ui/perfil/PerfilFragment.kt` | **Modificar** — TtsHelper + fabTts |

Ruta base: `app/src/main/java/com/example/vecindapp`

---

## Task 1: Recursos base — drawable ic_volume_up + strings TTS

**Files:**
- Create: `app/src/main/res/drawable/ic_volume_up.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Crear el vector drawable ic_volume_up**

Crear el fichero `app/src/main/res/drawable/ic_volume_up.xml` con este contenido exacto:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorOnPrimary">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M3,9v6h4l5,5V4L7,9H3zM16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02zM14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z"/>
</vector>
```

- [ ] **Step 2: Añadir strings TTS al final de la sección Accesibilidad en strings.xml**

Añadir justo antes del cierre `</resources>` en `app/src/main/res/values/strings.xml`:

```xml
    <!-- ═══ Accesibilidad TTS ═══ -->
    <string name="desc_tts">Leer en voz alta</string>
    <string name="tts_sin_pictogramas">Sin pictogramas seleccionados</string>
```

- [ ] **Step 3: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

---

## Task 2: Crear TtsHelper y PictogramaMapper + commit

**Files:**
- Create: `app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt`
- Create: `app/src/main/java/com/example/vecindapp/ui/valoracion/PictogramaMapper.kt`

- [ ] **Step 1: Crear el directorio common y TtsHelper**

Crear `app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt`:

```kotlin
package com.example.vecindapp.ui.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.Locale

/**
 * Helper de Text-To-Speech ligado al ciclo de vida de un Fragment.
 *
 * Se registra automáticamente como [DefaultLifecycleObserver] en el constructor,
 * por lo que el Fragment no necesita gestionar su ciclo de vida manualmente:
 * - [onStop]: para la reproducción en curso al cambiar de pantalla.
 * - [onDestroy]: hace shutdown del motor TTS al destruir la vista.
 *
 * ## Uso en un Fragment
 * ```kotlin
 * private lateinit var ttsHelper: TtsHelper
 *
 * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *     ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
 *     fabTts.setOnClickListener { ttsHelper.speak("Texto a leer") }
 * }
 * ```
 *
 * ## Inicialización asíncrona
 * El motor TTS tarda ~200 ms en inicializarse. Si [speak] se llama antes de que
 * esté listo, el texto se guarda en [pendingText] y se reproduce en cuanto
 * [TextToSpeech.OnInitListener.onInit] confirma éxito.
 *
 * @param context   Contexto para inicializar el motor. Se usa [Context.applicationContext]
 *                  para evitar fugas de memoria.
 * @param lifecycle Ciclo de vida al que se enlaza este helper (usar `viewLifecycleOwner.lifecycle`).
 */
class TtsHelper(
    context: Context,
    lifecycle: Lifecycle
) : DefaultLifecycleObserver {

    private var tts: TextToSpeech? = null
    private var pendingText: String? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "ES"))
                isReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED
                if (isReady) {
                    pendingText?.let { speak(it) }
                    pendingText = null
                } else {
                    Log.w("TtsHelper", "Idioma es_ES no soportado en este dispositivo")
                }
            } else {
                Log.e("TtsHelper", "Inicialización TTS fallida con status=$status")
            }
        }
        lifecycle.addObserver(this)
    }

    /**
     * Reproduce el texto dado en voz alta.
     *
     * Interrumpe cualquier reproducción en curso ([TextToSpeech.QUEUE_FLUSH]).
     * Si el motor aún no está listo, guarda el texto y lo reproduce en cuanto
     * esté disponible.
     *
     * @param text Texto a reproducir en español.
     */
    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            pendingText = text
        }
    }

    /**
     * Para la reproducción en curso sin liberar el motor.
     * Útil para implementar un botón de "parar".
     */
    fun stop() {
        tts?.stop()
    }

    /** Para la voz al cambiar de Fragment. Llamado automáticamente por el Lifecycle. */
    override fun onStop(owner: LifecycleOwner) {
        tts?.stop()
    }

    /** Libera el motor TTS al destruir la vista. Llamado automáticamente por el Lifecycle. */
    override fun onDestroy(owner: LifecycleOwner) {
        tts?.shutdown()
        tts = null
        isReady = false
    }

    companion object {
        /**
         * Formatea un valor de horas para lectura en voz alta.
         *
         * Elimina el decimal cuando es cero: `2.0` → `"2"`, `1.5` → `"1,5"`.
         * Usa coma como separador decimal para sonar natural en español.
         *
         * @param coste Valor en horas (Double).
         * @return String legible sin decimales innecesarios.
         */
        fun formatearCoste(coste: Double): String =
            if (coste == coste.toLong().toDouble())
                coste.toLong().toString()
            else
                coste.toString().replace('.', ',')
    }
}
```

- [ ] **Step 2: Crear PictogramaMapper**

Crear `app/src/main/java/com/example/vecindapp/ui/valoracion/PictogramaMapper.kt`:

```kotlin
package com.example.vecindapp.ui.valoracion

import android.content.Context
import com.example.vecindapp.R

/**
 * Mapeo centralizado de tags de pictogramas ARASAAC a sus recursos.
 *
 * Usado por [ValoracionBottomSheetFragment] (para TTS al seleccionar) y por
 * [DetalleValoracionBottomSheet] (para visualización en el historial),
 * evitando duplicar la misma lógica en dos clases.
 *
 * ## Tags soportados
 * - Bien: `bien_excelente`, `bien_amable`, `bien_puntual`
 * - Regular: `regular_normal`, `regular_mejorable`, `regular_lento`
 * - Mal: `mal_impuntual`, `mal_desagradable`, `mal_no_realizado`
 */
object PictogramaMapper {

    /**
     * Devuelve la descripción legible de un pictograma dado su tag interno.
     *
     * @param context Contexto para acceder a los recursos de strings.
     * @param tag     Tag interno del pictograma (p. ej. `"bien_excelente"`).
     * @return Descripción localizada (p. ej. `"Excelente"`), o el propio [tag] si no se reconoce.
     */
    fun obtenerDescripcion(context: Context, tag: String): String = when (tag) {
        "bien_excelente"    -> context.getString(R.string.desc_pictograma_bien1)
        "bien_amable"       -> context.getString(R.string.desc_pictograma_bien2)
        "bien_puntual"      -> context.getString(R.string.desc_pictograma_bien3)
        "regular_normal"    -> context.getString(R.string.desc_pictograma_regular1)
        "regular_mejorable" -> context.getString(R.string.desc_pictograma_regular2)
        "regular_lento"     -> context.getString(R.string.desc_pictograma_regular3)
        "mal_impuntual"     -> context.getString(R.string.desc_pictograma_mal1)
        "mal_desagradable"  -> context.getString(R.string.desc_pictograma_mal2)
        "mal_no_realizado"  -> context.getString(R.string.desc_pictograma_mal3)
        else                -> tag
    }

    /**
     * Devuelve el recurso drawable correspondiente a un tag de pictograma.
     *
     * @param tag Tag interno del pictograma.
     * @return ID del recurso drawable, o [android.R.drawable.ic_menu_help] si no se reconoce.
     */
    fun obtenerDrawable(tag: String): Int = when (tag) {
        "bien_excelente"    -> R.drawable.bien_excelente
        "bien_amable"       -> R.drawable.bien_amable
        "bien_puntual"      -> R.drawable.bien_puntual
        "regular_normal"    -> R.drawable.regular_ok
        "regular_mejorable" -> R.drawable.regular_mejorable
        "regular_lento"     -> R.drawable.regular_lento
        "mal_impuntual"     -> R.drawable.mal_impuntual
        "mal_desagradable"  -> R.drawable.mal_desagradable
        "mal_no_realizado"  -> R.drawable.mal_no_realizado
        else                -> android.R.drawable.ic_menu_help
    }
}
```

- [ ] **Step 3: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/drawable/ic_volume_up.xml \
        app/src/main/res/values/strings.xml \
        app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt \
        app/src/main/java/com/example/vecindapp/ui/valoracion/PictogramaMapper.kt
git commit -m "feat: añadir TtsHelper y PictogramaMapper (base reutilizable TTS)"
```

---

## Task 3: Refactorizar DetalleValoracionBottomSheet → PictogramaMapper + commit

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/valoracion/DetalleValoracionBottomSheet.kt`

- [ ] **Step 1: Añadir import de PictogramaMapper**

En `DetalleValoracionBottomSheet.kt`, añadir al bloque de imports:

```kotlin
import com.example.vecindapp.ui.valoracion.PictogramaMapper
```

(Ya está en el mismo paquete, por lo que puede que no sea necesario, pero no hace daño incluirlo explícitamente para mayor claridad.)

- [ ] **Step 2: Actualizar crearPictogramaView para usar PictogramaMapper**

Localizar el método `crearPictogramaView` (líneas ~106-136). Dentro de él, hay dos llamadas a métodos privados que deben ser sustituidas:

Sustituir:
```kotlin
val icono = obtenerDrawable(tag)
setImageResource(icono)
```

Por:
```kotlin
setImageResource(PictogramaMapper.obtenerDrawable(tag))
```

Sustituir:
```kotlin
text = obtenerDescripcion(tag)
```

Por:
```kotlin
text = PictogramaMapper.obtenerDescripcion(requireContext(), tag)
```

- [ ] **Step 3: Eliminar los métodos privados obtenerDescripcion y obtenerDrawable**

Eliminar completamente los métodos privados `obtenerDescripcion` (líneas ~159-172) y `obtenerDrawable` (líneas ~141-155) del fichero. El fichero no debe contener ninguna versión local de estos métodos.

- [ ] **Step 4: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/valoracion/DetalleValoracionBottomSheet.kt
git commit -m "refactor: DetalleValoracionBottomSheet delega en PictogramaMapper"
```

---

## Task 4: TTS en ValoracionBottomSheetFragment + commit

**Files:**
- Modify: `app/src/main/res/layout/bottom_sheet_valoracion.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/valoracion/ValoracionBottomSheetFragment.kt`

- [ ] **Step 1: Añadir ImageButton TTS al layout del BottomSheet**

En `bottom_sheet_valoracion.xml`, dentro del `LinearLayout` raíz (que tiene `android:padding="20dp"`), el primer hijo es un `TextView` con `android:text="@string/valoracion_titulo"`. Sustituirlo por un `LinearLayout` horizontal que lo envuelva junto al `ImageButton`:

Sustituir el bloque:
```xml
        <!-- Título -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@string/valoracion_titulo"
            android:textSize="20sp"
            android:textStyle="bold"
            android:gravity="center"
            android:paddingBottom="4dp" />
```

Por:
```xml
        <!-- Fila título + botón TTS -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingBottom="4dp">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/valoracion_titulo"
                android:textSize="20sp"
                android:textStyle="bold"
                android:gravity="center" />

            <ImageButton
                android:id="@+id/ibTts"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:src="@drawable/ic_volume_up"
                android:background="?android:attr/selectableItemBackgroundBorderless"
                android:contentDescription="@string/desc_tts"
                android:padding="8dp"
                android:tint="?attr/colorPrimary" />
        </LinearLayout>
```

- [ ] **Step 2: Añadir imports a ValoracionBottomSheetFragment**

En `ValoracionBottomSheetFragment.kt`, añadir al bloque de imports:

```kotlin
import android.widget.ImageButton
import com.example.vecindapp.ui.common.TtsHelper
import com.example.vecindapp.ui.valoracion.PictogramaMapper
```

- [ ] **Step 3: Añadir ttsHelper como propiedad de clase**

En `ValoracionBottomSheetFragment.kt`, después de la línea `private val colorNormal = Color.TRANSPARENT`, añadir:

```kotlin
private lateinit var ttsHelper: TtsHelper
```

- [ ] **Step 4: Inicializar TtsHelper y conectar ibTts en onViewCreated**

En `onViewCreated`, después de `super.onViewCreated(view, savedInstanceState)` y antes de `configurarPictogramas(view)`, añadir:

```kotlin
ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
configurarBotonTts(view)
```

- [ ] **Step 5: Implementar configurarBotonTts**

Añadir el siguiente método privado a `ValoracionBottomSheetFragment`, justo después de `configurarPictogramas`:

```kotlin
/**
 * Conecta el [ImageButton] de altavoz para leer en voz alta los pictogramas
 * actualmente seleccionados.
 *
 * Si no hay ninguno seleccionado, lee "Sin pictogramas seleccionados".
 * Las descripciones se obtienen de [PictogramaMapper] para ser legibles por humanos.
 */
private fun configurarBotonTts(view: View) {
    val ibTts = view.findViewById<ImageButton>(R.id.ibTts)
    ibTts.setOnClickListener {
        val texto = if (pictogramasSeleccionados.isEmpty()) {
            getString(R.string.tts_sin_pictogramas)
        } else {
            pictogramasSeleccionados.joinToString(", ") { tag ->
                PictogramaMapper.obtenerDescripcion(requireContext(), tag)
            }
        }
        ttsHelper.speak(texto)
    }
}
```

- [ ] **Step 6: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Probar en dispositivo/emulador**

```bash
./gradlew installDebug
```

Flujo a probar:
1. Completar una transacción para abrir el BottomSheet de valoración.
2. Pulsar el icono de altavoz sin seleccionar ningún pictograma → debe escucharse "Sin pictogramas seleccionados".
3. Seleccionar "Excelente" y "Puntual" → pulsar el altavoz → debe escucharse "Excelente, Puntual".
4. Cambiar de pantalla → la voz debe detenerse.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/res/layout/bottom_sheet_valoracion.xml \
        app/src/main/java/com/example/vecindapp/ui/valoracion/ValoracionBottomSheetFragment.kt
git commit -m "feat: TTS en BottomSheet de valoración"
```

---

## Task 5: TTS en EscaparateFragment + commit

**Files:**
- Modify: `app/src/main/res/layout/fragment_escaparate.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/escaparate/EscaparateFragment.kt`

- [ ] **Step 1: Añadir fabTts al layout del Escaparate**

En `fragment_escaparate.xml`, dentro del `ConstraintLayout` raíz, añadir un segundo FAB justo después del existente `fabCrearServicio`:

```xml
    <!-- Botón flotante para leer en voz alta los servicios visibles -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabTts"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/desc_tts"
        android:src="@drawable/ic_volume_up"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
```

- [ ] **Step 2: Añadir onLongClick opcional a ServicioAdapter**

En `ServicioAdapter.kt`, modificar el constructor de la clase para añadir el segundo lambda con valor por defecto `null`:

Sustituir:
```kotlin
class ServicioAdapter(
    private val onServicioClick: (Servicio) -> Unit
) : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(ServicioDiffCallback()) {
```

Por:
```kotlin
/**
 * Adapter del RecyclerView para mostrar las tarjetas de servicios en el escaparate.
 *
 * Utiliza [ListAdapter] con [DiffUtil] para calcular las diferencias entre
 * listas de forma eficiente: solo se redibujan las tarjetas que han cambiado,
 * en lugar de refrescar toda la lista.
 *
 * Cada tarjeta muestra: título, categoría, coste en horas y pictograma ARASAAC.
 * Al pulsar una tarjeta se ejecuta [onServicioClick]. Al mantener pulsada se
 * ejecuta [onServicioLongClick] si está definido.
 *
 * @property onServicioClick     Lambda ejecutado al pulsar una tarjeta.
 * @property onServicioLongClick Lambda ejecutado al mantener pulsada una tarjeta
 *                               (opcional). Debe devolver `true` para consumir el evento.
 *
 * @see EscaparateFragment
 * @see EscaparateViewModel
 */
class ServicioAdapter(
    private val onServicioClick: (Servicio) -> Unit,
    private val onServicioLongClick: ((Servicio) -> Boolean)? = null
) : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(ServicioDiffCallback()) {
```

- [ ] **Step 3: Registrar setOnLongClickListener en el ViewHolder**

En el método `bind` del `ServicioViewHolder`, después de `itemView.setOnClickListener { onServicioClick(servicio) }`, añadir:

```kotlin
if (onServicioLongClick != null) {
    itemView.setOnLongClickListener { onServicioLongClick.invoke(servicio) }
} else {
    itemView.setOnLongClickListener(null)
}
```

- [ ] **Step 4: Actualizar EscaparateFragment para usar TtsHelper**

Añadir los siguientes imports en `EscaparateFragment.kt` (los de `LinearLayoutManager` y `RecyclerView` ya están presentes — no duplicar):

```kotlin
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.vecindapp.ui.common.TtsHelper
```

Añadir las propiedades de clase después de `private lateinit var tvVacio: TextView`:

```kotlin
private lateinit var fabTts: FloatingActionButton
private lateinit var ttsHelper: TtsHelper
```

- [ ] **Step 5: Inicializar TtsHelper al inicio de onViewCreated**

En `onViewCreated`, añadir como primera línea (antes de `configurarVistas`):

```kotlin
ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
```

- [ ] **Step 6: Referenciar fabTts en configurarVistas**

En el método `configurarVistas`, añadir después de la línea `tvVacio = view.findViewById(R.id.tvVacio)`:

```kotlin
fabTts = view.findViewById(R.id.fabTts)
```

- [ ] **Step 7: Pasar onLongClick al adapter en configurarRecyclerView**

Sustituir la creación del adapter:
```kotlin
adapter = ServicioAdapter { servicio ->
    val bundle = Bundle().apply {
        putInt("servicioId", servicio.idServicio)
    }
    findNavController().navigate(R.id.action_escaparate_to_detalle, bundle)
}
```

Por:
```kotlin
adapter = ServicioAdapter(
    onServicioClick = { servicio ->
        val bundle = Bundle().apply {
            putInt("servicioId", servicio.idServicio)
        }
        findNavController().navigate(R.id.action_escaparate_to_detalle, bundle)
    },
    onServicioLongClick = { servicio ->
        ttsHelper.speak(
            "${servicio.titulo}, ${TtsHelper.formatearCoste(servicio.costeHoras)} horas"
        )
        true
    }
)
```

- [ ] **Step 8: Añadir llamada a configurarFabTts en onViewCreated y implementar el método**

En `onViewCreated`, añadir `configurarFabTts()` después de `configurarFab(view)`.

Añadir el método privado al final de la clase, antes del cierre `}`:

```kotlin
/**
 * Configura el FAB de TTS para leer en voz alta los servicios
 * actualmente visibles en el RecyclerView.
 *
 * Solo lee los ítems dentro del viewport (posiciones firstVisible..lastVisible).
 * Cada servicio se anuncia como "título, X horas". Los ítems se separan
 * con ". " para que el motor TTS interprete una pausa natural entre ellos.
 */
private fun configurarFabTts() {
    fabTts.setOnClickListener {
        val lm = rvEscaparate.layoutManager as LinearLayoutManager
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION) return@setOnClickListener
        val texto = (first..last)
            .mapNotNull { adapter.currentList.getOrNull(it) }
            .joinToString(". ") { s ->
                "${s.titulo}, ${TtsHelper.formatearCoste(s.costeHoras)} horas"
            }
        if (texto.isNotBlank()) ttsHelper.speak(texto)
    }
}
```

- [ ] **Step 9: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 10: Probar en dispositivo/emulador**

```bash
./gradlew installDebug
```

Flujo a probar:
1. Abrir el Escaparate → pulsar el FAB de altavoz (abajo-izquierda) → debe escuchar los títulos y costes de las tarjetas visibles.
2. Mantener pulsada una tarjeta → debe escuchar solo ese servicio.
3. Tap corto en tarjeta → debe navegar al detalle (comportamiento sin cambios).
4. Pulsar el FAB de altavoz y luego navegar a otra pestaña → la voz debe detenerse.

- [ ] **Step 11: Commit**

```bash
git add app/src/main/res/layout/fragment_escaparate.xml \
        app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt \
        app/src/main/java/com/example/vecindapp/ui/escaparate/EscaparateFragment.kt
git commit -m "feat: TTS en Escaparate (FAB global + long press en tarjeta)"
```

---

## Task 6: TTS en DetalleServicioFragment + commit

**Files:**
- Modify: `app/src/main/res/layout/fragment_detalle_servicio.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/DetalleServicioFragment.kt`

- [ ] **Step 1: Envolver fragment_detalle_servicio.xml en CoordinatorLayout**

El root actual es `<ScrollView>`. Hay que envolverlo en un `CoordinatorLayout` para poder añadir un FAB flotante sobre el contenido scrollable.

Sustituir la línea de apertura del `ScrollView`:
```xml
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
```

Por:
```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true">
```

Añadir el cierre del `ScrollView` y el FAB antes del cierre del `CoordinatorLayout`. Sustituir la línea final `</ScrollView>` por:

```xml
    </ScrollView>

    <!-- Botón flotante para leer en voz alta el contenido del servicio -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabTts"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|start"
        android:layout_margin="16dp"
        android:contentDescription="@string/desc_tts"
        android:src="@drawable/ic_volume_up" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

Nota: el `ConstraintLayout` interior y su contenido no cambian en absoluto.

- [ ] **Step 2: Añadir imports a DetalleServicioFragment**

Añadir al bloque de imports de `DetalleServicioFragment.kt`:

```kotlin
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.vecindapp.ui.common.TtsHelper
```

- [ ] **Step 3: Añadir propiedades de clase a DetalleServicioFragment**

Después de `private lateinit var btnEliminar: MaterialButton`, añadir:

```kotlin
private lateinit var fabTts: FloatingActionButton
private lateinit var ttsHelper: TtsHelper
private var servicioActual: Servicio? = null
```

- [ ] **Step 4: Referenciar fabTts en configurarVistas**

Al final del método `configurarVistas`, añadir:

```kotlin
fabTts = view.findViewById(R.id.fabTts)
```

- [ ] **Step 5: Inicializar TtsHelper y configurar fabTts en onViewCreated**

En `onViewCreated`, después de `configurarVistas(view)`, añadir:

```kotlin
ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
configurarFabTts()
```

- [ ] **Step 6: Guardar servicio en pintarDetalle**

En el método `pintarDetalle(servicio: Servicio)`, añadir como primera línea:

```kotlin
servicioActual = servicio
```

- [ ] **Step 7: Implementar configurarFabTts**

Añadir el siguiente método privado al final de la clase, antes del cierre `}`:

```kotlin
/**
 * Configura el FAB de TTS para leer en voz alta el detalle completo
 * del servicio actualmente cargado.
 *
 * Lee: título → categoría → coste → descripción → estado.
 * No hace nada si el servicio aún no se ha cargado.
 */
private fun configurarFabTts() {
    fabTts.setOnClickListener {
        val s = servicioActual ?: return@setOnClickListener
        val desc = s.descripcion ?: getString(R.string.sin_descripcion)
        val costeTexto = TtsHelper.formatearCoste(s.costeHoras)
        val texto = "${s.titulo}. ${s.categoria.name}. $costeTexto horas. $desc. ${s.estado.name}"
        ttsHelper.speak(texto)
    }
}
```

- [ ] **Step 8: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Probar en dispositivo/emulador**

```bash
./gradlew installDebug
```

Flujo a probar:
1. Pulsar una tarjeta del Escaparate para ir al Detalle.
2. Pulsar el FAB de altavoz (abajo-izquierda) → debe escuchar título, categoría, coste, descripción y estado.
3. Pulsar el botón Atrás o cambiar de pantalla → la voz debe detenerse.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/res/layout/fragment_detalle_servicio.xml \
        app/src/main/java/com/example/vecindapp/ui/servicio/DetalleServicioFragment.kt
git commit -m "feat: TTS en Detalle del Servicio"
```

---

## Task 7: TTS en PerfilFragment + commit

**Files:**
- Modify: `app/src/main/res/layout/fragment_perfil.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt`

- [ ] **Step 1: Añadir fabTts al layout del Perfil**

En `fragment_perfil.xml`, dentro del `ConstraintLayout` raíz, añadir el FAB al final (antes del cierre del `ConstraintLayout`):

```xml
    <!-- Botón flotante para leer en voz alta los datos del perfil -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabTts"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/desc_tts"
        android:src="@drawable/ic_volume_up"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
```

- [ ] **Step 2: Añadir imports a PerfilFragment**

Añadir al bloque de imports de `PerfilFragment.kt`:

```kotlin
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.vecindapp.ui.common.TtsHelper
```

- [ ] **Step 3: Añadir propiedades de clase a PerfilFragment**

Después de `private lateinit var tvVacioMisServicios: TextView`, añadir:

```kotlin
private lateinit var fabTts: FloatingActionButton
private lateinit var ttsHelper: TtsHelper
private var usuarioActual: Usuario? = null
```

- [ ] **Step 4: Referenciar fabTts en configurarVistas**

Al final del bloque de asignaciones en `configurarVistas` (después de la línea de `btnCerrarSesion`), añadir:

```kotlin
fabTts = view.findViewById(R.id.fabTts)
```

- [ ] **Step 5: Inicializar TtsHelper y configurar fabTts en onViewCreated**

En `onViewCreated`, después de `configurarVistas(view)`, añadir:

```kotlin
ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
configurarFabTts()
```

- [ ] **Step 6: Guardar usuario en pintarPerfil**

En el método `pintarPerfil(usuario: Usuario)`, añadir como primera línea:

```kotlin
usuarioActual = usuario
```

- [ ] **Step 7: Implementar configurarFabTts**

Añadir el siguiente método privado al final de la clase, antes del cierre `}`:

```kotlin
/**
 * Configura el FAB de TTS para leer en voz alta los datos principales
 * del perfil del usuario: nombre, barrio, saldo de horas y nivel.
 *
 * No lee la lista de servicios para evitar lecturas excesivamente largas.
 * No hace nada si el usuario aún no se ha cargado.
 */
private fun configurarFabTts() {
    fabTts.setOnClickListener {
        val u = usuarioActual ?: return@setOnClickListener
        val saldoTexto = TtsHelper.formatearCoste(u.saldoHoras)
        val texto = "${u.nombre}. ${u.barrio}. $saldoTexto horas. Nivel ${u.nivel.name}"
        ttsHelper.speak(texto)
    }
}
```

- [ ] **Step 8: Verificar que compila**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Probar en dispositivo/emulador**

```bash
./gradlew installDebug
```

Flujo a probar:
1. Ir a la pestaña Perfil.
2. Pulsar el FAB de altavoz (abajo-izquierda) → debe escuchar nombre, barrio, saldo y nivel.
3. Cambiar de pestaña → la voz debe detenerse.
4. Volver al Perfil y pulsar de nuevo → debe funcionar (nuevo TtsHelper inicializado).

- [ ] **Step 10: Commit**

```bash
git add app/src/main/res/layout/fragment_perfil.xml \
        app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt
git commit -m "feat: TTS en Perfil"
```

---

## Verificación final

- [ ] Ejecutar build limpio:

```bash
./gradlew clean assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] Smoke test completo en dispositivo:

```bash
./gradlew installDebug
```

Checklist:
- [ ] Escaparate: FAB altavoz abajo-izquierda lee tarjetas visibles
- [ ] Escaparate: Long press en tarjeta lee solo esa tarjeta
- [ ] Escaparate: Tap corto sigue navegando al detalle (sin cambios)
- [ ] Detalle Servicio: FAB altavoz lee título, categoría, coste, descripción y estado
- [ ] Perfil: FAB altavoz lee nombre, barrio, saldo y nivel
- [ ] BottomSheet valoración: botón altavoz lee pictogramas seleccionados
- [ ] BottomSheet valoración: sin pictogramas seleccionados lee "Sin pictogramas seleccionados"
- [ ] Cambiar de pantalla durante lectura → voz se detiene
- [ ] DetalleValoracionBottomSheet sigue mostrando pictogramas correctamente (refactor transparente)