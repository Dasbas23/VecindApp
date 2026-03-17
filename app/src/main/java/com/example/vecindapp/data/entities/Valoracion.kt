package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "valoracion",
    foreignKeys = [
        ForeignKey(
            entity = Transaccion::class,
            parentColumns = ["id_transaccion"],
            childColumns = ["id_transaccion_fk"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_valorador_fk"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_valorado_fk"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_transaccion_fk"]),
        Index(value = ["id_valorador_fk"]),
        Index(value = ["id_valorado_fk"])
    ]
)
data class Valoracion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_valoracion")
    val idValoracion: Int = 0,

    @ColumnInfo(name = "id_transaccion_fk")
    val idTransaccionFk: Int,

    @ColumnInfo(name = "id_valorador_fk")
    val idValoradorFk: Int,

    @ColumnInfo(name = "id_valorado_fk")
    val idValoradoFk: Int,

    @ColumnInfo(name = "pictogramas_json")
    val pictogramasJson: String, // JSON con los pictogramas ARASAAC seleccionados

    @ColumnInfo(name = "comentario")
    val comentario: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
