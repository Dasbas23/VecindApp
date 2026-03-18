package com.example.vecindapp.ui.servicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vecindapp.R

/**
 * Fragment para el formulario de creación de un nuevo servicio.
 *
 * Se completará con los campos del formulario (título, descripción,
 * categoría, coste) y la lógica de guardado en Room.
 */
class CrearServicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_servicio, container, false)
    }
}