package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vecindapp.data.entities.Valoracion
import kotlinx.coroutines.flow.Flow

@Dao
interface ValoracionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(valoracion: Valoracion): Long

    @Query("SELECT * FROM valoracion WHERE id_valorado_fk = :usuarioId ORDER BY timestamp DESC")
    fun getByValorado(usuarioId: Int): Flow<List<Valoracion>>

    @Query("SELECT * FROM valoracion WHERE id_transaccion_fk = :transaccionId")
    suspend fun getByTransaccion(transaccionId: Int): Valoracion?
}
