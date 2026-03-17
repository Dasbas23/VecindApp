package com.example.vecindapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vecindapp.data.entities.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario): Long

    @Update
    suspend fun update(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    fun getById(id: Int): Flow<Usuario?>

    @Query("SELECT * FROM usuario")
    fun getAll(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    suspend fun getByIdOnce(id: Int): Usuario?
}
