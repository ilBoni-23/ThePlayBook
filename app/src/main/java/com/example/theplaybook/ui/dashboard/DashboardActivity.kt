package com.example.theplaybook.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theplaybook.ThePlayBookApp
import com.example.theplaybook.databinding.ActivityDashboardBinding
import com.example.theplaybook.ui.login.LoginActivity
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater) // Questo carica activity_dashboard.xml
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "ThePlayBook"

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                android.R.id.home -> {
                    // Menu laterale
                    true
                }
                else -> false
            }
        }
    }

    private fun setupObservers() {
        viewModel.dashboardState.observe(this) { state ->
            when (state) {
                is DashboardState.Loading -> {
                    showLoading(true)
                }

                is DashboardState.Success -> {
                    showLoading(false)
                    updateUI(state.data)
                }

                is DashboardState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }

                is DashboardState.Logout -> {
                    // Torna al login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            viewModel.loadDashboardData()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun updateUI(data: DashboardData) {
        binding.tvSteamName.text = data.steamName
        binding.tvTotalHours.text = String.format("%.1f h", data.totalPlaytimeHours)
        binding.tvTotalGames.text = data.totalGames.toString()
        binding.tvCompletionRate.text = String.format("%.1f%%", data.completionRate)

        // Aggiorna progress bar
        binding.completionProgress.progress = data.completionRate.toInt()

        // Carica avatar con Glide se disponibile
        if (data.avatarUrl.isNotEmpty()) {
            // Glide.with(this).load(data.avatarUrl).into(binding.ivAvatar)
        }
    }

    private fun logout() {
        viewModel.logout()
    }
}