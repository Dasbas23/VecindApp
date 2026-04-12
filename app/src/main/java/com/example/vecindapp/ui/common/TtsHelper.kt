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
         * Usa unidades completamente con singular/plural correcto y conector "y"
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
            return if (horas == 0) {
                if (minutos == 0) "" else "$minutos minutos"
            } else {
                val horasStr = if (horas == 1) "hora" else "horas"
                val minutosStr = if (minutos == 0) "" else " y $minutos minutos"
                "$horas $horasStr$minutosStr"
            }
        }
    }
}
