package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vecindapp.domain.model.EstadoTransaccion

/**
 * Entidad Room que representa una transacción de horas entre dos vecinos.
 *
 * Se crea cuando un comprador solicita un [Servicio]. El flujo normal es:
 * [EstadoTransaccion.PENDIENTE] → [EstadoTransaccion.ACEPTADA] →
 * [EstadoTransaccion.COMPLETADA]. Al completarse, las horas se debitan
 * del comprador y se acreditan al vendedor de forma atómica (`@Transaction`).
 *
 * Tabla SQLite: `transaccion`
 *
 * Relaciones:
 * - [idCompradorFk] → [Usuario.idUsuario] (vecino que solicita el servicio).
 * - [idVendedorFk]  → [Usuario.idUsuario] (vecino que ofrece el servicio).
 * - [idServicioFk]  → [Servicio.idServicio] (servicio objeto del intercambio).
 * - Todas con `ON DELETE CASCADE`.
 *
 * @property idTransaccion    Clave primaria autoincremental.
 * @property idCompradorFk    FK → [Usuario]. Vecino que solicita y paga horas.
 * @property idVendedorFk     FK → [Usuario]. Vecino que presta el servicio y recibe horas.
 * @property idServicioFk     FK → [Servicio]. Servicio asociado a esta transacción.
 * @property horasTransferidas Cantidad de horas intercambiadas.
 * @property estado           Estado actual dentro del flujo de la transacción.
 * @property timestamp        Timestamp (millis) de creación de la transacción.
 *
 * @see EstadoTransaccion
 * @see com.example.vecindapp.data.db.TransaccionDao
 */
@Entity(
    tableName = "transaccion",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_comprador_fk"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_vendedor_fk"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Servicio::class,
            parentColumns = ["id_servicio"],
            childColumns = ["id_servicio_fk"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_comprador_fk"]),
        Index(value = ["id_vendedor_fk"]),
        Index(value = ["id_servicio_fk"])
    ]
)
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_transaccion")
    val idTransaccion: Int = 0,

    @ColumnInfo(name = "id_comprador_fk")
    val idCompradorFk: Int,

    @ColumnInfo(name = "id_vendedor_fk")
    val idVendedorFk: Int,

    @ColumnInfo(name = "id_servicio_fk")
    val idServicioFk: Int,

    @ColumnInfo(name = "horas_transferidas")
    val horasTransferidas: Double,

    @ColumnInfo(name = "estado")
    val estado: EstadoTransaccion = EstadoTransaccion.PENDIENTE,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Comprueba si la transacción ha finalizado con éxito.
     *
     * @return `true` si el estado es [EstadoTransaccion.COMPLETADA].
     */
    fun estaCompletada(): Boolean = estado == EstadoTransaccion.COMPLETADA
}