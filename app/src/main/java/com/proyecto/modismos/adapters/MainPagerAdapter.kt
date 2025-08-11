package com.proyecto.modismos.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.proyecto.modismos.fragments.DictionaryFragment
import com.proyecto.modismos.fragments.HomeFragment
import com.proyecto.modismos.fragments.ProfileFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3 // cantidad de pestañas
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DictionaryFragment() // izquierda
            1 -> HomeFragment()        // centro
            2 -> ProfileFragment()     // derecha
            else -> HomeFragment()
        }
    }
}

