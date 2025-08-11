package com.proyecto.modismos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.WordAdapter
import com.proyecto.modismos.models.Modismo

class DictionaryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var emptyStateContainer: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val discoveredWords = mutableListOf<Modismo>()

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
        setupRecyclerView()
        loadDiscoveredWords()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_modismos)
        // Inicialización segura para elementos que podrían no existir en el layout
        progressBar = view.findViewById(R.id.progress_bar) ?: run {
            // Si no existe en el layout, crear uno programáticamente pero mantenerlo invisible
            ProgressBar(requireContext()).apply {
                visibility = View.GONE
            }
        }
        emptyStateText = view.findViewById(R.id.tv_empty_state) ?: run {
            // Si no existe en el layout, crear uno programáticamente pero mantenerlo invisible
            TextView(requireContext()).apply {
                visibility = View.GONE
            }
        }
        emptyStateContainer = view.findViewById(R.id.empty_state_container) ?: run {
            // Si no existe en el layout, crear uno programáticamente pero mantenerlo invisible
            LinearLayout(requireContext()).apply {
                visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = WordAdapter(discoveredWords)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DictionaryFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadDiscoveredWords() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showEmptyState("Por favor inicia sesión para ver tu diccionario")
            return
        }

        showLoading(true)

        // Obtener las palabras descubiertas del usuario
        db.collection("usuarios")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val discoveredWordsList = userDocument.get("palabras") as? List<String> ?: emptyList()

                    if (discoveredWordsList.isEmpty()) {
                        showLoading(false)
                        showEmptyState("¡Descubre palabras jugando para llenar tu diccionario!")
                        return@addOnSuccessListener
                    }

                    // Cargar los detalles de cada palabra desde la colección "palabras"
                    loadWordDetails(discoveredWordsList)
                } else {
                    showLoading(false)
                    showEmptyState("¡Descubre palabras jugando para llenar tu diccionario!")
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                showEmptyState("Error al cargar el diccionario")
                Log.e("DictionaryFragment", "Error loading user data", exception)
            }
    }

    private fun loadWordDetails(wordNames: List<String>) {
        discoveredWords.clear()

        // Usar un contador para saber cuándo terminar de cargar todas las palabras
        var loadedCount = 0
        val totalWords = wordNames.size

        wordNames.forEach { wordName ->
            db.collection("palabras")
                .whereEqualTo("palabra", wordName)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val significados = doc.get("significados") as? List<String> ?: emptyList()
                        val sinonimos = doc.get("sinonimos") as? List<String> ?: emptyList()

                        val modismo = Modismo(
                            palabra = wordName,
                            tipo = "Modismo",
                            definiciones = significados,
                            sinonimos = sinonimos
                        )

                        discoveredWords.add(modismo)
                    }

                    loadedCount++

                    // Cuando se hayan cargado todas las palabras, actualizar la UI
                    if (loadedCount == totalWords) {
                        showLoading(false)
                        updateUI()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DictionaryFragment", "Error loading word: $wordName", exception)
                    loadedCount++

                    if (loadedCount == totalWords) {
                        showLoading(false)
                        updateUI()
                    }
                }
        }
    }

    private fun updateUI() {
        if (discoveredWords.isEmpty()) {
            showEmptyState("¡Descubre palabras jugando para llenar tu diccionario!")
        } else {
            // Ordenar palabras alfabéticamente
            discoveredWords.sortBy { it.palabra }

            if (::recyclerView.isInitialized) {
                recyclerView.visibility = View.VISIBLE
            }

            if (::emptyStateContainer.isInitialized && emptyStateContainer.parent != null) {
                emptyStateContainer.visibility = View.GONE
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun showLoading(show: Boolean) {
        // Solo mostrar/ocultar si los elementos existen en el layout
        if (::progressBar.isInitialized && progressBar.parent != null) {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
        }

        if (::recyclerView.isInitialized) {
            recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        }

        if (::emptyStateContainer.isInitialized && emptyStateContainer.parent != null) {
            emptyStateContainer.visibility = View.GONE
        }
    }

    private fun showEmptyState(message: String) {
        if (::recyclerView.isInitialized) {
            recyclerView.visibility = View.GONE
        }

        if (::progressBar.isInitialized && progressBar.parent != null) {
            progressBar.visibility = View.GONE
        }

        if (::emptyStateContainer.isInitialized && emptyStateContainer.parent != null) {
            emptyStateContainer.visibility = View.VISIBLE
            if (::emptyStateText.isInitialized && emptyStateText.parent != null) {
                emptyStateText.text = message
            }
        } else {
            // Si no hay contenedor para mostrar el estado vacío, usar Log para debug
            Log.d("DictionaryFragment", "Empty state: $message")
        }
    }

    // Método público para refrescar el diccionario (útil cuando se descubren nuevas palabras)
    fun refreshDictionary() {
        loadDiscoveredWords()
    }
}