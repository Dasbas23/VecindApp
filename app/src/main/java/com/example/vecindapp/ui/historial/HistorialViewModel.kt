package com.example.vecindapp.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * ViewModel para la pantalla de historial.
 *
 * Carga las transacciones completadas del usuario y las procesa
 * para alimentar tanto el gráfico de barras (horas ganadas vs gastadas
 * por mes) como la lista de transacciones finalizadas.
 *
 * ## Datos del gráfico
 * Se agrupan las transacciones completadas por mes y se calculan:
 * - **Horas ganadas**: transacciones donde el usuario es vendedor.
 * - **Horas gastadas**: transacciones donde el usuario es comprador.
 *
 * @property transaccionRepository Repositorio de transacciones.
 *
 * @see HistorialFragment
 */
class HistorialViewModel(
    private val transaccionRepository: TransaccionRepository,
    private val usuarioActualId: Int
) : ViewModel() {

    /** Transacciones completadas para la lista. */
    private val _completadas = MutableStateFlow<List<Transaccion>>(emptyList())
    val completadas: StateFlow<List<Transaccion>> = _completadas

    /** Datos del gráfico: lista de (mes, horasGanadas, horasGastadas). */
    private val _datosGrafico = MutableStateFlow<List<DatoMensual>>(emptyList())
    val datosGrafico: StateFlow<List<DatoMensual>> = _datosGrafico



    init {
        cargarHistorial()
    }

    /**
     * Carga las transacciones del usuario y filtra las completadas.
     * Después genera los datos agrupados por mes para el gráfico.
     */
    private fun cargarHistorial() {
        viewModelScope.launch {
            transaccionRepository.getByUsuario(usuarioActualId)
                .catch { e -> e.printStackTrace() }
                .collect { todas ->
                    val completadas = todas.filter {
                        it.estado == EstadoTransaccion.COMPLETADA
                    }
                    _completadas.value = completadas
                    _datosGrafico.value = agruparPorMes(completadas)
                }
        }
    }

    /**
     * Agrupa las transacciones completadas por mes y calcula
     * horas ganadas (vendedor) y gastadas (comprador) para cada mes.
     *
     * @param transacciones Lista de transacciones completadas.
     * @return Lista de [DatoMensual] ordenada cronológicamente.
     */
    private fun agruparPorMes(transacciones: List<Transaccion>): List<DatoMensual> {
        if (transacciones.isEmpty()) return emptyList()

        val sdf = SimpleDateFormat("MM/yy", Locale.getDefault())

        // Agrupar por mes
        val porMes = transacciones.groupBy { sdf.format(Date(it.timestamp)) }

        return porMes.map { (mes, lista) ->
            val ganadas = lista
                .filter { it.idVendedorFk == usuarioActualId }
                .sumOf { it.horasTransferidas }

            val gastadas = lista
                .filter { it.idCompradorFk == usuarioActualId }
                .sumOf { it.horasTransferidas }

            DatoMensual(mes, ganadas, gastadas)
        }.sortedBy { it.mes }
    }

    /**
     * Factory para crear [HistorialViewModel].
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val usuarioActualId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
                return HistorialViewModel(transaccionRepository, usuarioActualId) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}

/**
 * Datos agregados de un mes para el gráfico de barras.
 *
 * @property mes       Mes en formato "MM/yy" (ej: "03/26").
 * @property ganadas   Total de horas ganadas como vendedor en ese mes.
 * @property gastadas  Total de horas gastadas como comprador en ese mes.
 */
data class DatoMensual(
    val mes: String,
    val ganadas: Double,
    val gastadas: Double
)