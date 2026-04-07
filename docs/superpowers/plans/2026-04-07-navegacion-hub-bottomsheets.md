# Hub de Navegación y BottomSheets dinámicos — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Conectar las pantallas de la app para que el usuario pueda navegar fluidamente entre transacciones, historial, detalle del servicio y valoraciones sin callejones sin salida.

**Architecture:** Se añaden actions de navegación en `nav_graph.xml` (global action hacia `detalleServicioFragment`), se extiende `DetalleValoracionBottomSheet` con nuevos argumentos (`servicioId`, `esEnviada`), y se añade lógica en `DetalleServicioViewModel` para buscar la valoración asociada al servicio. No se toca lógica de negocio (aceptar/completar/cancelar), ni TTS, ni el Slider.

**Tech Stack:** Kotlin, XML layouts, Navigation Component, BottomSheetDialogFragment, Bundle arguments.

---

## Hallazgos del análisis previo

1. **`nav_graph.xml`**: Solo `escaparateFragment` tiene action hacia `detalleServicioFragment`. Ni `transaccionFragment` ni `perfilFragment` ni `historialFragment` la tienen. Se necesita una **global action** para evitar duplicar actions en cada fragment.
2. **`TransaccionAdapter`**: Actualmente no tiene callback `onItemClick` en el adapter. Los clicks van a los botones de acción (aceptar/completar/cancelar/valorar). Se necesita un nuevo callback para el tap en la tarjeta completa.
3. **`PerfilFragment`**: El `ServicioAdapter` ya tiene un `onServicioClick` lambda con un `TODO` — solo falta implementar la navegación.
4. **`HistorialFragment`**: El `onItemClick` del `HistorialAdapter` ya abre `DetalleValoracionBottomSheet` — no necesita cambios para T1, pero sí para T2 (pasar `servicioId`).
5. **`DetalleValoracionBottomSheet.newInstance()`** actualmente recibe: `pictogramasJson`, `comentario`, `timestamp`. Se amplía con `servicioId` y `esEnviada`.
6. **`DetalleServicioViewModel`** ya tiene acceso a `transaccionRepository` y `servicioRepository`. No tiene acceso a `valoracionRepository` — se necesita inyectar para T3.
7. **`TransaccionUI.transaccion.idServicioFk`** da acceso al servicioId desde cualquier transacción.
8. **`HistorialItem.transaccion.idServicioFk`** idem para historial.
9. **`TransaccionRepository.getByServicio(servicioId)`** devuelve la Transaccion asociada a un servicio.
10. **`ValoracionRepository.getByTransaccion(transaccionId)`** devuelve la Valoracion de una transacción.

---

## File Structure

| Archivo | Acción | Tarea |
|---------|--------|-------|
| `app/src/main/res/navigation/nav_graph.xml` | Modificar: añadir global action a detalleServicioFragment | T1 |
| `app/src/main/java/.../transaccion/TransaccionAdapter.kt` | Modificar: añadir callback onItemClick para tap en tarjeta | T1 |
| `app/src/main/res/layout/item_transaccion.xml` | Sin cambios (el click se pone en itemView) | — |
| `app/src/main/java/.../transaccion/TransaccionFragment.kt` | Modificar: pasar onItemClick que navega al detalle | T1 |
| `app/src/main/java/.../perfil/PerfilFragment.kt` | Modificar: implementar onServicioClick con navegación | T1 |
| `app/src/main/java/.../valoracion/DetalleValoracionBottomSheet.kt` | Modificar: añadir args servicioId + esEnviada, botón "Ver servicio", título dinámico | T2, T4 |
| `app/src/main/res/layout/bottom_sheet_detalle_valoracion.xml` | Modificar: añadir botón "Ver servicio", id al TextView título | T2, T4 |
| `app/src/main/java/.../historial/HistorialFragment.kt` | Modificar: pasar servicioId y esEnviada al crear DetalleValoracionBottomSheet | T2, T4 |
| `app/src/main/java/.../servicio/DetalleServicioFragment.kt` | Modificar: añadir botón "Ver valoración", lógica para mostrarlo | T3 |
| `app/src/main/res/layout/fragment_detalle_servicio.xml` | Modificar: añadir botón "Ver valoración" | T3 |
| `app/src/main/java/.../servicio/DetalleServicioViewModel.kt` | Modificar: añadir valoracionRepository, método para buscar valoración por servicio | T3 |
| `app/src/main/res/values/strings.xml` | Modificar: nuevos strings | T2, T3, T4 |

---

### Task 1: Navegar al detalle del servicio desde transacciones y perfil

**Files:**
- Modify: `app/src/main/res/navigation/nav_graph.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionFragment.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt`

**Estrategia:** Crear una **global action** en `nav_graph.xml` para que cualquier fragment pueda navegar a `detalleServicioFragment` sin necesitar una action individual. Añadir un `onItemClick` al `TransaccionAdapter` para el tap en la tarjeta (sin interferir con los botones de acción existentes). En `PerfilFragment`, completar el `onServicioClick` que ya tiene un `TODO`.

- [ ] **Step 1: Añadir global action en nav_graph.xml**

Añadir dentro de `<navigation>`, al mismo nivel que los `<fragment>`, justo antes de `</navigation>`:

```xml
<!-- ═══ Acción global: ir al detalle de servicio desde cualquier pantalla ═══ -->
<action
    android:id="@+id/action_global_to_detalle"
    app:destination="@id/detalleServicioFragment" />
```

Esto permite a cualquier fragment navegar con `findNavController().navigate(R.id.action_global_to_detalle, bundle)`.

- [ ] **Step 2: Añadir onItemClick al TransaccionAdapter**

En `TransaccionAdapter.kt`, añadir un nuevo parámetro lambda al constructor:

```kotlin
class TransaccionAdapter(
    private val onAceptar: (TransaccionUI) -> Unit,
    private val onCompletar: (TransaccionUI) -> Unit,
    private val onCancelar: (TransaccionUI) -> Unit,
    private val onValorar: (TransaccionUI) -> Unit,
    private val onItemClick: (TransaccionUI) -> Unit   // NUEVO
) : ListAdapter<TransaccionUI, TransaccionAdapter.TransaccionViewHolder>(TransaccionDiffCallback()) {
```

En `bind()`, añadir al final del método (después de `configurarBotones`):

```kotlin
itemView.setOnClickListener { onItemClick(item) }
```

**Nota:** Los botones (btnPositivo, btnNegativo) ya tienen sus propios `setOnClickListener`. Android propaga eventos del botón sin disparar el click del parent por defecto en este layout, así que no interfieren. Si se necesitara explícitamente evitar propagación, se podría usar `isClickable = true` en los botones (ya lo son por defecto).

- [ ] **Step 3: Pasar onItemClick en TransaccionFragment**

En `TransaccionFragment.configurarRecyclerView()`, añadir el parámetro `onItemClick` al crear el adapter. El lambda navega al detalle usando la global action:

```kotlin
adapter = TransaccionAdapter(
    onAceptar = { item -> /* ... existente ... */ },
    onCompletar = { item -> /* ... existente ... */ },
    onCancelar = { item -> /* ... existente ... */ },
    onValorar = { item -> /* ... existente ... */ },
    onItemClick = { item ->
        val bundle = Bundle().apply {
            putInt("servicioId", item.transaccion.idServicioFk)
        }
        findNavController().navigate(R.id.action_global_to_detalle, bundle)
    }
)
```

Añadir import si no existe:
```kotlin
import androidx.navigation.fragment.findNavController
```

- [ ] **Step 4: Implementar onServicioClick en PerfilFragment**

En `PerfilFragment.configurarRecyclerView()`, reemplazar el TODO:

```kotlin
servicioAdapter = ServicioAdapter(
    onServicioClick = { servicio ->
        val bundle = Bundle().apply {
            putInt("servicioId", servicio.idServicio)
        }
        findNavController().navigate(R.id.action_global_to_detalle, bundle)
    }
)
```

El import de `findNavController` ya existe en `PerfilFragment`.

- [ ] **Step 5: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/navigation/nav_graph.xml \
        app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt \
        app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt
git commit -m "feat: navegación al detalle del servicio desde transacciones y perfil"
```

---

### Task 2: Navegar al detalle del servicio desde el BottomSheet de valoración del historial

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/layout/bottom_sheet_detalle_valoracion.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/valoracion/DetalleValoracionBottomSheet.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/historial/HistorialFragment.kt`

**Estrategia:** Ampliar `newInstance()` con un parámetro `servicioId`. Añadir un botón "Ver servicio" en el layout. Al pulsarlo, cerrar el BottomSheet y navegar. El BottomSheet es un `BottomSheetDialogFragment` mostrado con `childFragmentManager`, así que para navegar necesita acceder al `NavController` del fragment padre (host). Se usa `requireActivity().findNavController(R.id.nav_host_fragment)` o se expone un callback lambda `onVerServicio`.

**Decisión de diseño — callback vs NavController directo:**
Usar un **callback** `onVerServicio: ((Int) -> Unit)?` es más limpio porque:
- El BottomSheet no necesita conocer el id del NavHost.
- Es consistente con el patrón `onDismissCallback` ya usado en `ValoracionBottomSheetFragment`.
- Evita acoplamiento con la estructura de navegación.

- [ ] **Step 1: Añadir strings**

En `strings.xml`, sección Valoraciones:
```xml
<string name="btn_ver_servicio">Ver servicio</string>
```

- [ ] **Step 2: Modificar bottom_sheet_detalle_valoracion.xml**

Añadir un botón "Ver servicio" encima del botón "Cerrar":

```xml
<!-- Botón ver servicio -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnVerServicio"
    style="@style/Widget.MaterialComponents.Button"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/btn_ver_servicio" />

<!-- Botón cerrar (existente) -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnCerrarDetalle"
    ... />
```

- [ ] **Step 3: Modificar DetalleValoracionBottomSheet.kt**

Añadir constante de argumento y propiedad de callback:

```kotlin
companion object {
    private const val ARG_PICTOGRAMAS = "pictogramasJson"
    private const val ARG_COMENTARIO = "comentario"
    private const val ARG_TIMESTAMP = "timestamp"
    private const val ARG_SERVICIO_ID = "servicioId"     // NUEVO

    fun newInstance(
        pictogramasJson: String,
        comentario: String?,
        timestamp: Long,
        servicioId: Int    // NUEVO
    ): DetalleValoracionBottomSheet {
        return DetalleValoracionBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_PICTOGRAMAS, pictogramasJson)
                putString(ARG_COMENTARIO, comentario ?: "")
                putLong(ARG_TIMESTAMP, timestamp)
                putInt(ARG_SERVICIO_ID, servicioId)     // NUEVO
            }
        }
    }
}
```

Añadir propiedad de clase:
```kotlin
private var servicioId: Int = -1

/** Callback que se invoca al pulsar "Ver servicio". Recibe el servicioId. */
var onVerServicioCallback: ((Int) -> Unit)? = null
```

En `onCreate()`, leer el nuevo argumento:
```kotlin
servicioId = it.getInt(ARG_SERVICIO_ID, -1)
```

En `onViewCreated()`, configurar el nuevo botón:
```kotlin
val btnVerServicio = view.findViewById<MaterialButton>(R.id.btnVerServicio)
if (servicioId > 0) {
    btnVerServicio.setOnClickListener {
        dismiss()
        onVerServicioCallback?.invoke(servicioId)
    }
} else {
    btnVerServicio.visibility = View.GONE
}
```

- [ ] **Step 4: Actualizar HistorialFragment para pasar servicioId y callback**

En `HistorialFragment.configurarRecyclerView()`, actualizar la llamada a `newInstance()`:

Actualmente:
```kotlin
val bottomSheet = DetalleValoracionBottomSheet.newInstance(
    pictogramasJson = valoracion.pictogramasJson,
    comentario = valoracion.comentario,
    timestamp = valoracion.timestamp
)
bottomSheet.show(childFragmentManager, "detalleValoracion")
```

Cambia a:
```kotlin
// Obtener el servicioId de la transacción del historial
val item = getItem(position)  // No disponible aquí — necesitamos pasar el servicioId
```

**Corrección:** El `onItemClick` del `HistorialAdapter` recibe `transaccionId`, no el `HistorialItem` completo. Hay que refactorizar: el callback debe pasar también el `servicioId`. Cambiar la lambda de `((Int) -> Unit)?` a `((HistorialItem) -> Unit)?` para tener acceso a `item.transaccion.idServicioFk`.

Cambios en `HistorialAdapter`:
```kotlin
class HistorialAdapter(
    private val usuarioActualId: Int,
    private val onItemClick: ((HistorialItem) -> Unit)? = null  // Cambiar de Int a HistorialItem
) : ...
```

En `bind()`:
```kotlin
itemView.setOnClickListener {
    onItemClick?.invoke(item)  // Pasar el item completo
}
```

En `HistorialFragment.configurarRecyclerView()`:
```kotlin
adapter = HistorialAdapter(
    usuarioActualId = sesion.obtenerUsuarioId()
) { item ->
    viewLifecycleOwner.lifecycleScope.launch {
        val valoracion = viewModel.obtenerValoracion(item.transaccion.idTransaccion)
        if (valoracion != null) {
            val bottomSheet = DetalleValoracionBottomSheet.newInstance(
                pictogramasJson = valoracion.pictogramasJson,
                comentario = valoracion.comentario,
                timestamp = valoracion.timestamp,
                servicioId = item.transaccion.idServicioFk
            )
            bottomSheet.onVerServicioCallback = { servicioId ->
                val bundle = Bundle().apply {
                    putInt("servicioId", servicioId)
                }
                findNavController().navigate(R.id.action_global_to_detalle, bundle)
            }
            bottomSheet.show(childFragmentManager, "detalleValoracion")
        } else {
            Toast.makeText(requireContext(), "Sin valoración", Toast.LENGTH_SHORT).show()
        }
    }
}
```

Añadir import en `HistorialFragment`:
```kotlin
import androidx.navigation.fragment.findNavController
```

- [ ] **Step 5: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/values/strings.xml \
        app/src/main/res/layout/bottom_sheet_detalle_valoracion.xml \
        app/src/main/java/com/example/vecindapp/ui/valoracion/DetalleValoracionBottomSheet.kt \
        app/src/main/java/com/example/vecindapp/ui/historial/HistorialFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt
git commit -m "feat: botón ver servicio en BottomSheet de valoración"
```

---

### Task 3: Abrir BottomSheet de valoración desde el detalle del servicio

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/layout/fragment_detalle_servicio.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/DetalleServicioViewModel.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt`

**Estrategia:** Desde el detalle del servicio, buscar si existe una transacción COMPLETADA asociada al servicio donde el usuario actual participe. Si la hay, buscar si existe una valoración para esa transacción. Si existe, mostrar un botón "Ver valoración". Al pulsarlo, abrir `DetalleValoracionBottomSheet` como modal.

**Flujo de datos:**
```
servicioId → TransaccionRepository.getByServicio(servicioId) → Transaccion?
    → si COMPLETADA y usuario participa →
        ValoracionRepository.getByTransaccion(transaccionId) → Valoracion?
            → si existe → mostrar botón "Ver valoración"
```

- [ ] **Step 1: Añadir strings**

En `strings.xml`:
```xml
<string name="btn_ver_valoracion">Ver valoración</string>
```

- [ ] **Step 2: Modificar fragment_detalle_servicio.xml**

Añadir un botón "Ver valoración" después de `btnEliminar`, dentro del ConstraintLayout:

```xml
<!-- ═══ Botón Ver Valoración ═══ -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnVerValoracion"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:text="@string/btn_ver_valoracion"
    android:visibility="gone"
    app:layout_constraintTop_toBottomOf="@id/btnEditar"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

**Nota sobre constraint:** El botón se posiciona debajo de `btnEditar`/`btnEliminar`. Estos botones están side-by-side constraídos a `btnSolicitar`. El nuevo botón va debajo de esa fila. Usar `btnEditar` como anchor top (tiene la misma bottom que `btnEliminar`).

- [ ] **Step 3: Añadir ValoracionRepository al DetalleServicioViewModel**

Modificar constructor y Factory:

```kotlin
class DetalleServicioViewModel(
    private val servicioRepository: ServicioRepository,
    private val transaccionRepository: TransaccionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val valoracionRepository: ValoracionRepository  // NUEVO
) : ViewModel() {
```

Añadir StateFlow para la valoración:
```kotlin
/** Valoración asociada al servicio (si existe). */
private val _valoracion = MutableStateFlow<Valoracion?>(null)
val valoracion: StateFlow<Valoracion?> = _valoracion
```

Añadir método para buscar la valoración:
```kotlin
/**
 * Busca si existe una valoración asociada a este servicio.
 * Cadena: servicio → transacción → valoración.
 */
fun buscarValoracion(servicioId: Int) {
    viewModelScope.launch {
        try {
            val transaccion = transaccionRepository.getByServicio(servicioId)
            if (transaccion != null) {
                val valoracion = valoracionRepository.getByTransaccion(transaccion.idTransaccion)
                _valoracion.value = valoracion
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```

Actualizar Factory:
```kotlin
class Factory(
    private val servicioRepository: ServicioRepository,
    private val transaccionRepository: TransaccionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val valoracionRepository: ValoracionRepository  // NUEVO
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleServicioViewModel::class.java)) {
            return DetalleServicioViewModel(
                servicioRepository, transaccionRepository, usuarioRepository, valoracionRepository
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

Imports necesarios:
```kotlin
import com.example.vecindapp.data.entities.Valoracion
import com.example.vecindapp.domain.repository.ValoracionRepository
```

- [ ] **Step 4: Modificar DetalleServicioFragment**

4a. Actualizar la creación del ViewModel para pasar `valoracionRepository`:

```kotlin
private val viewModel: DetalleServicioViewModel by viewModels {
    val app = requireActivity().application as VecindAppApplication
    DetalleServicioViewModel.Factory(
        app.servicioRepository,
        app.transaccionRepository,
        app.usuarioRepository,
        app.valoracionRepository  // NUEVO
    )
}
```

4b. Añadir referencia al nuevo botón:
```kotlin
private lateinit var btnVerValoracion: MaterialButton
```

En `configurarVistas()`:
```kotlin
btnVerValoracion = view.findViewById(R.id.btnVerValoracion)
```

4c. En `onViewCreated()`, después de `viewModel.cargarServicio(servicioId)`, llamar a:
```kotlin
viewModel.buscarValoracion(servicioId)
observarValoracion()
```

4d. Añadir método `observarValoracion()`:
```kotlin
/**
 * Observa si hay una valoración y muestra/oculta el botón correspondiente.
 */
private fun observarValoracion() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.valoracion.collect { valoracion ->
                if (valoracion != null) {
                    btnVerValoracion.visibility = View.VISIBLE
                    btnVerValoracion.setOnClickListener {
                        val bottomSheet = DetalleValoracionBottomSheet.newInstance(
                            pictogramasJson = valoracion.pictogramasJson,
                            comentario = valoracion.comentario,
                            timestamp = valoracion.timestamp,
                            servicioId = servicioActual?.idServicio ?: -1
                        )
                        bottomSheet.show(childFragmentManager, "detalleValoracion")
                    }
                } else {
                    btnVerValoracion.visibility = View.GONE
                }
            }
        }
    }
}
```

Import necesario:
```kotlin
import com.example.vecindapp.ui.valoracion.DetalleValoracionBottomSheet
```

**Nota:** Se abre como modal `BottomSheetDialogFragment` (no como navegación de fragment), evitando bucle en el back stack. El botón "Ver servicio" del BottomSheet no se mostrará aquí porque ya estamos en el detalle del servicio (el `onVerServicioCallback` no se configura, así que el dismiss no navega a ningún sitio).

- [ ] **Step 5: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/values/strings.xml \
        app/src/main/res/layout/fragment_detalle_servicio.xml \
        app/src/main/java/com/example/vecindapp/ui/servicio/DetalleServicioViewModel.kt \
        app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt
git commit -m "feat: ver valoración desde detalle del servicio como BottomSheet modal"
```

---

### Task 4: Título dinámico en BottomSheet de valoración

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/layout/bottom_sheet_detalle_valoracion.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/valoracion/DetalleValoracionBottomSheet.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/historial/HistorialFragment.kt`
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt` (si T3 ya pasaba al BottomSheet)

**Estrategia:** Añadir un booleano `esEnviada` a los arguments del BottomSheet. El título del BottomSheet se cambia dinámicamente: "Valoración enviada" si el usuario actual es el valorador, "Valoración recibida" si es el valorado. Esto requiere que quien abre el BottomSheet compare `valoracion.idValoradorFk` con el `usuarioActualId`.

- [ ] **Step 1: Añadir strings**

En `strings.xml`, sección Valoraciones:
```xml
<string name="valoracion_titulo_enviada">Valoración enviada</string>
<string name="valoracion_titulo_recibida">Valoración recibida</string>
```

- [ ] **Step 2: Añadir id al TextView del título en bottom_sheet_detalle_valoracion.xml**

El `TextView` del título actualmente NO tiene `android:id`. Añadir:

```xml
<TextView
    android:id="@+id/tvTituloValoracion"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    android:textSize="20sp"
    android:textStyle="bold"
    android:gravity="center" />
```

Quitar el `android:text="@string/detalle_valoracion_titulo"` estático — ahora se setea dinámicamente.

- [ ] **Step 3: Ampliar newInstance() con esEnviada**

En `DetalleValoracionBottomSheet.kt`:

```kotlin
companion object {
    private const val ARG_PICTOGRAMAS = "pictogramasJson"
    private const val ARG_COMENTARIO = "comentario"
    private const val ARG_TIMESTAMP = "timestamp"
    private const val ARG_SERVICIO_ID = "servicioId"
    private const val ARG_ES_ENVIADA = "esEnviada"     // NUEVO

    fun newInstance(
        pictogramasJson: String,
        comentario: String?,
        timestamp: Long,
        servicioId: Int,
        esEnviada: Boolean      // NUEVO
    ): DetalleValoracionBottomSheet {
        return DetalleValoracionBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_PICTOGRAMAS, pictogramasJson)
                putString(ARG_COMENTARIO, comentario ?: "")
                putLong(ARG_TIMESTAMP, timestamp)
                putInt(ARG_SERVICIO_ID, servicioId)
                putBoolean(ARG_ES_ENVIADA, esEnviada)   // NUEVO
            }
        }
    }
}
```

Añadir propiedad de clase:
```kotlin
private var esEnviada: Boolean = false
```

En `onCreate()`:
```kotlin
esEnviada = it.getBoolean(ARG_ES_ENVIADA, false)
```

En `onViewCreated()`, setear el título dinámicamente:
```kotlin
val tvTitulo = view.findViewById<TextView>(R.id.tvTituloValoracion)
tvTitulo.text = getString(
    if (esEnviada) R.string.valoracion_titulo_enviada
    else R.string.valoracion_titulo_recibida
)
```

- [ ] **Step 4: Actualizar HistorialFragment para pasar esEnviada**

En `HistorialFragment.configurarRecyclerView()`, al crear el BottomSheet, necesitamos saber si el usuario actual envió la valoración. `Valoracion` tiene `idValoradorFk`. Comparar con `sesion.obtenerUsuarioId()`:

```kotlin
val esEnviada = valoracion.idValoradorFk == sesion.obtenerUsuarioId()

val bottomSheet = DetalleValoracionBottomSheet.newInstance(
    pictogramasJson = valoracion.pictogramasJson,
    comentario = valoracion.comentario,
    timestamp = valoracion.timestamp,
    servicioId = item.transaccion.idServicioFk,
    esEnviada = esEnviada
)
```

**Nota:** La variable `sesion` ya se declara al principio de `configurarRecyclerView()`. Si está dentro del lambda, moverla fuera o declararla nuevamente.

- [ ] **Step 5: Actualizar DetalleServicioFragment para pasar esEnviada**

En el `observarValoracion()` de T3, al crear el BottomSheet:

```kotlin
val esEnviada = valoracion.idValoradorFk == usuarioActualId

val bottomSheet = DetalleValoracionBottomSheet.newInstance(
    pictogramasJson = valoracion.pictogramasJson,
    comentario = valoracion.comentario,
    timestamp = valoracion.timestamp,
    servicioId = servicioActual?.idServicio ?: -1,
    esEnviada = esEnviada
)
```

- [ ] **Step 6: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/values/strings.xml \
        app/src/main/res/layout/bottom_sheet_detalle_valoracion.xml \
        app/src/main/java/com/example/vecindapp/ui/valoracion/DetalleValoracionBottomSheet.kt \
        app/src/main/java/com/example/vecindapp/ui/historial/HistorialFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt
git commit -m "feat: título dinámico enviada/recibida en BottomSheet de valoración"
```

---

## Dependencias entre tareas

```
T1 (global action + navegación) ← T2 depende (usa action_global_to_detalle)
T2 (servicioId en BottomSheet) ← T3 depende (usa newInstance con servicioId)
T2 + T3 ← T4 depende (amplía newInstance con esEnviada, T3 ya pasa al BottomSheet)
```

**Orden de ejecución obligatorio:** T1 → T2 → T3 → T4.

## Notas de implementación

- **Global action vs action por fragment:** Se usa global action porque 3+ fragments necesitan navegar al mismo destino. Más limpio que duplicar actions.
- **BottomSheet vs navegación:** El BottomSheet se abre como modal (`show()`) y no como destino de navegación, evitando bucles en el back stack (DetalleServicio → BottomSheet → DetalleServicio).
- **Callback pattern:** Se usa `onVerServicioCallback` en vez de `NavController` directo para mantener el BottomSheet desacoplado de la estructura de navegación.
- **HistorialAdapter refactor:** Se cambia `onItemClick: ((Int) -> Unit)?` a `((HistorialItem) -> Unit)?` para poder acceder al `servicioId` sin queries adicionales. Es un cambio menor y backward compatible.
- **No se necesita `item_transaccion.xml`:** El click en la tarjeta de transacción se pone en `itemView.setOnClickListener` en el adapter, no en el XML.
