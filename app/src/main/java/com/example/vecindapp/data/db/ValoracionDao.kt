package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vecindapp.data.entities.Valoracion
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla `valoracion`.
 *
 * Gestiona las valoraciones que los vecinos se dejan mutuamente tras
 * completar una transacción. Estas valoraciones utilizan pictogramas
 * ARASAAC para favorecer la accesibilidad cognitiva.
 *
 * @see Valoracion
 * @see AppDatabase
 */
@Dao
interface ValoracionDao {

    /**
     * Inserta una nueva valoración en la base de datos.
     *
     * @param valoracion Objeto [Valoracion] a insertar.
     * @return El `rowId` generado por SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(valoracion: Valoracion): Long

    /**
     * Obtiene todas las valoraciones recibidas por un usuario,
     * ordenadas de más reciente a más antigua.
     *
     * Utilizado en la pantalla de perfil para mostrar la reputación.
     *
     * @param usuarioId ID del usuario valorado.
     * @return [Flow] reactivo con la lista de valoraciones recibidas.
     */
    @Query("SELECT * FROM valoracion WHERE id_valorado_fk = :usuarioId ORDER BY timestamp DESC")
    fun getByValorado(usuarioId: Int): Flow<List<Valoracion>>

    /**
     * Obtiene la valoración asociada a una transacción concreta (puntual).
     *
     * Útil para comprobar si ya se ha valorado una transacción.
     *
     * @param transaccionId ID de la transacción.
     * @return La [Valoracion] asociada o `null` si aún no se ha valorado.
     */
    @Query("SELECT * FROM valoracion WHERE id_transaccion_fk = :transaccionId")
    suspend fun getByTransaccion(transaccionId: Int): Valoracion?
}