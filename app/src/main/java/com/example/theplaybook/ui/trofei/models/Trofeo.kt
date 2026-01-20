package com.example.theplaybook.ui.trofei.models

data class Trofeo(
    val id: Int,
    val nome: String,
    val descrizione: String,
    val rarita: String, // COMUNE, RARO, EPICO, LEGGENDARIO
    val isSbloccato: Boolean,
    val iconRes: Int,
    val dataSblocco: String? = null,
    val percentualeCompletamento: Float = 0f // 0-100
)