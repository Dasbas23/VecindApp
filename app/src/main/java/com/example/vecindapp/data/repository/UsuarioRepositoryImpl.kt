package com.example.vecindapp.data.repository

import com.example.vecindapp.data.db.UsuarioDao
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación concreta de [UsuarioRepository].
 *
 * Delega todas las operaciones al [UsuarioDao] de Room. Esta clase es
 * el único punto de contacto entre la capa de dominio y la base de datos
 * para la tabla `usuario`.
 *
 * En el futuro, si se añadiese un servidor remoto, esta clase orquestaría
 * la lógica de caché local vs. petición de red, sin que el ViewModel
 * tenga que cambiar nada.
 *
 * @property usuarioDao DAO inyectado para acceder a la tabla `usuario`.
 *
 * @see UsuarioRepository
 * @see UsuarioDao
 */
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {

    override suspend fun insert(usuario: Usuario): Long =
        usuarioDao.insert(usuario)

    override suspend fun update(usuario: Usuario) =
        usuarioDao.update(usuario)

    override fun getById(id: Int): Flow<Usuario?> =
        usuarioDao.getById(id)

    override fun getAll(): Flow<List<Usuario>> =
        usuarioDao.getAll()

    override suspend fun getByIdOnce(id: Int): Usuario? =
        usuarioDao.getByIdOnce(id)
}
