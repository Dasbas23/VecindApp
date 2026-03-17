package com.example.vecindapp

import android.app.Application
import com.example.vecindapp.data.db.AppDatabase
import com.example.vecindapp.data.repository.ServicioRepositoryImpl
import com.example.vecindapp.data.repository.TransaccionRepositoryImpl
import com.example.vecindapp.data.repository.UsuarioRepositoryImpl
import com.example.vecindapp.data.repository.ValoracionRepositoryImpl
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.TransaccionRepository
import com.example.vecindapp.domain.repository.UsuarioRepository
import com.example.vecindapp.domain.repository.ValoracionRepository

/**
 * Clase Application de VecindApp.
 *
 * Actúa como contenedor central de dependencias (inyección manual).
 * Se instancia una sola vez al arrancar la app, antes que cualquier
 * Activity o Fragment, y proporciona acceso global a la base de datos
 * y a los repositorios.
 *
 * ## ¿Por qué no Hilt/Dagger?
 * Para simplificar el TFG se ha optado por inyección manual. Esta clase
 * centraliza la creación de objetos para evitar instancias duplicadas
 * de la BBDD (que provocarían errores de concurrencia).
 *
 * ## Uso desde cualquier Fragment
 * ```kotlin
 * val app = requireActivity().application as VecindAppApplication
 * val repo = app.servicioRepository
 * ```
 *
 * ## Registro obligatorio
 * Debe declararse en `AndroidManifest.xml`:
 * ```xml
 * <application android:name=".VecindAppApplication" ... />
 * ```
 *
 * @see AppDatabase
 */
class VecindAppApplication : Application() {

    /** Instancia única de la base de datos Room. */
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    /** Repositorio de usuarios (Singleton). */
    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepositoryImpl(database.usuarioDao())
    }

    /** Repositorio de servicios (Singleton). */
    val servicioRepository: ServicioRepository by lazy {
        ServicioRepositoryImpl(database.servicioDao())
    }

    /** Repositorio de transacciones (Singleton). */
    val transaccionRepository: TransaccionRepository by lazy {
        TransaccionRepositoryImpl(database.transaccionDao())
    }

    /** Repositorio de valoraciones (Singleton). */
    val valoracionRepository: ValoracionRepository by lazy {
        ValoracionRepositoryImpl(database.valoracionDao())
    }
}