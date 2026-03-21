package com.example.vecindapp.ui.transaccion

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.data.SesionUsuario
import kotlinx.coroutines.launch

/**
 * Fragment que muestra la lista de transacciones del usuario actual.
 *
 * Muestra las transacciones donde el usuario participa como comprador
 * o vendedor, con botones de acción según el contexto (aceptar,
 * completar, cancelar). La lista se actualiza reactivamente.
 *
 * @see TransaccionViewModel
 * @see TransaccionAdapter
 */
class TransaccionFragment : Fragment() {

    private val viewModel: TransaccionViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        val sesion = SesionUsuario(requireContext())
        TransaccionViewModel.Factory(
            app.transaccionRepository,
            app.servicioRepository,
            app.usuarioRepository,
            sesion.obtenerUsuarioId()
        )
    }

    private lateinit var adapter: TransaccionAdapter
    private lateinit var rvTransacciones: RecyclerView
    private lateinit var tvVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transacciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistas(view)
        configurarRecyclerView()
        observarTransacciones()
        observarMensajes()
    }

    private fun configurarVistas(view: View) {
        rvTransacciones = view.findViewById(R.id.rvTransacciones)
        tvVacio = view.findViewById(R.id.tvVacioTransacciones)
    }

    private fun configurarRecyclerView() {
        adapter = TransaccionAdapter(
            onAceptar = { item ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.titulo_confirmar_aceptar)
                    .setMessage(R.string.mensaje_confirmar_aceptar)
                    .setPositiveButton(R.string.btn_aceptar) { _, _ ->
                        viewModel.aceptarTransaccion(item)
                    }
                    .setNegativeButton(R.string.btn_cancelar, null)
                    .show()
            },
            onCompletar = { item ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.titulo_confirmar_completar)
                    .setMessage(
                        getString(R.string.mensaje_confirmar_completar, item.horas)
                    )
                    .setPositiveButton(R.string.btn_completar) { _, _ ->
                        viewModel.completarTransaccion(item)
                    }
                    .setNegativeButton(R.string.btn_cancelar, null)
                    .show()
            },
            onCancelar = { item ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.titulo_confirmar_cancelar_transaccion)
                    .setMessage(R.string.mensaje_confirmar_cancelar_transaccion)
                    .setPositiveButton(R.string.btn_confirmar_cancelar) { _, _ ->
                        viewModel.cancelarTransaccion(item)
                    }
                    .setNegativeButton(R.string.btn_cancelar, null)
                    .show()
            }
        )

        rvTransacciones.layoutManager = LinearLayoutManager(requireContext())
        rvTransacciones.adapter = adapter
    }

    /**
     * Observa las transacciones y actualiza la UI.
     */
    private fun observarTransacciones() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transacciones.collect { lista ->
                    adapter.submitList(lista.toMutableList())
                    tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    /**
     * Observa mensajes de feedback del ViewModel.
     */
    private fun observarMensajes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mensaje.collect { mensaje ->
                    if (mensaje != null) {
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        viewModel.limpiarMensaje()
                    }
                }
            }
        }
    }
}