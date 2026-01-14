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

    // Dichiarazione della variabile status come proprietà della classe
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout programmatico
        val linearLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F0F13"))
            setPadding(32, 32, 32, 32)
        }

        // Titolo
        val title = TextView(this).apply {
            text = "🎮 ThePlayBook"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        // Slogan
        val slogan = TextView(this).apply {
            text = "Tutte le tue statistiche su un'unica app"
            textSize = 16f
            setTextColor(Color.parseColor("#8C8C8C"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        // Status TextView - ora dichiarato correttamente
        statusTextView = TextView(this).apply {
            text = "Premi il pulsante per simulare login"
            textSize = 14f
            setTextColor(Color.parseColor("#FF6A00"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        // Bottone Login
        val loginBtn = Button(this).apply {
            text = "Simula Login Steam"
            setBackgroundColor(Color.parseColor("#171A21"))
            setTextColor(Color.WHITE)
            setPadding(32, 16, 32, 16)

            setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val authManager = com.example.theplaybook.auth.SteamAuthManager(this@MainActivity)
                    val result = authManager.signInWithSteam()

                    if (result.isSuccess) {
                        // Usa statusTextView invece di status
                        statusTextView.text = "✅ Login riuscito!\nSteam ID: ${result.getOrNull()}"
                    } else {
                        statusTextView.text = "❌ Login fallito"
                    }
                }
            }
        }

        // Aggiungi elementi nell'ordine corretto
        linearLayout.addView(title)
        linearLayout.addView(slogan)
        linearLayout.addView(loginBtn)
        linearLayout.addView(statusTextView)  // Aggiungi dopo averlo creato

        setContentView(linearLayout)
    }
}