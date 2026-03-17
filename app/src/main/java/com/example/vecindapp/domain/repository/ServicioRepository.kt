package com.example.vecindapp.domain.repository

import com.example.vecindapp.data.entities.Servicio
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de servicios.
 *
 * Define las operaciones CRUD y de consulta sobre la tabla `servicio`.
 * Es la interfaz que utilizarán los ViewModels del escaparate y del
 * módulo de publicación/edición de servicios.
 *
 * @see com.example.vecindapp.data.repository.ServicioRepositoryImpl
 */
interface ServicioRepository {

    /**
     * Inserta un nuevo servicio o reemplaza uno existente con la misma PK.
     *
     * @param servicio Objeto [Servicio] a persistir.
     * @return El `rowId` generado por SQLite.
     */
    suspend fun insert(servicio: Servicio): Long

    /**
     * Actualiza los datos de un servicio existente.
     *
     * @param servicio Objeto [Servicio] con los campos modificados.
     */
    suspend fun update(servicio: Servicio)

    /**
     * Elimina un servicio de la base de datos.
     *
     * @param servicio Objeto [Servicio] a eliminar.
     */
    suspend fun delete(servicio: Servicio)

    /**
     * Obtiene un servicio por su ID de forma reactiva.
     *
     * @param id Clave primaria del servicio.
     * @return [Flow] que emite el [Servicio] o `null` si no existe.
     */
    fun getById(id: Int): Flow<Servicio?>

    /**
     * Obtiene todos los servicios con estado ACTIVO, ordenados del más
     * reciente al más antiguo. Query principal del escaparate.
     *
     * @return [Flow] reactivo con la lista de servicios activos.
     */
    fun getActivos(): Flow<List<Servicio>>

    /**
     * Obtiene todos los servicios publicados por un usuario concreto.
     *
     * @param usuarioId ID del usuario propietario.
     * @return [Flow] reactivo con la lista de servicios del usuario.
     */
    fun getByUsuario(usuarioId: Int): Flow<List<Servicio>>

    /**
     * Filtra servicios activos por categoría.
     *
     * @param categoria Nombre de la categoría como String.
     * @return [Flow] reactivo con los servicios filtrados.
     */
    fun getByCategoria(categoria: String): Flow<List<Servicio>>

    /**
     * Cambia el estado de un servicio directamente.
     *
     * @param id     Clave primaria del servicio.
     * @param estado Nuevo estado como String.
     */
    suspend fun cambiarEstado(id: Int, estado: String)
}
