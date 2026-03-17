package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa la valoración de un vecino tras una transacción.
 *
 * Después de completar una [Transaccion], ambas partes pueden valorarse
 * mutuamente. La valoración utiliza pictogramas ARASAAC (almacenados como
 * JSON) en lugar de estrellas numéricas, priorizando la accesibilidad
 * cognitiva de la aplicación.
 *
 * Tabla SQLite: `valoracion`
 *
 * Relaciones:
 * - [idTransaccionFk] → [Transaccion.idTransaccion] (transacción valorada).
 * - [idValoradorFk]   → [Usuario.idUsuario] (vecino que emite la valoración).
 * - [idValoradoFk]    → [Usuario.idUsuario] (vecino que recibe la valoración).
 * - Todas con `ON DELETE CASCADE`.
 *
 * @property idValoracion     Clave primaria autoincremental.
 * @property idTransaccionFk  FK → [Transaccion]. Transacción a la que pertenece esta valoración.
 * @property idValoradorFk    FK → [Usuario]. Vecino que valora.
 * @property idValoradoFk     FK → [Usuario]. Vecino valorado.
 * @property pictogramasJson  JSON con los identificadores de pictogramas ARASAAC seleccionados.
 * @property comentario       Texto libre opcional para complementar los pictogramas.
 * @property timestamp        Timestamp (millis) de creación de la valoración.
 *
 * @see com.example.vecindapp.data.db.ValoracionDao
 */
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
    val pictogramasJson: String,

    @ColumnInfo(name = "comentario")
    val comentario: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)