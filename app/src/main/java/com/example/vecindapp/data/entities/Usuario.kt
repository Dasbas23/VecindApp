package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.vecindapp.domain.model.NivelVecino

/**
 * Entidad Room que representa a un vecino registrado en la comunidad.
 *
 * Cada usuario dispone de un saldo de horas que puede gastar solicitando
 * servicios o ganar ofreciéndolos. El [nivel] se recalcula automáticamente
 * en función de los intercambios completados.
 *
 * Tabla SQLite: `usuario`
 *
 * @property idUsuario    Clave primaria autoincremental.
 * @property nombre       Nombre visible del vecino en la comunidad.
 * @property barrio       Barrio o zona al que pertenece.
 * @property avatarPath   Ruta local de la imagen de perfil (nullable si no tiene).
 * @property saldoHoras   Horas disponibles para gastar. Se inicia con 5.0 h de bienvenida.
 * @property intercambiosTotal Número acumulado de transacciones completadas.
 * @property nivel        Nivel actual calculado a partir de [intercambiosTotal].
 * @property fechaRegistro Timestamp (millis) del momento de alta en la app.
 *
 * @see NivelVecino
 * @see com.example.vecindapp.data.db.UsuarioDao
 */
@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "barrio")
    val barrio: String,

    @ColumnInfo(name = "avatar_path")
    val avatarPath: String? = null,

    @ColumnInfo(name = "saldo_horas")
    val saldoHoras: Double = 5.0,

    @ColumnInfo(name = "intercambios_total")
    val intercambiosTotal: Int = 0,

    @ColumnInfo(name = "nivel")
    val nivel: NivelVecino = NivelVecino.NOVATO,

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    /**
     * Recalcula el [NivelVecino] según los intercambios acumulados.
     *
     * | Rango            | Nivel       |
     * |------------------|-------------|
     * | 0 – 2            | NOVATO      |
     * | 3 – 5            | ACTIVO      |
     * | 6 – 8            | VETERANO    |
     * | 9 +              | REFERENTE   |
     *
     * @return El [NivelVecino] correspondiente al número actual de intercambios.
     */
    fun calcularNivel(): NivelVecino = when {
        intercambiosTotal >= 9 -> NivelVecino.REFERENTE
        intercambiosTotal >= 6 -> NivelVecino.VETERANO
        intercambiosTotal >= 3 -> NivelVecino.ACTIVO
        else                   -> NivelVecino.NOVATO
    }
}