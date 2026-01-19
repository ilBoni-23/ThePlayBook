package com.example.theplaybook

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F0F13"))
            setPadding(48, 48, 48, 48)
        }

        // Titolo
        linearLayout.addView(TextView(this).apply {
            text = "🎮 ThePlayBook"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 16)
        })

        // Slogan
        linearLayout.addView(TextView(this).apply {
            text = "Tutte le tue statistiche su un'unica app"
            textSize = 16f
            setTextColor(Color.parseColor("#8C8C8C"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        })

        // Status text
        val statusText = TextView(this).apply {
            text = "Premi un pulsante"
            textSize = 14f
            setTextColor(Color.parseColor("#FF6A00"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        // Bottone Steam Login
        val steamBtn = Button(this).apply {
            text = "Simula Login Steam"
            setBackgroundColor(Color.parseColor("#171A21"))
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(48, 24, 48, 24)

            setOnClickListener {
                // Disabilita bottone durante login
                isEnabled = false
                text = "Login in corso..."
                statusText.text = "Autenticazione in corso..."

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // Usa DIRECTAMENTE SteamAuthManager
                        val authManager = com.example.theplaybook.auth.SteamAuthManager(this@MainActivity)
                        val result = authManager.signInWithSteam()

                        if (result.isSuccess) {
                            statusText.text = "✅ Login riuscito!\nSteam ID: ${result.getOrNull()}"
                            text = "Accesso effettuato"
                            setBackgroundColor(Color.parseColor("#4CAF50"))
                        } else {
                            statusText.text = "❌ Login fallito"
                            text = "Riprova"
                            isEnabled = true
                        }
                    } catch (e: Exception) {
                        statusText.text = "Errore: ${e.message}"
                        text = "Riprova"
                        isEnabled = true
                    }
                }
            }
        }

        // Bottone Demo
        val demoBtn = Button(this).apply {
            text = "Modalità Demo"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#8C8C8C"))
            textSize = 14f

            setOnClickListener {
                statusText.text = "🎮 Modalità demo attiva\nUsa dati di esempio"
                steamBtn.text = "Simula Login Steam"
                steamBtn.setBackgroundColor(Color.parseColor("#171A21"))
                steamBtn.isEnabled = true
            }
        }

        // Aggiungi elementi
        linearLayout.addView(steamBtn)
        linearLayout.addView(demoBtn)
        linearLayout.addView(statusText)

        setContentView(linearLayout)
    }
}