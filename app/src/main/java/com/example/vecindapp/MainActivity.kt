package com.example.vecindapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Activity principal y única de VecindApp.
 *
 * Actúa como contenedor para el [NavHostFragment], que es quien carga
 * y gestiona los fragments según el grafo de navegación (`nav_graph.xml`).
 * También conecta la [BottomNavigationView] con el Navigation Controller
 * para que las pestañas cambien de fragment automáticamente.
 *
 * ## Arquitectura Single Activity
 * Toda la app usa una sola Activity. Las "pantallas" son Fragments que
 * se intercambian dentro del [NavHostFragment]. Esto es el patrón
 * recomendado por Google para apps modernas con Navigation Component.
 *
 * @see nav_graph.xml
 * @see VecindAppApplication
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configurarNavegacion()
    }

    /**
     * Conecta la [BottomNavigationView] con el Navigation Controller.
     *
     * [setupWithNavController] hace que al pulsar una pestaña, el
     * NavController navegue automáticamente al fragment cuyo id
     * coincida con el id del item del menú. Por eso los ids en
     * `menu_bottom_nav.xml` deben coincidir con los del `nav_graph.xml`.
     */
    private fun configurarNavegacion() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)
    }
}