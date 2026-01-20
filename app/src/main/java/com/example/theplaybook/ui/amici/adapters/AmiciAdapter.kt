package com.example.theplaybook.ui.amici.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ItemAmicoBinding
import com.example.theplaybook.ui.amici.Amico

class AmiciAdapter : ListAdapter<Amico, AmiciAdapter.AmicoViewHolder>(AmicoDiffCallback()) {

    class AmicoViewHolder(private val binding: ItemAmicoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(amico: Amico) {
            binding.tvName.text = amico.nome
            binding.tvStatus.text = amico.stato

            // Usa setImageResource invece di CircleImageView specifico
            binding.ivAvatar.setImageResource(amico.avatarRes)

            // Applica forma circolare programmaticamente
            binding.ivAvatar.clipToOutline = true

            // Imposta status online/offline
            val statusDrawable = if (amico.isOnline) {
                R.drawable.circle_online
            } else {
                R.drawable.circle_offline
            }
            binding.viewStatus.setBackgroundResource(statusDrawable)

            // Click listener per messaggio
            binding.btnMessage.setOnClickListener {
                // Apri chat con amico
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmicoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAmicoBinding.inflate(inflater, parent, false)
        return AmicoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AmicoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class AmicoDiffCallback : DiffUtil.ItemCallback<Amico>() {
    override fun areItemsTheSame(oldItem: Amico, newItem: Amico): Boolean {
        return oldItem.nome == newItem.nome
    }

    override fun areContentsTheSame(oldItem: Amico, newItem: Amico): Boolean {
        return oldItem == newItem
    }
}