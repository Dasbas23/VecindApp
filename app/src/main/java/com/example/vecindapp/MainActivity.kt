package com.example.vecindapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.vecindapp.data.SesionUsuario
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Activity principal y única de VecindApp.
 *
 * Actúa como contenedor para el [NavHostFragment], que es quien carga
 * y gestiona los fragments según el grafo de navegación (`nav_graph.xml`).
 * También conecta la [BottomNavigationView] con el Navigation Controller
 * para que las pestañas cambien de fragment automáticamente, y gestiona
 * su visibilidad (oculta en login/registro, visible en el resto).
 *
 * ## Arquitectura Single Activity
 * Toda la app usa una sola Activity. Las "pantallas" son Fragments que
 * se intercambian dentro del [NavHostFragment]. Esto es el patrón
 * recomendado por Google para apps modernas con Navigation Component.
 *
 * ## Flujo de arranque
 * 1. El grafo `nav_graph` arranca por defecto en el `loginFragment`.
 * 2. Al inicializar, si hay sesión guardada en [SesionUsuario], se navega
 * automáticamente al `escaparateFragment`.
 * 3. Si no hay sesión, el usuario permanece en la pantalla de login.
 *
 * @see nav_graph.xml
 * @see VecindAppApplication
 * @see SesionUsuario
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configurar BottomNav
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        // Ocultar BottomNav en pantallas de login/registro
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registroFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }

        // Si ya hay sesión, saltar directamente al escaparate
        val sesion = SesionUsuario(this)
        if (sesion.haySesion() && savedInstanceState == null) {
            navController.navigate(R.id.action_login_to_escaparate)
        }
    }
}