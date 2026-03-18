package com.example.vecindapp.ui.escaparate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import kotlinx.coroutines.launch

/**
 * Fragment principal del escaparate de servicios.
 *
 * Muestra un [RecyclerView] con las tarjetas de servicios activos,
 * alimentado reactivamente por el [EscaparateViewModel]. Cada vez
 * que un servicio se publica, edita o elimina en la BBDD, la lista
 * se actualiza automáticamente sin intervención del usuario.
 *
 * ## Flujo de datos (MVVM)
 * ```
 * Room (DB) → DAO → Repository → ViewModel (StateFlow) → Fragment → Adapter → RecyclerView
 * ```
 *
 * ## Inyección de dependencias (manual)
 * Se construye la cadena DAO → Repository → ViewModel.Factory
 * directamente en el Fragment, sin Hilt/Dagger.
 *
 * @see EscaparateViewModel
 * @see ServicioAdapter
 */
class EscaparateFragment : Fragment() {

    /** ViewModel con inyección manual mediante Factory. */
    private val viewModel: EscaparateViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        EscaparateViewModel.Factory(app.servicioRepository)
    }

    /** Adapter del RecyclerView. */
    private lateinit var adapter: ServicioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_escaparate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView(view)
        observarServicios()
    }

    /**
     * Configura el RecyclerView con su LayoutManager y Adapter.
     *
     * Al pulsar una tarjeta se navegará al detalle del servicio.
     * Por ahora el callback está preparado pero sin navegación real
     * hasta que configuremos el Navigation Component.
     */
    private fun configurarRecyclerView(view: View) {
        adapter = ServicioAdapter { servicio ->
            // TODO: Navegar al detalle del servicio con Navigation Component
            // findNavController().navigate(
            //     EscaparateFragmentDirections.actionEscaparateToDetalle(servicio.idServicio)
            // )
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvEscaparate)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    /**
     * Se suscribe al [StateFlow] de servicios del ViewModel.
     *
     * Usa [repeatOnLifecycle] con [Lifecycle.State.STARTED] para que
     * la suscripción se pause cuando el Fragment no es visible y se
     * reanude al volver, evitando actualizaciones innecesarias.
     */
    private fun observarServicios() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.servicios.collect { lista ->
                    adapter.submitList(lista)
                }
            }
        }
    }
}