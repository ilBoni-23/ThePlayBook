package com.example.theplaybook.ui.trofei

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ActivityTrofeiBinding
import com.google.android.material.tabs.TabLayout
import com.example.theplaybook.ui.trofei.models.Trofeo
import com.example.theplaybook.ui.trofei.adapters.TrofeiAdapter


class TrofeiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrofeiBinding
    private lateinit var adapter: TrofeiAdapter
    private var currentTab = 0 // 0 = Da fare, 1 = Completati

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrofeiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        loadTrofei()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup search
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterTrofei(binding.etSearch.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterTrofeiByTab()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = TrofeiAdapter()
        binding.rvTrophies.layoutManager = LinearLayoutManager(this)
        binding.rvTrophies.adapter = adapter
    }

    private fun loadTrofei() {
        val trofei = listOf(
            Trofeo(
                id = 1,
                nome = "Primo Sangue",
                descrizione = "Sconfiggi il primo Boss",
                rarita = "COMUNE",
                isSbloccato = true,
                iconRes = R.drawable.ic_trophy_gold
            ),
            Trofeo(
                id = 2,
                nome = "Esploratore",
                descrizione = "Scopri tutte le mappe del gioco",
                rarita = "RARO",
                isSbloccato = false,
                iconRes = R.drawable.ic_trophy_silver
            ),
            Trofeo(
                id = 3,
                nome = "Maestro d'Armi",
                descrizione = "Porta un'arma al livello massimo",
                rarita = "RARO",
                isSbloccato = true,
                iconRes = R.drawable.ic_trophy_gold
            ),
            Trofeo(
                id = 4,
                nome = "Intoccabile",
                descrizione = "Finisci un livello senza subire danni",
                rarita = "LEGGENDARIO",
                isSbloccato = false,
                iconRes = R.drawable.ic_trophy_platinum
            ),
            Trofeo(
                id = 5,
                nome = "Collezionista",
                descrizione = "Trova 50 oggetti nascosti",
                rarita = "COMUNE",
                isSbloccato = true,
                iconRes = R.drawable.ic_trophy_bronze
            ),
            Trofeo(
                id = 6,
                nome = "Dio della Guerra",
                descrizione = "Sconfiggi il Boss Finale in modalità difficile",
                rarita = "EPICO",
                isSbloccato = false,
                iconRes = R.drawable.ic_trophy_platinum
            ),
            Trofeo(
                id = 7,
                nome = "Speedrun",
                descrizione = "Completa il gioco in meno di 5 ore",
                rarita = "LEGGENDARIO",
                isSbloccato = false,
                iconRes = R.drawable.ic_trophy_platinum
            )
        )
        adapter.submitList(trofei)
        updateTrofeiCount(trofei)
    }

    private fun filterTrofeiByTab() {
        val currentList = adapter.currentList
        val filtered = when (currentTab) {
            0 -> currentList.filter { !it.isSbloccato } // Da fare
            1 -> currentList.filter { it.isSbloccato }  // Completati
            else -> currentList
        }
        adapter.submitList(filtered)
        binding.tvTrophiesCount.text = "${filtered.size} TROFEI"
    }

    private fun filterTrofei(query: String) {
        val currentList = adapter.currentList
        val filtered = currentList.filter {
            it.nome.contains(query, true) || it.descrizione.contains(query, true)
        }
        adapter.submitList(filtered)
        binding.tvTrophiesCount.text = "${filtered.size} TROFEI"
    }

    private fun updateTrofeiCount(trofei: List<Trofeo>) {
        val total = trofei.size
        val completed = trofei.count { it.isSbloccato }
        binding.tvTrophiesCount.text = "$total TROFEI ($completed completati)"
    }
}