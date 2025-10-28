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
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.AlphabetAdapter
import com.proyecto.modismos.adapters.WordAdapter
import com.proyecto.modismos.models.Modismo

class DictionaryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alphabetRecyclerView: RecyclerView
    private lateinit var adapter: WordAdapter
    private lateinit var alphabetAdapter: AlphabetAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var wordCountTextView: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val discoveredWords = mutableListOf<Modismo>()
    private val alphabet = ('A'..'Z').map { it.toString() }

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
        setupAlphabetBar()
    }

    override fun onResume() {
        super.onResume()
        loadDiscoveredWords()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_modismos)
        alphabetRecyclerView = view.findViewById(R.id.rv_alphabet)
        wordCountTextView = view.findViewById(R.id.tv_word_count)

        progressBar = view.findViewById(R.id.progress_bar) ?: run {
            ProgressBar(requireContext()).apply {
                visibility = View.GONE
            }
        }
        emptyStateText = view.findViewById(R.id.tv_empty_state) ?: run {
            TextView(requireContext()).apply {
                visibility = View.GONE
            }
        }
        emptyStateContainer = view.findViewById(R.id.empty_state_container) ?: run {
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

    private fun setupAlphabetBar() {
        alphabetAdapter = AlphabetAdapter(alphabet) { letter ->
            scrollToLetter(letter)
        }

        alphabetRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alphabetAdapter
            setHasFixedSize(true)
        }
    }

    private fun scrollToLetter(letter: String) {
        val position = discoveredWords.indexOfFirst {
            it.palabra.firstOrNull()?.uppercaseChar().toString() == letter
        }

        if (position != -1) {
            recyclerView.smoothScrollToPosition(position)
        }
    }

    private fun updateAlphabetBar() {
        val activeLetters = discoveredWords
            .mapNotNull { it.palabra.firstOrNull()?.uppercaseChar()?.toString() }
            .toSet()

        alphabetAdapter.setActiveLetters(activeLetters)
    }

    private fun loadDiscoveredWords() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showEmptyState("Por favor inicia sesión para ver tu diccionario")
            return
        }

        showLoading(true)

        db.collection("usuarios")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val discoveredWordsList = userDocument.get("palabras") as? List<String> ?: emptyList()

                    if (discoveredWordsList.isEmpty()) {
                        showLoading(false)
                        updateWordCount(0)
                        showEmptyState("¡Descubre palabras para llenar tu diccionario!")
                        return@addOnSuccessListener
                    }

                    loadWordDetails(discoveredWordsList)
                } else {
                    showLoading(false)
                    updateWordCount(0)
                    showEmptyState("¡Descubre palabras jugando para llenar tu diccionario!")
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                updateWordCount(0)
                showEmptyState("Error al cargar el diccionario")
                Log.e("DictionaryFragment", "Error loading user data", exception)
            }
    }

    private fun loadWordDetails(wordNames: List<String>) {
        if (!isAdded) return

        discoveredWords.clear()

        var loadedCount = 0
        val totalWords = wordNames.size

        if (totalWords == 0) {
            showLoading(false)
            updateUI()
            return
        }

        wordNames.forEach { wordName ->
            db.collection("palabras")
                .whereEqualTo("palabra", wordName)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty && isAdded) {
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

                    if (loadedCount == totalWords && isAdded) {
                        showLoading(false)
                        updateUI()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DictionaryFragment", "Error loading word: $wordName", exception)
                    loadedCount++

                    if (loadedCount == totalWords && isAdded) {
                        showLoading(false)
                        updateUI()
                    }
                }
        }
    }

    private fun updateUI() {
        if (!isAdded) return

        if (discoveredWords.isEmpty()) {
            updateWordCount(0)
            showEmptyState("¡Descubre palabras para llenar tu diccionario!")
        } else {
            // Ordenar palabras alfabéticamente
            discoveredWords.sortBy { it.palabra }

            updateWordCount(discoveredWords.size)
            updateAlphabetBar()

            if (::recyclerView.isInitialized) {
                recyclerView.visibility = View.VISIBLE
            }

            if (::alphabetRecyclerView.isInitialized) {
                alphabetRecyclerView.visibility = View.VISIBLE
            }

            if (::emptyStateContainer.isInitialized && emptyStateContainer.parent != null) {
                emptyStateContainer.visibility = View.GONE
            }

            if (::adapter.isInitialized) {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateWordCount(count: Int) {
        if (::wordCountTextView.isInitialized) {
            wordCountTextView.text = "Modismos encontrados: $count"
        }
    }

    private fun showLoading(show: Boolean) {
        if (!isAdded) return

        if (::progressBar.isInitialized && progressBar.parent != null) {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
        }

        if (::recyclerView.isInitialized) {
            recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        }

        if (::alphabetRecyclerView.isInitialized) {
            alphabetRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
        }

        if (::emptyStateContainer.isInitialized && emptyStateContainer.parent != null) {
            emptyStateContainer.visibility = View.GONE
        }
    }

    private fun showEmptyState(message: String) {
        if (!isAdded) return

        if (::recyclerView.isInitialized) {
            recyclerView.visibility = View.GONE
        }

        if (::alphabetRecyclerView.isInitialized) {
            alphabetRecyclerView.visibility = View.GONE
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
            Log.d("DictionaryFragment", "Empty state: $message")
        }
    }

    fun refreshDictionary() {
        loadDiscoveredWords()
    }
}