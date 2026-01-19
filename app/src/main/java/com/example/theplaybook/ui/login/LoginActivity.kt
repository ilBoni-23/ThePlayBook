/*package com.example.theplaybook.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.theplaybook.ThePlayBookApp
import com.example.theplaybook.databinding.ActivityLoginBinding
import com.example.theplaybook.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var steamAuthHelper: SteamAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inizializza Steam Auth Helper
        steamAuthHelper = SteamAuthHelper(this)

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSteamLogin.setOnClickListener {
            // Metodo 1: Usa la versione semplice (per testing)
            viewModel.loginWithSteam()

            // Metodo 2: Usa web login completo (commentato per ora)
            // steamAuthHelper.startSteamLogin()
        }

        binding.btnDemoMode.setOnClickListener {
            viewModel.startDemoMode()
        }
    }
}*/