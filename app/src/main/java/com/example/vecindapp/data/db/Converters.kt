package com.example.vecindapp.data.db

import androidx.room.TypeConverter
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.model.NivelVecino

/**
 * Conversores para que Room pueda almacenar enums como String en SQLite.
 */
class Converters {

    // NivelVecino
    @TypeConverter
    fun fromNivelVecino(valor: NivelVecino): String = valor.name

    @TypeConverter
    fun toNivelVecino(valor: String): NivelVecino = NivelVecino.valueOf(valor)

    // CategoriaServicio
    @TypeConverter
    fun fromCategoriaServicio(valor: CategoriaServicio): String = valor.name

    @TypeConverter
    fun toCategoriaServicio(valor: String): CategoriaServicio = CategoriaServicio.valueOf(valor)

    // EstadoServicio
    @TypeConverter
    fun fromEstadoServicio(valor: EstadoServicio): String = valor.name

    @TypeConverter
    fun toEstadoServicio(valor: String): EstadoServicio = EstadoServicio.valueOf(valor)

    // EstadoTransaccion
    @TypeConverter
    fun fromEstadoTransaccion(valor: EstadoTransaccion): String = valor.name

    @TypeConverter
    fun toEstadoTransaccion(valor: String): EstadoTransaccion = EstadoTransaccion.valueOf(valor)
}