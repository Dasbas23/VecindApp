package com.example.vecindapp.ui.servicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.domain.model.CategoriaServicio
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Fragment con el formulario para publicar un nuevo servicio.
 *
 * Recoge los datos del usuario (título, descripción, categoría, coste),
 * los valida a través del [CrearServicioViewModel] y los inserta en Room.
 * Al guardarse con éxito, navega automáticamente de vuelta al escaparate,
 * donde la nueva tarjeta aparece sola gracias al Flow reactivo.
 *
 * ## Flujo de guardado
 * ```
 * Botón Guardar → ViewModel.guardarServicio() → Repository.insert() → Room
 *                                                                       ↓
 * Escaparate ← Flow emite lista nueva ← DAO.getActivos() ←────────────┘
 * ```
 *
 * @see CrearServicioViewModel
 */
class CrearServicioFragment : Fragment() {

    /** ViewModel con inyección manual desde [VecindAppApplication]. */
    private val viewModel: CrearServicioViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        CrearServicioViewModel.Factory(app.servicioRepository)
    }

    private lateinit var etTitulo: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var etCoste: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_servicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistas(view)
        configurarSpinner()
        configurarBotonGuardar()
        observarResultado()
    }

    /**
     * Guarda las referencias a las vistas del formulario.
     */
    private fun configurarVistas(view: View) {
        etTitulo = view.findViewById(R.id.etTitulo)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria)
        etCoste = view.findViewById(R.id.etCoste)
        btnGuardar = view.findViewById(R.id.btnGuardar)
    }

    /**
     * Rellena el Spinner con las categorías del enum [CategoriaServicio].
     *
     * Usa un [ArrayAdapter] sencillo que muestra el nombre de cada categoría.
     */
    private fun configurarSpinner() {
        val categorias = CategoriaServicio.values().map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categorias
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
    }

    /**
     * Configura el click del botón "Guardar" para recoger los datos
     * del formulario y pasarlos al ViewModel.
     */
    private fun configurarBotonGuardar() {
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val categoria = CategoriaServicio.values()[spinnerCategoria.selectedItemPosition]
            val coste = etCoste.text.toString()

            viewModel.guardarServicio(titulo, descripcion, categoria, coste)
        }
    }

    /**
     * Observa los StateFlows del ViewModel para reaccionar al resultado.
     *
     * - Si [guardado] es `true` → muestra Toast de éxito y navega atrás.
     * - Si [error] tiene mensaje → muestra Toast con el error.
     */
    private fun observarResultado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.guardado.collect { guardado ->
                        if (guardado == true) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.servicio_guardado),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                }

                launch {
                    viewModel.error.collect { mensaje ->
                        if (mensaje != null) {
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                            viewModel.limpiarError()
                        }
                    }
                }
            }
        }
    }
}