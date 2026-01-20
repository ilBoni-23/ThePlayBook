package com.example.theplaybook

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.theplaybook.ui.dashboard.DashboardActivity

class MainActivity : AppCompatActivity() {

    // View references
    private lateinit var btnSteamLogin: Button
    private lateinit var btnDemoMode: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loginCard: CardView
    private lateinit var etDemoUsername: EditText

    // Preferences
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inizializza preferences
        prefs = getSharedPreferences("theplaybook_prefs", MODE_PRIVATE)

        // Inizializza le view
        initViews()

        // Carica nome demo salvato (se esiste)
        loadSavedDemoName()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initViews() {
        btnSteamLogin = findViewById(R.id.btnSteamLogin)
        btnDemoMode = findViewById(R.id.btnDemoMode)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        loginCard = findViewById(R.id.loginCard)
        etDemoUsername = findViewById(R.id.etDemoUsername)
    }

    private fun loadSavedDemoName() {
        val savedName = prefs.getString("demo_player_name", null)
        if (savedName != null) {
            etDemoUsername.setText(savedName)
        }
    }

    private fun setupClickListeners() {
        // Bottone Steam Login
        btnSteamLogin.setOnClickListener {
            // Disabilita bottoni durante login
            btnSteamLogin.isEnabled = false
            btnDemoMode.isEnabled = false
            btnSteamLogin.text = "Login in corso..."
            tvStatus.text = "Autenticazione in corso..."
            progressBar.visibility = android.view.View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Usa SteamAuthManager
                    val authManager = com.example.theplaybook.auth.SteamAuthManager(this@MainActivity)
                    val result = authManager.signInWithSteam()

                    if (result.isSuccess) {
                        val steamId = result.getOrNull() ?: ""
                        tvStatus.text = "✅ Login riuscito!\nSteam ID: $steamId"
                        btnSteamLogin.text = "Accesso effettuato"
                        btnSteamLogin.setBackgroundResource(R.drawable.button_steam_background)

                        // Salva anche il nome demo se inserito
                        val demoName = etDemoUsername.text.toString().trim()
                        if (demoName.isNotEmpty()) {
                            prefs.edit().putString("demo_player_name", demoName).apply()
                        }

                        // Apri DashboardActivity
                        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                        intent.putExtra("STEAM_ID", steamId)
                        startActivity(intent)

                    } else {
                        tvStatus.text = "❌ Login fallito"
                        btnSteamLogin.text = "Riprova"
                        btnSteamLogin.setBackgroundResource(R.drawable.button_steam_background)
                        btnSteamLogin.isEnabled = true
                        btnDemoMode.isEnabled = true
                    }
                } catch (e: Exception) {
                    tvStatus.text = "Errore: ${e.message}"
                    btnSteamLogin.text = "Riprova"
                    btnSteamLogin.setBackgroundResource(R.drawable.button_steam_background)
                    btnSteamLogin.isEnabled = true
                    btnDemoMode.isEnabled = true
                } finally {
                    progressBar.visibility = android.view.View.GONE
                }
            }
        }

        // Bottone Demo
        btnDemoMode.setOnClickListener {
            // Salva il nome demo
            val demoName = etDemoUsername.text.toString().trim()
            if (demoName.isNotEmpty()) {
                prefs.edit().putString("demo_player_name", demoName).apply()
                tvStatus.text = "🎮 Modalità demo attiva\nGiocatore: $demoName"
            } else {
                tvStatus.text = "🎮 Modalità demo attiva\nUsa dati di esempio"
            }

            btnSteamLogin.text = "Simula Login Steam"
            btnSteamLogin.setBackgroundResource(R.drawable.button_steam_background)
            btnSteamLogin.isEnabled = true

            // Apri DashboardActivity in modalità demo
            val intent = Intent(this, DashboardActivity::class.java)
            // Passa anche il nome demo
            intent.putExtra("DEMO_NAME", demoName)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Quando si torna indietro, riabilita i bottoni
        btnSteamLogin.isEnabled = true
        btnDemoMode.isEnabled = true
        btnSteamLogin.text = "Simula Login Steam"
        progressBar.visibility = android.view.View.GONE
    }
}