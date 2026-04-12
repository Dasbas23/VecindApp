package com.example.vecindapp.ui.servicio

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.vecindapp.data.SesionUsuario
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.ui.common.TtsHelper
import com.example.vecindapp.ui.common.mostrarSnackbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment que muestra el detalle completo de un servicio.
 *
 * Muestra botones diferentes según quién esté viendo:
 * - **Propietario** (mismo userId): ve "Editar" y "Eliminar".
 * - **Otro vecino**: ve "Solicitar servicio".
 *
 * Al solicitar, se crea una [Transaccion] PENDIENTE y el servicio
 * pasa a RESERVADO (desaparece del escaparate).
 *
 * @see DetalleServicioViewModel
 */
class DetalleServicioFragment : Fragment() {


    private val usuarioActualId: Int by lazy {
        SesionUsuario(requireContext()).obtenerUsuarioId()
    }

    private val viewModel: DetalleServicioViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        DetalleServicioViewModel.Factory(
            app.servicioRepository,
            app.transaccionRepository,
            app.usuarioRepository,
            app.valoracionRepository
        )
    }

    private lateinit var ivPictograma: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvCategoria: TextView
    private lateinit var tvCoste: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var layoutAccionesPropietario: LinearLayout
    private lateinit var btnSolicitar: MaterialButton
    private lateinit var btnEditar: MaterialButton
    private lateinit var btnEliminar: MaterialButton
    private lateinit var btnVerValoracion: MaterialButton
    private lateinit var fabTts: FloatingActionButton
    private lateinit var ttsHelper: TtsHelper
    private var servicioActual: Servicio? = null

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
        ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
        configurarFabTts()

        val servicioId = arguments?.getInt("servicioId") ?: run {
            findNavController().popBackStack()
            return
        }

        viewModel.cargarServicio(servicioId)
        viewModel.buscarValoracion(servicioId)
        observarServicio()
        observarValoracion()
        observarSolicitud()
        observarCancelacion()
        observarEliminacion()
        observarActualizacion()
        observarErrores()
    }

    private fun configurarVistas(view: View) {
        ivPictograma = view.findViewById(R.id.ivPictogramaDetalle)
        tvTitulo = view.findViewById(R.id.tvTituloDetalle)
        tvCategoria = view.findViewById(R.id.tvCategoriaDetalle)
        tvCoste = view.findViewById(R.id.tvCosteDetalle)
        tvDescripcion = view.findViewById(R.id.tvDescripcionDetalle)
        tvEstado = view.findViewById(R.id.tvEstadoDetalle)
        tvFecha = view.findViewById(R.id.tvFechaDetalle)
        layoutAccionesPropietario = view.findViewById(R.id.layoutAccionesPropietario)
        btnSolicitar = view.findViewById(R.id.btnSolicitar)
        btnEditar = view.findViewById(R.id.btnEditar)
        btnEliminar = view.findViewById(R.id.btnEliminar)
        btnVerValoracion = view.findViewById(R.id.btnVerValoracion)
        fabTts = view.findViewById(R.id.fabTts)
    }

    /**
     * Observa el servicio cargado y pinta los datos.
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
     * Observa si hay una valoración para mostrar el botón correspondiente.
     */
    private fun observarValoracion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.valoracion.collect { valoracion ->
                    if (valoracion != null) {
                        btnVerValoracion.visibility = View.VISIBLE
                        btnVerValoracion.setOnClickListener {
                            // En el detalle del servicio, si yo soy el que valoró (valorador),
                            // la valoración que veo es la ENVIADA (esEnviada = true).
                            val esEnviada = valoracion.idValoradorFk == usuarioActualId

                            val bottomSheet =
                                com.example.vecindapp.ui.valoracion.DetalleValoracionBottomSheet.newInstance(
                                    pictogramasJson = valoracion.pictogramasJson,
                                    comentario = valoracion.comentario,
                                    timestamp = valoracion.timestamp,
                                    servicioId = servicioActual?.idServicio
                                        ?: -1, // No mostrar botón "Ver servicio" desde aquí
                                    esEnviada = esEnviada // False o True
                                )
                            bottomSheet.show(childFragmentManager, "DetalleValoracion")
                        }
                    } else {
                        btnVerValoracion.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Rellena las vistas y muestra/oculta botones según el contexto.
     */
    private fun pintarDetalle(servicio: Servicio) {
        servicioActual = servicio
        tvTitulo.text = servicio.titulo
        tvCategoria.text = servicio.categoria.name
        tvCoste.text = TtsHelper.formatearCosteHumano(servicio.costeHoras)
        tvDescripcion.text = servicio.descripcion ?: getString(R.string.sin_descripcion)
        tvEstado.text = getString(R.string.formato_estado, servicio.estado.name)

        //Simple data format
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvFecha.text = getString(
            R.string.formato_fecha_publicacion,
            sdf.format(Date(servicio.fechaPublicacion))
        )

        // Mostrar botones según quién mira y el estado del servicio
        val esPropietario = servicio.idUsuarioFk == usuarioActualId

        if (esPropietario) {
            if (servicio.estado == EstadoServicio.ACTIVO) {
                layoutAccionesPropietario.visibility = View.VISIBLE
                btnSolicitar.visibility = View.GONE
            } else if (servicio.estado == EstadoServicio.RESERVADO) {
                layoutAccionesPropietario.visibility = View.GONE
                btnSolicitar.visibility = View.VISIBLE
                btnSolicitar.text = getString(R.string.btn_cancelar_solicitud)
                btnSolicitar.isEnabled = true
            } else {
                layoutAccionesPropietario.visibility = View.GONE
                btnSolicitar.visibility = View.GONE
            }
        } else {
            layoutAccionesPropietario.visibility = View.GONE
            // Si no es propietario, solo ve Solicitar si está ACTIVO
            if (servicio.estado == EstadoServicio.ACTIVO) {
                btnSolicitar.visibility = View.VISIBLE
                btnSolicitar.text = getString(R.string.btn_solicitar)
                btnSolicitar.isEnabled = true
            } else {
                btnSolicitar.visibility = View.GONE
            }
        }

        // Configurar clicks
        btnSolicitar.setOnClickListener {
            if (esPropietario && servicio.estado == EstadoServicio.RESERVADO) {
                mostrarDialogoConfirmarCancelarSolicitud()
            } else {
                mostrarDialogoConfirmarSolicitud(servicio)
            }
        }
        btnEditar.setOnClickListener { mostrarDialogoEditar(servicio) }
        btnEliminar.setOnClickListener { mostrarDialogoConfirmarEliminar() }
    }

    /**
     * Muestra un diálogo de confirmación antes de solicitar el servicio.
     * Informa al usuario del coste en horas que se le debitará.
     */
    private fun mostrarDialogoConfirmarSolicitud(servicio: Servicio) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_confirmar_solicitud)
            .setMessage(
                getString(R.string.mensaje_confirmar_solicitud, servicio.costeHoras)
            )
            .setPositiveButton(R.string.btn_solicitar) { _, _ ->
                viewModel.solicitarServicio(usuarioActualId)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    /**
     * Muestra un diálogo con campos editables para modificar el servicio.
     */
    private fun mostrarDialogoEditar(servicio: Servicio) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_servicio, null)

        val etTitulo = dialogView.findViewById<EditText>(R.id.etEditarTitulo)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etEditarDescripcion)
        val sliderCoste = dialogView.findViewById<com.google.android.material.slider.Slider>(
            R.id.sliderEditarCoste
        )
        val tvLabelCoste = dialogView.findViewById<TextView>(R.id.tvEditarLabelCoste)

        etTitulo.setText(servicio.titulo)
        etDescripcion.setText(servicio.descripcion ?: "")
        sliderCoste.value = servicio.costeHoras.toFloat()
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

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_editar_servicio)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val titulo = etTitulo.text.toString()
                val descripcion = etDescripcion.text.toString()
                val coste = sliderCoste.value.toDouble()

                if (titulo.isBlank()) {
                    // Toast intencional: validación inline del AlertDialog (Snackbar no es viable aquí)
                    Toast.makeText(
                        requireContext(),
                        R.string.error_titulo_vacio,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                viewModel.actualizarServicio(titulo, descripcion, coste)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar.
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
     * Muestra un diálogo de confirmación antes de cancelar la solicitud.
     */
    private fun mostrarDialogoConfirmarCancelarSolicitud() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_confirmar_cancelar_solicitud)
            .setMessage(R.string.mensaje_confirmar_cancelar_solicitud)
            .setPositiveButton(R.string.btn_confirmar_cancelar) { _, _ ->
                viewModel.cancelarSolicitud()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    /**
     * Observa si la solicitud fue exitosa para navegar de vuelta.
     */
    private fun observarSolicitud() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.solicitado.collect { solicitado ->
                    if (solicitado) {
                        mostrarSnackbar(R.string.servicio_solicitado)
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    /**
     * Observa si la cancelación fue exitosa.
     */
    private fun observarCancelacion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cancelado.collect { cancelado ->
                    if (cancelado) {
                        mostrarSnackbar(R.string.solicitud_cancelada)
                        // No navegamos atrás, el servicio vuelve a estar ACTIVO y visible
                    }
                }
            }
        }
    }

    /**
     * Observa si el servicio fue eliminado para navegar de vuelta.
     */
    private fun observarEliminacion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eliminado.collect { eliminado ->
                    if (eliminado) {
                        mostrarSnackbar(R.string.servicio_eliminado)
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
                        mostrarSnackbar(R.string.servicio_actualizado)
                        viewModel.limpiarActualizado()
                    }
                }
            }
        }
    }

    /**
     * Observa errores del ViewModel para mostrarlos al usuario.
     */
    private fun observarErrores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { mensaje ->
                    if (mensaje != null) {
                        mostrarSnackbar(mensaje, Snackbar.LENGTH_LONG)
                        viewModel.limpiarError()
                    }
                }
            }
        }
    }

    /**
     * Configura el FAB de TTS para leer en voz alta el detalle completo
     * del servicio actualmente cargado.
     *
     * Lee: título → categoría → coste → descripción → estado.
     * No hace nada si el servicio aún no se ha cargado.
     * Los puntos en [texto] son necesarios para las pausas
     */
    private fun configurarFabTts() {
        fabTts.setOnClickListener {
            val s = servicioActual ?: return@setOnClickListener
            val desc = s.descripcion ?: getString(R.string.sin_descripcion)
            val costeTexto = TtsHelper.formatearCosteConUnidad(s.costeHoras)
            val texto =
                "${s.titulo}. Categoría: ${s.categoria.name}. Coste: $costeTexto . Descripción: $desc. Estado: ${s.estado.name}"
            ttsHelper.speak(texto)
        }
    }
}