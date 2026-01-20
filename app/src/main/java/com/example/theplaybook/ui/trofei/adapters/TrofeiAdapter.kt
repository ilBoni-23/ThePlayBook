package com.example.theplaybook.ui.trofei.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ItemTrofeoBinding
import com.example.theplaybook.ui.trofei.models.Trofeo

class TrofeiAdapter : ListAdapter<Trofeo, TrofeiAdapter.TrofeoViewHolder>(TrofeoDiffCallback()) {

    class TrofeoViewHolder(private val binding: ItemTrofeoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trofeo: Trofeo) {
            binding.tvTrophyName.text = trofeo.nome
            binding.tvTrophyDescription.text = trofeo.descrizione
            binding.ivTrophyIcon.setImageResource(trofeo.iconRes)
            binding.badgeRarity.text = trofeo.rarita

            // Colore badge in base alla rarità
            val badgeColorRes = when (trofeo.rarita) {
                "LEGGENDARIO" -> R.drawable.badge_background_legendary
                "EPICO" -> R.drawable.badge_background_epic
                "RARO" -> R.drawable.badge_background_rare
                else -> R.drawable.badge_background_common
            }
            binding.badgeRarity.setBackgroundResource(badgeColorRes)

            // Icona stato
            if (trofeo.isSbloccato) {
                binding.ivStatusIcon.setImageResource(R.drawable.ic_check)
                binding.ivStatusIcon.setColorFilter(binding.root.context.getColor(R.color.gold))
                binding.ivTrophyIcon.alpha = 1f
            } else {
                binding.ivStatusIcon.setImageResource(R.drawable.ic_lock)
                binding.ivStatusIcon.setColorFilter(binding.root.context.getColor(R.color.light_gray))
                binding.ivTrophyIcon.alpha = 0.5f
            }

            // Click listener
            binding.root.setOnClickListener {
                // Mostra dettagli trofeo
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrofeoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTrofeoBinding.inflate(inflater, parent, false)
        return TrofeoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrofeoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TrofeoDiffCallback : DiffUtil.ItemCallback<Trofeo>() {
    override fun areItemsTheSame(oldItem: Trofeo, newItem: Trofeo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Trofeo, newItem: Trofeo): Boolean {
        return oldItem == newItem
    }
}