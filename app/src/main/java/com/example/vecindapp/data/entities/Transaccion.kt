package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vecindapp.domain.model.EstadoTransaccion

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
    fun estaCompletada(): Boolean = estado == EstadoTransaccion.COMPLETADA
}
