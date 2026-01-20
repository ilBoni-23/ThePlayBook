package com.example.theplaybook.ui.profilo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ActivityProfiloBinding
import com.example.theplaybook.ui.amici.AmiciActivity
import com.example.theplaybook.ui.calendario.CalendarioActivity
import com.example.theplaybook.ui.dashboard.DashboardActivity
import com.example.theplaybook.ui.gioco_singolo.GiocoSingoloActivity

class ProfiloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfiloBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfiloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadProfileData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadProfileData() {
        // Carica dati utente (mock per ora)
        binding.tvUserName.text = "DarkGamer99"
        binding.tvUserLocation.text = "Milano, IT • Membro dal 2023"
        binding.tvFriendsCount.text = "42"
        binding.tvFollowersCount.text = "1.2k"
        binding.tvHoursCount.text = "150"
        binding.tvLevelBadge.text = "50"
        binding.tvTrophyProgress.text = "85%"
        binding.progressBarTrophies.progress = 85

        // Carica avatar
        Glide.with(this)
            .load(R.drawable.ic_default_avatar)
            .circleCrop()
            .into(binding.ivAvatar)

        // Aggiungi giochi recenti
        addRecentGames()
    }

    private fun addRecentGames() {
        val games = listOf(
            GameData("Elden Ring", "#FF5D4037", 24),
            GameData("Cyberpunk 2077", "#FFFBC02D", 45),
            GameData("Minecraft", "#FF388E3C", 120),
            GameData("Fortnite", "#FF1976D2", 89),
            GameData("FIFA 24", "#FF512DA8", 32)
        )

        binding.gamesContainer.removeAllViews()

        for (game in games) {
            val gameCard = layoutInflater.inflate(R.layout.item_game_card, binding.gamesContainer, false)

            val tvGameName = gameCard.findViewById<android.widget.TextView>(R.id.tvGameName)
            val tvPlaytime = gameCard.findViewById<android.widget.TextView>(R.id.tvPlaytime)
            val cardView = gameCard.findViewById<androidx.cardview.widget.CardView>(R.id.cardGame)

            tvGameName.text = game.name
            tvPlaytime.text = " ${game.hours}h giocate"

            // Imposta colore di sfondo
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(game.color))

            // Aggiungi click listener
            gameCard.setOnClickListener {
                val intent = Intent(this, GiocoSingoloActivity::class.java)
                intent.putExtra("GAME_NAME", game.name)
                startActivity(intent)
            }

            binding.gamesContainer.addView(gameCard)
        }
    }

    private fun setupClickListeners() {
        binding.btnFriends.setOnClickListener {
            val intent = Intent(this, AmiciActivity::class.java)
            startActivity(intent)
        }

        binding.btnFollowers.setOnClickListener {
            val intent = Intent(this, AmiciActivity::class.java) // Per ora usa stessa activity
            intent.putExtra("IS_FOLLOWERS", true)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            // Logout e torna al login
            Toast.makeText(this, "Disconnessione...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    data class GameData(
        val name: String,
        val color: String,
        val hours: Int
    )
}