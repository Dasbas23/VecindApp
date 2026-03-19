package com.example.vecindapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.data.entities.Valoracion

/**
 * Base de datos principal de VecindApp.
 *
 * Utiliza Room (capa de abstracción sobre SQLite) para gestionar las 4 tablas
 * de la aplicación de forma local y offline-first. No existe servidor remoto;
 * toda la persistencia es local en el dispositivo.
 *
 * ## Tablas registradas
 * - [Usuario]     — Vecinos registrados en la comunidad.
 * - [Servicio]    — Servicios publicados en el escaparate.
 * - [Transaccion] — Intercambios de horas entre vecinos.
 * - [Valoracion]  — Valoraciones con pictogramas ARASAAC.
 *
 * ## Patrón Singleton
 * Se utiliza el patrón Singleton con `@Volatile` + `synchronized` para
 * garantizar que solo exista una instancia de la BBDD en toda la app,
 * evitando problemas de concurrencia.
 *
 * ## Migraciones
 * Durante el desarrollo se usa [fallbackToDestructiveMigration], que borra
 * y recrea la BBDD al cambiar la versión. En producción habrá que definir
 * migraciones manuales con `addMigrations(...)`.
 *
 * @see Converters
 */
@Database(
    entities = [Usuario::class, Servicio::class, Transaccion::class, Valoracion::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /** Devuelve el DAO para operar con la tabla `usuario`. */
    abstract fun usuarioDao(): UsuarioDao

    /** Devuelve el DAO para operar con la tabla `servicio`. */
    abstract fun servicioDao(): ServicioDao

    /** Devuelve el DAO para operar con la tabla `transaccion`. */
    abstract fun transaccionDao(): TransaccionDao

    /** Devuelve el DAO para operar con la tabla `valoracion`. */
    abstract fun valoracionDao(): ValoracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos.
         *
         * Si no existe, la crea utilizando [Room.databaseBuilder].
         * El nombre del fichero SQLite es `vecindapp_db`.
         *
         * @param context Contexto de la aplicación (se usa `applicationContext`
         *                para evitar memory leaks).
         * @return Instancia singleton de [AppDatabase].
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vecindapp_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}