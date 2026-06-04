package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.JsonClass

class BourbonViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BourbonDatabase.getDatabase(application)
    private val repository = BourbonRepository(database.bourbonDao())

    val allBottles: StateFlow<List<Bottle>> = repository.allBottles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<Review>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubBottles: StateFlow<List<SubBottle>> = repository.allSubBottles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlinds: StateFlow<List<Blind>> = repository.allBlinds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlindReveals: StateFlow<List<BlindReveal>> = repository.allBlindReveals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    var appTheme by mutableStateOf("Elegant Dark")

    // Navigation selection states
    var activeBottleId by mutableStateOf<Long?>(null)
    var activeSubBottleId by mutableStateOf<Long?>(null)
    var activeReadOnlyReview by mutableStateOf<Review?>(null)
    var activeCompletedBlindId by mutableStateOf<Long?>(null)

    fun getBottleNameSuggestions(query: String): List<Bottle> {
        if (query.isBlank()) return emptyList()
        return allBottles.value.filter {
            it.name.contains(query, ignoreCase = true)
        }.take(5)
    }

    fun getNicknameSuggestions(query: String): List<Bottle> {
        if (query.isBlank()) return emptyList()
        return allBottles.value.filter {
            it.nickname.contains(query, ignoreCase = true)
        }.take(5)
    }

    // --- State for reviewing flow ---
    var reviewBottleName by mutableStateOf("")
    var reviewNickname by mutableStateOf("")
    var selectedAutoCompleteBottle by mutableStateOf<Bottle?>(null)

    // Bottle Info inputs
    var infoDistillery by mutableStateOf("")
    var infoAge by mutableStateOf("")
    var infoProof by mutableStateOf("")
    var infoCategory by mutableStateOf("Bourbon") // Default
    var infoFinish by mutableStateOf("")
    var infoOther by mutableStateOf("")

    // Owned inputs
    var ownedSizeMl by mutableStateOf("")
    var ownedPrice by mutableStateOf("")
    var ownedBoughtWhen by mutableStateOf("")
    var ownedBoughtWhere by mutableStateOf("")
    var ownedOther by mutableStateOf("")

    // Pour inputs
    var pourWhen by mutableStateOf("")
    var pourWhere by mutableStateOf("")
    var pourPrice by mutableStateOf("0")
    var pourPriceReadOnly by mutableStateOf(false)
    var pourDrunkScore by mutableStateOf(4)
    var pourAmountOz by mutableStateOf("")
    var pourAmountMl by mutableStateOf("")
    var pourGlass by mutableStateOf("")
    var pourIce by mutableStateOf(false)
    var pourOther by mutableStateOf("")

    // Nose inputs
    var noseNotes by mutableStateOf("")
    var noseScore by mutableStateOf(4)

    // Palate inputs
    var palateNotes by mutableStateOf("")
    var palateScore by mutableStateOf(4)

    // Finish inputs
    var finishNotes by mutableStateOf("")
    var finishScore by mutableStateOf(4)

    // Profile Flavor state (10 dimensions)
    val flavorScores = mutableStateMapOf<String, Int>().apply {
        put("Nutty", 4)
        put("Woody", 4)
        put("Heat", 4)
        put("Herbal", 4)
        put("Citrusy", 4)
        put("Stone Fruity", 4)
        put("Smokey", 4)
        put("Sour", 4)
        put("Sweet", 4)
        put("Savory", 4)
    }

    // Overall inputs
    var overallNotes by mutableStateOf("")
    var overallScore by mutableStateOf(4)

    // Flow path trackers
    var isNewBottleFlow by mutableStateOf(false)
    var isNewBottleOwned by mutableStateOf(false)
    var reReviewType by mutableStateOf("") // "Same Bottle", "Re-Purchase", "Single Pour"
    var directReviewSubBottleId by mutableStateOf<Long?>(null)

    // Clear Review forms
    fun clearReviewFlow() {
        reviewBottleName = ""
        reviewNickname = ""
        selectedAutoCompleteBottle = null
        isNewBottleOwned = false
        directReviewSubBottleId = null
        infoDistillery = ""
        infoAge = ""
        infoProof = ""
        infoCategory = "Bourbon"
        infoFinish = ""
        infoOther = ""
        ownedSizeMl = ""
        ownedPrice = ""
        ownedBoughtWhen = ""
        ownedBoughtWhere = ""
        ownedOther = ""
        pourWhen = ""
        pourWhere = ""
        pourPrice = "0"
        pourPriceReadOnly = false
        pourDrunkScore = 4
        pourAmountOz = ""
        pourAmountMl = ""
        pourGlass = ""
        pourIce = false
        pourOther = ""
        noseNotes = ""
        noseScore = 4
        palateNotes = ""
        palateScore = 4
        finishNotes = ""
        finishScore = 4
        overallNotes = ""
        overallScore = 4
        flavorScores.forEach { (k, _) -> flavorScores[k] = 4 }
    }

    // Triggered when user selects a bottle/nickname from autocomplete
    fun selectExistingReviewBottle(bottle: Bottle) {
        selectedAutoCompleteBottle = bottle
        reviewBottleName = bottle.name
        reviewNickname = bottle.nickname
        infoDistillery = bottle.distillery
        infoAge = bottle.age
        infoProof = bottle.proof
        infoCategory = bottle.category
        infoFinish = bottle.finish
        infoOther = bottle.other
    }

    // Save bottle/reviews to Room
    fun saveReview(onComplete: () -> Unit) {
        viewModelScope.launch {
            val bottleId: Long
            val existing = selectedAutoCompleteBottle

            if (existing != null) {
                bottleId = existing.id
            } else {
                val targetName = reviewBottleName.trim()
                val bottlesList = repository.getAllBottlesSync()
                val matched = bottlesList.find { it.name.trim().equals(targetName, ignoreCase = true) }
                if (matched != null) {
                    bottleId = matched.id
                } else {
                    // Create a completely new bottle
                    val newBottle = Bottle(
                        name = targetName,
                        nickname = reviewNickname.trim(),
                        distillery = infoDistillery.trim(),
                        age = infoAge.trim(),
                        proof = infoProof.trim(),
                        category = infoCategory,
                        finish = infoFinish.trim(),
                        other = infoOther.trim()
                    )
                    bottleId = repository.createBottleWithSubBottles(newBottle)
                }
            }

            // Determine correct sub-bottle
            var targetSubBottleId: Long = 0L

            if (directReviewSubBottleId != null) {
                targetSubBottleId = directReviewSubBottleId!!
            } else if (reReviewType == "Single Pour") {
                // Target the unowned catch-all (SubBottle 0)
                val sub0 = repository.getSubBottleByNumber(bottleId, 0)
                if (sub0 == null) {
                    val s0 = SubBottle(
                        bottleId = bottleId,
                        subBottleNumber = 0,
                        sizeMl = "N/A",
                        price = "N/A",
                        boughtWhen = "N/A",
                        boughtWhere = "N/A",
                        other = "Unowned Default Catch-All"
                    )
                    targetSubBottleId = repository.insertSubBottle(s0)
                } else {
                    targetSubBottleId = sub0.subBottleId
                }
            } else if (reReviewType == "Re-Purchase" || (isNewBottleFlow && isNewBottleOwned)) {
                // Fetch existing sub-bottles to determine sequence number
                val list = repository.getSubBottlesForBottleSync(bottleId)
                val count = list.filter { it.subBottleNumber >= 1 }.size + 1
                val newSub = SubBottle(
                    bottleId = bottleId,
                    subBottleNumber = count,
                    sizeMl = ownedSizeMl.ifEmpty { "N/A" },
                    price = ownedPrice.ifEmpty { "N/A" },
                    boughtWhen = ownedBoughtWhen.ifEmpty { "N/A" },
                    boughtWhere = ownedBoughtWhere.ifEmpty { "N/A" },
                    other = ownedOther
                )
                targetSubBottleId = repository.insertSubBottle(newSub)
            } else {
                // Default: same bottle, pick highest subsequence >= 1, if none exists fallback to subbottle 0
                val list = repository.getSubBottlesForBottleSync(bottleId)
                val sortedOwned = list.filter { it.subBottleNumber >= 1 }.sortedByDescending { it.subBottleNumber }
                val chosenSubId = if (sortedOwned.isNotEmpty()) {
                    sortedOwned.first().subBottleId
                } else {
                    val fallbackSub0 = list.firstOrNull { it.subBottleNumber == 0 }
                    if (fallbackSub0 != null) {
                        fallbackSub0.subBottleId
                    } else {
                        // Create sub 0 on the fly
                        val s0 = SubBottle(
                            bottleId = bottleId,
                            subBottleNumber = 0,
                            sizeMl = "N/A",
                            price = "N/A",
                            boughtWhen = "N/A",
                            boughtWhere = "N/A",
                            other = "Unowned Default Catch-All"
                        )
                        repository.insertSubBottle(s0)
                    }
                }
                targetSubBottleId = chosenSubId
            }

            // Create the Review object
            val review = Review(
                subBottleId = targetSubBottleId,
                whenDate = pourWhen.ifEmpty { "N/A" },
                wherePlace = pourWhere.ifEmpty { "N/A" },
                price = pourPrice.ifEmpty { "N/A" },
                drunkScore = pourDrunkScore,
                amountOz = pourAmountOz.ifEmpty { "N/A" },
                amountMl = pourAmountMl.ifEmpty { "N/A" },
                glass = pourGlass.ifEmpty { "N/A" },
                ice = pourIce,
                otherPour = pourOther,
                noseNotes = noseNotes,
                noseScore = noseScore,
                palateNotes = palateNotes,
                palateScore = palateScore,
                finishNotes = finishNotes,
                finishScore = finishScore,
                savory = flavorScores["Savory"] ?: 4,
                sweet = flavorScores["Sweet"] ?: 4,
                sour = flavorScores["Sour"] ?: 4,
                smokey = flavorScores["Smokey"] ?: 4,
                stoneFruity = flavorScores["Stone Fruity"] ?: 4,
                citrusy = flavorScores["Citrusy"] ?: 4,
                herbal = flavorScores["Herbal"] ?: 4,
                heat = flavorScores["Heat"] ?: 4,
                woody = flavorScores["Woody"] ?: 4,
                nutty = flavorScores["Nutty"] ?: 4,
                overallNotes = overallNotes,
                overallScore = overallScore
            )

            repository.insertReview(review)
            clearReviewFlow()
            onComplete()
        }
    }


    // --- State for Blind Tasting Flow ---
    var blindDate by mutableStateOf("")
    var blindWhere by mutableStateOf("")
    var blindNumPours by mutableStateOf("")
    var blindTheme by mutableStateOf("")

    // Active state
    var currentBlindId by mutableStateOf<Long?>(null)
    var currentTastePourIndex by mutableStateOf(0) // 0 to N-1
    var blindPoursGuess = mutableStateListOf<BlindPourGuess>()

    // Re-ordering rank list
    var blindRankList = mutableStateListOf<Int>() // List of pour indices in order of preference



    fun startBlindTasting(onComplete: () -> Unit) {
        val num = blindNumPours.toIntOrNull()?.coerceIn(1, 10) ?: 3
        blindPoursGuess.clear()
        for (i in 0 until num) {
            blindPoursGuess.add(BlindPourGuess())
        }
        currentTastePourIndex = 0
        blindRankList.clear()
        for (i in 1..num) {
            blindRankList.add(i)
        }
        onComplete()
    }

    fun saveBlindTasting(onComplete: () -> Unit) {
        viewModelScope.launch {
            val blind = Blind(
                date = blindDate.ifEmpty { "N/A" },
                wherePlace = blindWhere.ifEmpty { "N/A" },
                numberOfPours = blindPoursGuess.size,
                theme = blindTheme.ifEmpty { "N/A" }
            )
            val blindId = repository.insertBlind(blind)

            // Save individual reveals
            blindPoursGuess.forEachIndexed { index, guess ->
                val pourNum = index + 1
                // Find rank of this active pour
                val rank = blindRankList.indexOf(pourNum) + 1

                val revealObj = BlindReveal(
                    blindId = blindId,
                    pourNumber = pourNum,
                    guessDistillery = guess.distillery,
                    guessProof = guess.proof,
                    guessCategory = guess.category,
                    guessNoseScore = guess.noseScore,
                    guessPalateScore = guess.palateScore,
                    guessFinishScore = guess.finishScore,
                    guessNotes = guess.notes,
                    rank = rank,
                    revealedName = guess.revName.ifEmpty { "Unknown Bourbon" },
                    revealedDistillery = guess.revDistillery.ifEmpty { "N/A" },
                    revealedProof = guess.revProof.ifEmpty { "N/A" },
                    revealedCategory = guess.revCategory,
                    revealedNoseScore = guess.revNoseScore,
                    revealedPalateScore = guess.revPalateScore,
                    revealedFinishScore = guess.revFinishScore,
                    revealedNotes = guess.revNotes
                )
                repository.insertBlindReveal(revealObj)
            }

            // Clear Blind entries
            blindDate = ""
            blindWhere = ""
            blindNumPours = ""
            blindTheme = ""
            blindPoursGuess.clear()
            onComplete()
        }
    }


    // --- Mathematical Stats Aggregation ---
    // Rule: "ensure all mathematical aggregations for stats pull exclusively from the Bottles database, keeping the Blinds database completely isolated."
    val statsState: StateFlow<BourbonStats> = combine(
        allBottles,
        allReviews,
        allSubBottles
    ) { bottles, reviews, subBottles ->
        calculateStats(bottles, reviews, subBottles)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BourbonStats())

    private fun calculateStats(
        bottles: List<Bottle>,
        reviews: List<Review>,
        subBottles: List<SubBottle>
    ): BourbonStats {
        if (bottles.isEmpty() || reviews.isEmpty()) {
            return BourbonStats()
        }

        // Map sub-bottle to bottle details
        val bottleOfSub = mutableMapOf<Long, Bottle>()
        subBottles.forEach { sub ->
            bottles.find { it.id == sub.bottleId }?.let {
                bottleOfSub[sub.subBottleId] = it
            }
        }

        // Step 1: Find highest ranked bottle (Highest average overall score in Reviews)
        val bottleScoresList = mutableMapOf<Long, MutableList<Int>>()
        reviews.forEach { r ->
            val bId = bottleOfSub[r.subBottleId]?.id
            if (bId != null) {
                bottleScoresList.getOrPut(bId) { mutableListOf() }.add(r.overallScore)
            }
        }

        var highestRankedBottle: Bottle? = null
        var highestRankedAvg = 0.0
        bottleScoresList.forEach { (bId, scores) ->
            val avg = scores.average()
            if (avg > highestRankedAvg) {
                highestRankedAvg = avg
                highestRankedBottle = bottles.find { it.id == bId }
            }
        }

        // Step 2: Highest overall number of reviews
        val reviewCounts = mutableMapOf<Long, Int>()
        reviews.forEach { r ->
            val bId = bottleOfSub[r.subBottleId]?.id
            if (bId != null) {
                reviewCounts[bId] = (reviewCounts[bId] ?: 0) + 1
            }
        }
        val mostReviewedBottleId = reviewCounts.maxByOrNull { it.value }?.key
        val mostReviewedBottle = bottles.find { it.id == mostReviewedBottleId }
        val mostReviewedCount = reviewCounts[mostReviewedBottleId] ?: 0

        // Step 3: Highest overall in each category & Category with highest average score
        val categoryScores = mutableMapOf<String, MutableList<Int>>()
        val categoryReviews = mutableMapOf<String, Int>()

        reviews.forEach { r ->
            val cat = bottleOfSub[r.subBottleId]?.category ?: "Unknown"
            categoryScores.getOrPut(cat) { mutableListOf() }.add(r.overallScore)
            categoryReviews[cat] = (categoryReviews[cat] ?: 0) + 1
        }

        val categoryAverages = categoryScores.mapValues { it.value.average() }
        val topCategoryByAvg = categoryAverages.maxByOrNull { it.value }?.key ?: "N/A"
        val topCategoryAvgValue = categoryAverages[topCategoryByAvg] ?: 0.0

        // Most filled category
        val mostFilledCategory = categoryReviews.maxByOrNull { it.value }?.key ?: "N/A"
        val mostFilledCategoryCount = categoryReviews[mostFilledCategory] ?: 0

        // Overall stats object
        return BourbonStats(
            highestRankedBottle = highestRankedBottle?.name ?: "No Reviews Yet",
            highestRankedAvg = highestRankedAvg,
            mostReviewedBottle = mostReviewedBottle?.name ?: "No Reviews Yet",
            mostReviewedCount = mostReviewedCount,
            topCategory = topCategoryByAvg,
            topCategoryAverage = topCategoryAvgValue,
            mostFilledCategory = mostFilledCategory,
            mostFilledCount = mostFilledCategoryCount,
            totalBottlesCount = bottles.size,
            totalReviewsCount = reviews.size
        )
    }

    fun getRevealsForBlind(blindId: Long): Flow<List<BlindReveal>> {
        return repository.getRevealsForBlind(blindId)
    }

    suspend fun getBlindRevealById(revealId: Long): BlindReveal? {
        return repository.getBlindRevealById(revealId)
    }

    fun resetAndPrepopulateDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetAndPrepopulate()
            onComplete()
        }
    }

    var databaseBackup by mutableStateOf<DatabaseBackup?>(null)
        private set

    fun clearDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearDatabase()
            onComplete()
        }
    }

    fun prepopulateSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.prepopulateSampleData()
            onComplete()
        }
    }

    fun backupDatabaseToMemory(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val bottles = repository.getAllBottlesSync()
                val subBottles = repository.getAllSubBottlesSync()
                val reviews = repository.getAllReviewsSync()
                val blinds = repository.getAllBlindsSync()
                val blindReveals = repository.getAllBlindRevealsSync()

                databaseBackup = DatabaseBackup(
                    bottles = bottles,
                    subBottles = subBottles,
                    reviews = reviews,
                    blinds = blinds,
                    blindReveals = blindReveals
                )
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun restoreDatabaseFromMemory(onComplete: (Boolean) -> Unit = {}) {
        val backup = databaseBackup
        if (backup == null) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            try {
                repository.restoreDatabase(
                    bottles = backup.bottles,
                    subBottles = backup.subBottles,
                    reviews = backup.reviews,
                    blinds = backup.blinds,
                    reveals = backup.blindReveals
                )
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun exportDatabaseToJsonString(onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val bottles = repository.getAllBottlesSync()
                val subBottles = repository.getAllSubBottlesSync()
                val reviews = repository.getAllReviewsSync()
                val blinds = repository.getAllBlindsSync()
                val blindReveals = repository.getAllBlindRevealsSync()

                val backup = DatabaseBackup(
                    bottles = bottles,
                    subBottles = subBottles,
                    reviews = reviews,
                    blinds = blinds,
                    blindReveals = blindReveals
                )

                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(DatabaseBackup::class.java)
                val jsonStr = adapter.toJson(backup)
                onComplete(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    fun importDatabaseFromJsonString(jsonStr: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(DatabaseBackup::class.java)
                val backup = adapter.fromJson(jsonStr)
                if (backup != null) {
                    repository.restoreDatabase(
                        bottles = backup.bottles,
                        subBottles = backup.subBottles,
                        reviews = backup.reviews,
                        blinds = backup.blinds,
                        reveals = backup.blindReveals
                    )
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class DatabaseBackup(
    val bottles: List<Bottle>,
    val subBottles: List<SubBottle>,
    val reviews: List<Review>,
    val blinds: List<Blind>,
    val blindReveals: List<BlindReveal>
)

data class BourbonStats(
    val highestRankedBottle: String = "N/A",
    val highestRankedAvg: Double = 0.0,
    val mostReviewedBottle: String = "N/A",
    val mostReviewedCount: Int = 0,
    val topCategory: String = "N/A",
    val topCategoryAverage: Double = 0.0,
    val mostFilledCategory: String = "N/A",
    val mostFilledCount: Int = 0,
    val totalBottlesCount: Int = 0,
    val totalReviewsCount: Int = 0
)

class BlindPourGuess {
    var distillery by mutableStateOf("")
    var proof by mutableStateOf("")
    var category by mutableStateOf("Bourbon")
    var noseScore by mutableStateOf(4)
    var palateScore by mutableStateOf(4)
    var finishScore by mutableStateOf(4)
    var notes by mutableStateOf("")

    // Reveal page inputs
    var revName by mutableStateOf("")
    var revDistillery by mutableStateOf("")
    var revProof by mutableStateOf("")
    var revCategory by mutableStateOf("Bourbon")
    var revNoseScore by mutableStateOf(4)
    var revPalateScore by mutableStateOf(4)
    var revFinishScore by mutableStateOf(4)
    var revNotes by mutableStateOf("")
}
