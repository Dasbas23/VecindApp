package com.example.vecindapp.data.db

import androidx.room.TypeConverter
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.model.NivelVecino

/**
 * Conversores de tipos para Room.
 *
 * Room/SQLite solo admite tipos primitivos (Int, String, Long, Double, etc.).
 * Esta clase le enseña a Room cómo transformar los enums del dominio a [String]
 * para guardarlos y cómo reconstruirlos al leer de la BBDD.
 *
 * Se registra en [AppDatabase] mediante la anotación `@TypeConverters`.
 *
 * @see NivelVecino
 * @see CategoriaServicio
 * @see EstadoServicio
 * @see EstadoTransaccion
 */
class Converters {

    // ── NivelVecino ──────────────────────────────────────────

    /** Convierte [NivelVecino] → [String] para almacenar en SQLite. */
    @TypeConverter
    fun fromNivelVecino(valor: NivelVecino): String = valor.name

    /** Convierte [String] → [NivelVecino] al leer de SQLite. */
    @TypeConverter
    fun toNivelVecino(valor: String): NivelVecino = NivelVecino.valueOf(valor)

    // ── CategoriaServicio ────────────────────────────────────

    /** Convierte [CategoriaServicio] → [String] para almacenar en SQLite. */
    @TypeConverter
    fun fromCategoriaServicio(valor: CategoriaServicio): String = valor.name

    /** Convierte [String] → [CategoriaServicio] al leer de SQLite. */
    @TypeConverter
    fun toCategoriaServicio(valor: String): CategoriaServicio = CategoriaServicio.valueOf(valor)

    // ── EstadoServicio ───────────────────────────────────────

    /** Convierte [EstadoServicio] → [String] para almacenar en SQLite. */
    @TypeConverter
    fun fromEstadoServicio(valor: EstadoServicio): String = valor.name

    /** Convierte [String] → [EstadoServicio] al leer de SQLite. */
    @TypeConverter
    fun toEstadoServicio(valor: String): EstadoServicio = EstadoServicio.valueOf(valor)

    // ── EstadoTransaccion ────────────────────────────────────

    /** Convierte [EstadoTransaccion] → [String] para almacenar en SQLite. */
    @TypeConverter
    fun fromEstadoTransaccion(valor: EstadoTransaccion): String = valor.name

    /** Convierte [String] → [EstadoTransaccion] al leer de SQLite. */
    @TypeConverter
    fun toEstadoTransaccion(valor: String): EstadoTransaccion = EstadoTransaccion.valueOf(valor)
}