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

/**
 * Fragment de historial de transacciones.
 *
 * Muestra un gráfico de barras agrupadas (MPAndroidChart) con las horas
 * ganadas y gastadas por mes, y debajo una lista de todas las transacciones
 * completadas del usuario.
 *
 * ## Gráfico de barras
 * - **Verde**: horas ganadas (el usuario fue vendedor).
 * - **Rojo**: horas gastadas (el usuario fue comprador).
 * - Las barras se agrupan por mes.
 *
 * @see HistorialViewModel
 * @see HistorialAdapter
 */
class HistorialFragment : Fragment() {

    private val viewModel: HistorialViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        val sesion = SesionUsuario(requireContext())
        HistorialViewModel.Factory(app.transaccionRepository, sesion.obtenerUsuarioId())
    }

    private lateinit var barChart: BarChart
    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var adapter: HistorialAdapter

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
        observarDatosGrafico()
        observarCompletadas()
    }

    private fun configurarVistas(view: View) {
        barChart = view.findViewById(R.id.barChart)
        rvHistorial = view.findViewById(R.id.rvHistorial)
        tvVacio = view.findViewById(R.id.tvVacioHistorial)
    }

    /**
     * Configura el aspecto visual del gráfico de barras.
     *
     * Se elimina la descripción, la leyenda se posiciona abajo,
     * y el eje X muestra los nombres de los meses.
     */
    private fun configurarGrafico() {
        barChart.apply {
            description.isEnabled = false
            setNoDataText(getString(R.string.grafico_sin_datos))
            setFitBars(true)
            animateY(800)

            // Eje X: meses
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            // Eje Y izquierdo: horas
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }

            // Ocultar eje Y derecho
            axisRight.isEnabled = false

            // Leyenda
            legend.isEnabled = true
        }
    }

    /**
     * Configura el RecyclerView de transacciones completadas.
     */
    private fun configurarRecyclerView() {
        adapter = HistorialAdapter()
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvHistorial.adapter = adapter
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
     *
     * Cada mes tiene dos barras: verde (ganadas) y roja (gastadas).
     *
     * @param datos Lista de [DatoMensual] con horas por mes.
     */
    private fun pintarGrafico(datos: List<DatoMensual>) {
        val etiquetasMeses = datos.map { it.mes }

        // Barras de horas ganadas (verde)
        val entradasGanadas = datos.mapIndexed { i, dato ->
            BarEntry(i.toFloat(), dato.ganadas.toFloat())
        }
        val dataSetGanadas = BarDataSet(entradasGanadas, "Ganadas").apply {
            color = Color.parseColor("#10B981") // Verde
            valueTextSize = 10f
        }

        // Barras de horas gastadas (rojo)
        val entradasGastadas = datos.mapIndexed { i, dato ->
            BarEntry(i.toFloat(), dato.gastadas.toFloat())
        }
        val dataSetGastadas = BarDataSet(entradasGastadas, "Gastadas").apply {
            color = Color.parseColor("#EF4444") // Rojo
            valueTextSize = 10f
        }

        // Configurar barras agrupadas
        val barData = BarData(dataSetGanadas, dataSetGastadas).apply {
            barWidth = 0.3f
        }

        barChart.apply {
            data = barData

            // Configurar agrupación
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
                    adapter.submitList(lista.toMutableList())
                    tvVacio.visibility =
                        if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}