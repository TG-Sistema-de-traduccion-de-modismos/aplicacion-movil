package com.proyecto.modismos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.proyecto.modismos.R
import com.proyecto.modismos.models.Modismo

class ModismosAdapter(
    private val modismos: List<Modismo>
) : RecyclerView.Adapter<ModismosAdapter.ModismoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModismoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_modismo, parent, false)
        return ModismoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModismoViewHolder, position: Int) {
        holder.bind(modismos[position])
    }

    override fun getItemCount(): Int = modismos.size

    inner class ModismoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardModismo: MaterialCardView = itemView.findViewById(R.id.cardModismo)
        private val tvPalabra: TextView = itemView.findViewById(R.id.tvPalabra)
        private val ivExpandIcon: ImageView = itemView.findViewById(R.id.ivExpandIcon)
        private val llExpandedContent: LinearLayout = itemView.findViewById(R.id.llExpandedContent)
        private val tvDefinicion: TextView = itemView.findViewById(R.id.tvDefinicion)
        private val tvSinonimos: TextView = itemView.findViewById(R.id.tvSinonimos)
        private val ivInfoIcon: ImageView = itemView.findViewById(R.id.ivInfoIcon)

        fun bind(modismo: Modismo) {
            tvPalabra.text = modismo.palabra
            tvDefinicion.text = modismo.definiciones[0]

            // Formatear sinónimos con el punto al inicio en rojo
            val sinonimosText = modismo.sinonimos.joinToString(", ")
            tvSinonimos.text = sinonimosText

            // Configurar estado expandido/colapsado
            llExpandedContent.isVisible = modismo.isExpanded
            ivExpandIcon.rotation = if (modismo.isExpanded) 180f else 0f

            // Click listener para expandir/colapsar
            cardModismo.setOnClickListener {
                modismo.isExpanded = !modismo.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }
}