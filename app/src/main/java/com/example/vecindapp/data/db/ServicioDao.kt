package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vecindapp.data.entities.Servicio
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(servicio: Servicio): Long

    @Update
    suspend fun update(servicio: Servicio)

    @Delete
    suspend fun delete(servicio: Servicio)

    @Query("SELECT * FROM servicio WHERE id_servicio = :id")
    fun getById(id: Int): Flow<Servicio?>

    @Query("SELECT * FROM servicio WHERE estado = 'ACTIVO' ORDER BY fecha_publicacion DESC")
    fun getActivos(): Flow<List<Servicio>>

    @Query("SELECT * FROM servicio WHERE id_usuario_fk = :usuarioId ORDER BY fecha_publicacion DESC")
    fun getByUsuario(usuarioId: Int): Flow<List<Servicio>>

    @Query("SELECT * FROM servicio WHERE categoria = :categoria AND estado = 'ACTIVO' ORDER BY fecha_publicacion DESC")
    fun getByCategoria(categoria: String): Flow<List<Servicio>>
}
