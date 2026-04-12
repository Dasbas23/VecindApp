package com.example.vecindapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel principal asociado a [MainActivity].
 *
 * Expone un [StateFlow] reactivo con el número de transacciones que
 * requieren atención del usuario (pendientes + completadas sin valorar),
 * utilizado para mostrar un badge en la pestaña de Transacciones del BottomNav.
 *
 * @property transaccionRepository Repositorio de transacciones.
 * @property usuarioId             ID del usuario con sesión activa.
 *
 * @see MainActivity
 */
class MainViewModel(
    transaccionRepository: TransaccionRepository,
    private val usuarioId: Int
) : ViewModel() {

    /**
     * Conteo reactivo de transacciones pendientes de atención.
     *
     * Se actualiza automáticamente cuando cambian las tablas `transaccion`
     * o `valoracion` en Room. Usa [SharingStarted.WhileSubscribed] para
     * cancelar la suscripción 5 s después de que la UI deje de observar.
     */
    val notificaciones: StateFlow<Int> = transaccionRepository
        .getConteoNotificaciones(usuarioId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Factory para inyección manual de dependencias.
     *
     * @property transaccionRepository Repositorio de transacciones.
     * @property usuarioId             ID del usuario con sesión activa.
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val usuarioId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(transaccionRepository, usuarioId) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
