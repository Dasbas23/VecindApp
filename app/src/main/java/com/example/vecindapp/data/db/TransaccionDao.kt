package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vecindapp.data.entities.Transaccion
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaccion: Transaccion): Long

    @Update
    suspend fun update(transaccion: Transaccion)

    @Query("SELECT * FROM transaccion WHERE id_comprador_fk = :usuarioId OR id_vendedor_fk = :usuarioId ORDER BY timestamp DESC")
    fun getByUsuario(usuarioId: Int): Flow<List<Transaccion>>

    @Query("SELECT * FROM transaccion WHERE id_servicio_fk = :servicioId")
    suspend fun getByServicio(servicioId: Int): Transaccion?

    @Query("SELECT * FROM transaccion WHERE id_transaccion = :id")
    suspend fun getByIdOnce(id: Int): Transaccion?
}
