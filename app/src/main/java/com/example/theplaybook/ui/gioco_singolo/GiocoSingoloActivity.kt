package com.example.theplaybook.ui.gioco_singolo

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ActivityGiocoSingoloBinding
import com.example.theplaybook.ui.trofei.TrofeiActivity

class GiocoSingoloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGiocoSingoloBinding
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiocoSingoloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameName = intent.getStringExtra("GAME_NAME") ?: "Gioco Sconosciuto"

        setupToolbar()
        loadGameData(gameName)
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadGameData(gameName: String) {
        // Imposta titolo
        binding.tvGameTitle.text = gameName

        // Carica immagine copertina
        Glide.with(this)
            .load(R.drawable.ic_default_game_cover)
            .into(binding.ivCover)

        // Dati mock per demo
        when (gameName) {
            "Elden Ring" -> {
                binding.tvGameSubtitle.text = "FromSoftware • 2022 • Action RPG"
                binding.tvDescription.text = "Un gioco action RPG in un mondo aperto ambientato in un universo fantasy."
            }
            "Cyberpunk 2077" -> {
                binding.tvGameSubtitle.text = "CD Projekt Red • 2020 • Action RPG"
                binding.tvDescription.text = "Un action RPG a mondo aperto ambientato in Night City."
            }
            else -> {
                binding.tvGameSubtitle.text = "Sviluppatore • Anno • Genere"
                binding.tvDescription.text = "Descrizione del gioco..."
            }
        }

        // Aggiungi elementi UI
        addMediaItems()
        addDlcItems()
    }

    private fun addMediaItems() {
        // Controlla se il layout item_media esiste
        try {
            for (i in 1..4) {
                val mediaView = layoutInflater.inflate(R.layout.item_media, binding.mediaContainer, false)
                binding.mediaContainer.addView(mediaView)
            }
        } catch (e: Exception) {
            // Fallback: crea ImageView dinamiche se il layout non esiste
            for (i in 1..4) {
                val imageView = ImageView(this)
                val layoutParams = ViewGroup.LayoutParams(120, 80)
                imageView.layoutParams = layoutParams
                imageView.setBackgroundResource(R.color.card_background)
                imageView.setImageResource(R.drawable.ic_default_game_cover)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setPadding(0, 0, 8, 0)
                binding.mediaContainer.addView(imageView)
            }
        }
    }

    private fun addDlcItems() {
        try {
            val dlcList = listOf(
                DlcItem("Shadow of the Erdtree", "Espansione", "39.99€", R.color.gold),
                DlcItem("Phantom Liberty", "Espansione", "29.99€", R.color.blue_accent),
                DlcItem("Blood and Wine", "DLC", "19.99€", R.color.success_green)
            )

            dlcList.forEach { dlc ->
                val dlcView = layoutInflater.inflate(R.layout.item_dlc, binding.dlcContainer, false)
                binding.dlcContainer.addView(dlcView)
            }
        } catch (e: Exception) {
            // Fallback: crea TextView dinamiche
            val dlcList = listOf(
                DlcItem("Shadow of the Erdtree", "Espansione", "39.99€", R.color.gold),
                DlcItem("Phantom Liberty", "Espansione", "29.99€", R.color.blue_accent),
                DlcItem("Blood and Wine", "DLC", "19.99€", R.color.success_green)
            )

            dlcList.forEach { dlc ->
                val textView = TextView(this)
                textView.text = "${dlc.name} - ${dlc.price}"
                textView.setPadding(16, 12, 16, 12)
                textView.setTextColor(getColor(R.color.text_primary))
                textView.setBackgroundColor(getColor(R.color.card_background))

                // CORRETTO: Usa ViewGroup.MarginLayoutParams
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.bottomMargin = 8
                textView.layoutParams = layoutParams
                binding.dlcContainer.addView(textView)
            }
        }
    }

    private fun setupClickListeners() {
        // Bottone preferiti
        binding.btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            val iconRes = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            binding.btnFavorite.setImageResource(iconRes)

            val message = if (isFavorite) "Aggiunto ai preferiti" else "Rimosso dai preferiti"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Card progresso trofei
        binding.cardTrophyProgress.setOnClickListener {
            val intent = Intent(this, TrofeiActivity::class.java)
            startActivity(intent)
        }

        // Giochi simili
        setupSimilarGames()
    }

    private fun setupSimilarGames() {
        try {
            val similarGames = listOf(
                SimilarGame("Dark Souls III", R.color.card_background),
                SimilarGame("Bloodborne", R.color.background_dark),
                SimilarGame("Sekiro", R.color.card_background)
            )

            similarGames.forEach { game ->
                val gameView = layoutInflater.inflate(R.layout.item_similar_game, binding.similarGamesContainer, false)
                binding.similarGamesContainer.addView(gameView)

                gameView.setOnClickListener {
                    val intent = Intent(this, GiocoSingoloActivity::class.java)
                    intent.putExtra("GAME_NAME", game.name)
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            // Fallback per giochi simili
            val similarGames = listOf("Dark Souls III", "Bloodborne", "Sekiro")

            similarGames.forEach { gameName ->
                val textView = TextView(this)
                textView.text = gameName
                textView.setPadding(16, 8, 16, 8)
                textView.setBackgroundResource(R.drawable.rounded_card_background)
                textView.setTextColor(getColor(R.color.text_primary))

                val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin = 8
                textView.layoutParams = layoutParams

                textView.setOnClickListener {
                    val intent = Intent(this, GiocoSingoloActivity::class.java)
                    intent.putExtra("GAME_NAME", gameName)
                    startActivity(intent)
                }

                binding.similarGamesContainer.addView(textView)
            }
        }
    }

    data class DlcItem(
        val name: String,
        val type: String,
        val price: String,
        val colorRes: Int
    )

    data class SimilarGame(
        val name: String,
        val colorRes: Int
    )
}