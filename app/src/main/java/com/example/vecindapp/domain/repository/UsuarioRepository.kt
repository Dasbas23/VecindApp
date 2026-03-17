package com.example.vecindapp.domain.repository

import com.example.vecindapp.data.entities.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de usuarios.
 *
 * Define las operaciones disponibles sobre la tabla `usuario` sin exponer
 * detalles de implementación (Room, SQLite, etc.). Los ViewModels dependen
 * de esta interfaz, nunca de la implementación concreta, respetando el
 * principio de inversión de dependencias (Clean Architecture).
 *
 * @see com.example.vecindapp.data.repository.UsuarioRepositoryImpl
 */
interface UsuarioRepository {

    /**
     * Inserta un nuevo usuario o reemplaza uno existente con la misma PK.
     *
     * @param usuario Objeto [Usuario] a persistir.
     * @return El `rowId` generado por SQLite.
     */
    suspend fun insert(usuario: Usuario): Long

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario Objeto [Usuario] con los campos modificados.
     */
    suspend fun update(usuario: Usuario)

    /**
     * Obtiene un usuario por su ID de forma reactiva.
     *
     * @param id Clave primaria del usuario.
     * @return [Flow] que emite el [Usuario] o `null` si no existe.
     */
    fun getById(id: Int): Flow<Usuario?>

    /**
     * Obtiene la lista completa de usuarios de forma reactiva.
     *
     * @return [Flow] que emite la lista actualizada de [Usuario].
     */
    fun getAll(): Flow<List<Usuario>>

    /**
     * Obtiene un usuario por su ID de forma puntual (no reactiva).
     *
     * @param id Clave primaria del usuario.
     * @return El [Usuario] o `null` si no existe.
     */
    suspend fun getByIdOnce(id: Int): Usuario?
}
