package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio

/**
 * Entidad Room que representa un servicio publicado en el escaparate.
 *
 * Un servicio es la unidad básica de intercambio: un vecino ofrece su tiempo
 * en una categoría concreta y establece un coste en horas. Otros vecinos
 * pueden solicitarlo desde el escaparate, generando una [Transaccion].
 *
 * Tabla SQLite: `servicio`
 *
 * Relaciones:
 * - Cada servicio pertenece a un único [Usuario] (FK: [idUsuarioFk]).
 * - `ON DELETE CASCADE`: si se elimina el usuario, sus servicios desaparecen.
 *
 * @property idServicio       Clave primaria autoincremental.
 * @property idUsuarioFk      FK → [Usuario.idUsuario]. Vecino que publica el servicio.
 * @property titulo           Título corto visible en la tarjeta del escaparate.
 * @property descripcion      Descripción detallada (opcional).
 * @property categoria        Categoría del servicio para filtrado. --Implementar en vías futuras--
 * @property pictogramaId     Identificador del pictograma ARASAAC asociado.
 * @property costeHoras       Precio en horas que se debitará al comprador.
 * @property estado           Estado actual dentro de su ciclo de vida.
 * @property fechaPublicacion Timestamp (millis) de creación del servicio.
 * @property fechaCaducidad   Timestamp (millis) opcional de expiración automática.
 *
 * @see CategoriaServicio
 * @see EstadoServicio
 * @see com.example.vecindapp.data.db.ServicioDao
 */
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

    @ColumnInfo(name = "categoria")
    val categoria: CategoriaServicio,

    @ColumnInfo(name = "pictograma_id")
    val pictogramaId: String,

    @ColumnInfo(name = "coste_horas")
    val costeHoras: Double,

    @ColumnInfo(name = "estado")
    val estado: EstadoServicio = EstadoServicio.ACTIVO,

    @ColumnInfo(name = "fecha_publicacion")
    val fechaPublicacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "fecha_caducidad")
    val fechaCaducidad: Long? = null
) {
    /**
     * Comprueba si el servicio está actualmente disponible en el escaparate.
     *
     * @return `true` si el estado es [EstadoServicio.ACTIVO].
     */
    fun estaActivo(): Boolean = estado == EstadoServicio.ACTIVO

    /**
     * Comprueba si el servicio ha superado su fecha de caducidad.
     *
     * @return `true` si [fechaCaducidad] no es null y ya ha pasado.
     */
    fun estaVencido(): Boolean =
        fechaCaducidad != null && System.currentTimeMillis() > fechaCaducidad
}