package com.example.theplaybook

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.theplaybook.data.local.GameDatabase
import com.example.theplaybook.data.remote.SteamApiService
import com.example.theplaybook.repository.GameRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ThePlayBookApp : Application() {

    companion object {
        lateinit var instance: ThePlayBookApp
            private set
    }

    // Repository accessibile globalmente
    lateinit var gameRepository: GameRepository
        private set

    // Database
    private lateinit var database: GameDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this

        initializeDatabase()
        initializeNetwork()
        initializeRepository()
    }

    private fun initializeDatabase() {
        database = Room.databaseBuilder(
            applicationContext,
            GameDatabase::class.java,
            "playbook_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun initializeNetwork() {
        // Ottieni API key da BuildConfig
        val apiKey = BuildConfig.STEAM_API_KEY

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val originalHttpUrl = original.url

                // Aggiungi API key a tutte le richieste
                val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("key", apiKey)
                    .addQueryParameter("format", "json")
                    .build()

                val requestBuilder = original.newBuilder()
                    .url(url)

                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val steamApiService = retrofit.create(SteamApiService::class.java)

        // Salva il servizio per uso futuro
        GameRepository.steamApiService = steamApiService
    }

    private fun initializeRepository() {
        gameRepository = GameRepository(
            dao = database.gameDao(),
            context = applicationContext
        )
    }

    // Metodi helper per ottenere istanze
    fun getGameDatabase(): GameDatabase = database
}