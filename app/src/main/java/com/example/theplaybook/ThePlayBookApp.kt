package com.example.theplaybook

import android.app.Application
import com.example.theplaybook.data.RepositoryFactory

class ThePlayBookApp : Application() {

    companion object {
        // Mantieni per avere accesso al context
        lateinit var instance: ThePlayBookApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Eventuale inizializzazione qui
        // Esempio: imposta default mode per RepositoryFactory
        RepositoryFactory.toggleMockMode(true) // O false per API reali
    }
}