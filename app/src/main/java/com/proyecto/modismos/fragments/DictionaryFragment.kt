package com.proyecto.modismos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.WordAdapter
import com.proyecto.modismos.models.Modismo

class DictionaryFragment : Fragment() {


    // Lista de modismos con sus datos
    private val modismos = listOf(
        Modismo("bacano", "Modismo", listOf(
            "chévere, estupendo.",
            "Referido a persona, extraordinaria, que sobresale por su inteligencia o por sus grandes habilidades para algo.",
            "Referido a persona, que va a la última moda."
        ), listOf("chévere", "estupendo", "genial", "agradable", "atractivo"), false),

        Modismo("chévere", "Modismo", listOf(
            "Excelente, muy bueno.",
            "Agradable, simpático.",
            "Que está de moda o es popular."
        ), listOf("bacano", "genial", "bueno", "agradable", "popular"), false),

        Modismo("parce", "Modismo", listOf(
            "Amigo, compañero.",
            "Persona de confianza."
        ), listOf("amigo", "parcero", "compañero", "hermano", "pana"), false),

        Modismo("chimba", "Modismo", listOf(
            "Excelente, muy bueno.",
            "Cosa extraordinaria o admirable."
        ), listOf("genial", "bacano", "chévere", "espectacular", "increíble"), false),

        Modismo("mamagallismo","Modismo" , listOf(
            "Comportamiento o actitud poco seria.",
            "Acción irresponsable o descuidada."
        ), listOf("irresponsabilidad", "descuido", "falta de seriedad"), false),

        Modismo("verraco", "Modismo", listOf(
            "Excelente, extraordinario.",
            "Persona valiente o decidida.",
            "Difícil, complicado."
        ), listOf("valiente", "extraordinario", "difícil", "complicado"), false)
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dictionary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()

        view.post {
            setupRecyclerView(view)
        }
    }

    private fun initViews(view: View) {

    }

    private fun setupClickListeners() {

    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rv_modismos)
        adapter = WordAdapter(modismos)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DictionaryFragment.adapter
            // Opcional: mejorar performance si el tamaño del RecyclerView es fijo
            setHasFixedSize(true)
        }
    }


}