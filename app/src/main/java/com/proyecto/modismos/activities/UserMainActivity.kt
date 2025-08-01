package com.proyecto.modismos.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.proyecto.modismos.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyecto.modismos.R

class UserMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        supportActionBar?.hide()
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setupTransparentBars()


        val targetFragment = intent.getIntExtra("fragment", R.id.nav_home)

        val fragment = when (targetFragment) {
            R.id.nav_dictionary -> DictionaryFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }


        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        bottomNavigationView.selectedItemId = targetFragment

        // Listener de navegación
        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_dictionary -> DictionaryFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            true
        }

        // Ahora, después de establecer el listener, selecciona el ítem:
        bottomNavigationView.selectedItemId = targetFragment

    }

    private fun setupTransparentBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        val isNightMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        insetsController.isAppearanceLightStatusBars = !isNightMode
        insetsController.isAppearanceLightNavigationBars = !isNightMode
    }
}