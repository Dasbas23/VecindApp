package com.example.vecindapp.ui.historial

import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.data.SesionUsuario
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

/**
 * Fragment de historial de transacciones.
 *
 * Muestra un gráfico de barras agrupadas (MPAndroidChart) con las horas
 * ganadas y gastadas por mes, y debajo dos listas:
 * - Transacciones completadas (con título del servicio y signo +/- en color).
 * - Transacciones canceladas (visibles solo si existen).
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
            sesion.obtenerUsuarioId()
        )
    }

    private lateinit var barChart: BarChart
    private lateinit var rvHistorial: RecyclerView
    private lateinit var rvCanceladas: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var tvSubtituloCanceladas: TextView
    private lateinit var adapterCompletadas: HistorialAdapter
    private lateinit var adapterCanceladas: HistorialAdapter

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
        configurarRecyclerViews()
        observarDatosGrafico()
        observarCompletadas()
        observarCanceladas()
    }

    private fun configurarVistas(view: View) {
        barChart = view.findViewById(R.id.barChart)
        rvHistorial = view.findViewById(R.id.rvHistorial)
        rvCanceladas = view.findViewById(R.id.rvCanceladas)
        tvVacio = view.findViewById(R.id.tvVacioHistorial)
        tvSubtituloCanceladas = view.findViewById(R.id.tvSubtituloCanceladas)
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
     * Configura los dos RecyclerViews (completadas y canceladas).
     */
    private fun configurarRecyclerViews() {
        adapterCompletadas = HistorialAdapter()
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvHistorial.adapter = adapterCompletadas

        adapterCanceladas = HistorialAdapter()
        rvCanceladas.layoutManager = LinearLayoutManager(requireContext())
        rvCanceladas.adapter = adapterCanceladas
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
                    adapterCompletadas.submitList(lista.toMutableList())
                    actualizarEstadoVacio()
                }
            }
        }
    }

    /**
     * Observa la lista de transacciones canceladas.
     * Muestra la sección solo si existen canceladas.
     */
    private fun observarCanceladas() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canceladas.collect { lista ->
                    adapterCanceladas.submitList(lista.toMutableList())
                    val visibilidad = if (lista.isEmpty()) View.GONE else View.VISIBLE
                    tvSubtituloCanceladas.visibility = visibilidad
                    rvCanceladas.visibility = visibilidad
                    actualizarEstadoVacio()
                }
            }
        }
    }

    private fun actualizarEstadoVacio() {
        val sinDatos = adapterCompletadas.itemCount == 0 && adapterCanceladas.itemCount == 0
        tvVacio.visibility = if (sinDatos) View.VISIBLE else View.GONE
    }
}
