package com.example.theplaybook.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        private val tvLastPlayed: TextView = itemView.findViewById(R.id.tvLastPlayed)

        fun bind(game: SteamGame) {
            tvGameName.text = game.name
            tvPlaytime.text = "Giocato: ${game.playtimeForever / 60}h"

            val lastPlayed = if (game.rtimeLastPlayed != null) {
                val daysAgo = (System.currentTimeMillis() / 1000 - game.rtimeLastPlayed) / 86400
                if (daysAgo == 0L) "Oggi" else "$daysAgo giorni fa"
            } else {
                "Mai"
            }
            tvLastPlayed.text = "Ultimo: $lastPlayed"
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