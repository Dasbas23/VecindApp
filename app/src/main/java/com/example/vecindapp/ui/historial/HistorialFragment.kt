package com.example.vecindapp.ui.historial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vecindapp.R

/**
 * Fragment placeholder para la pantalla de historial de transacciones.
 *
 * Se implementará en un sprint posterior con un RecyclerView de
 * transacciones y un gráfico de barras (MPAndroidChart).
 */
class HistorialFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }
}