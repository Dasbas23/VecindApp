package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vecindapp.data.entities.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla `usuario`.
 *
 * Proporciona las operaciones CRUD necesarias para gestionar los vecinos
 * registrados en la aplicación. Las funciones que retornan [Flow] son
 * reactivas: emiten automáticamente cuando los datos cambian en la BBDD.
 *
 * @see Usuario
 * @see AppDatabase
 */
@Dao
interface UsuarioDao {

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * Si ya existe un usuario con la misma PK, lo reemplaza.
     *
     * @param usuario Objeto [Usuario] a insertar.
     * @return El `rowId` generado por SQLite para el registro insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario): Long

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario Objeto [Usuario] con los campos modificados.
     */
    @Update
    suspend fun update(usuario: Usuario)

    /**
     * Obtiene un usuario por su ID de forma reactiva.
     *
     * @param id Clave primaria del usuario.
     * @return [Flow] que emite el [Usuario] o `null` si no existe.
     */
    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    fun getById(id: Int): Flow<Usuario?>

    /**
     * Obtiene la lista completa de usuarios registrados de forma reactiva.
     *
     * @return [Flow] que emite la lista actualizada de [Usuario].
     */
    @Query("SELECT * FROM usuario")
    fun getAll(): Flow<List<Usuario>>

    /**
     * Obtiene un usuario por su ID de forma puntual (no reactiva).
     *
     * Útil para operaciones donde no se necesita observar cambios,
     * como validaciones previas a una transacción.
     *
     * @param id Clave primaria del usuario.
     * @return El [Usuario] o `null` si no existe.
     */
    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    suspend fun getByIdOnce(id: Int): Usuario?

    /**
     * Actualiza directamente el saldo de horas de un usuario.
     *
     * Operación atómica que evita tener que cargar el objeto completo.
     * Utilizada tras completar una transacción (débito/crédito).
     *
     * @param id    Clave primaria del usuario.
     * @param saldo Nuevo saldo de horas.
     */
    @Query("UPDATE usuario SET saldo_horas = :saldo WHERE id_usuario = :id")
    suspend fun updateSaldo(id: Int, saldo: Double)

    /**
     * Busca un usuario por nombre (case-insensitive).
     *
     * @param nombre Nombre a buscar.
     * @return El [Usuario] o null si no existe.
     */
    @Query("SELECT * FROM usuario WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Usuario?
}