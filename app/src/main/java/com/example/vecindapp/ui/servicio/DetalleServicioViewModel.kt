package com.example.vecindapp.ui.servicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de un servicio.
 *
 * Gestiona la visualización del servicio, su edición, eliminación
 * y la solicitud por parte de otro vecino (creación de transacción).
 *
 * Al solicitar un servicio:
 * 1. Se crea una [Transaccion] con estado PENDIENTE.
 * 2. Se cambia el estado del servicio a RESERVADO.
 * 3. El escaparate se actualiza solo (el servicio desaparece de "activos").
 *
 * @property servicioRepository    Repositorio de servicios.
 * @property transaccionRepository Repositorio de transacciones.
 *
 * @see DetalleServicioFragment
 */
class DetalleServicioViewModel(
    private val servicioRepository: ServicioRepository,
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {

    /** Servicio cargado de la BBDD de forma reactiva. */
    private val _servicio = MutableStateFlow<Servicio?>(null)
    val servicio: StateFlow<Servicio?> = _servicio

    /** Indica si el servicio se ha eliminado con éxito. */
    private val _eliminado = MutableStateFlow(false)
    val eliminado: StateFlow<Boolean> = _eliminado

    /** Indica si el servicio se ha actualizado con éxito. */
    private val _actualizado = MutableStateFlow(false)
    val actualizado: StateFlow<Boolean> = _actualizado

    /** Indica si la solicitud se ha realizado con éxito. */
    private val _solicitado = MutableStateFlow(false)
    val solicitado: StateFlow<Boolean> = _solicitado

    /** Mensaje de error para mostrar al usuario. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Carga el servicio por su ID desde la BBDD de forma reactiva.
     *
     * @param servicioId Clave primaria del servicio a cargar.
     */
    fun cargarServicio(servicioId: Int) {
        viewModelScope.launch {
            servicioRepository.getById(servicioId)
                .catch { e -> e.printStackTrace() }
                .collect { servicio ->
                    _servicio.value = servicio
                }
        }
    }

    /**
     * Solicita el servicio creando una transacción PENDIENTE.
     *
     * Realiza dos operaciones:
     * 1. Inserta una [Transaccion] con el comprador y vendedor.
     * 2. Cambia el estado del servicio a [EstadoServicio.RESERVADO].
     *
     * @param compradorId ID del vecino que solicita (comprador).
     */
    fun solicitarServicio(compradorId: Int = 1) { // TODO: Obtener del usuario logueado
        val servicioActual = _servicio.value ?: return

        // No puedes solicitar tu propio servicio
        if (servicioActual.idUsuarioFk == compradorId) {
            _error.value = "No puedes solicitar tu propio servicio"
            return
        }

        // Solo se puede solicitar si está ACTIVO
        if (servicioActual.estado != EstadoServicio.ACTIVO) {
            _error.value = "Este servicio ya no está disponible"
            return
        }

        viewModelScope.launch {
            try {
                // 1. Crear la transacción
                val transaccion = Transaccion(
                    idCompradorFk = compradorId,
                    idVendedorFk = servicioActual.idUsuarioFk,
                    idServicioFk = servicioActual.idServicio,
                    horasTransferidas = servicioActual.costeHoras
                )
                transaccionRepository.insert(transaccion)

                // 2. Cambiar estado del servicio a RESERVADO
                servicioRepository.cambiarEstado(
                    servicioActual.idServicio,
                    EstadoServicio.RESERVADO.name
                )

                _solicitado.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al solicitar el servicio"
            }
        }
    }

    /**
     * Elimina el servicio actual de la BBDD.
     */
    fun eliminarServicio() {
        val servicioActual = _servicio.value ?: return
        viewModelScope.launch {
            try {
                servicioRepository.delete(servicioActual)
                _eliminado.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Actualiza los campos editables del servicio en la BBDD.
     *
     * @param titulo      Nuevo título.
     * @param descripcion Nueva descripción.
     * @param costeHoras  Nuevo coste en horas.
     */
    fun actualizarServicio(titulo: String, descripcion: String, costeHoras: Double) {
        val servicioActual = _servicio.value ?: return
        val servicioEditado = servicioActual.copy(
            titulo = titulo.trim(),
            descripcion = descripcion.trim().ifBlank { null },
            costeHoras = costeHoras
        )
        viewModelScope.launch {
            try {
                servicioRepository.update(servicioEditado)
                _actualizado.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Resetea el flag de actualizado. */
    fun limpiarActualizado() {
        _actualizado.value = false
    }

    /** Limpia el mensaje de error. */
    fun limpiarError() {
        _error.value = null
    }

    /**
     * Factory actualizada con ambos repositorios.
     */
    class Factory(
        private val servicioRepository: ServicioRepository,
        private val transaccionRepository: TransaccionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetalleServicioViewModel::class.java)) {
                return DetalleServicioViewModel(servicioRepository, transaccionRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}