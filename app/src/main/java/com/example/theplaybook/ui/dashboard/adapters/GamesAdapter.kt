package com.example.theplaybook.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theplaybook.R
import com.example.theplaybook.data.remote.models.SteamGame

class GamesAdapter : ListAdapter<SteamGame, GamesAdapter.GameViewHolder>(GameDiffCallback()) {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGameName: TextView = itemView.findViewById(R.id.tvGameName)
        private val tvPlaytime: TextView = itemView.findViewById(R.id.tvPlaytime)
        private val tvGameDescription: TextView = itemView.findViewById(R.id.tvGameDescription)
        private val ivGameIcon: ImageView = itemView.findViewById(R.id.ivGameIcon)

        fun bind(game: SteamGame) {
            tvGameName.text = game.name
            tvPlaytime.text = "Giocato: ${game.playtimeForever / 60}h"

            // Descrizione basata sul tempo di gioco
            val hours = game.playtimeForever / 60
            tvGameDescription.text = when {
                hours > 100 -> "Esperto - ${hours}h totali"
                hours > 50 -> "Avanzato - ${hours}h totali"
                hours > 10 -> "Intermedio - ${hours}h totali"
                else -> "Principiante - ${hours}h totali"
            }

            // TODO: Caricare immagine icona quando disponibile
            // ivGameIcon.setImageURI(game.imgIconUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class GameDiffCallback : DiffUtil.ItemCallback<SteamGame>() {
    override fun areItemsTheSame(oldItem: SteamGame, newItem: SteamGame): Boolean {
        return oldItem.appId == newItem.appId
    }

    override fun areContentsTheSame(oldItem: SteamGame, newItem: SteamGame): Boolean {
        return oldItem == newItem
    }
}