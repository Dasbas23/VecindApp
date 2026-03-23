package com.example.vecindapp.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * ViewModel para la pantalla de historial.
 *
 * Carga las transacciones del usuario y las procesa para alimentar:
 * - El gráfico de barras (horas ganadas vs gastadas por mes).
 * - La lista de transacciones completadas (con título del servicio).
 * - La lista de transacciones canceladas (con título del servicio).
 *
 * @property transaccionRepository Repositorio de transacciones.
 * @property servicioRepository    Repositorio de servicios (para obtener títulos).
 *
 * @see HistorialFragment
 */
class HistorialViewModel(
    private val transaccionRepository: TransaccionRepository,
    private val servicioRepository: ServicioRepository,
    private val usuarioActualId: Int
) : ViewModel() {

    /** Transacciones completadas enriquecidas con el título del servicio. */
    private val _completadas = MutableStateFlow<List<HistorialItem>>(emptyList())
    val completadas: StateFlow<List<HistorialItem>> = _completadas

    /** Transacciones canceladas enriquecidas con el título del servicio. */
    private val _canceladas = MutableStateFlow<List<HistorialItem>>(emptyList())
    val canceladas: StateFlow<List<HistorialItem>> = _canceladas

    /** Datos del gráfico: lista de (mes, horasGanadas, horasGastadas). */
    private val _datosGrafico = MutableStateFlow<List<DatoMensual>>(emptyList())
    val datosGrafico: StateFlow<List<DatoMensual>> = _datosGrafico

    init {
        cargarHistorial()
    }

    /**
     * Carga las transacciones del usuario, las filtra por estado
     * y las enriquece con el título del servicio.
     */
    private fun cargarHistorial() {
        viewModelScope.launch {
            transaccionRepository.getByUsuario(usuarioActualId)
                .catch { e -> e.printStackTrace() }
                .collect { todas ->
                    val completadas = todas
                        .filter { it.estado == EstadoTransaccion.COMPLETADA }
                        .map { enriquecer(it) }

                    val canceladas = todas
                        .filter { it.estado == EstadoTransaccion.CANCELADA }
                        .map { enriquecer(it) }

                    _completadas.value = completadas
                    _canceladas.value = canceladas
                    _datosGrafico.value = agruparPorMes(completadas)
                }
        }
    }

    /**
     * Enriquece una transacción con el título del servicio y el signo de horas.
     */
    private suspend fun enriquecer(transaccion: Transaccion): HistorialItem {
        val servicio = servicioRepository.getById(transaccion.idServicioFk).first()
        val titulo = servicio?.titulo ?: "Servicio eliminado"
        val esVendedor = transaccion.idVendedorFk == usuarioActualId
        return HistorialItem(transaccion, titulo, esVendedor)
    }

    /**
     * Agrupa las transacciones completadas por mes y calcula
     * horas ganadas (vendedor) y gastadas (comprador) para cada mes.
     *
     * @param items Lista de [HistorialItem] de transacciones completadas.
     * @return Lista de [DatoMensual] ordenada cronológicamente.
     */
    private fun agruparPorMes(items: List<HistorialItem>): List<DatoMensual> {
        if (items.isEmpty()) return emptyList()

        val sdf = SimpleDateFormat("MM/yy", Locale.getDefault())

        val porMes = items.groupBy { sdf.format(Date(it.transaccion.timestamp)) }

        return porMes.map { (mes, lista) ->
            val ganadas = lista
                .filter { it.esVendedor }
                .sumOf { it.transaccion.horasTransferidas }

            val gastadas = lista
                .filter { !it.esVendedor }
                .sumOf { it.transaccion.horasTransferidas }

            DatoMensual(mes, ganadas, gastadas)
        }.sortedBy { it.mes }
    }

    /**
     * Factory para crear [HistorialViewModel].
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val servicioRepository: ServicioRepository,
        private val usuarioActualId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
                return HistorialViewModel(transaccionRepository, servicioRepository, usuarioActualId) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}

/**
 * Modelo de presentación para una entrada del historial.
 *
 * @property transaccion   Entidad original de Room.
 * @property tituloServicio Título del servicio asociado.
 * @property esVendedor    `true` si el usuario actual fue el vendedor (ganó horas).
 */
data class HistorialItem(
    val transaccion: Transaccion,
    val tituloServicio: String,
    val esVendedor: Boolean
)

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
