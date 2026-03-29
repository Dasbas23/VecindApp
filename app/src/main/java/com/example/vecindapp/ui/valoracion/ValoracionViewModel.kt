package com.example.vecindapp.ui.valoracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecindapp.data.entities.Valoracion
import com.example.vecindapp.domain.repository.ValoracionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el proceso de valoración de un vecino.
 *
 * Gestiona la inserción de una [Valoracion] en Room tras completar
 * una transacción. Recibe los pictogramas seleccionados como lista
 * de Strings y los serializa a JSON para almacenarlos.
 *
 * @property valoracionRepository Repositorio de valoraciones.
 *
 * @see ValoracionBottomSheetFragment
 */
class ValoracionViewModel(
    private val valoracionRepository: ValoracionRepository
) : ViewModel() {

    /** Indica si la valoración se guardó con éxito. */
    private val _guardada = MutableStateFlow(false)
    val guardada: StateFlow<Boolean> = _guardada

    /** Mensaje de error. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Guarda una valoración en la base de datos.
     *
     * Convierte la lista de IDs de pictogramas seleccionados a JSON
     * y crea el registro de [Valoracion] con todos los datos necesarios.
     *
     * @param idTransaccion ID de la transacción valorada.
     * @param idValoradorFk ID del usuario que emite la valoración.
     * @param idValoradoFk  ID del usuario que recibe la valoración.
     * @param pictogramas   Lista de IDs de pictogramas seleccionados.
     * @param comentario    Comentario opcional de texto libre.
     */
    fun guardarValoracion(
        idTransaccion: Int,
        idValoradorFk: Int,
        idValoradoFk: Int,
        pictogramas: List<String>,
        comentario: String
    ) {
        if (pictogramas.isEmpty()) {
            _error.value = "Selecciona al menos un pictograma"
            return
        }

        // Serializar la lista de pictogramas a JSON simple
        val pictogramasJson = pictogramas.joinToString(
            separator = "\",\"",
            prefix = "[\"",
            postfix = "\"]"
        )

        val valoracion = Valoracion(
            idTransaccionFk = idTransaccion,
            idValoradorFk = idValoradorFk,
            idValoradoFk = idValoradoFk,
            pictogramasJson = pictogramasJson,
            comentario = comentario.trim().ifBlank { null }
        )

        viewModelScope.launch {
            try {
                valoracionRepository.insert(valoracion)
                _guardada.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al guardar la valoración"
            }
        }
    }

    /** Limpia el error. */
    fun limpiarError() {
        _error.value = null
    }

    class Factory(
        private val valoracionRepository: ValoracionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ValoracionViewModel::class.java)) {
                return ValoracionViewModel(valoracionRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}