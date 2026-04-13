package com.example.vecindapp.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import com.example.vecindapp.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment

/**
 * Muestra un Snackbar anclado a la vista del fragment.
 * Si el BottomNavigationView está visible en la Activity, el Snackbar flota encima.
 */
fun Fragment.mostrarSnackbar(
    mensaje: String,
    duracion: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) {
    val vista = view ?: return

    // En un DialogFragment usamos la propia vista del fragment (el CoordinatorLayout interno
    // del layout del sheet) para que el Snackbar se muestre dentro del sheet.
    val snackbar = Snackbar.make(vista, mensaje, duracion)

    when {
        // Ancla explícita tiene prioridad.
        anchorView != null -> snackbar.anchorView = anchorView
        // Solo anclamos al BottomNav si NO estamos en un diálogo.
        this !is DialogFragment -> {
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.let { bottomNav ->
                if (bottomNav.isVisible) {
                    snackbar.anchorView = bottomNav
                }
            }
        }
    }

    snackbar.show()
}

/**
 * Sobrecarga para usar strings.xml directamente (DRY).
 */
fun Fragment.mostrarSnackbar(
    mensajeResId: Int,
    duracion: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) {
    mostrarSnackbar(getString(mensajeResId), duracion, anchorView)
}
