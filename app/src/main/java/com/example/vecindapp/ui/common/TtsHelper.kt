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
 *     super.onViewCreated(view, savedInstanceState)
 *     ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
 *     fabTts.setOnClickListener { ttsHelper.speak("Texto a leer") }
 * }
 * ```
 *
 * ## Inicialización asíncrona
 * El motor TTS tarda ~200 ms en inicializarse. Si [speak] se llama antes de que
 * esté listo, el texto se guarda en [pendingText] y se reproduce en cuanto
 * [TextToSpeech.OnInitListener.onInit] confirma éxito.
 *
 * @param context   Contexto para inicializar el motor. Se usa [Context.applicationContext]
 *                  para evitar fugas de memoria.
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
        private fun formatearCoste(coste: Double): String {
            return if (coste % 1.0 == 0.0) {
                coste.toLong().toString()
            } else {
                String.format(Locale.forLanguageTag("es-ES"), "%.1f", coste)
            }
        }

        /**
         * Comprueba si tiene que leer "una hora" en singular o plural.
         *
         * @param coste Valor en horas (Double).
         */
        fun formatearCosteConUnidad(coste: Double): String {
            val texto = formatearCoste(coste)
            val unidad = if (texto == "1") "hora" else "horas"
            return "$texto $unidad"

        }
    }
}
