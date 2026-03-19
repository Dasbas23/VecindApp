package com.example.vecindapp.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Callback que se ejecuta una única vez al crear la base de datos por primera vez.
 *
 * Inserta un usuario de prueba para poder trabajar durante el desarrollo
 * sin necesidad de implementar aún la pantalla de registro/login.
 *
 * Se eliminará o sustituirá cuando se implemente el flujo real de registro.
 *
 * @see AppDatabase
 */
class SeedDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            db.execSQL(
                """
                INSERT INTO usuario (nombre, barrio, saldo_horas, intercambios_total, nivel, fecha_registro)
                VALUES ('Vecino Demo', 'Centro', 10.0, 0, 'NOVATO', ${System.currentTimeMillis()})
                """.trimIndent()
            )
        }
    }
}