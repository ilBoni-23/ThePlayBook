package com.example.theplaybook

import android.app.Application
import android.content.Context
import com.example.theplaybook.repository.GameRepository

class ThePlayBookApp : Application() {

    companion object {
        lateinit var instance: ThePlayBookApp
            private set
    }

    // Repository accessibile globalmente
    lateinit var gameRepository: GameRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        initializeRepository()
    }

    private fun initializeRepository() {
        // Crea repository semplice senza database
        gameRepository = GameRepository(applicationContext)
    }
}