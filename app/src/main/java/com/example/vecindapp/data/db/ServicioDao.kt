package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vecindapp.data.entities.Servicio
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla `servicio`.
 *
 * Gestiona el CRUD completo de los servicios publicados en el escaparate.
 * Incluye consultas de filtrado por estado, usuario y categoría que
 * alimentan directamente al RecyclerView del escaparate mediante [Flow].
 *
 * @see Servicio
 * @see AppDatabase
 */
@Dao
interface ServicioDao {

    /**
     * Inserta un nuevo servicio en la base de datos.
     *
     * Si ya existe un servicio con la misma PK, lo reemplaza, pero como es autoincremental no pasará
     *
     * @param servicio Objeto [Servicio] a insertar.
     * @return El `rowId` generado por SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(servicio: Servicio): Long

    /**
     * Actualiza los datos de un servicio existente.
     *
     * @param servicio Objeto [Servicio] con los campos modificados.
     */
    @Update
    suspend fun update(servicio: Servicio)

    /**
     * Elimina un servicio de la base de datos.
     *
     * @param servicio Objeto [Servicio] a eliminar (se identifica por su PK).
     */
    @Delete
    suspend fun delete(servicio: Servicio)

    /**
     * Obtiene un servicio por su ID de forma reactiva.
     *
     * @param id Clave primaria del servicio.
     * @return [Flow] que emite el [Servicio] o `null` si no existe.
     */
    @Query("SELECT * FROM servicio WHERE id_servicio = :id")
    fun getById(id: Int): Flow<Servicio?>

    /**
     * Obtiene todos los servicios con estado ACTIVO, ordenados del más
     * reciente al más antiguo.
     *
     * Esta es la query principal del escaparate.
     *
     * @return [Flow] reactivo con la lista de servicios activos.
     */
    @Query("SELECT * FROM servicio WHERE estado = 'ACTIVO' ORDER BY fecha_publicacion DESC")
    fun getActivos(): Flow<List<Servicio>>

    /**
     * Obtiene todos los servicios publicados por un usuario concreto.
     *
     * Utilizado en la pantalla de perfil para mostrar "Mis servicios".
     *
     * @param usuarioId ID del usuario propietario.
     * @return [Flow] reactivo con la lista de servicios del usuario.
     */
    @Query("SELECT * FROM servicio WHERE id_usuario_fk = :usuarioId ORDER BY fecha_publicacion DESC")
    fun getByUsuario(usuarioId: Int): Flow<List<Servicio>>

    /**
     * Filtra servicios activos por categoría.
     *
     * Utilizado cuando el usuario aplica un filtro en el escaparate.
     *
     * @param categoria Nombre de la categoría (debe coincidir con el valor del enum en String).
     * @return [Flow] reactivo con los servicios filtrados.
     */
    @Query("SELECT * FROM servicio WHERE categoria = :categoria AND estado = 'ACTIVO' ORDER BY fecha_publicacion DESC")
    fun getByCategoria(categoria: String): Flow<List<Servicio>>
}