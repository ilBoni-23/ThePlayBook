package com.example.theplaybook.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.theplaybook.MainActivity
import com.example.theplaybook.databinding.ActivityDashboardBinding
import com.example.theplaybook.data.mock.MockPlayer
import com.example.theplaybook.ui.dashboard.adapters.DashboardPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val steamId = intent.getStringExtra("STEAM_ID")
            ?: MockPlayer.PLAYER_1.steamId

        setupUI()
        setupObservers()
        setupViewPager()
        viewModel.loadDashboardData(steamId)
    }

    private fun setupUI() {
        // Toolbar menu
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    logout()
                    true
                }
                R.id.menu_refresh -> {
                    val steamId = intent.getStringExtra("STEAM_ID")
                        ?: MockPlayer.PLAYER_1.steamId
                    viewModel.loadDashboardData(steamId)
                    true
                }
                else -> false
            }
        }

        binding.switchMockMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleMockMode(isChecked)
            Snackbar.make(binding.root,
                if (isChecked) "Modalità Mock attiva" else "Modalità Mock disattiva",
                Snackbar.LENGTH_SHORT
            ).show()

            val steamId = intent.getStringExtra("STEAM_ID")
                ?: MockPlayer.PLAYER_1.steamId
            viewModel.loadDashboardData(steamId)
        }
    }

    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Overview"
                1 -> "Games"
                2 -> "Achievements"
                3 -> "Stats"
                else -> "Tab $position"
            }
        }.attach()
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DashboardUiState.Loading -> showLoading(true)
                is DashboardUiState.Success -> {
                    showLoading(false)
                    updateToolbar(state.data)
                }
                is DashboardUiState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isMockMode.observe(this) { isMock ->
            binding.tvMockIndicator.isVisible = isMock
            binding.switchMockMode.isChecked = isMock
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    private fun updateToolbar(data: DashboardData) {
        binding.tvPlayerName.text = data.playerName
        if (data.isMockData) {
            binding.toolbar.title = "ThePlayBook 🎮 (Mock)"
        }
    }

    private fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            val authManager = com.example.theplaybook.auth.SteamAuthManager(this@DashboardActivity)
            authManager.signOut()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}