package com.example.vecindapp.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import com.example.vecindapp.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.isVisible

/**
 * Muestra un Snackbar anclado a la vista del fragment.
 * Si el BottomNavigationView está visible en la Activity, el Snackbar flota encima.
 */
fun Fragment.mostrarSnackbar(
    mensaje: String,
    duracion: Int = Snackbar.LENGTH_SHORT
) {
    val vista = view ?: return
    val snackbar = Snackbar.make(vista, mensaje, duracion)
    
    // Anclar encima del BottomNav si existe y está visible (MainActivity)
    activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.let { bottomNav ->
        if (bottomNav.isVisible) {
            snackbar.anchorView = bottomNav
        }
    }
    
    snackbar.show()
}

/**
 * Sobrecarga para usar strings.xml directamente (DRY).
 */
fun Fragment.mostrarSnackbar(
    mensajeResId: Int,
    duracion: Int = Snackbar.LENGTH_SHORT
) {
    mostrarSnackbar(getString(mensajeResId), duracion)
}
