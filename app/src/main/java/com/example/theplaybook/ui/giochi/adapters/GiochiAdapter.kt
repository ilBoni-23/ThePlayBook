package com.example.theplaybook.ui.giochi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theplaybook.databinding.ItemGiocoBinding
import com.example.theplaybook.ui.giochi.models.Gioco

class GiochiAdapter(
    private val onItemClick: (Gioco) -> Unit
) : ListAdapter<Gioco, GiochiAdapter.GiocoViewHolder>(GiocoDiffCallback()) {

    class GiocoViewHolder(
        private val binding: ItemGiocoBinding,
        private val onItemClick: (Gioco) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentGioco: Gioco? = null

        init {
            binding.root.setOnClickListener {
                currentGioco?.let { onItemClick(it) }
            }
        }

        fun bind(gioco: Gioco) {
            currentGioco = gioco

            binding.tvGameName.text = gioco.nome
            binding.tvGameDescription.text = gioco.descrizione
            binding.tvPlaytime.text = "${gioco.oreGiocateFormatted} giocate"

            // Imposta icona - ora usa sempre la risorsa passata
            binding.ivGameIcon.setImageResource(gioco.iconRes)

            // Indica se installato
            if (gioco.isInstalled) {
                binding.tvInstalledIndicator.visibility = android.view.View.VISIBLE
            } else {
                binding.tvInstalledIndicator.visibility = android.view.View.GONE
            }

            // Barra progresso completion rate
            if (gioco.completionRate > 0) {
                binding.progressCompletion.visibility = android.view.View.VISIBLE
                binding.progressCompletion.progress = gioco.completionRate.toInt()
            } else {
                binding.progressCompletion.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiocoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGiocoBinding.inflate(inflater, parent, false)
        return GiocoViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: GiocoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class GiocoDiffCallback : DiffUtil.ItemCallback<Gioco>() {
    override fun areItemsTheSame(oldItem: Gioco, newItem: Gioco): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Gioco, newItem: Gioco): Boolean {
        return oldItem == newItem
    }
}