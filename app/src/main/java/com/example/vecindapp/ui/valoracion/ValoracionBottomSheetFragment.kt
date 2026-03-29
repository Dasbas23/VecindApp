package com.example.vecindapp.ui.valoracion

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * BottomSheet obligatorio para valorar al vecino tras completar una transacción.
 *
 * Se abre automáticamente al completar una transacción y NO se puede cerrar
 * sin enviar la valoración (`isCancelable = false`). Esto garantiza que
 * todas las transacciones completadas tengan su valoración asociada.
 *
 * ## Flujo
 * 1. Se muestra con 3 secciones de pictogramas (Bien, Regular, Mal).
 * 2. El usuario selecciona uno o más pictogramas (se marcan visualmente).
 * 3. Opcionalmente escribe un comentario.
 * 4. Pulsa "Enviar" → se guarda en Room → se cierra el BottomSheet.
 *
 * ## Argumentos requeridos
 * - `transaccionId`: ID de la transacción completada.
 * - `valoradorId`: ID del usuario que valora (el que pulsa "Completar").
 * - `valoradoId`: ID del usuario valorado (la otra parte).
 *
 * @see ValoracionViewModel
 */
class ValoracionBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: ValoracionViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        ValoracionViewModel.Factory(app.valoracionRepository)
    }

    /** Lista mutable de pictogramas seleccionados (por su tag). */
    private val pictogramasSeleccionados = mutableListOf<String>()

    /** Color de fondo para pictogramas seleccionados. */
    private val colorSeleccionado = Color.parseColor("#DBEAFE") // Azul claro
    private val colorNormal = Color.TRANSPARENT

    private var transaccionId: Int = 0
    private var valoradorId: Int = 0
    private var valoradoId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No se puede cerrar sin valorar
        isCancelable = false

        // Leer argumentos
        arguments?.let {
            transaccionId = it.getInt(ARG_TRANSACCION_ID)
            valoradorId = it.getInt(ARG_VALORADOR_ID)
            valoradoId = it.getInt(ARG_VALORADO_ID)
        }
    }

    /** Callback que se ejecuta al cerrar el BottomSheet. */
    var onDismissCallback: (() -> Unit)? = null

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_valoracion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarPictogramas(view)
        configurarBotonEnviar(view)
        observarResultado()
        observarErrores()
    }

    /**
     * Registra el click en cada pictograma para toglear su selección.
     *
     * Al pulsar un pictograma:
     * - Si no estaba seleccionado → se marca con fondo azul y se añade a la lista.
     * - Si ya estaba seleccionado → se desmarca y se quita de la lista.
     */
    private fun configurarPictogramas(view: View) {
        val todosLosPictogramas = listOf(
            // Bien
            view.findViewById<ImageView>(R.id.ivBien1),
            view.findViewById<ImageView>(R.id.ivBien2),
            view.findViewById<ImageView>(R.id.ivBien3),
            // Regular
            view.findViewById<ImageView>(R.id.ivRegular1),
            view.findViewById<ImageView>(R.id.ivRegular2),
            view.findViewById<ImageView>(R.id.ivRegular3),
            // Mal
            view.findViewById<ImageView>(R.id.ivMal1),
            view.findViewById<ImageView>(R.id.ivMal2),
            view.findViewById<ImageView>(R.id.ivMal3)
        )

        for (iv in todosLosPictogramas) {
            iv.setOnClickListener {
                val tag = iv.tag as? String ?: return@setOnClickListener
                togglePictograma(iv, tag)
            }
        }
    }

    /**
     * Alterna la selección de un pictograma.
     */
    private fun togglePictograma(imageView: ImageView, tag: String) {
        if (pictogramasSeleccionados.contains(tag)) {
            // Deseleccionar
            pictogramasSeleccionados.remove(tag)
            imageView.setBackgroundColor(colorNormal)
        } else {
            // Seleccionar
            pictogramasSeleccionados.add(tag)
            imageView.setBackgroundColor(colorSeleccionado)
        }
    }

    /**
     * Configura el botón "Enviar Valoración" para recoger los datos
     * y pasarlos al ViewModel.
     */
    private fun configurarBotonEnviar(view: View) {
        val etComentario = view.findViewById<TextInputEditText>(R.id.etComentario)
        val btnEnviar = view.findViewById<MaterialButton>(R.id.btnEnviarValoracion)

        btnEnviar.setOnClickListener {
            viewModel.guardarValoracion(
                idTransaccion = transaccionId,
                idValoradorFk = valoradorId,
                idValoradoFk = valoradoId,
                pictogramas = pictogramasSeleccionados.toList(),
                comentario = etComentario.text.toString()
            )
        }
    }

    /**
     * Si la valoración se guardó con éxito, muestra confirmación y cierra.
     */
    private fun observarResultado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.guardada.collect { guardada ->
                    if (guardada) {
                        Toast.makeText(
                            requireContext(),
                            R.string.valoracion_enviada,
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                }
            }
        }
    }

    private fun observarErrores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { mensaje ->
                    if (mensaje != null) {
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        viewModel.limpiarError()
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_TRANSACCION_ID = "transaccionId"
        private const val ARG_VALORADOR_ID = "valoradorId"
        private const val ARG_VALORADO_ID = "valoradoId"

        /**
         * Crea una nueva instancia del BottomSheet con los argumentos necesarios.
         *
         * @param transaccionId ID de la transacción completada.
         * @param valoradorId   ID del usuario que valora.
         * @param valoradoId    ID del usuario valorado.
         */
        fun newInstance(
            transaccionId: Int,
            valoradorId: Int,
            valoradoId: Int
        ): ValoracionBottomSheetFragment {
            return ValoracionBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TRANSACCION_ID, transaccionId)
                    putInt(ARG_VALORADOR_ID, valoradorId)
                    putInt(ARG_VALORADO_ID, valoradoId)
                }
            }
        }
    }
}