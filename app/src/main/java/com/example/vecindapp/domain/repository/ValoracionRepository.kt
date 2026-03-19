package com.example.vecindapp.domain.repository

import com.example.vecindapp.data.entities.Valoracion
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de valoraciones.
 *
 * Define las operaciones sobre la tabla `valoracion`. Las valoraciones
 * utilizan pictogramas ARASAAC como sistema de puntuación accesible.
 *
 * @see com.example.vecindapp.data.repository.ValoracionRepositoryImpl
 */
interface ValoracionRepository {

    /**
     * Inserta una nueva valoración.
     *
     * @param valoracion Objeto [Valoracion] a persistir.
     * @return El `rowId` generado por SQLite.
     */
    suspend fun insert(valoracion: Valoracion): Long

    /**
     * Obtiene todas las valoraciones recibidas por un usuario,
     * ordenadas de más reciente a más antigua.
     *
     * @param usuarioId ID del usuario valorado.
     * @return [Flow] reactivo con la lista de valoraciones recibidas.
     */
    fun getByValorado(usuarioId: Int): Flow<List<Valoracion>>

    /**
     * Obtiene la valoración asociada a una transacción concreta.
     *
     * @param transaccionId ID de la transacción.
     * @return La [Valoracion] o `null` si aún no se ha valorado.
     */
    suspend fun getByTransaccion(transaccionId: Int): Valoracion?
}
