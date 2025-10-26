package com.proyecto.modismos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.modismos.R

class AlphabetAdapter(
    private val letters: List<String>,
    private val onLetterClick: (String) -> Unit
) : RecyclerView.Adapter<AlphabetAdapter.LetterViewHolder>() {

    private var activeLetters = setOf<String>()

    fun setActiveLetters(letters: Set<String>) {
        activeLetters = letters
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alphabet_letter, parent, false)
        return LetterViewHolder(view)
    }

    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        val letter = letters[position]
        holder.bind(letter, activeLetters.contains(letter))
    }

    override fun getItemCount() = letters.size

    inner class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val letterText: TextView = itemView.findViewById(R.id.tv_letter)

        fun bind(letter: String, isActive: Boolean) {
            letterText.text = letter
            letterText.alpha = if (isActive) 1.0f else 0.3f

            if (isActive) {
                itemView.setOnClickListener {
                    onLetterClick(letter)
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }
}