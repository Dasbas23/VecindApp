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
 * Fragment de registro de nuevo vecino.
 *
 * Recoge nombre y barrio, crea el usuario en Room, guarda la sesión
 * en [SesionUsuario] y navega directamente al escaparate.
 *
 * @see RegistroViewModel
 * @see SesionUsuario
 */
class RegistroFragment : Fragment() {

    private val viewModel: RegistroViewModel by viewModels {
        val app = requireActivity().application as VecindAppApplication
        RegistroViewModel.Factory(app.usuarioRepository)
    }

    private lateinit var etNombre: TextInputEditText
    private lateinit var etBarrio: TextInputEditText
    private lateinit var btnRegistrarse: MaterialButton
    private lateinit var btnVolverLogin: MaterialButton
    private lateinit var sesion: SesionUsuario

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registro, container, false)
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
        etNombre = view.findViewById(R.id.etNombreRegistro)
        etBarrio = view.findViewById(R.id.etBarrioRegistro)
        btnRegistrarse = view.findViewById(R.id.btnRegistrarse)
        btnVolverLogin = view.findViewById(R.id.btnVolverLogin)
    }

    private fun configurarBotones() {
        btnRegistrarse.setOnClickListener {
            viewModel.registrar(
                etNombre.text.toString(),
                etBarrio.text.toString()
            )
        }

        btnVolverLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Si el registro es exitoso, guarda la sesión y navega al escaparate.
     */
    private fun observarResultado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrado.collect { userId ->
                    if (userId != null) {
                        sesion.guardarUsuarioId(userId)
                        Toast.makeText(
                            requireContext(),
                            R.string.registro_exitoso,
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_registro_to_escaparate)
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