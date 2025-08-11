package com.proyecto.modismos.activities

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.MainPagerAdapter

class UserMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        supportActionBar?.hide()
        setupTransparentBars()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        viewPager = findViewById(R.id.viewPager)

        viewPager.adapter = MainPagerAdapter(this)
        viewPager.isUserInputEnabled = true // swipe manual habilitado

        // --- Selección inicial: Home en el centro ---
        viewPager.setCurrentItem(1, false)
        bottomNavigationView.selectedItemId = R.id.nav_home

        // --- Menú inferior → ViewPager (animación suave tipo WhatsApp) ---
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dictionary -> viewPager.setCurrentItem(0, false)
                R.id.nav_home -> viewPager.setCurrentItem(1, false)
                R.id.nav_profile -> viewPager.setCurrentItem(2, false)
            }
            true
        }

        // --- ViewPager → Menú inferior ---
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNavigationView.selectedItemId = R.id.nav_dictionary
                    1 -> bottomNavigationView.selectedItemId = R.id.nav_home
                    2 -> bottomNavigationView.selectedItemId = R.id.nav_profile
                }
            }
        })

        // --- Botón atrás → diálogo de salida ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("¿Salir de la app?")
            .setMessage("¿Estás seguro de que quieres salir?")
            .setPositiveButton("Sí") { _, _ -> finishAffinity() }
            .setNegativeButton("No", null)
            .show()
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
