package com.example.vecindapp.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de perfil del vecino.
 *
 * Carga los datos del usuario actual de forma reactiva y la lista
 * de servicios que ha publicado. Ambos se actualizan automáticamente
 * cuando hay cambios en la BBDD (por ejemplo, tras completar una
 * transacción el saldo y los intercambios se reflejan al instante).
 *
 * @property usuarioRepository  Repositorio de usuarios.
 * @property servicioRepository Repositorio de servicios.
 *
 * @see PerfilFragment
 */
class PerfilViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val servicioRepository: ServicioRepository,
    private val usuarioActualId: Int
) : ViewModel() {

    /** Datos del usuario actual. */
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    /** Lista de servicios publicados por el usuario. */
    private val _misServicios = MutableStateFlow<List<Servicio>>(emptyList())
    val misServicios: StateFlow<List<Servicio>> = _misServicios

    init {
        cargarPerfil()
        cargarMisServicios()
    }

    /**
     * Carga los datos del usuario de forma reactiva.
     *
     * Cada vez que se actualiza el saldo, nivel o intercambios
     * en la BBDD, el Flow emite los datos nuevos y la UI se refresca.
     */
    private fun cargarPerfil() {
        viewModelScope.launch {
            usuarioRepository.getById(usuarioActualId)
                .catch { e -> e.printStackTrace() }
                .collect { usuario ->
                    _usuario.value = usuario
                }
        }
    }

    /**
     * Carga los servicios publicados por el usuario de forma reactiva.
     *
     * Incluye servicios en cualquier estado (activos, reservados,
     * completados) para que el usuario tenga visibilidad total.
     */
    private fun cargarMisServicios() {
        viewModelScope.launch {
            servicioRepository.getByUsuario(usuarioActualId)
                .catch { e -> e.printStackTrace() }
                .collect { servicios ->
                    _misServicios.value = servicios
                }
        }
    }

    /**
     * Factory con los repositorios necesarios.
     */
    class Factory(
        private val usuarioRepository: UsuarioRepository,
        private val servicioRepository: ServicioRepository,
        private val usuarioActualId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
                return PerfilViewModel(usuarioRepository, servicioRepository, usuarioActualId) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}