package com.example.theplaybook.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.theplaybook.ThePlayBookApp
import com.example.theplaybook.databinding.ActivityLoginBinding
import com.example.theplaybook.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater) // Questo carica activity_login.xml
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()

        // Controlla se l'utente è già loggato
        viewModel.checkIfUserIsLoggedIn()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }

                is LoginState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnLogin.isEnabled = true

                    // Avvia Dashboard
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }

                is LoginState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }

                is LoginState.LoggedIn -> {
                    // Utente già loggato, vai alla dashboard
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }

                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            viewModel.loginWithSteam()
        }

        binding.btnSkip.setOnClickListener {
            // Modalità demo senza login
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}