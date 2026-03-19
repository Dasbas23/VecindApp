package com.example.vecindapp.ui.servicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.repository.ServicioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el formulario de creación de un nuevo servicio.
 *
 * Gestiona la validación de los campos del formulario y la inserción
 * del servicio en la base de datos a través del [ServicioRepository].
 * Expone un [StateFlow] con el resultado de la operación para que el
 * Fragment pueda reaccionar (mostrar error o navegar atrás).
 *
 * @property servicioRepository Repositorio de servicios inyectado manualmente.
 *
 * @see CrearServicioFragment
 */
class CrearServicioViewModel(
    private val servicioRepository: ServicioRepository
) : ViewModel() {

    /**
     * Resultado de la operación de guardado.
     * - `null` → estado inicial, no se ha intentado guardar.
     * - `true` → guardado exitoso.
     * - `false` → error al guardar.
     */
    private val _guardado = MutableStateFlow<Boolean?>(null)
    val guardado: StateFlow<Boolean?> = _guardado

    /** Mensaje de error de validación para mostrar al usuario. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Valida los campos del formulario e inserta el servicio en Room.
     *
     * Si la validación falla, actualiza [error] con el mensaje correspondiente.
     * Si la inserción es exitosa, actualiza [guardado] a `true`.
     *
     * @param titulo      Título del servicio (obligatorio, no vacío).
     * @param descripcion Descripción del servicio (opcional).
     * @param categoria   Categoría seleccionada en el Spinner.
     * @param costeTexto  Coste en horas como texto del EditText.
     * @param usuarioId   ID del usuario que publica (por ahora fijo a 1).
     */
    fun guardarServicio(
        titulo: String,
        descripcion: String,
        categoria: CategoriaServicio,
        costeTexto: String,
        usuarioId: Int = 1 // TODO: Obtener del usuario logueado
    ) {
        // Validaciones
        if (titulo.isBlank()) {
            _error.value = "El título no puede estar vacío"
            return
        }

        val coste = costeTexto.toDoubleOrNull()
        if (coste == null || coste <= 0) {
            _error.value = "Introduce un coste válido (mayor que 0)"
            return
        }

        // Crear el servicio
        val servicio = Servicio(
            idUsuarioFk = usuarioId,
            titulo = titulo.trim(),
            descripcion = descripcion.trim().ifBlank { null },
            categoria = categoria,
            pictogramaId = categoria.name.lowercase(), // TODO: Selector de pictograma ARASAAC
            costeHoras = coste
        )

        // Insertar en Room
        viewModelScope.launch {
            try {
                servicioRepository.insert(servicio)
                _guardado.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al guardar el servicio"
                _guardado.value = false
            }
        }
    }

    /** Limpia el mensaje de error después de mostrarlo. */
    fun limpiarError() {
        _error.value = null
    }

    /**
     * Factory para crear [CrearServicioViewModel] con inyección manual.
     *
     * @property servicioRepository Repositorio a inyectar.
     */
    class Factory(
        private val servicioRepository: ServicioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CrearServicioViewModel::class.java)) {
                return CrearServicioViewModel(servicioRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}