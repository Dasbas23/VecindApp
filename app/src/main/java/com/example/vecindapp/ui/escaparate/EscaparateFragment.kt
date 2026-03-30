package com.example.vecindapp.ui.escaparate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.ui.common.TtsHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

/**
 * Fragment principal del escaparate de servicios.
 *
 * Muestra un [RecyclerView] con las tarjetas de servicios activos,
 * alimentado reactivamente por el [EscaparateViewModel]. Incluye un
 * [FloatingActionButton] para navegar al formulario de creación de
 * servicio y gestión automática del estado vacío.
 *
 * ## Flujo de datos (MVVM)
 * ```
 * Room (DB) → DAO → Repository → ViewModel (StateFlow) → Fragment → Adapter → RecyclerView
 * ```
 *
 * @see EscaparateViewModel
 * @see ServicioAdapter
 */
class EscaparateFragment : Fragment() {

    /** ViewModel con inyección manual desde [VecindAppApplication]. */
    private val viewModel: EscaparateViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        EscaparateViewModel.Factory(app.servicioRepository)
    }

    private lateinit var adapter: ServicioAdapter
    private lateinit var rvEscaparate: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var fabTts: FloatingActionButton
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_escaparate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ttsHelper = TtsHelper(requireContext(), viewLifecycleOwner.lifecycle)
        configurarVistas(view)
        configurarRecyclerView()
        configurarFab(view)
        configurarFabTts()
        observarServicios()
    }

    /**
     * Guarda las referencias a las vistas que necesitamos manipular.
     */
    private fun configurarVistas(view: View) {
        rvEscaparate = view.findViewById(R.id.rvEscaparate)
        tvVacio = view.findViewById(R.id.tvVacio)
        fabTts = view.findViewById(R.id.fabTts)
    }

    /**
     * Configura el RecyclerView con su LayoutManager y Adapter.
     *
     * Al pulsar una tarjeta se navegará al detalle del servicio.
     */
    private fun configurarRecyclerView() {
        adapter = ServicioAdapter(
            onServicioClick = { servicio ->
                val bundle = Bundle().apply {
                    putInt("servicioId", servicio.idServicio)
                }
                findNavController().navigate(R.id.action_escaparate_to_detalle, bundle)
            },
            onServicioLongClick = { servicio ->
                ttsHelper.speak(
                    "${servicio.titulo}, ${TtsHelper.formatearCoste(servicio.costeHoras)} horas"
                )
                true
            }
        )
        // Fuera del lambda, donde siempre debería haber estado
        rvEscaparate.layoutManager = LinearLayoutManager(requireContext())
        rvEscaparate.adapter = adapter
    }

    /**
     * Configura el FloatingActionButton para navegar al formulario
     * de creación de servicio.
     */
    private fun configurarFab(view: View) {
        val fab = view.findViewById<FloatingActionButton>(R.id.fabCrearServicio)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_escaparate_to_crearServicio)
        }
    }

    /**
     * Observa el [StateFlow] de servicios del ViewModel.
     *
     * Usa [repeatOnLifecycle] con [Lifecycle.State.STARTED] para pausar
     * la suscripción cuando el Fragment no es visible. También gestiona
     * la visibilidad del mensaje "No hay servicios" automáticamente.
     */
    private fun observarServicios() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.servicios.collect { lista ->
                    adapter.submitList(lista.toMutableList())

                    // Solo toglear el mensaje de vacío, el RV siempre visible
                    tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    /**
     * Configura el FAB de TTS para leer en voz alta los servicios
     * actualmente visibles en el RecyclerView.
     *
     * Solo lee los ítems dentro del viewport (posiciones firstVisible..lastVisible).
     * Cada servicio se anuncia como "título, X horas". Los ítems se separan
     * con ". " para que el motor TTS interprete una pausa natural entre ellos.
     */
    private fun configurarFabTts() {
        fabTts.setOnClickListener {
            val lm = rvEscaparate.layoutManager as LinearLayoutManager
            val first = lm.findFirstVisibleItemPosition()
            val last = lm.findLastVisibleItemPosition()
            if (first == RecyclerView.NO_POSITION) return@setOnClickListener
            val texto = (first..last)
                .mapNotNull { adapter.currentList.getOrNull(it) }
                .joinToString(". ") { s ->
                    "${s.titulo}, ${TtsHelper.formatearCoste(s.costeHoras)} horas"
                }
            if (texto.isNotBlank()) ttsHelper.speak(texto)
        }
    }
}