package com.example.vecindapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.SesionUsuario
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel principal asociado a [MainActivity].
 *
 * Expone un [StateFlow] reactivo con el número de transacciones que
 * requieren atención del usuario (pendientes + completadas sin valorar),
 * utilizado para mostrar un badge en la pestaña de Transacciones del BottomNav.
 *
 * ## Reactividad al cambio de usuario
 * El `usuarioId` no se fija en el constructor: se actualiza dinámicamente
 * con [setUsuarioId]. Internamente, [notificaciones] usa [flatMapLatest]
 * para cancelar la suscripción anterior y recolectar los datos del nuevo
 * usuario cada vez que el ID cambia. Esto resuelve el problema de
 * "state bleed" en la arquitectura Single-Activity (la Activity no se
 * destruye al cambiar de sesión).
 *
 * @property transaccionRepository Repositorio de transacciones.
 *
 * @see MainActivity
 */
class MainViewModel(
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {

    /**
     * ID del usuario con sesión activa. Valor inicial: [SesionUsuario.SIN_SESION].
     * Al cambiar, [flatMapLatest] cancela la recolección anterior y empieza
     * a recolectar [TransaccionRepository.getConteoNotificaciones] del nuevo ID.
     */
    private val _usuarioId = MutableStateFlow(SesionUsuario.SIN_SESION)

    /**
     * Conteo reactivo de transacciones pendientes de atención.
     *
     * Se actualiza automáticamente cuando cambian las tablas `transaccion`
     * o `valoracion` en Room, y también cuando cambia el usuario activo
     * (via [setUsuarioId]). Usa [SharingStarted.WhileSubscribed] para
     * cancelar la suscripción 5 s después de que la UI deje de observar.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notificaciones: StateFlow<Int> = _usuarioId
        .flatMapLatest { id ->
            if (id > 0) transaccionRepository.getConteoNotificaciones(id)
            else flowOf(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Actualiza el usuario activo. Llamar tras login, registro
     * o al detectar sesión existente. Pasar [SesionUsuario.SIN_SESION]
     * al cerrar sesión para limpiar el badge.
     *
     * @param id ID del usuario (o [SesionUsuario.SIN_SESION] para limpiar).
     */
    fun setUsuarioId(id: Int) {
        _usuarioId.value = id
    }

    /**
     * Factory simplificada — ya no recibe `usuarioId`.
     *
     * @property transaccionRepository Repositorio de transacciones.
     */
    class Factory(
        private val transaccionRepository: TransaccionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(transaccionRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}