package com.example.vecindapp.domain.repository

import com.example.vecindapp.data.entities.Transaccion
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de transacciones.
 *
 * Define las operaciones sobre la tabla `transaccion`. La lógica atómica
 * de débito/crédito de horas se implementa en
 * [com.example.vecindapp.data.repository.TransaccionRepositoryImpl]
 * utilizando `@Transaction` de Room.
 *
 * @see com.example.vecindapp.data.repository.TransaccionRepositoryImpl
 */
interface TransaccionRepository {

    /**
     * Inserta una nueva transacción.
     *
     * @param transaccion Objeto [Transaccion] a persistir.
     * @return El `rowId` generado por SQLite.
     */
    suspend fun insert(transaccion: Transaccion): Long

    /**
     * Actualiza una transacción existente (cambio de estado, por ejemplo).
     *
     * @param transaccion Objeto [Transaccion] con los campos modificados.
     */
    suspend fun update(transaccion: Transaccion)

    /**
     * Obtiene todas las transacciones de un usuario (como comprador o vendedor),
     * ordenadas de más reciente a más antigua.
     *
     * @param usuarioId ID del usuario.
     * @return [Flow] reactivo con la lista de transacciones.
     */
    fun getByUsuario(usuarioId: Int): Flow<List<Transaccion>>

    /**
     * Obtiene la transacción asociada a un servicio concreto.
     *
     * @param servicioId ID del servicio.
     * @return La [Transaccion] o `null` si no existe.
     */
    suspend fun getByServicio(servicioId: Int): Transaccion?

    /**
     * Obtiene una transacción por su ID de forma puntual.
     *
     * @param id Clave primaria de la transacción.
     * @return La [Transaccion] o `null` si no existe.
     */
    suspend fun getByIdOnce(id: Int): Transaccion?
}
