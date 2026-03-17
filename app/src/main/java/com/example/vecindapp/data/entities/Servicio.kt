package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio

@Entity(
    tableName = "servicio",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario_fk"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_usuario_fk"])]
)
data class Servicio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_servicio")
    val idServicio: Int = 0,

    @ColumnInfo(name = "id_usuario_fk")
    val idUsuarioFk: Int,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "categoria") //**Implementar en vías futuras
    val categoria: CategoriaServicio,

    @ColumnInfo(name = "pictograma_id")
    val pictogramaId: String,

    @ColumnInfo(name = "coste_horas")
    val costeHoras: Double,

    @ColumnInfo(name = "estado") //Por defecto se deja en ACTIVO
    val estado: EstadoServicio = EstadoServicio.ACTIVO,

    @ColumnInfo(name = "fecha_publicacion")
    val fechaPublicacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "fecha_caducidad")
    val fechaCaducidad: Long? = null
) {
    fun estaActivo(): Boolean = estado == EstadoServicio.ACTIVO

    fun estaVencido(): Boolean =
        fechaCaducidad != null && System.currentTimeMillis() > fechaCaducidad
}
