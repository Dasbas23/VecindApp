package com.example.vecindapp.data.repository

import com.example.vecindapp.data.db.TransaccionDao
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.repository.TransaccionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [TransaccionRepository].
 *
 * Delega las operaciones básicas al [TransaccionDao]. La lógica atómica
 * de débito/crédito de horas (que implica actualizar también la tabla
 * `usuario`) se implementará en el UseCase correspondiente, no aquí,
 * ya que los repositorios deben ser estancos y no llamarse entre sí.
 *
 * @property transaccionDao DAO inyectado para acceder a la tabla `transaccion`.
 *
 * @see TransaccionRepository
 * @see TransaccionDao
 */
class TransaccionRepositoryImpl(
    private val transaccionDao: TransaccionDao
) : TransaccionRepository {

    override suspend fun insert(transaccion: Transaccion): Long =
        transaccionDao.insert(transaccion)

    override suspend fun update(transaccion: Transaccion) =
        transaccionDao.update(transaccion)

    override fun getByUsuario(usuarioId: Int): Flow<List<Transaccion>> =
        transaccionDao.getByUsuario(usuarioId)

    override suspend fun getByServicio(servicioId: Int): Transaccion? =
        transaccionDao.getByServicio(servicioId)

    override suspend fun getByIdOnce(id: Int): Transaccion? =
        transaccionDao.getByIdOnce(id)
}
