# Formato "horas/minutos humano" unificado — Plan de implementación  claude --resume a2a876f9-076b-4839-a382-d20ca0170fd9

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unificar el formateo de horas en toda la app: sustituir el rígido `"%.1f h"` por un formato humano (`"2h"`, `"1h 30min"`, `"30min"`) para la UI y una lectura TTS natural (`"2 horas"`, `"1 hora y 30 minutos"`), centralizando la lógica en `TtsHelper` para eliminar duplicación con `CrearServicioFragment.formatearHoras`.

**Architecture:** Una única fuente de verdad en `TtsHelper.kt` (companion object) expone dos funciones públicas puras: `formatearCosteHumano(Double)` para textos visuales y `formatearCosteConUnidad(Double)` para TTS. Ambas se basan en descomponer el `Double` en horas enteras + minutos (`totalMinutos = (coste * 60).toInt()`). Todos los Adapters/Fragments pasan a llamar a estas funciones en lugar de `getString(R.string.formato_coste_horas, ...)` o `String.format("%.1f", ...)`. Los recursos de string obsoletos se eliminan. El cambio es puramente de presentación: nada de DB, entidades ni DAOs se tocan.

**Tech Stack:** Kotlin, Android framework (res/strings, Fragment/Adapter), `TtsHelper` existente.

---

## Inventario de usos confirmados (auditoría previa)

Búsqueda de `formato_coste_horas`, `formato_historial_horas` y `"%.1f"` aplicado a horas:

**`R.string.formato_coste_horas` (`"%.1f h"`)**
- `app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt:67-70` (`tvCoste`)
- `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt:74-77` (`tvHoras`)
- `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt:178` (`tvCoste`)

**`R.string.formato_historial_horas` (`"%1$s%2$.1f h"`)**
- `app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt:70-74` (`tvHoras`, con signo `+`/`-`)

**`String.format("%.1f", ...)` sobre horas**
- `app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt:144` (`tvSaldoHoras`)

**`CrearServicioFragment.formatearHoras(Float)` (duplicado a refactorizar)**
- Definición: `CrearServicioFragment.kt:166-170` (companion).
- Usos internos: `CrearServicioFragment.kt:107, 108, 110` (slider crear servicio).
- Usos externos: `Detalleserviciofragment.kt:265, 267, 271` (slider del diálogo de edición).

**Strings con `%.1f` FUERA DE ALCANCE (dialogos de confirmación — NO se tocan en este plan):**
- `strings.xml:50` `mensaje_confirmar_solicitud`
- `strings.xml:65` `mensaje_confirmar_completar`
- Estos son textos de `AlertDialog` cuyo redactado completo es distinto y no encajan con la sustitución directa. Si el usuario quiere actualizarlos, será en un plan separado.

---

## File Structure

**Modificar:**
- `app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt` — reemplazar el companion object: eliminar `formatearCoste` privado, añadir `formatearCosteHumano` público, reescribir `formatearCosteConUnidad` con lógica h/min.
- `app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt` — `bind` usa `TtsHelper.formatearCosteHumano`.
- `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt` — idem.
- `app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt` — concatenar signo con `formatearCosteHumano`.
- `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt` — `tvCoste` y los 3 usos del label del slider en el diálogo de edición.
- `app/src/main/java/com/example/vecindapp/ui/servicio/CrearServicioFragment.kt` — eliminar companion `formatearHoras`; los 3 usos locales delegan en `TtsHelper.formatearCosteHumano(valor.toDouble())`.
- `app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt` — `tvSaldoHoras` usa `formatearCosteHumano`.
- `app/src/main/res/values/strings.xml` — eliminar `formato_coste_horas` y `formato_historial_horas`.

**Crear:** ninguno.

**Test:** proyecto sin infraestructura de tests más allá de `ExampleUnitTest`. La verificación será un build Gradle + revisión manual en Task 7.

---

## Task 1: Refactor de `TtsHelper.kt` — nuevas funciones de formato

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt:104-133`

- [ ] **Step 1: Reemplazar el `companion object` completo**

Sustituir todo el bloque `companion object { ... }` (líneas 104-133) por:

```kotlin
    companion object {

        /**
         * Formatea un valor de horas (Double) para mostrar en la UI.
         *
         * Descompone el coste en horas y minutos enteros, eliminando decimales
         * del tipo `"1,5 h"` en favor de un formato más humano:
         * - `2.0` → `"2h"`
         * - `1.5` → `"1h 30min"`
         * - `0.25` → `"15min"`
         * - `0.0` → `"0min"`
         *
         * @param coste Valor en horas (Double).
         * @return String corto listo para pintar en un TextView.
         */
        fun formatearCosteHumano(coste: Double): String {
            val totalMinutos = (coste * 60).toInt()
            val horas = totalMinutos / 60
            val minutos = totalMinutos % 60
            return when {
                horas == 0 -> "${minutos}min"
                minutos == 0 -> "${horas}h"
                else -> "${horas}h ${minutos}min"
            }
        }

        /**
         * Formatea un valor de horas (Double) para lectura en voz alta por TTS.
         *
         * Usa unidades completas con singular/plural correcto y conector "y"
         * cuando hay horas y minutos:
         * - `2.0` → `"2 horas"`
         * - `1.0` → `"1 hora"`
         * - `1.5` → `"1 hora y 30 minutos"`
         * - `0.25` → `"15 minutos"`
         * - `0.0` → `"0 horas"`
         *
         * @param coste Valor en horas (Double).
         * @return String natural para ser pronunciado por el motor TTS.
         */
        fun formatearCosteConUnidad(coste: Double): String {
            val totalMinutos = (coste * 60).toInt()
            val horas = totalMinutos / 60
            val minutos = totalMinutos % 60

            val textoHoras = when (horas) {
                0 -> null
                1 -> "1 hora"
                else -> "$horas horas"
            }
            val textoMinutos = when (minutos) {
                0 -> null
                1 -> "1 minuto"
                else -> "$minutos minutos"
            }

            return when {
                textoHoras != null && textoMinutos != null -> "$textoHoras y $textoMinutos"
                textoHoras != null -> textoHoras
                textoMinutos != null -> textoMinutos
                else -> "0 horas"
            }
        }
    }
```

Notas:
- Se elimina el `private fun formatearCoste(coste: Double)` anterior porque ya no se usa en ningún sitio (la nueva `formatearCosteConUnidad` ya no lo necesita).
- `import java.util.Locale` puede dejar de usarse tras este cambio. Si el IDE lo marca como import no usado, elimínalo en el Step 2.

- [ ] **Step 2: Limpiar import `java.util.Locale` si queda huérfano**

Revisar el archivo completo. Tras el Step 1 ya no se usa `Locale` dentro del companion. Si tampoco se usa en otra parte del archivo (solo estaba en la línea 9), eliminar la línea:

```kotlin
import java.util.Locale
```

- [ ] **Step 3: Compilar para verificar que no hay errores en TtsHelper**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL. (Habrá errores en otros archivos que aún usan `formatearHoras` de `CrearServicioFragment` — OK, se resolverán en tareas siguientes; pero `TtsHelper.kt` por sí mismo debe compilar sin errores. Si aparece un error, arreglarlo antes de continuar.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt
git commit -m "refactor(tts): nueva API de formateo de horas en TtsHelper

- Añadir formatearCosteHumano(Double) para UI ('2h', '1h 30min').
- Reescribir formatearCosteConUnidad con lógica horas/minutos natural.
- Eliminar formatearCoste privado (ya no necesario)."
```

---

## Task 2: Sustituir `formato_coste_horas` en `ServicioAdapter`

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt:64-71`

- [ ] **Step 1: Reemplazar la asignación de `tvCoste.text`**

Buscar el bloque:

```kotlin
            tvCoste.text = itemView.context.getString(
                R.string.formato_coste_horas,
                servicio.costeHoras
            )
```

y reemplazarlo por:

```kotlin
            tvCoste.text = TtsHelper.formatearCosteHumano(servicio.costeHoras)
```

- [ ] **Step 2: Añadir el import si falta**

Asegurar que al inicio del archivo exista:

```kotlin
import com.example.vecindapp.ui.common.TtsHelper
```

(Android Studio puede añadirlo automáticamente; verificar manualmente si el build falla.)

- [ ] **Step 3: Compilar**

Run: `./gradlew compileDebugKotlin`
Expected: este archivo compila (pueden persistir errores en otros ficheros por el refactor en curso).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt
git commit -m "refactor(escaparate): usar TtsHelper.formatearCosteHumano en ServicioAdapter"
```

---

## Task 3: Sustituir `formato_coste_horas` en `TransaccionAdapter`

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt:74-77`

- [ ] **Step 1: Reemplazar la asignación de `tvHoras.text`**

Buscar:

```kotlin
            tvHoras.text = itemView.context.getString(
                R.string.formato_coste_horas,
                item.horas
            )
```

y reemplazar por:

```kotlin
            tvHoras.text = TtsHelper.formatearCosteHumano(item.horas)
```

- [ ] **Step 2: Añadir import si falta**

```kotlin
import com.example.vecindapp.ui.common.TtsHelper
```

- [ ] **Step 3: Compilar**

Run: `./gradlew compileDebugKotlin`
Expected: este archivo compila.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt
git commit -m "refactor(transaccion): usar TtsHelper.formatearCosteHumano en TransaccionAdapter"
```

---

## Task 4: Sustituir `formato_historial_horas` en `HistorialAdapter` (conservando signo)

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt:69-75`

- [ ] **Step 1: Reemplazar la asignación de `tvHoras.text`**

Buscar:

```kotlin
            // Horas con signo y color
            tvHoras.text = itemView.context.getString(
                R.string.formato_historial_horas,
                signo,
                item.transaccion.horasTransferidas
            )
```

y reemplazar por:

```kotlin
            // Horas con signo y color
            tvHoras.text = signo + TtsHelper.formatearCosteHumano(item.transaccion.horasTransferidas)
```

(El signo sigue viniendo de la variable `signo` ya calculada en la línea 52: `"+"` si `item.esVendedor`, `"-"` si no.)

- [ ] **Step 2: Añadir import si falta**

```kotlin
import com.example.vecindapp.ui.common.TtsHelper
```

- [ ] **Step 3: Compilar**

Run: `./gradlew compileDebugKotlin`
Expected: este archivo compila.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt
git commit -m "refactor(historial): usar TtsHelper.formatearCosteHumano conservando signo +/-"
```

---

## Task 5: Sustituir `formato_coste_horas` en `DetalleServicioFragment` (tvCoste)

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt:178`

- [ ] **Step 1: Reemplazar la asignación de `tvCoste.text`**

Buscar:

```kotlin
        tvCoste.text = getString(R.string.formato_coste_horas, servicio.costeHoras)
```

y reemplazar por:

```kotlin
        tvCoste.text = TtsHelper.formatearCosteHumano(servicio.costeHoras)
```

(El import `com.example.vecindapp.ui.common.TtsHelper` ya existe en este archivo porque se usa más abajo en `TtsHelper.formatearCosteConUnidad(s.costeHoras)` — verificar y no duplicar.)

- [ ] **Step 2: Compilar**

Run: `./gradlew compileDebugKotlin`
Expected: este archivo compila (los usos restantes de `CrearServicioFragment.formatearHoras` en líneas 265-271 seguirán funcionando porque el método aún existe; se eliminarán en la Task 6).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt
git commit -m "refactor(detalle-servicio): usar TtsHelper.formatearCosteHumano en tvCoste"
```

---

## Task 6: Eliminar `CrearServicioFragment.formatearHoras` y delegar en `TtsHelper`

Esta tarea actualiza los 3 usos en `CrearServicioFragment`, los 3 usos en el diálogo de edición de `Detalleserviciofragment`, y finalmente elimina el `companion` duplicado. Todo en un solo commit porque es un refactor atómico (eliminar el método sin haber migrado a todos los llamadores rompería la compilación).

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/CrearServicioFragment.kt:107-110, 160-172`
- Modify: `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt:263-273`

- [ ] **Step 1: Migrar los 3 usos dentro de `CrearServicioFragment.configurarSlider`**

Buscar:

```kotlin
    private fun configurarSlider() {
        sliderCoste.setLabelFormatter { valor -> formatearHoras(valor) }
        tvLabelCoste.text = getString(R.string.label_coste_slider, formatearHoras(sliderCoste.value))
        sliderCoste.addOnChangeListener { _, valor, _ ->
            tvLabelCoste.text = getString(R.string.label_coste_slider, formatearHoras(valor))
        }
    }
```

y reemplazar por:

```kotlin
    private fun configurarSlider() {
        sliderCoste.setLabelFormatter { valor -> TtsHelper.formatearCosteHumano(valor.toDouble()) }
        tvLabelCoste.text = getString(
            R.string.label_coste_slider,
            TtsHelper.formatearCosteHumano(sliderCoste.value.toDouble())
        )
        sliderCoste.addOnChangeListener { _, valor, _ ->
            tvLabelCoste.text = getString(
                R.string.label_coste_slider,
                TtsHelper.formatearCosteHumano(valor.toDouble())
            )
        }
    }
```

- [ ] **Step 2: Eliminar el `companion object` con `formatearHoras` de `CrearServicioFragment`**

Buscar (aprox. líneas 160-172):

```kotlin
    companion object {
        /**
         * Formatea un valor de horas (Float) en texto legible.
         *
         * Ejemplos: 2.0 → "2h", 1.25 → "1h 15min", 0.5 → "0h 30min".
         */
        fun formatearHoras(valor: Float): String {
            val horas = valor.toInt()
            val minutos = ((valor - horas) * 60).toInt()
            return if (minutos == 0) "${horas}h" else "${horas}h ${minutos}min"
        }
    }
```

y eliminar ese bloque entero (incluida la llave de cierre del companion). Asegurar que sigue existiendo la llave de cierre de la clase `CrearServicioFragment`.

- [ ] **Step 3: Añadir import `TtsHelper` en `CrearServicioFragment` si falta**

Asegurar al inicio del archivo:

```kotlin
import com.example.vecindapp.ui.common.TtsHelper
```

- [ ] **Step 4: Migrar los 3 usos en el diálogo de edición de `Detalleserviciofragment`**

Buscar (líneas 263-273):

```kotlin
        tvLabelCoste.text = getString(
            R.string.label_coste_slider,
            CrearServicioFragment.formatearHoras(sliderCoste.value)
        )
        sliderCoste.setLabelFormatter { CrearServicioFragment.formatearHoras(it) }
        sliderCoste.addOnChangeListener { _, valor, _ ->
            tvLabelCoste.text = getString(
                R.string.label_coste_slider,
                CrearServicioFragment.formatearHoras(valor)
            )
        }
```

y reemplazar por:

```kotlin
        tvLabelCoste.text = getString(
            R.string.label_coste_slider,
            TtsHelper.formatearCosteHumano(sliderCoste.value.toDouble())
        )
        sliderCoste.setLabelFormatter { TtsHelper.formatearCosteHumano(it.toDouble()) }
        sliderCoste.addOnChangeListener { _, valor, _ ->
            tvLabelCoste.text = getString(
                R.string.label_coste_slider,
                TtsHelper.formatearCosteHumano(valor.toDouble())
            )
        }
```

- [ ] **Step 5: Revisar si queda algún import de `CrearServicioFragment` huérfano en `Detalleserviciofragment`**

Si el archivo ya no referencia `CrearServicioFragment` en ningún otro sitio, eliminar el import correspondiente. En caso contrario, dejarlo.

Run: `Grep pattern="CrearServicioFragment" path=".../Detalleserviciofragment.kt"`
Expected: 0 resultados → eliminar el import; cualquier otro número → dejarlo.

- [ ] **Step 6: Compilar todo el módulo**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL. No deben quedar referencias a `CrearServicioFragment.formatearHoras` en ningún archivo.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/servicio/CrearServicioFragment.kt \
        app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt
git commit -m "refactor(servicio): eliminar CrearServicioFragment.formatearHoras duplicado

Delegar el formato del slider en TtsHelper.formatearCosteHumano tanto en
CrearServicioFragment como en el diálogo de edición de DetalleServicio."
```

---

## Task 7: Sustituir `String.format("%.1f")` en `PerfilFragment.tvSaldoHoras`

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt:144`

- [ ] **Step 1: Reemplazar la asignación de `tvSaldoHoras.text`**

Buscar:

```kotlin
        tvSaldoHoras.text = String.format("%.1f", usuario.saldoHoras)
```

y reemplazar por:

```kotlin
        tvSaldoHoras.text = TtsHelper.formatearCosteHumano(usuario.saldoHoras)
```

(El import `TtsHelper` ya existe en este archivo — se usa en `configurarFabTts` línea 181.)

- [ ] **Step 2: Compilar**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/perfil/PerfilFragment.kt
git commit -m "refactor(perfil): usar TtsHelper.formatearCosteHumano en tvSaldoHoras"
```

---

## Task 8: Eliminar recursos de string obsoletos

Llegados aquí, `R.string.formato_coste_horas` y `R.string.formato_historial_horas` no deberían tener ningún uso en Kotlin ni XML. Se eliminan para evitar deuda.

**Files:**
- Modify: `app/src/main/res/values/strings.xml:6, 83`

- [ ] **Step 1: Verificar ausencia total de usos**

Run (con la herramienta Grep):

```
Grep pattern="formato_coste_horas" path="app"
Grep pattern="formato_historial_horas" path="app"
```

Expected: 0 resultados fuera de `strings.xml`. Si hay algún uso superviviente, resolverlo antes de continuar.

- [ ] **Step 2: Eliminar la línea `formato_coste_horas` de `strings.xml`**

Buscar y eliminar la línea:

```xml
    <string name="formato_coste_horas">%.1f h</string>
```

- [ ] **Step 3: Eliminar la línea `formato_historial_horas` de `strings.xml`**

Buscar y eliminar la línea:

```xml
    <string name="formato_historial_horas">%1$s%2$.1f h</string>
```

- [ ] **Step 4: Compilar recursos**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL. Si algún lugar (layout XML, otro recurso) referenciase estos strings, el build fallaría con `resource not found`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "chore(strings): eliminar formato_coste_horas y formato_historial_horas obsoletos"
```

---

## Task 9: Build final y verificación manual

**Files:** ninguno (solo validación).

- [ ] **Step 1: Build completo**

Run: `./gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Instalar en dispositivo/emulador**

Run: `./gradlew installDebug`
Expected: APK instalada sin errores.

- [ ] **Step 3: Checklist de verificación manual en la app**

Probar estas pantallas y confirmar que los textos se ven con el nuevo formato:

- **Escaparate** — cada tarjeta de servicio muestra coste como `"2h"` / `"1h 30min"` / `"30min"` (no `"1,5 h"`).
- **Detalle servicio** — `tvCoste` con el mismo formato.
- **Crear servicio** — label del slider `"Coste en horas: 2h"` etc.; al mover el slider el label se actualiza suavemente.
- **Editar servicio (diálogo en Detalle)** — mismo comportamiento del slider.
- **Transacciones (pestañas)** — cada fila muestra horas con formato humano.
- **Historial** — cada fila muestra `"+2h"` (GANADAS, verde) o `"-1h 30min"` (GASTADAS, rojo), con el signo antepuesto.
- **Perfil** — `tvSaldoHoras` con formato humano.
- **TTS (FAB en Perfil, Escaparate, Detalle)** — pulsar el botón y comprobar que la lectura suena natural:
  - `2 horas` para 2.0
  - `1 hora` para 1.0
  - `1 hora y 30 minutos` para 1.5
  - `30 minutos` para 0.5

- [ ] **Step 4: Repaso de regresiones visibles**

Confirmar que no hay:
- Textos vacíos o `"null"` en ninguna tarjeta.
- Crashes al abrir cualquier pantalla.
- Cortes de texto en las tarjetas (el formato nuevo es ligeramente más largo en el peor caso: `"1h 30min"` vs `"1.5 h"`).

- [ ] **Step 5: (Opcional) Crear commit "docs" con notas si procede**

Si durante la verificación has detectado algún ajuste adicional necesario (p. ej. ancho de una tarjeta), abrirlo como tarea nueva fuera de este plan.

---

## Checklist de auto-revisión (hecho durante la escritura del plan)

- **Cobertura del spec:**
  - ✅ Buscar usos de `formato_coste_horas`, `formato_historial_horas`, `"%.1f"` sobre horas → Task inventario arriba + Tasks 2, 3, 4, 5, 7, 8.
  - ✅ Modificar `TtsHelper` con `formatearCosteHumano` y refactor de `formatearCosteConUnidad` → Task 1.
  - ✅ Sustituir llamadas en Adapters/Fragments → Tasks 2, 3, 4, 5, 7.
  - ✅ Preservar signo +/- en `HistorialAdapter` → Task 4 Step 1.
  - ✅ Eliminar `CrearServicioFragment.formatearHoras` y delegar en `TtsHelper` → Task 6.
  - ✅ No tocar DB, entidades ni DAOs → confirmado; todos los archivos modificados son de `ui/` o `res/`.

- **Placeholders:** ninguno — todos los steps muestran código concreto o comandos exactos.

- **Consistencia de tipos:** `formatearCosteHumano` y `formatearCosteConUnidad` aceptan `Double`. Los llamadores que trabajan con `Float` (sliders) convierten explícitamente con `.toDouble()`. Los Adapters/ViewModels ya trabajan en `Double` (`costeHoras`, `horas`, `saldoHoras`, `horasTransferidas`), así que no hay conversiones adicionales.
