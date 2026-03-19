package com.example.vecindapp.ui.servicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.domain.repository.ServicioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de un servicio.
 *
 * Carga los datos del servicio por su ID, y expone acciones para
 * editar y eliminar. Al eliminar, el Fragment navega de vuelta al
 * escaparate, donde la tarjeta desaparece automáticamente gracias
 * al Flow reactivo de Room.
 *
 * @property servicioRepository Repositorio de servicios inyectado manualmente.
 *
 * @see DetalleServicioFragment
 */
class DetalleServicioViewModel(
    private val servicioRepository: ServicioRepository
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

    /**
     * Carga el servicio por su ID desde la BBDD de forma reactiva.
     *
     * Se suscribe al [Flow] del DAO, por lo que si el servicio se
     * edita desde otra pantalla, el detalle se actualiza solo.
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
     * Elimina el servicio actual de la BBDD.
     *
     * Si la eliminación es exitosa, actualiza [eliminado] a `true`
     * para que el Fragment navegue de vuelta al escaparate.
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
     * Crea una copia del servicio actual con los campos modificados
     * y la persiste. El Flow reactivo actualizará [servicio] automáticamente.
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

    /** Resetea el flag de actualizado después de que el Fragment lo consuma. */
    fun limpiarActualizado() {
        _actualizado.value = false
    }

    /**
     * Factory para crear [DetalleServicioViewModel] con inyección manual.
     */
    class Factory(
        private val servicioRepository: ServicioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetalleServicioViewModel::class.java)) {
                return DetalleServicioViewModel(servicioRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}