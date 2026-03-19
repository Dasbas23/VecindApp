package com.example.vecindapp.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Callback que se ejecuta una única vez al crear la base de datos.
 *
 * Inserta datos de prueba para desarrollo: dos usuarios vecinos para
 * poder probar el flujo completo de transacciones (solicitar, aceptar,
 * completar).
 *
 * - **Usuario 1** (id=1): "Vecino Demo" — el usuario actual de la app.
 * - **Usuario 2** (id=2): "María López" — vecina que publica servicios.
 *
 * Se eliminará o sustituirá cuando se implemente el flujo real de registro.
 *
 * @see AppDatabase
 */
class SeedDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            // Usuario 1: el "yo" de la app durante desarrollo
            db.execSQL(
                """
                INSERT INTO usuario (nombre, barrio, saldo_horas, intercambios_total, nivel, fecha_registro)
                VALUES ('Vecino Demo', 'Centro', 10.0, 0, 'NOVATO', ${System.currentTimeMillis()})
                """.trimIndent()
            )
            // Usuario 2: vecina para probar transacciones
            db.execSQL(
                """
                INSERT INTO usuario (nombre, barrio, saldo_horas, intercambios_total, nivel, fecha_registro)
                VALUES ('María López', 'Ensanche', 8.0, 3, 'NOVATO', ${System.currentTimeMillis()})
                """.trimIndent()
            )
            // Servicio de prueba de María (para que usuario 1 pueda solicitarlo)
            db.execSQL(
                """
                INSERT INTO servicio (id_usuario_fk, titulo, descripcion, categoria, pictograma_id, coste_horas, estado, fecha_publicacion)
                VALUES (2, 'Clases de inglés', 'Conversación nivel intermedio, 1h por sesión', 'EDUCACION', 'educacion', 1.5, 'ACTIVO', ${System.currentTimeMillis()})
                """.trimIndent()
            )
        }
    }
}