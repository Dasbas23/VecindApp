package com.example.vecindapp.ui.historial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.data.SesionUsuario
import com.example.vecindapp.ui.valoracion.DetalleValoracionBottomSheet
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

/**
 * Fragment de historial de transacciones.
 *
 * Muestra un gráfico de barras agrupadas (MPAndroidChart) con las horas
 * ganadas y gastadas por mes, y debajo un [TabLayout] con dos pestañas
 * para alternar entre transacciones completadas y canceladas en un
 * único [RecyclerView].
 *
 * @see HistorialViewModel
 * @see HistorialAdapter
 */
class HistorialFragment : Fragment() {

    private val viewModel: HistorialViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        val sesion = SesionUsuario(requireContext())
        HistorialViewModel.Factory(
            app.transaccionRepository,
            app.servicioRepository,
            app.valoracionRepository,
            sesion.obtenerUsuarioId()
        )
    }

    private lateinit var barChart: BarChart
    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: HistorialAdapter

    private var listaCompletadas: List<HistorialItem> = emptyList()
    private var listaCanceladas: List<HistorialItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistas(view)
        configurarGrafico()
        configurarRecyclerView()
        configurarTabLayout()
        observarDatosGrafico()
        observarCompletadas()
        observarCanceladas()
    }

    private fun configurarVistas(view: View) {
        barChart = view.findViewById(R.id.barChart)
        rvHistorial = view.findViewById(R.id.rvHistorial)
        tvVacio = view.findViewById(R.id.tvVacioHistorial)
        tabLayout = view.findViewById(R.id.tabLayoutHistorial)
    }

    /**
     * Configura el aspecto visual del gráfico de barras.
     */
    private fun configurarGrafico() {
        barChart.apply {
            description.isEnabled = false
            setNoDataText(getString(R.string.grafico_sin_datos))
            setFitBars(true)
            animateY(800)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    /**
     * Configura el único RecyclerView compartido por ambas pestañas.
     */
    private fun configurarRecyclerView() {
        val sesion = SesionUsuario(requireContext())
        adapter = HistorialAdapter(
            usuarioActualId = sesion.obtenerUsuarioId()
        ) { item ->
            viewLifecycleOwner.lifecycleScope.launch {
                val valoracion = viewModel.obtenerValoracion(item.transaccion.idTransaccion)
                if (valoracion != null) {
                    val bottomSheet = DetalleValoracionBottomSheet.newInstance(
                        pictogramasJson = valoracion.pictogramasJson,
                        comentario = valoracion.comentario,
                        timestamp = valoracion.timestamp,
                        servicioId = item.transaccion.idServicioFk
                    )
                    bottomSheet.onVerServicioCallback = { id ->
                        val bundle = Bundle().apply {
                            putInt("servicioId", id)
                        }
                        findNavController().navigate(R.id.action_global_to_detalle, bundle)
                    }
                    bottomSheet.show(childFragmentManager, "detalleValoracion")
                } else {
                    Toast.makeText(requireContext(), "Sin valoración", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvHistorial.adapter = adapter
    }

    /**
     * Configura el [TabLayout] para alternar entre completadas y canceladas.
     * Por defecto muestra la pestaña "Completadas" (posición 0).
     */
    private fun configurarTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                actualizarLista()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * Observa los datos agrupados por mes y actualiza el gráfico.
     */
    private fun observarDatosGrafico() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.datosGrafico.collect { datos ->
                    if (datos.isEmpty()) {
                        barChart.clear()
                        return@collect
                    }
                    pintarGrafico(datos)
                }
            }
        }
    }

    /**
     * Pinta el gráfico de barras agrupadas con los datos mensuales.
     */
    private fun pintarGrafico(datos: List<DatoMensual>) {
        val etiquetasMeses = datos.map { it.mes }

        val entradasGanadas = datos.mapIndexed { i, dato ->
            BarEntry(i.toFloat(), dato.ganadas.toFloat())
        }
        val dataSetGanadas = BarDataSet(entradasGanadas, "Ganadas").apply {
            color = "#10B981".toColorInt()
            valueTextSize = 10f
        }

        val entradasGastadas = datos.mapIndexed { i, dato ->
            BarEntry(i.toFloat(), dato.gastadas.toFloat())
        }
        val dataSetGastadas = BarDataSet(entradasGastadas, "Gastadas").apply {
            color = "#EF4444".toColorInt()
            valueTextSize = 10f
        }

        val barData = BarData(dataSetGanadas, dataSetGastadas).apply {
            barWidth = 0.3f
        }

        barChart.apply {
            data = barData
            val groupSpace = 0.2f
            val barSpace = 0.05f
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = barData.getGroupWidth(groupSpace, barSpace) * datos.size
            xAxis.valueFormatter = IndexAxisValueFormatter(etiquetasMeses)
            xAxis.setCenterAxisLabels(true)
            groupBars(0f, groupSpace, barSpace)
            invalidate()
        }
    }

    /**
     * Observa la lista de transacciones completadas y actualiza el RecyclerView.
     */
    private fun observarCompletadas() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.completadas.collect { lista ->
                    listaCompletadas = lista
                    actualizarLista()
                }
            }
        }
    }

    /**
     * Observa la lista de transacciones canceladas.
     */
    private fun observarCanceladas() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canceladas.collect { lista ->
                    listaCanceladas = lista
                    actualizarLista()
                }
            }
        }
    }

    /**
     * Actualiza el RecyclerView con la lista correspondiente a la pestaña activa.
     * Pestaña 0 = Completadas, Pestaña 1 = Canceladas.
     */
    private fun actualizarLista() {
        val listaActiva = if (tabLayout.selectedTabPosition == 1) {
            listaCanceladas
        } else {
            listaCompletadas
        }
        adapter.submitList(listaActiva.toMutableList())
        tvVacio.visibility = if (listaActiva.isEmpty()) View.VISIBLE else View.GONE
    }
}
