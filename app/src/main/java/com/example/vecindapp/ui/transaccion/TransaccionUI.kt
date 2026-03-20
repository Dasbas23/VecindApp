package com.example.vecindapp.ui.transaccion

import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.model.EstadoTransaccion

/**
 * Modelo de presentación para una transacción en el RecyclerView.
 *
 * Enriquece los datos de la [Transaccion] con información que
 * necesita la UI pero que no está directamente en la entidad
 * (como el título del servicio o el rol del usuario actual).
 *
 * @property transaccion   Entidad original de Room.
 * @property tituloServicio Título del servicio asociado.
 * @property rol           "COMPRADOR" o "VENDEDOR" según el usuario actual.
 * @property puedeAceptar  `true` si el usuario puede aceptar esta transacción.
 * @property puedeCompletar `true` si el usuario puede completar esta transacción.
 * @property puedeCancelar  `true` si el usuario puede cancelar esta transacción.
 */
data class TransaccionUI(
    val transaccion: Transaccion,
    val tituloServicio: String,
    val rol: String,
    val puedeAceptar: Boolean,
    val puedeCompletar: Boolean,
    val puedeCancelar: Boolean
) {
    val id: Int get() = transaccion.idTransaccion
    val estado: EstadoTransaccion get() = transaccion.estado
    val horas: Double get() = transaccion.horasTransferidas
    val timestamp: Long get() = transaccion.timestamp
}