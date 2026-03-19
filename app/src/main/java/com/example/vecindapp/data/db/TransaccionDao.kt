package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vecindapp.data.entities.Transaccion
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla `transaccion`.
 *
 * Gestiona las operaciones de lectura y escritura sobre las transacciones
 * de horas entre vecinos. La lógica atómica de débito/crédito se implementa
 * en la capa Repository con `@Transaction` de Room.
 *
 * @see Transaccion
 * @see AppDatabase
 */
@Dao
interface TransaccionDao {

    /**
     * Inserta una nueva transacción en la base de datos.
     *
     * @param transaccion Objeto [Transaccion] a insertar.
     * @return El `rowId` generado por SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaccion: Transaccion): Long

    /**
     * Actualiza una transacción existente (cambio de estado, por ejemplo).
     *
     * @param transaccion Objeto [Transaccion] con los campos modificados.
     */
    @Update
    suspend fun update(transaccion: Transaccion)

    /**
     * Obtiene todas las transacciones en las que participa un usuario,
     * ya sea como comprador o como vendedor, ordenadas de más reciente a más antigua.
     *
     * Utilizado en la pantalla de historial.
     *
     * @param usuarioId ID del usuario.
     * @return [Flow] reactivo con la lista de transacciones del usuario.
     */
    @Query("SELECT * FROM transaccion WHERE id_comprador_fk = :usuarioId OR id_vendedor_fk = :usuarioId ORDER BY timestamp DESC")
    fun getByUsuario(usuarioId: Int): Flow<List<Transaccion>>

    /**
     * Obtiene la transacción asociada a un servicio concreto (puntual).
     *
     * Útil para comprobar si un servicio ya tiene una transacción activa.
     *
     * @param servicioId ID del servicio.
     * @return La [Transaccion] asociada o `null` si no existe.
     */
    @Query("SELECT * FROM transaccion WHERE id_servicio_fk = :servicioId")
    suspend fun getByServicio(servicioId: Int): Transaccion?

    /**
     * Obtiene una transacción por su ID de forma puntual.
     *
     * @param id Clave primaria de la transacción.
     * @return La [Transaccion] o `null` si no existe.
     */
    @Query("SELECT * FROM transaccion WHERE id_transaccion = :id")
    suspend fun getByIdOnce(id: Int): Transaccion?
}