package com.example.vecindapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.db.UsuarioDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión.
 *
 * Busca un usuario por nombre en la BBDD. Si existe, emite su ID
 * para que el Fragment lo guarde en [SesionUsuario] y navegue al
 * escaparate. Si no existe, emite un mensaje de error.
 *
 * @property usuarioDao DAO directo (no Repository) porque esta pantalla
 *                      necesita una query específica que no está en el repo.
 *
 * @see LoginFragment
 */
class LoginViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    /** ID del usuario encontrado. `null` si no se ha buscado aún. */
    private val _usuarioEncontrado = MutableStateFlow<Int?>(null)
    val usuarioEncontrado: StateFlow<Int?> = _usuarioEncontrado

    /** Mensaje de error. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Busca un usuario por nombre en la BBDD.
     *
     * La búsqueda no distingue mayúsculas/minúsculas.
     *
     * @param nombre Nombre introducido por el usuario.
     */
    fun iniciarSesion(nombre: String) {
        if (nombre.isBlank()) {
            _error.value = "Introduce tu nombre"
            return
        }

        viewModelScope.launch {
            val usuarios = usuarioDao.buscarPorNombre(nombre.trim())
            if (usuarios != null) {
                _usuarioEncontrado.value = usuarios.idUsuario
            } else {
                _error.value = "No se ha encontrado ningún vecino con ese nombre"
            }
        }
    }

    /** Limpia el error. */
    fun limpiarError() {
        _error.value = null
    }

    class Factory(
        private val usuarioDao: UsuarioDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(usuarioDao) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}