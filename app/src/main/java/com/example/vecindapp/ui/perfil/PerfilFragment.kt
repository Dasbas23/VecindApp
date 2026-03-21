package com.example.vecindapp.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.ui.escaparate.ServicioAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

/**
 * Fragment de perfil del vecino.
 *
 * Muestra los datos del usuario actual (nombre, barrio, avatar),
 * sus estadísticas (saldo de horas, nivel, intercambios totales)
 * y la lista de servicios que ha publicado.
 *
 * Reutiliza el [ServicioAdapter] del escaparate para "Mis servicios",
 * ya que las tarjetas tienen el mismo aspecto visual.
 *
 * ## Reactividad
 * Todos los datos se observan mediante [StateFlow]. Cuando se completa
 * una transacción y el saldo cambia, el perfil se actualiza solo sin
 * tener que recargar la pantalla.
 *
 * @see PerfilViewModel
 */
class PerfilFragment : Fragment() {

    private val viewModel: PerfilViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        PerfilViewModel.Factory(app.usuarioRepository, app.servicioRepository)
    }

    private lateinit var ivAvatar: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvBarrio: TextView
    private lateinit var tvSaldoHoras: TextView
    private lateinit var tvNivel: TextView
    private lateinit var tvIntercambios: TextView
    private lateinit var rvMisServicios: RecyclerView
    private lateinit var tvVacioMisServicios: TextView

    private lateinit var servicioAdapter: ServicioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVistas(view)
        configurarRecyclerView()
        observarUsuario()
        observarMisServicios()
    }

    private fun configurarVistas(view: View) {
        ivAvatar = view.findViewById(R.id.ivAvatar)
        tvNombre = view.findViewById(R.id.tvNombre)
        tvBarrio = view.findViewById(R.id.tvBarrio)
        tvSaldoHoras = view.findViewById(R.id.tvSaldoHoras)
        tvNivel = view.findViewById(R.id.tvNivel)
        tvIntercambios = view.findViewById(R.id.tvIntercambios)
        rvMisServicios = view.findViewById(R.id.rvMisServicios)
        tvVacioMisServicios = view.findViewById(R.id.tvVacioMisServicios)
        val btnCerrarSesion = view.findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            SesionUsuario(requireContext()).cerrarSesion()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    /**
     * Configura el RecyclerView de "Mis servicios" reutilizando
     * el [ServicioAdapter] del escaparate.
     */
    private fun configurarRecyclerView() {
        servicioAdapter = ServicioAdapter { servicio ->
            // TODO: Navegar al detalle del servicio desde el perfil
            // Requiere añadir action en nav_graph desde perfil a detalle
        }
        rvMisServicios.layoutManager = LinearLayoutManager(requireContext())
        rvMisServicios.adapter = servicioAdapter
    }

    /**
     * Observa los datos del usuario y actualiza la cabecera del perfil.
     */
    private fun observarUsuario() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usuario.collect { usuario ->
                    usuario?.let { pintarPerfil(it) }
                }
            }
        }
    }

    /**
     * Rellena las vistas de la cabecera con los datos del usuario.
     */
    private fun pintarPerfil(usuario: Usuario) {
        tvNombre.text = usuario.nombre
        tvBarrio.text = usuario.barrio
        tvSaldoHoras.text = String.format("%.1f", usuario.saldoHoras)
        tvNivel.text = usuario.nivel.name
        tvIntercambios.text = usuario.intercambiosTotal.toString()

        // TODO: Cargar avatar con Glide
        // Glide.with(this)
        //     .load(usuario.avatarPath)
        //     .placeholder(R.drawable.ic_avatar_default)
        //     .circleCrop()
        //     .into(ivAvatar)
    }

    /**
     * Observa la lista de servicios publicados por el usuario.
     */
    private fun observarMisServicios() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.misServicios.collect { lista ->
                    servicioAdapter.submitList(lista.toMutableList())
                    tvVacioMisServicios.visibility =
                        if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}