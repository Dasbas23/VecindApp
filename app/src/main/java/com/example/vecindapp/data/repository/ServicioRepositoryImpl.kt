package com.example.vecindapp.data.repository

import com.example.vecindapp.data.db.ServicioDao
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.domain.repository.ServicioRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [ServicioRepository].
 *
 * Delega todas las operaciones al [ServicioDao] de Room. Esta clase es
 * el único punto de contacto entre la capa de dominio y la base de datos
 * para la tabla `servicio`.
 *
 * @property servicioDao DAO inyectado para acceder a la tabla `servicio`.
 *
 * @see ServicioRepository
 * @see ServicioDao
 */
class ServicioRepositoryImpl(
    private val servicioDao: ServicioDao
) : ServicioRepository {

    override suspend fun insert(servicio: Servicio): Long =
        servicioDao.insert(servicio)

    override suspend fun update(servicio: Servicio) =
        servicioDao.update(servicio)

    override suspend fun delete(servicio: Servicio) =
        servicioDao.delete(servicio)

    override fun getById(id: Int): Flow<Servicio?> =
        servicioDao.getById(id)

    override fun getActivos(): Flow<List<Servicio>> =
        servicioDao.getActivos()

    override fun getByUsuario(usuarioId: Int): Flow<List<Servicio>> =
        servicioDao.getByUsuario(usuarioId)

    override fun getByCategoria(categoria: String): Flow<List<Servicio>> =
        servicioDao.getByCategoria(categoria)

    override suspend fun cambiarEstado(id: Int, estado: String) =
        servicioDao.cambiarEstado(id, estado)
}
