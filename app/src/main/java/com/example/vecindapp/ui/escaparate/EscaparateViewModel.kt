package com.example.vecindapp.ui.escaparate

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
 * ViewModel para la pantalla del escaparate (catálogo de servicios).
 *
 * Expone la lista de servicios activos como un [StateFlow] reactivo al que
 * el Fragment se suscribe. Cada vez que se inserta, edita o elimina un
 * servicio en la BBDD, la lista se actualiza automáticamente gracias al
 * [Flow] que emite Room desde el DAO.
 *
 * Soporta filtrado por categoría: al llamar a [filtrarPorCategoria], se
 * cambia el flujo de datos para mostrar solo los servicios de esa categoría.
 * [quitarFiltro] restaura la vista completa.
 *
 * @property servicioRepository Repositorio de servicios inyectado manualmente.
 *
 * @see EscaparateFragment
 * @see ServicioAdapter
 */
class EscaparateViewModel(
    private val servicioRepository: ServicioRepository
) : ViewModel() {

    /** Lista reactiva de servicios que alimenta el RecyclerView. */
    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios

    /** Indica si se está aplicando un filtro de categoría. */
    private val _filtroActivo = MutableStateFlow<String?>(null)
    val filtroActivo: StateFlow<String?> = _filtroActivo

    init {
        cargarServiciosActivos()
    }

    /**
     * Carga todos los servicios con estado ACTIVO, ordenados del más
     * reciente al más antiguo. Se ejecuta automáticamente al crear el ViewModel.
     */
    private fun cargarServiciosActivos() {
        viewModelScope.launch {
            servicioRepository.getActivos()
                .catch { e -> e.printStackTrace() }
                .collect { lista ->
                    _servicios.value = lista
                }
        }
    }
    /**
     * Filtra los servicios del escaparate por una categoría concreta.
     *
     * Cancela la suscripción anterior y abre una nueva con la query filtrada.
     *
     * @param categoria Nombre de la categoría (debe coincidir con [CategoriaServicio.name]).
     */
    fun filtrarPorCategoria(categoria: String) {
        _filtroActivo.value = categoria
        viewModelScope.launch {
            servicioRepository.getByCategoria(categoria)
                .catch { e -> e.printStackTrace() }
                .collect { lista ->
                    _servicios.value = lista
                }
        }
    }

    /**
     * Elimina el filtro activo y vuelve a mostrar todos los servicios activos.
     */
    fun quitarFiltro() {
        _filtroActivo.value = null
        cargarServiciosActivos()
    }

    /**
     * Factory para crear [EscaparateViewModel] con inyección manual.
     *
     * Necesaria porque el ViewModel tiene dependencias en el constructor
     * y no usamos Hilt/Dagger.
     *
     * Uso desde el Fragment:
     * ```
     * private val viewModel: EscaparateViewModel by viewModels {
     *     EscaparateViewModel.Factory(servicioRepository)
     * }
     * ```
     *
     * @property servicioRepository Repositorio a inyectar.
     */
    class Factory(
        private val servicioRepository: ServicioRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EscaparateViewModel::class.java)) {
                return EscaparateViewModel(servicioRepository) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}