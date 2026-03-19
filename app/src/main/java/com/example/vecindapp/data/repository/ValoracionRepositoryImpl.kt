package com.example.vecindapp.data.repository

import com.example.vecindapp.data.db.ValoracionDao
import com.example.vecindapp.data.entities.Valoracion
import com.example.vecindapp.domain.repository.ValoracionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [ValoracionRepository].
 *
 * Delega todas las operaciones al [ValoracionDao] de Room. Esta clase es
 * el único punto de contacto entre la capa de dominio y la base de datos
 * para la tabla `valoracion`.
 *
 * @property valoracionDao DAO inyectado para acceder a la tabla `valoracion`.
 *
 * @see ValoracionRepository
 * @see ValoracionDao
 */
class ValoracionRepositoryImpl(
    private val valoracionDao: ValoracionDao
) : ValoracionRepository {

    override suspend fun insert(valoracion: Valoracion): Long =
        valoracionDao.insert(valoracion)

    override fun getByValorado(usuarioId: Int): Flow<List<Valoracion>> =
        valoracionDao.getByValorado(usuarioId)

    override suspend fun getByTransaccion(transaccionId: Int): Valoracion? =
        valoracionDao.getByTransaccion(transaccionId)
}
