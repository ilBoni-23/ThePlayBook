package com.example.theplaybook.ui.giochi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ActivityGiochiBinding
import com.example.theplaybook.ui.calendario.CalendarioActivity
import com.example.theplaybook.ui.dashboard.DashboardActivity
import com.example.theplaybook.ui.gioco_singolo.GiocoSingoloActivity
import com.example.theplaybook.ui.giochi.adapters.GiochiAdapter
import com.example.theplaybook.ui.giochi.models.Gioco

class GiochiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGiochiBinding
    private lateinit var adapter: GiochiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiochiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadGiochi()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup search
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                filterGiochi(binding.etSearch.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = GiochiAdapter { gioco ->
            // Click listener per gioco
            val intent = Intent(this, GiocoSingoloActivity::class.java)
            intent.putExtra("GAME_NAME", gioco.nome)
            intent.putExtra("GAME_ID", gioco.id)
            startActivity(intent)
        }

        binding.rvGiochi.layoutManager = LinearLayoutManager(this)
        binding.rvGiochi.adapter = adapter
    }

    private fun loadGiochi() {
        val giochi = listOf(
            Gioco(
                id = 730,
                nome = "Counter-Strike 2",
                descrizione = "FPS competitivo • Valve",
                oreGiocate = 1250,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = true,
                completionRate = 75f
            ),
            Gioco(
                id = 570,
                nome = "Dota 2",
                descrizione = "MOBA • Valve",
                oreGiocate = 890,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = true,
                completionRate = 45f
            ),
            Gioco(
                id = 271590,
                nome = "Grand Theft Auto V",
                descrizione = "Azione • Rockstar Games",
                oreGiocate = 456,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = false,
                completionRate = 30f
            ),
            Gioco(
                id = 1245620,
                nome = "Elden Ring",
                descrizione = "Action RPG • FromSoftware",
                oreGiocate = 234,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = true,
                completionRate = 60f
            ),
            Gioco(
                id = 1091500,
                nome = "Cyberpunk 2077",
                descrizione = "RPG • CD Projekt Red",
                oreGiocate = 189,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = true,
                completionRate = 40f
            ),
            Gioco(
                id = 255710,
                nome = "Minecraft",
                descrizione = "Sandbox • Mojang",
                oreGiocate = 789,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = false,
                completionRate = 85f
            ),
            Gioco(
                id = 1172470,
                nome = "Apex Legends",
                descrizione = "Battle Royale • Respawn",
                oreGiocate = 654,
                iconRes = R.drawable.ic_default_game_cover, // AGGIUNTO
                isInstalled = true,
                completionRate = 55f
            )
        )

        adapter.submitList(giochi)
        binding.tvGamesCount.text = "${giochi.size} GIOCHI"
    }

    private fun filterGiochi(query: String) {
        // Implementa filtro se necessario
        Toast.makeText(this, "Cerca: $query", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }

        // La pagina corrente (Giochi) è già selezionata
        binding.indicatorGames.visibility = android.view.View.VISIBLE
        binding.indicatorHome.visibility = android.view.View.INVISIBLE
        binding.indicatorCalendar.visibility = android.view.View.INVISIBLE
    }
}