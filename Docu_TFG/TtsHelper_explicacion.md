# TtsHelper.kt — Explicación para juniors

> Ubicación: `app/src/main/java/com/example/vecindapp/ui/common/TtsHelper.kt`

Esta clase es un **envoltorio (wrapper) del motor Text-To-Speech (TTS) de Android** que hace dos cosas importantes:

1. **Lee texto en voz alta** en español.
2. **Se limpia a sí misma** automáticamente siguiendo el ciclo de vida del Fragment, para que tú no tengas que acordarte de llamar a `stop()` ni `shutdown()`.

Es una pieza clave de la accesibilidad de VecindApp: cualquier pantalla que necesite "leer" textos al usuario instancia un `TtsHelper` y listo.

---

## 1. ¿Qué problema resuelve?

Usar la API `android.speech.tts.TextToSpeech` directamente desde un Fragment tiene tres inconvenientes:

| Problema | Qué pasa si lo ignoras |
|---|---|
| Inicialización **asíncrona** (~200 ms) | Si llamas a `speak()` demasiado pronto, no se oye nada y el error es silencioso. |
| Hay que llamar a `shutdown()` | Si no lo haces, el motor TTS queda vivo → **fuga de memoria**. |
| Hay que parar la voz al cambiar de pantalla | Si no, el usuario oye a la app hablar en otra Fragment. |

`TtsHelper` esconde los tres y expone una API mínima: `speak(texto)` y `stop()`.

---

## 2. Anatomía de la clase

```kotlin
class TtsHelper(
    context: Context,
    lifecycle: Lifecycle
) : DefaultLifecycleObserver
```

- **Hereda de `DefaultLifecycleObserver`**: esto le permite enterarse de eventos como `onStop` u `onDestroy` del Fragment.
- **Recibe un `Lifecycle`** en el constructor (normalmente `viewLifecycleOwner.lifecycle`) y se auto-registra como observador.

### Propiedades privadas

```kotlin
private var tts: TextToSpeech? = null   // el motor real
private var pendingText: String? = null // texto guardado si llegó antes de estar listo
private var isReady = false              // bandera de "ya puedo hablar"
```

El patrón `pendingText` + `isReady` es el truco para soportar la inicialización asíncrona sin obligar al Fragment a esperar.

---

## 3. El bloque `init` — la parte más interesante

```kotlin
init {
    tts = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("es-ES"))
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
```

Paso a paso:

1. **`context.applicationContext`**: usamos el Context de la aplicación, no el del Fragment. Si guardáramos el Context del Fragment dentro del motor TTS, este mantendría viva la Activity entera → fuga de memoria.
2. **Lambda `OnInitListener`**: se dispara cuando el motor termina de cargar (puede tardar 100–300 ms). Android llama a esta lambda desde un hilo interno.
3. **`setLanguage(Locale.forLanguageTag("es-ES"))`**: fijamos español de España. El método devuelve un código; comprobamos que **no** sea `LANG_MISSING_DATA` ni `LANG_NOT_SUPPORTED`.
4. **Si hay texto pendiente**, lo reproducimos: este es el caso en el que el usuario pulsó el botón antes de que el motor estuviera listo.
5. **`lifecycle.addObserver(this)`**: el helper se suscribe al ciclo de vida. A partir de ahora recibirá `onStop` y `onDestroy` automáticamente.

> **Idea importante para un junior:** el motor TTS **no está listo nada más construirlo**. Cualquier API asíncrona en Android sigue este patrón — guardar estado, actuar cuando llegue el callback.

---

## 4. `speak(text)` — reproducir texto

```kotlin
fun speak(text: String) {
    if (isReady) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    } else {
        pendingText = text
    }
}
```

Dos caminos:

- **El motor está listo** → llama a `TextToSpeech.speak()` con `QUEUE_FLUSH`. Esta constante significa *"si ya estabas leyendo algo, cállate y lee esto nuevo"*. La alternativa sería `QUEUE_ADD`, que encola.
- **Aún no está listo** → guarda el texto en `pendingText`. El `init` lo reproducirá cuando termine de cargar.

Ojo: solo se guarda **un** texto pendiente. Si el usuario pulsa el botón dos veces rapidísimo antes de que el TTS arranque, solo se oirá el último. Para este caso de uso es lo correcto.

---

## 5. `stop()` — parar sin destruir

```kotlin
fun stop() {
    tts?.stop()
}
```

Detiene la locución actual pero **mantiene el motor vivo**, así la siguiente llamada a `speak()` es inmediata. Útil para un botón de "parar" en la UI.

No confundir con `shutdown()`, que libera el motor entero.

---

## 6. La integración con el Lifecycle

```kotlin
override fun onStop(owner: LifecycleOwner) {
    tts?.stop()
}

override fun onDestroy(owner: LifecycleOwner) {
    tts?.shutdown()
    tts = null
    isReady = false
}
```

Android llama a estos métodos automáticamente porque el helper es un `DefaultLifecycleObserver` registrado con `lifecycle.addObserver(this)`.

- **`onStop`** (el Fragment pasa a segundo plano o navegas a otra pantalla): paramos la voz. Sin esto, oirías a la app hablando mientras ves otra Fragment.
- **`onDestroy`** (la vista del Fragment se destruye): **liberamos el motor TTS**. Esto es imprescindible: el motor TTS consume recursos del sistema y otras apps también lo usan.

Poner `tts = null` e `isReady = false` deja el objeto en estado seguro por si alguien llama a `speak()` después del destroy (no pasaría nada, pero es buena higiene).

> **Patrón reutilizable:** esta técnica — un helper que se auto-registra como `LifecycleObserver` — es la forma moderna y recomendada de manejar recursos con ciclo de vida en Android. Úsala siempre que tengas algo que inicializar y liberar.

---

## 7. El `companion object` — utilidades de formato

```kotlin
companion object {
    private fun formatearCoste(coste: Double): String { ... }
    fun formatearCosteConUnidad(coste: Double): String { ... }
}
```

Estos métodos **no tocan TTS**, son helpers de texto pensados para que lo que se *lea* suene natural en español:

### `formatearCoste(2.0)` → `"2"`, `formatearCoste(1.5)` → `"1,5"`

```kotlin
if (coste % 1.0 == 0.0) {
    coste.toLong().toString()
} else {
    String.format(Locale.forLanguageTag("es-ES"), "%.1f", coste)
}
```

- Si el número es entero (`2.0`), lo convertimos a `Long` para que no se lea "dos punto cero".
- Si tiene decimal, usamos el `Locale` español para que el separador sea **coma**, no punto. El motor TTS español leería `1.5` como "uno punto cinco"; con `1,5` lee "uno coma cinco", que es lo que un hispanohablante espera.

### `formatearCosteConUnidad(1.0)` → `"1 hora"`, `(2.5)` → `"2,5 horas"`

```kotlin
val texto = formatearCoste(coste)
val unidad = if (texto == "1") "hora" else "horas"
return "$texto $unidad"
```

Gestiona el **plural**: solo "1" se lee en singular; cualquier otro valor (incluido "1,5") va en plural.

> Estos métodos están en el `companion object` porque no necesitan estado del motor TTS. Son puras funciones de formato y puedes llamarlas desde cualquier parte con `TtsHelper.formatearCosteConUnidad(2.5)`.

---

## 8. Cómo se usa desde un Fragment

```kotlin
class MiFragment : Fragment() {

    private lateinit var ttsHelper: TtsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Crear el helper ligado al ciclo de vida de la VISTA del Fragment
        ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)

        // 2. Usarlo donde haga falta
        binding.fabLeer.setOnClickListener {
            val coste = TtsHelper.formatearCosteConUnidad(servicio.costeHoras)
            ttsHelper.speak("Este servicio cuesta $coste")
        }
    }

    // 3. NO hace falta override onDestroyView para hacer shutdown:
    //    el propio TtsHelper se encarga vía Lifecycle.
}
```

Puntos importantes:

- Usa **`viewLifecycleOwner.lifecycle`**, NO `this.lifecycle`. En un Fragment, el ciclo de vida de la *vista* es distinto al del Fragment en sí (por ejemplo, tras un `replace` con back stack). Usar el de la vista evita bugs raros al volver atrás.
- **No llames a `shutdown` manualmente**: lo hace el helper.

---

## 9. Resumen mental

| API pública | Qué hace |
|---|---|
| `TtsHelper(context, lifecycle)` | Crea el motor y se auto-registra en el ciclo de vida. |
| `speak(text)` | Lee en voz alta. Funciona aunque el motor aún no esté listo. |
| `stop()` | Para la voz actual (no destruye el motor). |
| `TtsHelper.formatearCosteConUnidad(coste)` | Formatea horas para que suenen naturales en español. |

Y lo que **no** tienes que hacer:
- No llamas a `shutdown()`.
- No compruebas si el motor está listo.
- No paras la voz al cambiar de Fragment.

Todo eso lo resuelve el helper gracias al `LifecycleObserver` y a la pareja `isReady` / `pendingText`.

---

## 10. Posibles mejoras (para pensar, no obligatorias)

- Cola de múltiples `pendingText` si alguna vez se necesitan varias frases antes de la inicialización.
- Parámetro de `Locale` configurable en el constructor, por si en el futuro se soportan más idiomas.
- Exponer un `LiveData<Boolean>` con el estado `isReady` para que la UI pueda deshabilitar el botón hasta que el motor esté listo.

Ninguna es urgente: la clase cumple su función y es simple, que es justo lo que un helper debe ser.
