package com.example.vecindapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.vecindapp.R
import com.example.vecindapp.VecindAppApplication
import com.example.vecindapp.data.SesionUsuario
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Fragment de inicio de sesión.
 *
 * El usuario introduce su nombre, se busca en la BBDD, y si existe
 * se guarda su ID en [SesionUsuario] y se navega al escaparate.
 * Si no existe, se muestra un error sugiriendo registrarse.
 *
 * @see LoginViewModel
 * @see SesionUsuario
 */
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        LoginViewModel.Factory(app.database.usuarioDao())
    }

    private lateinit var etNombre: TextInputEditText
    private lateinit var btnIniciarSesion: MaterialButton
    private lateinit var btnIrRegistro: MaterialButton
    private lateinit var sesion: SesionUsuario

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sesion = SesionUsuario(requireContext())
        configurarVistas(view)
        configurarBotones()
        observarResultado()
        observarErrores()
    }

    private fun configurarVistas(view: View) {
        etNombre = view.findViewById(R.id.etNombreLogin)
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion)
        btnIrRegistro = view.findViewById(R.id.btnIrRegistro)
    }

    private fun configurarBotones() {
        btnIniciarSesion.setOnClickListener {
            viewModel.iniciarSesion(etNombre.text.toString())
        }

        btnIrRegistro.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }
    }

    /**
     * Si se encuentra el usuario, guarda la sesión y navega al escaparate.
     */
    private fun observarResultado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usuarioEncontrado.collect { userId ->
                    if (userId != null) {
                        sesion.guardarUsuarioId(userId)
                        findNavController().navigate(R.id.action_login_to_escaparate)
                    }
                }
            }
        }
    }

    private fun observarErrores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { mensaje ->
                    if (mensaje != null) {
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        viewModel.limpiarError()
                    }
                }
            }
        }
    }
}