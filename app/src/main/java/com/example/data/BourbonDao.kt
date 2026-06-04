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
