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

/**
 * Muestra un Snackbar usando la vista raíz de la Activity como base, anclado al
 * BottomNavigationView si está visible. Pensado para mostrarlo JUSTO ANTES de un
 * popBackStack(): al vivir en la Activity, sobrevive a la destrucción del fragment.
 */
fun Fragment.mostrarSnackbarGlobal(
    mensaje: String,
    duracion: Int = Snackbar.LENGTH_SHORT
) {
    val act = activity ?: return
    val root = act.findViewById<View>(android.R.id.content) ?: return
    val snackbar = Snackbar.make(root, mensaje, duracion)
    act.findViewById<BottomNavigationView>(R.id.bottomNav)?.let { bottomNav ->
        if (bottomNav.isVisible) snackbar.anchorView = bottomNav
    }
    snackbar.show()
}

/** Sobrecarga con resource id. */
fun Fragment.mostrarSnackbarGlobal(
    mensajeResId: Int,
    duracion: Int = Snackbar.LENGTH_SHORT
) {
    mostrarSnackbarGlobal(getString(mensajeResId), duracion)
}
