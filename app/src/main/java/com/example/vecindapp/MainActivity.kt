package com.example.vecindapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.example.vecindapp.data.SesionUsuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

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

    /** Evita suscribirse al badge más de una vez por sesión. */
    private var badgeIniciado = false

    /** ViewModel principal — se inicializa en [iniciarBadge]. */
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configurarNavegacion(savedInstanceState)
    }

    private fun configurarNavegacion(savedInstanceState: Bundle?) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Navegación con limpieza de back stack
        bottomNav.setOnItemSelectedListener { item ->
            val builder = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.escaparateFragment, false)
                .setLaunchSingleTop(true)
            navController.navigate(item.itemId, null, builder.build())
            true
        }

        // Sincronizar icono del BottomNav + ocultar en login/registro
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registroFragment -> {
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                    val menuItem = bottomNav.menu.findItem(destination.id)
                    if (menuItem != null) {
                        menuItem.isChecked = true
                    }
                    iniciarBadge(bottomNav)
                }
            }
        }

        // Si ya hay sesión, saltar al escaparate
        val sesion = SesionUsuario(this)
        if (sesion.haySesion() && savedInstanceState == null) {
            navController.navigate(R.id.action_login_to_escaparate)
        }
    }

    /**
     * Inicia la observación reactiva del badge de notificaciones.
     *
     * Se ejecuta en cada navegación fuera de login/registro.
     * - La primera vez: crea el ViewModel y arranca el collector.
     * - Las siguientes: solo actualiza el [MainViewModel.setUsuarioId]
     *   para que [flatMapLatest] resubscriba al usuario correcto.
     */
    private fun iniciarBadge(bottomNav: BottomNavigationView) {
        val sesion = SesionUsuario(this)
        if (!sesion.haySesion()) return

        // Inicializar ViewModel una sola vez (sin usuarioId en Factory)
        if (!::mainViewModel.isInitialized) {
            val app = application as VecindAppApplication
            mainViewModel = ViewModelProvider(
                this,
                MainViewModel.Factory(app.transaccionRepository)
            )[MainViewModel::class.java]
        }

        // Siempre actualizar el ID — flatMapLatest resubscribe automáticamente
        mainViewModel.setUsuarioId(sesion.obtenerUsuarioId())

        // Arrancar collector una sola vez
        if (!badgeIniciado) {
            badgeIniciado = true
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mainViewModel.notificaciones.collect { conteo ->
                        val badge = bottomNav.getOrCreateBadge(R.id.transaccionFragment)
                        if (conteo > 0) {
                            badge.isVisible = true
                            badge.number = conteo
                        } else {
                            badge.isVisible = false
                        }
                    }
                }
            }
        }
    }
}