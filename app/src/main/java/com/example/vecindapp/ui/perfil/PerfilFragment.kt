package com.example.vecindapp.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vecindapp.R

/**
 * Fragment placeholder para la pantalla de perfil del vecino.
 *
 * Se implementará en un sprint posterior con los datos del usuario,
 * su nivel, saldo de horas y lista de servicios publicados.
 */
class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }
}