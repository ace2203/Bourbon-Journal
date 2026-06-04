package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BourbonDao {
    @Query("SELECT * FROM bottles ORDER BY name ASC")
    fun getAllBottles(): Flow<List<Bottle>>

    @Query("SELECT * FROM bottles ORDER BY name ASC")
    suspend fun getAllBottlesSync(): List<Bottle>

    @Query("SELECT * FROM bottles WHERE id = :id")
    suspend fun getBottleById(id: Long): Bottle?

    @Query("SELECT * FROM bottles WHERE name = :name LIMIT 1")
    suspend fun getBottleByName(name: String): Bottle?

    @Query("SELECT * FROM bottles WHERE nickname = :nickname LIMIT 1")
    suspend fun getBottleByNickname(nickname: String): Bottle?

    @Query("SELECT * FROM sub_bottles WHERE bottleId = :bottleId ORDER BY subBottleNumber ASC")
    fun getSubBottlesForBottle(bottleId: Long): Flow<List<SubBottle>>

    @Query("SELECT * FROM sub_bottles WHERE bottleId = :bottleId ORDER BY subBottleNumber ASC")
    suspend fun getSubBottlesForBottleSync(bottleId: Long): List<SubBottle>

    @Query("SELECT * FROM sub_bottles WHERE bottleId = :bottleId AND subBottleNumber = :number LIMIT 1")
    suspend fun getSubBottleByNumber(bottleId: Long, number: Int): SubBottle?

    @Query("SELECT * FROM reviews WHERE subBottleId = :subBottleId ORDER BY timestamp DESC")
    fun getReviewsForSubBottle(subBottleId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE subBottleId IN (SELECT subBottleId FROM sub_bottles WHERE bottleId = :bottleId) ORDER BY timestamp DESC")
    fun getReviewsForBottle(bottleId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM sub_bottles")
    fun getAllSubBottles(): Flow<List<SubBottle>>

    @Query("SELECT * FROM blinds ORDER BY timestamp DESC")
    fun getAllBlinds(): Flow<List<Blind>>

    @Query("SELECT * FROM blind_reveals WHERE blindId = :blindId ORDER BY pourNumber ASC")
    fun getRevealsForBlind(blindId: Long): Flow<List<BlindReveal>>

    @Query("SELECT * FROM blind_reveals")
    fun getAllBlindRevealsFlow(): Flow<List<BlindReveal>>

    @Query("SELECT * FROM blind_reveals WHERE revealId = :revealId LIMIT 1")
    suspend fun getBlindRevealById(revealId: Long): BlindReveal?

    @Query("DELETE FROM bottles")
    suspend fun deleteAllBottles()

    @Query("DELETE FROM sub_bottles")
    suspend fun deleteAllSubBottles()

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()

    @Query("DELETE FROM blinds")
    suspend fun deleteAllBlinds()

    @Query("DELETE FROM blind_reveals")
    suspend fun deleteAllBlindReveals()

    @Query("SELECT * FROM sub_bottles")
    suspend fun getAllSubBottlesSync(): List<SubBottle>

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviewsSync(): List<Review>

    @Query("SELECT * FROM blinds")
    suspend fun getAllBlindsSync(): List<Blind>

    @Query("SELECT * FROM blind_reveals")
    suspend fun getAllBlindRevealsSync(): List<BlindReveal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBottles(bottles: List<Bottle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubBottles(subBottles: List<SubBottle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlinds(blinds: List<Blind>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlindReveals(reveals: List<BlindReveal>)

    @Transaction
    suspend fun clearDatabase() {
        deleteAllBlindReveals()
        deleteAllReviews()
        deleteAllSubBottles()
        deleteAllBottles()
        deleteAllBlinds()
    }

    @Transaction
    suspend fun resetAndPrepopulate() {
        clearDatabase()
        prepopulateSampleData()
    }

    @Transaction
    suspend fun prepopulateSampleData() {
        // 1. Buffalo Trace
        val btId = insertBottle(
            Bottle(
                name = "Buffalo Trace",
                nickname = "BT",
                distillery = "Buffalo Trace Distillery",
                age = "NAS (~8 Years)",
                proof = "90",
                category = "Kentucky Straight Bourbon",
                finish = "Sweet & lingering with light oak",
                other = "Mashbill #1"
            )
        )
        // Sub 0
        insertSubBottle(
            SubBottle(
                bottleId = btId,
                subBottleNumber = 0,
                sizeMl = "N/A",
                price = "N/A",
                boughtWhen = "N/A",
                boughtWhere = "N/A",
                other = "Unowned Default Catch-All"
            )
        )
        // Sub 1 (Owned)
        val btSub1 = insertSubBottle(
            SubBottle(
                bottleId = btId,
                subBottleNumber = 1,
                sizeMl = "750ml",
                price = "29.99",
                boughtWhen = "2026-04-15",
                boughtWhere = "Total Wine",
                other = "Daily drinker staple"
            )
        )
        // BT Review 1
        insertReview(
            Review(
                subBottleId = btSub1,
                whenDate = "2026-05-15",
                wherePlace = "Home patio",
                price = "29.99",
                drunkScore = 6,
                amountOz = "2.0",
                amountMl = "59",
                glass = "Glencairn",
                ice = false,
                otherPour = "Neat",
                noseNotes = "Sweet vanilla, cherries, brown sugar",
                noseScore = 5,
                palateNotes = "Rich caramel, hint of spice, soft oak",
                palateScore = 6,
                finishNotes = "Sweet corn and toasted oak",
                finishScore = 5,
                savory = 2, sweet = 6, sour = 2, smokey = 1, stoneFruity = 5, citrusy = 3, herbal = 2, heat = 3, woody = 4, nutty = 3,
                overallNotes = "A quintessential classic Kentucky bourbon. Sweet, fruity, and highly accessible.",
                overallScore = 6
            )
        )
        // BT Review 2
        insertReview(
            Review(
                subBottleId = btSub1,
                whenDate = "2026-06-01",
                wherePlace = "Den with friends",
                price = "29.99",
                drunkScore = 5,
                amountOz = "1.5",
                amountMl = "44",
                glass = "Tumbler",
                ice = true,
                otherPour = "Single large cube",
                noseNotes = "Cherry cola, butterscotch, light wood",
                noseScore = 5,
                palateNotes = "Caramel corn, leather, cinnamon spice",
                palateScore = 5,
                finishNotes = "Vanilla bean, medium lingering heat",
                finishScore = 5,
                savory = 3, sweet = 5, sour = 2, smokey = 1, stoneFruity = 4, citrusy = 2, herbal = 3, heat = 2, woody = 4, nutty = 3,
                overallNotes = "Opens up nicely with a drop of water. Classic profiles dominate.",
                overallScore = 5
            )
        )

        // 2. Woodford Reserve
        val wrId = insertBottle(
            Bottle(
                name = "Woodford Reserve",
                nickname = "Woodford",
                distillery = "Woodford Reserve Distillery",
                age = "NAS",
                proof = "90.4",
                category = "Kentucky Straight Bourbon",
                finish = "Smooth, clean warmth with rye spice",
                other = "Triple POT distilled blend"
            )
        )
        insertSubBottle(
            SubBottle(
                bottleId = wrId,
                subBottleNumber = 0,
                sizeMl = "N/A",
                price = "N/A",
                boughtWhen = "N/A",
                boughtWhere = "N/A",
                other = "Unowned Default Catch-All"
            )
        )
        val wrSub1 = insertSubBottle(
            SubBottle(
                bottleId = wrId,
                subBottleNumber = 1,
                sizeMl = "750ml",
                price = "34.99",
                boughtWhen = "2026-03-10",
                boughtWhere = "Costco",
                other = "Triple distilled batch"
            )
        )
        // WR Review 1
        insertReview(
            Review(
                subBottleId = wrSub1,
                whenDate = "2026-05-10",
                wherePlace = "Study room",
                price = "34.99",
                drunkScore = 5,
                amountOz = "2.0",
                amountMl = "59",
                glass = "Glencairn",
                ice = false,
                otherPour = "Neat",
                noseNotes = "Heavy oak, cocoa powder, leather, orange peel",
                noseScore = 5,
                palateNotes = "Toasted nuts, chocolate, cinnamon, rye spice",
                palateScore = 5,
                finishNotes = "Warm charcoal, vanilla, lingering clove",
                finishScore = 5,
                savory = 4, sweet = 4, sour = 3, smokey = 2, stoneFruity = 3, citrusy = 4, herbal = 3, heat = 3, woody = 5, nutty = 5,
                overallNotes = "Thick, robust, oaky and cocoa-forward. Excellent traditional style.",
                overallScore = 5
            )
        )
        // WR Review 2
        insertReview(
            Review(
                subBottleId = wrSub1,
                whenDate = "2026-05-22",
                wherePlace = "Whiskey Club Lounge",
                price = "34.99",
                drunkScore = 5,
                amountOz = "2.0",
                amountMl = "59",
                glass = "Snifter",
                ice = false,
                otherPour = "Neat",
                noseNotes = "Baking spices, almonds, rich honey",
                noseScore = 5,
                palateNotes = "Creamy fudge, toasted bread, pepper",
                palateScore = 5,
                finishNotes = "Minty herbal note and soft wood",
                finishScore = 5,
                savory = 3, sweet = 5, sour = 2, smokey = 2, stoneFruity = 3, citrusy = 3, herbal = 4, heat = 3, woody = 4, nutty = 5,
                overallNotes = "Quite rich and nutty. A very satisfying standard bourbon.",
                overallScore = 5
            )
        )

        // 3. Wild Turkey 101
        val wtId = insertBottle(
            Bottle(
                name = "Wild Turkey 101",
                nickname = "WT101",
                distillery = "Wild Turkey Distillery",
                age = "NAS (6-8 Years)",
                proof = "101",
                category = "Kentucky Straight Bourbon",
                finish = "Bold, spicy cinnamon and high warmth",
                other = "Jimmy Russell master blend"
            )
        )
        insertSubBottle(
            SubBottle(
                bottleId = wtId,
                subBottleNumber = 0,
                sizeMl = "N/A",
                price = "N/A",
                boughtWhen = "N/A",
                boughtWhere = "N/A",
                other = "Unowned Default Catch-All"
            )
        )
        val wtSub1 = insertSubBottle(
            SubBottle(
                bottleId = wtId,
                subBottleNumber = 1,
                sizeMl = "1000ml",
                price = "24.99",
                boughtWhen = "2026-05-01",
                boughtWhere = "Local Liquor",
                other = "Big bottle value"
            )
        )
        // WT Review 1
        insertReview(
            Review(
                subBottleId = wtSub1,
                whenDate = "2026-05-18",
                wherePlace = "Backyard Grill",
                price = "24.99",
                drunkScore = 6,
                amountOz = "1.5",
                amountMl = "44",
                glass = "Neat glass",
                ice = false,
                otherPour = "Neat",
                noseNotes = "Bold rye spice, leather, deep charred oak, orange zest",
                noseScore = 6,
                palateNotes = "High Proof prickle, intense caramel, black pepper, cinnamon candy",
                palateScore = 6,
                finishNotes = "Long spicy finish, maple syrup coating",
                finishScore = 6,
                savory = 4, sweet = 5, sour = 3, smokey = 3, stoneFruity = 3, citrusy = 4, herbal = 5, heat = 5, woody = 5, nutty = 4,
                overallNotes = "Incredible value champ. Robust rye spice, high proof boldness, classic Wild Turkey funk.",
                overallScore = 6
            )
        )
        // WT Review 2
        insertReview(
            Review(
                subBottleId = wtSub1,
                whenDate = "2026-06-03",
                wherePlace = "Living Room",
                price = "24.99",
                drunkScore = 7,
                amountOz = "2.0",
                amountMl = "59",
                glass = "Glencairn",
                ice = false,
                otherPour = "Neat",
                noseNotes = "Brown sugar, vanilla pod, spicy pipe tobacco",
                noseScore = 6,
                palateNotes = "Toffee, honey-sweetened dark tea, hefty cinnamon kick",
                palateScore = 7,
                finishNotes = "Dry charred wood, rich molasses, warming clove",
                finishScore = 6,
                savory = 3, sweet = 6, sour = 2, smokey = 2, stoneFruity = 3, citrusy = 3, herbal = 4, heat = 5, woody = 5, nutty = 4,
                overallNotes = "Stellar pour. Showcases beautiful deep char wood notes balanced with rich sweet backing.",
                overallScore = 7
            )
        )

        // 4. Blind 1: "High Proof Showdown"
        val b1Id = insertBlind(
            Blind(
                date = "2026-05-20",
                wherePlace = "Clubhouse Lounge",
                numberOfPours = 3,
                theme = "High Proof Showdown"
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b1Id,
                pourNumber = 1,
                guessCategory = "Bourbon",
                guessDistillery = "Buffalo Trace",
                guessProof = "90",
                guessNoseScore = 5,
                guessPalateScore = 5,
                guessFinishScore = 5,
                guessNotes = "Soft fruit, sweet oak. Feels around 90-95 proof, very drinkable.",
                rank = 2,
                revealedName = "Wild Turkey 101",
                revealedDistillery = "Wild Turkey Distillery",
                revealedProof = "101",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 6,
                revealedPalateScore = 6,
                revealedFinishScore = 6,
                revealedNotes = "Excellent high-rye corn mashbill. Tasted way and proof is actually 101."
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b1Id,
                pourNumber = 2,
                guessCategory = "Bourbon",
                guessDistillery = "Heaven Hill",
                guessProof = "100",
                guessNoseScore = 6,
                guessPalateScore = 6,
                guessFinishScore = 5,
                guessNotes = "Rich nuttiness, chocolate notes. Thought it was Bottled-In-Bond.",
                rank = 1,
                revealedName = "Woodford Reserve",
                revealedDistillery = "Woodford Reserve Distillery",
                revealedProof = "90.4",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 5,
                revealedPalateScore = 5,
                revealedFinishScore = 5,
                revealedNotes = "Deep dark sweet wood. Woodford Reserve at 90.4 proof has very high pot still content."
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b1Id,
                pourNumber = 3,
                guessCategory = "Rye",
                guessDistillery = "MGP Distillery",
                guessProof = "95",
                guessNoseScore = 4,
                guessPalateScore = 4,
                guessFinishScore = 4,
                guessNotes = "Muted on the nose. Tasted quite green, guessed Rye due to herbal notes.",
                rank = 3,
                revealedName = "Buffalo Trace",
                revealedDistillery = "Buffalo Trace Distillery",
                revealedProof = "90",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 5,
                revealedPalateScore = 5,
                revealedFinishScore = 5,
                revealedNotes = "Classic cherry notes did not come through in this dark cup."
            )
        )

        // 5. Blind 2: "Everyday Bourbon Blind"
        val b2Id = insertBlind(
            Blind(
                date = "2026-05-25",
                wherePlace = "John's Place",
                numberOfPours = 3,
                theme = "Everyday Bourbons under $40"
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b2Id,
                pourNumber = 1,
                guessCategory = "Bourbon",
                guessDistillery = "Wild Turkey",
                guessProof = "101",
                guessNoseScore = 6,
                guessPalateScore = 6,
                guessFinishScore = 5,
                guessNotes = "Big spice, very flavorful. Thought this had high ABV.",
                rank = 1,
                revealedName = "Buffalo Trace",
                revealedDistillery = "Buffalo Trace Distillery",
                revealedProof = "90",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 5,
                revealedPalateScore = 6,
                revealedFinishScore = 5,
                revealedNotes = "Surprising. Round cherry oak sweetness stood out as best overall cup."
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b2Id,
                pourNumber = 2,
                guessCategory = "Bourbon",
                guessDistillery = "Jim Beam",
                guessProof = "86",
                guessNoseScore = 4,
                guessPalateScore = 4,
                guessFinishScore = 4,
                guessNotes = "Peanutty, somewhat thin. Fair, but nothing special.",
                rank = 3,
                revealedName = "Woodford Reserve",
                revealedDistillery = "Woodford Reserve Distillery",
                revealedProof = "90.4",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 5,
                revealedPalateScore = 5,
                revealedFinishScore = 5,
                revealedNotes = "Expected better, but it drank a little flat against other competitors today."
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b2Id,
                pourNumber = 3,
                guessCategory = "Bourbon",
                guessDistillery = "Heaven Hill",
                guessProof = "90",
                guessNoseScore = 5,
                guessPalateScore = 5,
                guessFinishScore = 5,
                guessNotes = "Caramel, vanilla. Good robust body.",
                rank = 2,
                revealedName = "Wild Turkey 101",
                revealedDistillery = "Wild Turkey Distillery",
                revealedProof = "101",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 6,
                revealedPalateScore = 6,
                revealedFinishScore = 6,
                revealedNotes = "Great finish. Spicy, cinnamon warmth makes a great second place."
            )
        )

        // 6. Blind 3: "Distillery Comparison"
        val b3Id = insertBlind(
            Blind(
                date = "2026-06-02",
                wherePlace = "Whiskey Club Room",
                numberOfPours = 3,
                theme = "Distillery Mashbill Walkthrough"
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b3Id,
                pourNumber = 1,
                guessCategory = "Bourbon",
                guessDistillery = "Woodford Reserve",
                guessProof = "90",
                guessNoseScore = 5,
                guessPalateScore = 5,
                guessFinishScore = 5,
                guessNotes = "Very nutty and cocoa forward. Spot on guess.",
                rank = 2,
                revealedName = "Woodford Reserve",
                revealedDistillery = "Woodford Reserve Distillery",
                revealedProof = "90.4",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 5,
                revealedPalateScore = 5,
                revealedFinishScore = 5,
                revealedNotes = "Cohesive cocoa sweet wood profile matches Woodford's unique character."
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b3Id,
                pourNumber = 2,
                guessCategory = "Bourbon",
                guessDistillery = "Wild Turkey",
                guessProof = "101",
                guessNoseScore = 7,
                guessPalateScore = 6,
                guessFinishScore = 6,
                guessNotes = "Explosive spice and pipe tobacco notes. Magnificent.",
                rank = 1,
                revealedName = "Wild Turkey 101",
                revealedDistillery = "Wild Turkey Distillery",
                revealedProof = "101",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 6,
                revealedPalateScore = 7,
                revealedFinishScore = 6,
                revealedNotes = "Perfect proof, spice-sweet sync. Highlight of the night."
            )
        )
        insertBlindReveal(
            BlindReveal(
                blindId = b3Id,
                pourNumber = 3,
                guessCategory = "Bourbon",
                guessDistillery = "Buffalo Trace",
                guessProof = "90",
                guessNoseScore = 4,
                guessPalateScore = 5,
                guessFinishScore = 4,
                guessNotes = "Soft orchard fruits and cherries. Extremely accessible.",
                rank = 3,
                revealedName = "Buffalo Trace",
                revealedDistillery = "Buffalo Trace Distillery",
                revealedProof = "90",
                revealedCategory = "Kentucky Straight Bourbon",
                revealedNoseScore = 5,
                revealedPalateScore = 5,
                revealedFinishScore = 5,
                revealedNotes = "A solid, easy drinker, although overshadowed by WT101's heavy spice."
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBottle(bottle: Bottle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubBottle(subBottle: SubBottle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlind(blind: Blind): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlindReveal(reveal: BlindReveal): Long

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Update
    suspend fun updateSubBottle(subBottle: SubBottle)

    @Transaction
    suspend fun createBottleWithSubBottles(bottle: Bottle): Long {
        val bottleId = insertBottle(bottle)
        // Automatically create the initial subbottle 0 to capture reviews/pours unrelated to physical owned bottle instances
        insertSubBottle(
            SubBottle(
                bottleId = bottleId,
                subBottleNumber = 0,
                sizeMl = "N/A",
                price = "N/A",
                boughtWhen = "N/A",
                boughtWhere = "N/A",
                other = "Unowned Default Catch-All"
            )
        )
        return bottleId
    }
}
