package com.example.theplaybook.ui.amici

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theplaybook.R
import com.example.theplaybook.databinding.ActivityAmiciBinding
import com.example.theplaybook.ui.amici.adapters.AmiciAdapter

class AmiciActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmiciBinding
    private lateinit var adapter: AmiciAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmiciBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadAmici()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = AmiciAdapter()
        binding.rvFriends.layoutManager = LinearLayoutManager(this)
        binding.rvFriends.adapter = adapter
    }

    private fun loadAmici() {
        val amici = listOf(
            Amico("SniperWolf", "Online - Giocando a COD", true, R.drawable.ic_default_avatar),
            Amico("Kratos_99", "Offline da 2 ore", false, R.drawable.ic_default_avatar),
            Amico("ZeldaFan", "Online - Nella Lobby", true, R.drawable.ic_default_avatar),
            Amico("MarioBros", "Offline", false, R.drawable.ic_default_avatar),
            Amico("LaraCroft", "Online - Giocando a Tomb Raider", true, R.drawable.ic_default_avatar),
            Amico("MasterChief", "In attesa...", true, R.drawable.ic_default_avatar),
            Amico("Pikachu_007", "Offline", false, R.drawable.ic_default_avatar)
        )
        adapter.submitList(amici)
        binding.tvFriendsCount.text = "${amici.size} AMICI"
    }
}

data class Amico(
    val nome: String,
    val stato: String,
    val isOnline: Boolean,
    val avatarRes: Int = R.drawable.ic_default_avatar
)