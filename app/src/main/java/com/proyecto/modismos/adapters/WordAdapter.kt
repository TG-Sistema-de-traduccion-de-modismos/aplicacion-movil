package com.proyecto.modismos.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.proyecto.modismos.R
import com.proyecto.modismos.activities.WordDetailActivity
import com.proyecto.modismos.models.Modismo

class WordAdapter(private val modismos: List<Modismo>) :
    RecyclerView.Adapter<WordAdapter.ModismoViewHolder>() {

    class ModismoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_modismo)
        val wordTextView: TextView = itemView.findViewById(R.id.tv_word)
        val arrowImageView: ImageView = itemView.findViewById(R.id.iv_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModismoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return ModismoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModismoViewHolder, position: Int) {
        val modismo = modismos[position]

        // Configurar la palabra
        holder.wordTextView.text = modismo.palabra

        // Configurar click listener
        holder.cardView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, WordDetailActivity::class.java).apply {
                putExtra("palabra", modismo.palabra)
                putExtra("tipo", modismo.tipo)
                putStringArrayListExtra("definiciones", ArrayList(modismo.definiciones))
                putStringArrayListExtra("sinonimos", ArrayList(modismo.sinonimos))

                // Pasar ejemplos como arrays separados
                val ejemplosTextos = ArrayList(modismo.ejemplos.map { it.texto })
                val ejemplosSignificados = ArrayList(modismo.ejemplos.map { it.significado })
                putStringArrayListExtra("ejemplos_textos", ejemplosTextos)
                putStringArrayListExtra("ejemplos_significados", ejemplosSignificados)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = modismos.size
}