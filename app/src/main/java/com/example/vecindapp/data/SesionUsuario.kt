package com.example.vecindapp.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper para gestionar la sesión del usuario activo mediante SharedPreferences.
 *
 * Almacena el ID del usuario que ha iniciado sesión de forma persistente.
 * Si la app se cierra y se vuelve a abrir, el usuario sigue logueado.
 * Al "cerrar sesión" se borra el ID y la app muestra la pantalla de login.
 *
 * @property context Contexto de la aplicación.
 */
class SesionUsuario(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Guarda el ID del usuario que ha iniciado sesión.
     *
     * @param id Clave primaria del usuario en la tabla `usuario`.
     */
    fun guardarUsuarioId(id: Int) {
        prefs.edit().putInt(KEY_USUARIO_ID, id).apply()
    }

    /**
     * Obtiene el ID del usuario activo.
     *
     * @return El ID del usuario o [SIN_SESION] (-1) si no hay nadie logueado.
     */
    fun obtenerUsuarioId(): Int {
        return prefs.getInt(KEY_USUARIO_ID, SIN_SESION)
    }

    /**
     * Comprueba si hay un usuario con sesión activa.
     *
     * @return `true` si hay alguien logueado.
     */
    fun haySesion(): Boolean {
        return obtenerUsuarioId() != SIN_SESION
    }

    /**
     * Cierra la sesión eliminando el ID guardado.
     */
    fun cerrarSesion() {
        prefs.edit().remove(KEY_USUARIO_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "vecindapp_sesion"
        private const val KEY_USUARIO_ID = "usuario_id"
        const val SIN_SESION = -1
    }
}