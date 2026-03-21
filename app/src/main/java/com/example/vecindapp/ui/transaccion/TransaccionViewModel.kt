package com.example.vecindapp.ui.transaccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.TransaccionRepository
import com.example.vecindapp.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de transacciones del usuario.
 *
 * Carga todas las transacciones donde el usuario participa (como
 * comprador o vendedor), las enriquece con datos de presentación
 * ([TransaccionUI]) y expone acciones para gestionar su ciclo de vida.
 *
 * ## Flujo de estados de una transacción
 * ```
 * PENDIENTE → ACEPTADA → COMPLETADA
 *     ↓          ↓
 *  CANCELADA  CANCELADA
 * ```
 *
 * ## Lógica al COMPLETAR (operación atómica)
 * 1. Debitar horas del comprador.
 * 2. Acreditar horas al vendedor.
 * 3. Incrementar contador de intercambios de ambos.
 * 4. Cambiar estado de transacción a COMPLETADA.
 * 5. Cambiar estado del servicio a COMPLETADO.
 *
 * @property transaccionRepository Repositorio de transacciones.
 * @property servicioRepository    Repositorio de servicios.
 * @property usuarioRepository     Repositorio de usuarios.
 *
 * @see TransaccionFragment
 * @see TransaccionAdapter
 */
class TransaccionViewModel(
    private val transaccionRepository: TransaccionRepository,
    private val servicioRepository: ServicioRepository,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioActualId: Int
) : ViewModel() {

    /** Lista enriquecida de transacciones para la UI. */
    private val _transacciones = MutableStateFlow<List<TransaccionUI>>(emptyList())
    val transacciones: StateFlow<List<TransaccionUI>> = _transacciones

    /** Mensaje de feedback para el usuario. */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    init {
        cargarTransacciones()
    }

    /**
     * Carga las transacciones del usuario actual y las transforma
     * en [TransaccionUI] con datos enriquecidos.
     */
    private fun cargarTransacciones() {
        viewModelScope.launch {
            transaccionRepository.getByUsuario(usuarioActualId)
                .catch { e -> e.printStackTrace() }
                .collect { lista ->
                    _transacciones.value = lista.map { t -> enriquecerTransaccion(t) }
                }
        }
    }

    /**
     * Transforma una [Transaccion] en [TransaccionUI] con el título
     * del servicio, el rol del usuario y los permisos de acción.
     */
    private suspend fun enriquecerTransaccion(transaccion: Transaccion): TransaccionUI {
        // Obtener título del servicio
        val servicio = servicioRepository.getById(transaccion.idServicioFk).first()
        val titulo = servicio?.titulo ?: "Servicio eliminado"

        // Determinar rol del usuario actual
        val esVendedor = transaccion.idVendedorFk == usuarioActualId
        val rol = if (esVendedor) "VENDEDOR" else "COMPRADOR"

        // Determinar acciones disponibles segun rol y estado
        val puedeAceptar = esVendedor && transaccion.estado == EstadoTransaccion.PENDIENTE
        val puedeCompletar = esVendedor && transaccion.estado == EstadoTransaccion.ACEPTADA
        val puedeCancelar = transaccion.estado == EstadoTransaccion.PENDIENTE ||
                transaccion.estado == EstadoTransaccion.ACEPTADA

        return TransaccionUI(
            transaccion = transaccion,
            tituloServicio = titulo,
            rol = rol,
            puedeAceptar = puedeAceptar,
            puedeCompletar = puedeCompletar,
            puedeCancelar = puedeCancelar
        )
    }

    /**
     * Acepta una transacción pendiente.
     *
     * Cambia el estado de PENDIENTE → ACEPTADA.
     * Solo el vendedor puede aceptar.
     */
    fun aceptarTransaccion(item: TransaccionUI) {
        viewModelScope.launch {
            try {
                val actualizada = item.transaccion.copy(estado = EstadoTransaccion.ACEPTADA)
                transaccionRepository.update(actualizada)
                _mensaje.value = "Transacción aceptada"
            } catch (e: Exception) {
                e.printStackTrace()
                _mensaje.value = "Error al aceptar"
            }
        }
    }

    /**
     * Completa una transacción aceptada. Operación atómica.
     *
     * 1. Verifica que el comprador tiene saldo suficiente.
     * 2. Debita horas del comprador.
     * 3. Acredita horas al vendedor.
     * 4. Incrementa contadores de intercambios.
     * 5. Cambia estado de transacción a COMPLETADA.
     * 6. Cambia estado del servicio a COMPLETADO.
     */
    fun completarTransaccion(item: TransaccionUI) {
        viewModelScope.launch {
            try {
                val transaccion = item.transaccion
                val horas = transaccion.horasTransferidas

                // 1. Verificar saldo del comprador
                val comprador = usuarioRepository.getByIdOnce(transaccion.idCompradorFk)
                if (comprador == null) {
                    _mensaje.value = "Error: comprador no encontrado"
                    return@launch
                }
                if (comprador.saldoHoras < horas) {
                    _mensaje.value = "El comprador no tiene saldo suficiente"
                    return@launch
                }

                // 2. Obtener vendedor
                val vendedor = usuarioRepository.getByIdOnce(transaccion.idVendedorFk)
                if (vendedor == null) {
                    _mensaje.value = "Error: vendedor no encontrado"
                    return@launch
                }

                // 3. Debitar comprador (quitar horas al comprador)
                usuarioRepository.updateSaldo(
                    comprador.idUsuario,
                    comprador.saldoHoras - horas
                )

                // 4. Acreditar vendedor (poner las horas en la cuenta del vendedor)
                usuarioRepository.updateSaldo(
                    vendedor.idUsuario,
                    vendedor.saldoHoras + horas
                )

                // 5. Incrementar contadores de intercambios
                val compradorActualizado = comprador.copy(
                    intercambiosTotal = comprador.intercambiosTotal + 1
                )
                val vendedorActualizado = vendedor.copy(
                    intercambiosTotal = vendedor.intercambiosTotal + 1
                )
                // Recalcular niveles
                usuarioRepository.update(
                    compradorActualizado.copy(nivel = compradorActualizado.calcularNivel())
                )
                usuarioRepository.update(
                    vendedorActualizado.copy(nivel = vendedorActualizado.calcularNivel())
                )

                // 6. Cambiar estado de transacción
                transaccionRepository.update(
                    transaccion.copy(estado = EstadoTransaccion.COMPLETADA)
                )

                // 7. Cambiar estado del servicio
                servicioRepository.cambiarEstado(
                    transaccion.idServicioFk,
                    EstadoServicio.COMPLETADO.name
                )

                _mensaje.value = "Transacción completada. ${horas}h transferidas"

            } catch (e: Exception) {
                e.printStackTrace()
                _mensaje.value = "Error al completar la transacción"
            }
        }
    }

    /**
     * Cancela una transacción (pendiente o aceptada).
     *
     * Cambia el estado a CANCELADA y devuelve el servicio a ACTIVO
     * para que vuelva a aparecer en el escaparate.
     */
    fun cancelarTransaccion(item: TransaccionUI) {
        viewModelScope.launch {
            try {
                // Cancelar la transacción
                transaccionRepository.update(
                    item.transaccion.copy(estado = EstadoTransaccion.CANCELADA)
                )

                // Devolver el servicio a ACTIVO
                servicioRepository.cambiarEstado(
                    item.transaccion.idServicioFk,
                    EstadoServicio.ACTIVO.name
                )

                _mensaje.value = "Transacción cancelada"
            } catch (e: Exception) {
                e.printStackTrace()
                _mensaje.value = "Error al cancelar"
            }
        }
    }

    /** Limpia el mensaje de feedback. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /**
     * Factory con los 3 repositorios necesarios.
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val servicioRepository: ServicioRepository,
        private val usuarioRepository: UsuarioRepository,
        private val usuarioActualId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransaccionViewModel::class.java)) {
                return TransaccionViewModel(
                    transaccionRepository, servicioRepository, usuarioRepository, usuarioActualId
                ) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}