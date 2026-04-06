package com.example.vecindapp.ui.valoracion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.vecindapp.R
import com.example.vecindapp.ui.common.TtsHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BottomSheet de solo lectura para visualizar una valoración existente.
 *
 * Se abre al pulsar una transacción completada en el historial.
 * Muestra los pictogramas seleccionados (con su texto descriptivo),
 * el comentario si lo hay, y la fecha de la valoración.
 *
 * ## Argumentos
 * - `pictogramasJson`: JSON con los IDs de pictogramas.
 * - `comentario`: Texto del comentario (puede ser vacío).
 * - `timestamp`: Fecha de la valoración en millis.
 */
class DetalleValoracionBottomSheet : BottomSheetDialogFragment() {

    private var pictogramasJson: String = "[]"
    private var comentario: String = ""
    private var timestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pictogramasJson = it.getString(ARG_PICTOGRAMAS, "[]")
            comentario = it.getString(ARG_COMENTARIO, "")
            timestamp = it.getLong(ARG_TIMESTAMP, 0L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_detalle_valoracion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val llPictogramas = view.findViewById<LinearLayout>(R.id.llPictogramasDetalle)
        val tvComentario = view.findViewById<TextView>(R.id.tvComentarioDetalle)
        val tvLabelComentario = view.findViewById<TextView>(R.id.tvLabelComentarioDetalle)
        val tvFecha = view.findViewById<TextView>(R.id.tvFechaValoracion)
        val btnCerrar = view.findViewById<MaterialButton>(R.id.btnCerrarDetalle)

        // Parsear y mostrar pictogramas
        val pictogramas = parsearPictogramas(pictogramasJson)
        for (tag in pictogramas) {
            val item = crearPictogramaView(tag)
            llPictogramas.addView(item)
        }

        // Comentario
        if (comentario.isBlank()) {
            tvLabelComentario.visibility = View.GONE
            tvComentario.visibility = View.GONE
        } else {
            tvComentario.text = comentario
        }

        // Fecha
        if (timestamp > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = getString(R.string.detalle_valoracion_fecha, sdf.format(Date(timestamp)))
        }

        btnCerrar.setOnClickListener { dismiss() }

        // TTS
        val ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)

        val ibTts = view.findViewById<ImageButton>(R.id.ibTtsDetalle)
        ibTts.setOnClickListener {
            val pictogramas = parsearPictogramas(pictogramasJson)
            val textosPictogramas = pictogramas.map { tag ->
                PictogramaMapper.obtenerDescripcion(requireContext(), tag)
            }

            val textoCompleto = buildString {
                append("Valoración. ")
                append("Pictogramas: ${textosPictogramas.joinToString(", ")}. ")
                if (comentario.isNotBlank()) {
                    append("Comentario: $comentario")
                }
            }
            ttsHelper.speak(textoCompleto)
        }
    }

    /**
     * Parsea el JSON de pictogramas a una lista de tags.
     * Formato esperado: ["bien_excelente","regular_normal"]
     */
    private fun parsearPictogramas(json: String): List<String> {
        return try {
            json.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Crea un LinearLayout vertical con el pictograma y su texto descriptivo.
     *
     * Por ahora usa iconos placeholder. Cuando se integren los PNG de ARASAAC,
     * se cargará desde assets con Glide.
     */
    private fun crearPictogramaView(tag: String): LinearLayout {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams = params
        }

        // Pictograma
        val iv = ImageView(requireContext()).apply {
            val size = (48 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            setPadding(4, 4, 4, 4)

            // Color según categoría
            setImageResource(PictogramaMapper.obtenerDrawable(tag))
        }

        // Texto descriptivo
        val tv = TextView(requireContext()).apply {
            text = PictogramaMapper.obtenerDescripcion(requireContext(), tag)
            textSize = 10f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 4, 0, 0)
        }

        container.addView(iv)
        container.addView(tv)
        return container
    }

    companion object {
        private const val ARG_PICTOGRAMAS = "pictogramasJson"
        private const val ARG_COMENTARIO = "comentario"
        private const val ARG_TIMESTAMP = "timestamp"

        /**
         * Crea una instancia para visualizar una valoración.
         */
        fun newInstance(
            pictogramasJson: String,
            comentario: String?,
            timestamp: Long
        ): DetalleValoracionBottomSheet {
            return DetalleValoracionBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PICTOGRAMAS, pictogramasJson)
                    putString(ARG_COMENTARIO, comentario ?: "")
                    putLong(ARG_TIMESTAMP, timestamp)
                }
            }
        }
    }
}