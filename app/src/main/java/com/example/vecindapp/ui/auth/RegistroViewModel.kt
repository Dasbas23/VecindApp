package com.example.vecindapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de registro de nuevo vecino.
 *
 * Valida nombre y barrio, inserta el usuario en Room y emite
 * su ID para que el Fragment lo guarde en [SesionUsuario].
 *
 * @property usuarioRepository Repositorio de usuarios.
 *
 * @see RegistroFragment
 */
class RegistroViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    /** ID del usuario recién creado. `null` si no se ha registrado aún. */
    private val _registrado = MutableStateFlow<Int?>(null)
    val registrado: StateFlow<Int?> = _registrado

    /** Mensaje de error. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Valida los campos e inserta un nuevo usuario en la BBDD.
     *
     * El nuevo vecino recibe 5.0 horas de bienvenida y nivel NOVATO.
     *
     * @param nombre Nombre del nuevo vecino.
     * @param barrio Barrio o zona donde vive.
     */
    fun registrar(nombre: String, barrio: String) {
        if (nombre.isBlank()) {
            _error.value = "El nombre no puede estar vacío"
            return
        }
        if (barrio.isBlank()) {
            _error.value = "El barrio no puede estar vacío"
            return
        }

        viewModelScope.launch {
            try {
                val usuario = Usuario(
                    nombre = nombre.trim(),
                    barrio = barrio.trim()
                )
                val id = usuarioRepository.insert(usuario)
                _registrado.value = id.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al registrar el usuario"
            }
        }
    }

    /** Limpia el error. */
    fun limpiarError() {
        _error.value = null
    }

    class Factory(
        private val usuarioRepository: UsuarioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
                return RegistroViewModel(usuarioRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}