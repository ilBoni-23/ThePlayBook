package com.example.theplaybook.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.theplaybook.R
import com.example.theplaybook.data.RepositoryFactory
import com.example.theplaybook.data.mock.MockPlayer
import com.example.theplaybook.data.remote.SteamApiService
import com.example.theplaybook.data.remote.models.OwnedGamesResponse
import com.example.theplaybook.data.remote.models.PlayerSummary
import com.example.theplaybook.data.remote.models.SteamAchievement
import com.example.theplaybook.data.remote.models.SteamGame
import com.example.theplaybook.databinding.ActivityDashboardBinding
import com.example.theplaybook.ui.amici.AmiciActivity
import com.example.theplaybook.ui.calendario.CalendarioActivity
import com.example.theplaybook.ui.giochi.GiochiActivity
import com.example.theplaybook.ui.profilo.ProfiloActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var repository: SteamApiService

    // Variabili per gestire il caching dei dati
    private var isDataLoaded = false
    private var currentSteamId: String? = null
    private var currentDemoName: String? = null
    private var isMockMode = false
    private var isDemoMode = false
    private var lastLoadedTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000 // 5 minuti in millisecondi

    // NUOVA CLASSE PER GIOCO CON CATEGORIA
    data class GameWithCategory(
        val game: SteamGame,
        val category: String,
        val subcategories: List<String> = emptyList()
    )

    // NUOVA CLASSE PER ACHIEVEMENT CON GIOCO
    data class AchievementWithGame(
        val achievement: SteamAchievement,
        val gameName: String,
        val gameId: Long,
        val gameCategory: String, // AGGIUNTO: categoria del gioco
        val isCompleted: Boolean
    )

    // NUOVA CLASSE PER STATISTICHE CATEGORIE
    data class CategoryStats(
        val category: String,
        val totalHours: Float,
        val gameCount: Int,
        val percentage: Float
    )

    // Variabili per memorizzare i dati correnti
    private var currentTotalPlaytimeHours: Float = 0f
    private var currentTotalGames: Int = 0
    private var currentCategoryStats: List<CategoryStats> = emptyList()
    private var currentTopCategory: CategoryStats? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inizializza repository
        repository = RepositoryFactory.createSteamRepository()
        isMockMode = RepositoryFactory.isMockMode()

        // Ottieni Steam ID o usa demo
        currentSteamId = intent.getStringExtra("STEAM_ID") ?: MockPlayer.PLAYER_1.steamId
        currentDemoName = intent.getStringExtra("DEMO_NAME")
        isDemoMode = intent.getBooleanExtra("IS_DEMO_MODE", false) || currentDemoName != null

        // Setup UI
        setupUI()

        // Setup observers per ViewModel
        setupViewModelObservers()

        // Carica dati solo se necessario
        if (shouldLoadData()) {
            caricaDatiCompleta(currentSteamId!!, currentDemoName)
        } else {
            // Mostra dati esistenti se disponibili
            showExistingDataIfAvailable()
        }
    }

    private fun setupUI() {
        // Tasto Indietro
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Avatar click -> Profilo
        binding.ivAvatar.setOnClickListener {
            val intent = Intent(this, ProfiloActivity::class.java)
            startActivity(intent)
        }

        // Switch Mock Mode
        binding.switchMockMode.setOnCheckedChangeListener { _, isChecked ->
            RepositoryFactory.toggleMockMode(isChecked)
            repository = RepositoryFactory.createSteamRepository()
            isMockMode = isChecked

            Snackbar.make(
                binding.root,
                if (isChecked) "🎮 Modalità Mock attiva" else "🌐 Modalità Reale attiva",
                Snackbar.LENGTH_SHORT
            ).show()

            // Forza ricaricamento quando cambia la modalità
            isDataLoaded = false
            val steamId = currentSteamId ?: MockPlayer.PLAYER_1.steamId
            val demoName = currentDemoName
            caricaDatiCompleta(steamId, demoName)
        }

        // Bottone Aggiorna
        binding.btnRefresh.setOnClickListener {
            // Forza il ricaricamento completo
            isDataLoaded = false
            lastLoadedTime = 0

            val steamId = currentSteamId ?: MockPlayer.PLAYER_1.steamId
            val demoName = currentDemoName
            caricaDatiCompleta(steamId, demoName)

            Snackbar.make(
                binding.root,
                "🔄 Aggiornamento dati...",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Bottom Navigation
        binding.btnHome.setOnClickListener {
            // Se siamo già in Dashboard, non ricaricare se i dati sono già presenti
            if (!isDataLoaded) {
                val steamId = currentSteamId ?: MockPlayer.PLAYER_1.steamId
                val demoName = currentDemoName
                caricaDatiCompleta(steamId, demoName)
            } else {
                Snackbar.make(
                    binding.root,
                    "🏠 Dashboard",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnGames.setOnClickListener {
            val intent = Intent(this, GiochiActivity::class.java)
            startActivity(intent)
        }

        binding.btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewModelObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DashboardUiState.Loading -> showLoading(true)
                is DashboardUiState.Success -> {
                    showLoading(false)
                    isDataLoaded = true
                    lastLoadedTime = System.currentTimeMillis()
                    aggiornaUIDaViewModel(state.data)
                }
                is DashboardUiState.Error -> {
                    showLoading(false)
                    Snackbar.make(
                        binding.root,
                        "⚠️ ${state.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.isMockMode.observe(this) { isMock ->
            binding.switchMockMode.isChecked = isMock
        }
    }

    // ==================== LOGICA DI CACHING ====================

    private fun shouldLoadData(): Boolean {
        // Se non sono mai stati caricati dati
        if (!isDataLoaded) return true

        // In modalità demo, non ricaricare a meno che non sia esplicitamente richiesto
        if (isDemoMode) return false

        // Se i dati sono più vecchi di CACHE_DURATION
        if (System.currentTimeMillis() - lastLoadedTime > CACHE_DURATION) return true

        // Se è cambiata la modalità mock
        val currentMockMode = RepositoryFactory.isMockMode()
        if (currentMockMode != isMockMode) return true

        return false
    }

    private fun showExistingDataIfAvailable() {
        // Se i dati sono già caricati, mostra quelli esistenti
        if (isDataLoaded) {
            Snackbar.make(
                binding.root,
                "📱 Usando dati in cache",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    // ==================== METODO PRINCIPALE PER CARICARE DATI ====================

    private fun caricaDatiCompleta(steamId: String, demoName: String? = null) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1. Carica profilo utente
                val playerData = caricaProfiloUtente(steamId)

                // 2. Carica giochi posseduti
                val gamesData = caricaGiochi(steamId)

                // 3. Aggiungi categorie ai giochi
                val gamesWithCategories = aggiungiCategorieAiGiochi(gamesData.games)

                // 4. Calcola statistiche
                currentTotalPlaytimeHours = gamesData.games.sumOf { it.playtimeForever } / 60f
                currentTotalGames = gamesData.gameCount

                val recentGames = gamesData.games
                    .filter { it.rtimeLastPlayed != null }
                    .sortedByDescending { it.rtimeLastPlayed }
                    .take(5)

                // 5. Calcola statistiche per categorie
                currentCategoryStats = calcolaStatisticheCategorie(gamesWithCategories)
                currentTopCategory = currentCategoryStats.maxByOrNull { it.totalHours }

                // 6. Carica achievement per primi 3 giochi CON NOME DEL GIOCO E CATEGORIA
                val achievementsWithGames = caricaAchievementsConGioco(steamId, gamesWithCategories.take(3))

                // 7. Calcola completion rate
                val completionRate = calcolaCompletionRate(achievementsWithGames)

                // 8. Aggiorna UI
                aggiornaUICompleta(
                    playerData = playerData,
                    totalPlaytimeHours = currentTotalPlaytimeHours,
                    totalGames = currentTotalGames,
                    recentGames = recentGames,
                    gamesWithCategories = gamesWithCategories.take(5), // Mostra primi 5 giochi con categorie
                    achievementsWithGames = achievementsWithGames.take(5),
                    completionRate = completionRate,
                    demoName = demoName,
                    isMockMode = RepositoryFactory.isMockMode()
                )

                showLoading(false)
                isDataLoaded = true
                lastLoadedTime = System.currentTimeMillis()

                Snackbar.make(
                    binding.root,
                    "✅ Dati caricati con successo!",
                    Snackbar.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                showLoading(false)
                Snackbar.make(
                    binding.root,
                    "❌ Errore: ${e.localizedMessage}",
                    Snackbar.LENGTH_LONG
                ).show()

                // Fallback a dati mock
                caricaDatiFallback(steamId, demoName)
            }
        }
    }

    // ==================== METODI AUSILIARI ====================

    private suspend fun caricaProfiloUtente(steamId: String): PlayerSummary {
        return withContext(Dispatchers.IO) {
            try {
                val response = repository.getPlayerSummaries(steamId)
                if (response.response.players.isNotEmpty()) {
                    response.response.players.first()
                } else {
                    // Fallback
                    MockPlayer.PLAYER_1.copy(steamId = steamId)
                }
            } catch (e: Exception) {
                // Fallback in caso di errore
                MockPlayer.PLAYER_1.copy(
                    steamId = steamId,
                    personaName = "Utente Steam"
                )
            }
        }
    }

    private suspend fun caricaGiochi(steamId: String): OwnedGamesResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = repository.getOwnedGames(steamId)
                response.response
            } catch (e: Exception) {
                // Fallback a dati mock limitati
                OwnedGamesResponse(
                    gameCount = 8,
                    games = MockGames.ALL_GAMES.take(8)
                )
            }
        }
    }

    // NUOVO METODO: Aggiunge categorie ai giochi basandosi sul nome o altre caratteristiche
    private fun aggiungiCategorieAiGiochi(games: List<SteamGame>): List<GameWithCategory> {
        return games.map { game ->
            val category = determinaCategoria(game.name, game.playtimeForever)
            val subcategories = determinaSottoCategorie(game.name, category)

            GameWithCategory(
                game = game,
                category = category,
                subcategories = subcategories
            )
        }
    }

    // METODO: Determina la categoria principale del gioco
    private fun determinaCategoria(gameName: String, playtime: Int): String {
        val name = gameName.lowercase()

        return when {
            name.contains("counter-strike") || name.contains("cs") ||
                    name.contains("call of duty") || name.contains("cod") ||
                    name.contains("valorant") || name.contains("overwatch") ||
                    name.contains("apex") || name.contains("rainbow six") -> "FPS"

            name.contains("dota") || name.contains("league of legends") ||
                    name.contains("lol") || name.contains("smite") ||
                    name.contains("heroes of the storm") -> "MOBA"

            name.contains("elden ring") || name.contains("dark souls") ||
                    name.contains("sekiro") || name.contains("bloodborne") ||
                    name.contains("monster hunter") || name.contains("nioh") -> "Souls-like"

            name.contains("cyberpunk") || name.contains("witcher") ||
                    name.contains("skyrim") || name.contains("fallout") ||
                    name.contains("mass effect") || name.contains("dragon age") -> "RPG"

            name.contains("fifa") || name.contains("nba") ||
                    name.contains("madden") || name.contains("pes") ||
                    name.contains("football manager") -> "Sport"

            name.contains("minecraft") || name.contains("terraria") ||
                    name.contains("stardew valley") || name.contains("factorio") -> "Sandbox"

            name.contains("civilization") || name.contains("cities: skylines") ||
                    name.contains("total war") || name.contains("crusader kings") -> "Strategy"

            name.contains("racing") || name.contains("need for speed") ||
                    name.contains("forza") || name.contains("gran turismo") -> "Racing"

            name.contains("resident evil") || name.contains("silent hill") ||
                    name.contains("outlast") || name.contains("amnesia") -> "Horror"

            name.contains("grand theft auto") || name.contains("gta") ||
                    name.contains("red dead") || name.contains("watch dogs") -> "Open World"

            else -> "Altro"
        }
    }

    // METODO: Determina sotto-categorie del gioco
    private fun determinaSottoCategorie(gameName: String, mainCategory: String): List<String> {
        val name = gameName.lowercase()
        val subcategories = mutableListOf<String>()

        // Aggiungi caratteristiche basate sul nome
        if (name.contains("multiplayer") || name.contains("online")) {
            subcategories.add("Multiplayer")
        }
        if (name.contains("singleplayer") || name.contains("campaign")) {
            subcategories.add("Singleplayer")
        }
        if (name.contains("co-op") || name.contains("coop")) {
            subcategories.add("Co-op")
        }
        if (name.contains("competitive") || name.contains("ranked")) {
            subcategories.add("Competitive")
        }
        if (name.contains("story") || name.contains("narrative")) {
            subcategories.add("Story-rich")
        }
        if (name.contains("open world") || name.contains("exploration")) {
            subcategories.add("Open World")
        }

        return subcategories
    }

    // NUOVO METODO: Calcola statistiche per categorie
    private fun calcolaStatisticheCategorie(gamesWithCategories: List<GameWithCategory>): List<CategoryStats> {
        val categoryMap = mutableMapOf<String, MutableList<GameWithCategory>>()

        // Raggruppa giochi per categoria
        gamesWithCategories.forEach { gameWithCategory ->
            val category = gameWithCategory.category
            if (!categoryMap.containsKey(category)) {
                categoryMap[category] = mutableListOf()
            }
            categoryMap[category]?.add(gameWithCategory)
        }

        // Calcola ore totali per categoria
        val totalHoursAll = gamesWithCategories.sumOf { it.game.playtimeForever } / 60f

        return categoryMap.map { (category, games) ->
            val totalHours = games.sumOf { it.game.playtimeForever } / 60f
            val percentage = if (totalHoursAll > 0) (totalHours / totalHoursAll * 100) else 0f

            CategoryStats(
                category = category,
                totalHours = totalHours,
                gameCount = games.size,
                percentage = percentage
            )
        }.sortedByDescending { it.totalHours }
    }

    // METODO MODIFICATO: Carica achievement con nome del gioco E categoria
    private suspend fun caricaAchievementsConGioco(
        steamId: String,
        gamesWithCategories: List<GameWithCategory>
    ): List<AchievementWithGame> {
        return withContext(Dispatchers.IO) {
            val achievementsWithGames = mutableListOf<AchievementWithGame>()

            gamesWithCategories.forEach { gameWithCategory ->
                try {
                    val response = repository.getPlayerAchievements(steamId, gameWithCategory.game.appId)
                    response.response.playerStats.achievements?.let { gameAchievements ->
                        // Prendi 2 achievement per gioco (1 completato, 1 da completare)
                        val achievementsToAdd = gameAchievements
                            .sortedBy { it.achieved } // Prima quelli da completare (achieved = 0)
                            .take(2)

                        achievementsToAdd.forEach { achievement ->
                            achievementsWithGames.add(
                                AchievementWithGame(
                                    achievement = achievement,
                                    gameName = gameWithCategory.game.name,
                                    gameId = gameWithCategory.game.appId,
                                    gameCategory = gameWithCategory.category, // AGGIUNTA CATEGORIA
                                    isCompleted = achievement.achieved == 1
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Se non riesci a caricare gli achievement per questo gioco,
                    // aggiungi achievement mock per questo gioco
                    achievementsWithGames.addAll(
                        createMockAchievementsForGame(
                            gameWithCategory.game.name,
                            gameWithCategory.game.appId,
                            gameWithCategory.category
                        )
                    )
                }
            }

            // Se non ci sono achievement, aggiungi alcuni mock
            if (achievementsWithGames.isEmpty()) {
                achievementsWithGames.addAll(
                    createMockAchievements()
                )
            }

            achievementsWithGames
        }
    }

    private fun createMockAchievementsForGame(gameName: String, gameId: Long, category: String): List<AchievementWithGame> {
        return listOf(
            AchievementWithGame(
                achievement = SteamAchievement(
                    apiName = "MOCK_1_$gameId",
                    achieved = 0,
                    unlockTime = null,
                    name = "Primo Passo in $gameName",
                    description = "Completa il primo livello"
                ),
                gameName = gameName,
                gameId = gameId,
                gameCategory = category,
                isCompleted = false
            ),
            AchievementWithGame(
                achievement = SteamAchievement(
                    apiName = "MOCK_2_$gameId",
                    achieved = 1,
                    unlockTime = System.currentTimeMillis() / 1000 - 86400,
                    name = "Esploratore di $gameName",
                    description = "Scopri tutte le aree"
                ),
                gameName = gameName,
                gameId = gameId,
                gameCategory = category,
                isCompleted = true
            )
        )
    }

    private fun createMockAchievements(): List<AchievementWithGame> {
        return listOf(
            AchievementWithGame(
                achievement = SteamAchievement(
                    apiName = "MOCK_CS2_1",
                    achieved = 0,
                    unlockTime = null,
                    name = "Ace Round",
                    description = "Uccidi tutti e 5 i nemici in un round"
                ),
                gameName = "Counter-Strike 2",
                gameId = 730,
                gameCategory = "FPS",
                isCompleted = false
            ),
            AchievementWithGame(
                achievement = SteamAchievement(
                    apiName = "MOCK_CS2_2",
                    achieved = 1,
                    unlockTime = System.currentTimeMillis() / 1000 - 172800,
                    name = "First Blood",
                    description = "Ottieni la prima uccisione in una partita"
                ),
                gameName = "Counter-Strike 2",
                gameId = 730,
                gameCategory = "FPS",
                isCompleted = true
            ),
            AchievementWithGame(
                achievement = SteamAchievement(
                    apiName = "MOCK_ELDEN_1",
                    achieved = 0,
                    unlockTime = null,
                    name = "Elden Lord",
                    description = "Sconfiggi il boss finale"
                ),
                gameName = "Elden Ring",
                gameId = 1245620,
                gameCategory = "Souls-like",
                isCompleted = false
            ),
            AchievementWithGame(
                achievement = SteamAchievement(
                    apiName = "MOCK_CYBERPUNK_1",
                    achieved = 1,
                    unlockTime = System.currentTimeMillis() / 1000 - 259200,
                    name = "Night City Legend",
                    description = "Completa la storia principale"
                ),
                gameName = "Cyberpunk 2077",
                gameId = 1091500,
                gameCategory = "RPG",
                isCompleted = true
            )
        )
    }

    private fun calcolaCompletionRate(achievements: List<AchievementWithGame>): Float {
        return if (achievements.isNotEmpty()) {
            val total = achievements.size
            val completed = achievements.count { it.isCompleted }
            (completed.toFloat() / total * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    private fun caricaDatiFallback(steamId: String, demoName: String?) {
        // Usa dati mock completi in caso di fallimento
        val mockPlayer = MockPlayer.PLAYER_1.copy(steamId = steamId)
        val mockGames = MockGames.ALL_GAMES.take(10)
        val gamesWithCategories = aggiungiCategorieAiGiochi(mockGames)
        currentCategoryStats = calcolaStatisticheCategorie(gamesWithCategories)
        currentTopCategory = currentCategoryStats.maxByOrNull { it.totalHours }
        currentTotalPlaytimeHours = 342.5f
        currentTotalGames = mockGames.size
        val mockAchievements = createMockAchievements()

        aggiornaUICompleta(
            playerData = mockPlayer,
            totalPlaytimeHours = currentTotalPlaytimeHours,
            totalGames = currentTotalGames,
            recentGames = mockGames.take(5),
            gamesWithCategories = gamesWithCategories.take(5),
            achievementsWithGames = mockAchievements.take(5),
            completionRate = 65.5f,
            demoName = demoName,
            isMockMode = true
        )

        isDataLoaded = true
        lastLoadedTime = System.currentTimeMillis()

        Snackbar.make(
            binding.root,
            "⚠️ Usando dati demo",
            Snackbar.LENGTH_LONG
        ).show()
    }

    // ==================== AGGIORNAMENTO UI ====================

    private fun aggiornaUICompleta(
        playerData: PlayerSummary,
        totalPlaytimeHours: Float,
        totalGames: Int,
        recentGames: List<SteamGame>,
        gamesWithCategories: List<GameWithCategory>,
        achievementsWithGames: List<AchievementWithGame>,
        completionRate: Float,
        demoName: String?,
        isMockMode: Boolean
    ) {
        // 1. Header - Profilo
        binding.tvPlayerName.text = demoName ?: playerData.personaName
        binding.tvSteamId.text = "Steam ID: ${playerData.steamId.take(12)}..."

        // 2. Avatar
        caricaAvatar(playerData.avatarFull)

        // 3. Statistiche principali
        binding.tvTotalHours.text = "%.1f".format(totalPlaytimeHours)
        binding.tvTotalGames.text = totalGames.toString()
        binding.tvCompletion.text = "%.1f%%".format(completionRate)

        // 4. Giochi recenti CON CATEGORIE
        aggiornaGiochiRecentiConCategorie(gamesWithCategories)

        // 5. Achievement quasi completati CON CATEGORIA GIOCO
        aggiornaAchievementConGiocoECategoria(achievementsWithGames)

        // 6. Statistiche categorie (mostra in un Toast per ora)
        mostraStatisticheCategorie()

        // 7. Indicatore modalità
        aggiornaIndicatoreModalita(isMockMode)
    }

    private fun aggiornaUIDaViewModel(data: DashboardData) {
        // Versione che usa i dati dal ViewModel
        binding.tvPlayerName.text = data.playerName
        binding.tvSteamId.text = "Steam ID: ${data.steamId.take(12)}..."
        binding.tvTotalHours.text = "%.1f".format(data.totalPlaytimeHours)
        binding.tvTotalGames.text = data.totalGames.toString()
        binding.tvCompletion.text = "%.1f%%".format(data.completionRate)

        caricaAvatar(data.avatarUrl)
        aggiornaGiochiRecenti(data.recentGames)
        aggiornaIndicatoreModalita(data.isMockData)
    }

    private fun caricaAvatar(avatarUrl: String) {
        if (avatarUrl.isNotEmpty()) {
            try {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } catch (e: Exception) {
                binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    // NUOVO METODO: Aggiorna giochi recenti con categorie
    private fun aggiornaGiochiRecentiConCategorie(gamesWithCategories: List<GameWithCategory>) {
        binding.tvRecentGames.text = if (gamesWithCategories.isEmpty()) {
            "Nessun gioco recente"
        } else {
            gamesWithCategories.joinToString("\n") { gameWithCategory ->
                val game = gameWithCategory.game
                val category = gameWithCategory.category
                val subcategories = if (gameWithCategory.subcategories.isNotEmpty()) {
                    " [${gameWithCategory.subcategories.joinToString(", ")}]"
                } else ""

                "🎮 ${game.name}\n   🏷️ $category$subcategories\n   ⏱️ ${game.playtimeForever / 60}h"
            }
        }
    }

    // Vecchio metodo mantenuto per compatibilità
    private fun aggiornaGiochiRecenti(games: List<SteamGame>) {
        binding.tvRecentGames.text = if (games.isEmpty()) {
            "Nessun gioco recente"
        } else {
            games.joinToString("\n") { game ->
                "🎮 ${game.name}\n   ⏱️ ${game.playtimeForever / 60}h"
            }
        }
    }

    // NUOVO METODO: Aggiorna achievement con nome del gioco E categoria
    private fun aggiornaAchievementConGiocoECategoria(achievementsWithGames: List<AchievementWithGame>) {
        binding.tvNearlyComplete.text = if (achievementsWithGames.isEmpty()) {
            "Nessun achievement in progress"
        } else {
            achievementsWithGames.take(5).joinToString("\n") { achievementWithGame ->
                val icon = if (achievementWithGame.isCompleted) "✅" else "🎯"
                val status = if (achievementWithGame.isCompleted) "Completato" else "Da completare"
                val game = achievementWithGame.gameName
                val category = achievementWithGame.gameCategory
                val achievementName = achievementWithGame.achievement.name

                "$icon $achievementName\n   🎮 $game (🏷️ $category)\n   📝 $status"
            }
        }
    }

    // Vecchio metodo mantenuto per compatibilità
    private fun aggiornaAchievement(achievements: List<SteamAchievement>) {
        binding.tvNearlyComplete.text = if (achievements.isEmpty()) {
            "Nessun achievement in progress"
        } else {
            achievements.take(5).joinToString("\n") { achievement ->
                val icon = if (achievement.achieved == 1) "✅" else "🎯"
                val status = if (achievement.achieved == 1) "Completato" else "Da completare"
                "$icon ${achievement.name}\n   📝 $status"
            }
        }
    }

    // NUOVO METODO: Mostra statistiche categorie in un Toast
    private fun mostraStatisticheCategorie() {
        if (currentTopCategory != null) {
            val statsText = buildString {
                append("📊 STATISTICHE CATEGORIE\n\n")

                append("🏆 Categoria più giocata:\n")
                append("   ${currentTopCategory!!.category}\n")
                append("   ⏱️ ${"%.1f".format(currentTopCategory!!.totalHours)}h (${"%.0f".format(currentTopCategory!!.percentage)}%)\n\n")

                append("Top Categorie:\n")
                currentCategoryStats.take(3).forEachIndexed { index, stats ->
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "•"
                    }
                    append("$medal ${stats.category}: ${"%.1f".format(stats.totalHours)}h (${stats.gameCount} giochi)\n")
                }
            }

            // Mostra in un Toast
            Toast.makeText(
                this,
                "Categoria più giocata: ${currentTopCategory!!.category} (${"%.1f".format(currentTopCategory!!.totalHours)}h)",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun aggiornaIndicatoreModalita(isMockMode: Boolean) {
        binding.switchMockMode.isChecked = isMockMode
        binding.switchMockMode.text = if (isMockMode) "Mock Mode ON" else "Mock Mode OFF"
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.scrollView.isVisible = !show
    }

    // ==================== LIFECYCLE ====================

    override fun onResume() {
        super.onResume()

        // In modalità demo, NON ricaricare i dati automaticamente
        if (isDemoMode && isDataLoaded) {
            return
        }

        // Altrimenti, controlla se è necessario ricaricare
        if (shouldLoadData() && !binding.progressBar.isVisible) {
            val steamId = currentSteamId ?: MockPlayer.PLAYER_1.steamId
            val demoName = currentDemoName
            caricaDatiCompleta(steamId, demoName)
        }
    }
}

// ==================== CLASSI AUSILIARIE LOCALI ====================

object MockGames {
    val ALL_GAMES = listOf(
        SteamGame(730, "Counter-Strike 2", 1250, 65, "icon1", "logo1", true, System.currentTimeMillis()/1000 - 86400),
        SteamGame(570, "Dota 2", 890, 42, "icon2", "logo2", true, System.currentTimeMillis()/1000 - 172800),
        SteamGame(271590, "GTA V", 456, 23, "icon3", "logo3", true, System.currentTimeMillis()/1000 - 2592000),
        SteamGame(1245620, "Elden Ring", 234, 78, "icon4", "logo4", true, System.currentTimeMillis()/1000 - 345600),
        SteamGame(1091500, "Cyberpunk 2077", 189, 45, "icon5", "logo5", true, System.currentTimeMillis()/1000 - 1728000),
        SteamGame(1172470, "Apex Legends", 654, 55, "icon6", "logo6", true, System.currentTimeMillis()/1000 - 432000),
        SteamGame(255710, "Minecraft", 789, 85, "icon7", "logo7", true, System.currentTimeMillis()/1000 - 864000),
        SteamGame(292030, "The Witcher 3", 345, 95, "icon8", "logo8", true, System.currentTimeMillis()/1000 - 1296000)
    )
}

object MockAchievements {
    fun getForGame(appId: Long): List<SteamAchievement> {
        return listOf(
            SteamAchievement("ACH_1", 1, System.currentTimeMillis()/1000 - 864000, "First Blood", "Get first kill"),
            SteamAchievement("ACH_2", 0, null, "Master", "Complete all levels"),
            SteamAchievement("ACH_3", 1, System.currentTimeMillis()/1000 - 1728000, "Explorer", "Discover all areas"),
            SteamAchievement("ACH_4", 0, null, "Speedrun", "Finish under 10 hours"),
            SteamAchievement("ACH_5", 1, System.currentTimeMillis()/1000 - 2592000, "Collector", "Get all items")
        )
    }
}