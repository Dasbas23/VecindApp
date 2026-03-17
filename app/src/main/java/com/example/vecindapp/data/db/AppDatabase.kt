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

@Database(
    entities = [Usuario::class, Servicio::class, Transaccion::class, Valoracion::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun servicioDao(): ServicioDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun valoracionDao(): ValoracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vecindapp_db"
                )
                    .fallbackToDestructiveMigration() // Solo durante desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
