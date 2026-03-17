package com.example.vecindapp.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.vecindapp.domain.model.NivelVecino

@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "barrio")
    val barrio: String,

    @ColumnInfo(name = "avatar_path") //**Implementar en vías futuras
    val avatarPath: String? = null,

    @ColumnInfo(name = "saldo_horas")
    val saldoHoras: Double = 5.0, // Saldo inicial de bienvenida

    @ColumnInfo(name = "intercambios_total")
    val intercambiosTotal: Int = 0,

    @ColumnInfo(name = "nivel")
    val nivel: NivelVecino = NivelVecino.NOVATO,

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    /**
     * Recalcula el nivel según los intercambios acumulados.
     */
    fun calcularNivel(): NivelVecino = when {
        intercambiosTotal >= 8 -> NivelVecino.REFERENTE
        intercambiosTotal >= 6 -> NivelVecino.VETERANO
        intercambiosTotal >= 3  -> NivelVecino.ACTIVO
        else                    -> NivelVecino.NOVATO
    }
}

