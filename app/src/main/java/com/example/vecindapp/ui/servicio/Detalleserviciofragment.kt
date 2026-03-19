package com.example.vecindapp.ui.servicio

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.data.entities.Servicio
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment que muestra el detalle completo de un servicio.
 *
 * Recibe el ID del servicio como argumento de navegación y carga
 * los datos de forma reactiva desde Room. Permite al propietario
 * editar o eliminar el servicio.
 *
 * ## Flujo de datos
 * - **Ver**: Room → DAO → Repository → ViewModel (Flow) → Fragment
 * - **Editar**: Fragment → ViewModel.actualizarServicio() → Repository → Room → Flow actualiza la vista
 * - **Eliminar**: Fragment → ViewModel.eliminarServicio() → Repository → Room → navega atrás
 *
 * @see DetalleServicioViewModel
 */
class DetalleServicioFragment : Fragment() {

    private val viewModel: DetalleServicioViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        DetalleServicioViewModel.Factory(app.servicioRepository)
    }

    private lateinit var ivPictograma: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvCategoria: TextView
    private lateinit var tvCoste: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var btnEditar: MaterialButton
    private lateinit var btnEliminar: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_servicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistas(view)

        // Obtener el ID del servicio pasado como argumento
        val servicioId = arguments?.getInt("servicioId") ?: run {
            findNavController().popBackStack()
            return
        }

        viewModel.cargarServicio(servicioId)
        observarServicio()
        observarEliminacion()
        observarActualizacion()
    }

    private fun configurarVistas(view: View) {
        ivPictograma = view.findViewById(R.id.ivPictogramaDetalle)
        tvTitulo = view.findViewById(R.id.tvTituloDetalle)
        tvCategoria = view.findViewById(R.id.tvCategoriaDetalle)
        tvCoste = view.findViewById(R.id.tvCosteDetalle)
        tvDescripcion = view.findViewById(R.id.tvDescripcionDetalle)
        tvEstado = view.findViewById(R.id.tvEstadoDetalle)
        tvFecha = view.findViewById(R.id.tvFechaDetalle)
        btnEditar = view.findViewById(R.id.btnEditar)
        btnEliminar = view.findViewById(R.id.btnEliminar)
    }

    /**
     * Observa el servicio cargado y pinta los datos en las vistas.
     */
    private fun observarServicio() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.servicio.collect { servicio ->
                    servicio?.let { pintarDetalle(it) }
                }
            }
        }
    }

    /**
     * Rellena todas las vistas con los datos del servicio.
     */
    private fun pintarDetalle(servicio: Servicio) {
        tvTitulo.text = servicio.titulo
        tvCategoria.text = servicio.categoria.name
        tvCoste.text = getString(R.string.formato_coste_horas, servicio.costeHoras)
        tvDescripcion.text = servicio.descripcion ?: getString(R.string.sin_descripcion)
        tvEstado.text = getString(R.string.formato_estado, servicio.estado.name)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvFecha.text = getString(
            R.string.formato_fecha_publicacion,
            sdf.format(Date(servicio.fechaPublicacion))
        )

        // Configurar botones
        btnEditar.setOnClickListener { mostrarDialogoEditar(servicio) }
        btnEliminar.setOnClickListener { mostrarDialogoConfirmarEliminar() }
    }

    /**
     * Muestra un diálogo con campos editables para modificar el servicio.
     *
     * Solo permite editar título, descripción y coste. La categoría
     * no se puede cambiar una vez publicado.
     */
    private fun mostrarDialogoEditar(servicio: Servicio) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_servicio, null)

        val etTitulo = dialogView.findViewById<EditText>(R.id.etEditarTitulo)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etEditarDescripcion)
        val etCoste = dialogView.findViewById<EditText>(R.id.etEditarCoste)

        // Rellenar con los valores actuales
        etTitulo.setText(servicio.titulo)
        etDescripcion.setText(servicio.descripcion ?: "")
        etCoste.setText(servicio.costeHoras.toString())

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_editar_servicio)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val titulo = etTitulo.text.toString()
                val descripcion = etDescripcion.text.toString()
                val coste = etCoste.text.toString().toDoubleOrNull()

                if (titulo.isBlank()) {
                    Toast.makeText(requireContext(), R.string.error_titulo_vacio, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (coste == null || coste <= 0) {
                    Toast.makeText(requireContext(), R.string.error_coste_invalido, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.actualizarServicio(titulo, descripcion, coste)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar el servicio.
     */
    private fun mostrarDialogoConfirmarEliminar() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_confirmar_eliminar)
            .setMessage(R.string.mensaje_confirmar_eliminar)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                viewModel.eliminarServicio()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    /**
     * Observa si el servicio fue eliminado para navegar de vuelta.
     */
    private fun observarEliminacion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eliminado.collect { eliminado ->
                    if (eliminado) {
                        Toast.makeText(requireContext(), R.string.servicio_eliminado, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    /**
     * Observa si el servicio fue actualizado para mostrar confirmación.
     */
    private fun observarActualizacion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actualizado.collect { actualizado ->
                    if (actualizado) {
                        Toast.makeText(requireContext(), R.string.servicio_actualizado, Toast.LENGTH_SHORT).show()
                        viewModel.limpiarActualizado()
                    }
                }
            }
        }
    }
}