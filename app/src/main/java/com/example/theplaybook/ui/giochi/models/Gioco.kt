package com.example.theplaybook.ui.giochi.models

data class Gioco(
    val id: Long,
    val nome: String,
    val descrizione: String,
    val oreGiocate: Int, // in minuti
    val iconRes: Int,
    val isInstalled: Boolean = false,
    val lastPlayed: String? = null,
    val completionRate: Float = 0f // 0-100
) {
    val oreGiocateFormatted: String
        get() = "${oreGiocate / 60}h"
}