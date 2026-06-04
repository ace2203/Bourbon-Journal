package com.example.data

import kotlinx.coroutines.flow.Flow

class BourbonRepository(private val dao: BourbonDao) {
    val allBottles: Flow<List<Bottle>> = dao.getAllBottles()
    val allReviews: Flow<List<Review>> = dao.getAllReviews()
    val allSubBottles: Flow<List<SubBottle>> = dao.getAllSubBottles()
    val allBlinds: Flow<List<Blind>> = dao.getAllBlinds()

    suspend fun getBottleById(id: Long): Bottle? = dao.getBottleById(id)
    suspend fun getBottleByName(name: String): Bottle? = dao.getBottleByName(name)
    suspend fun getBottleByNickname(nickname: String): Bottle? = dao.getBottleByNickname(nickname)
    suspend fun getAllBottlesSync(): List<Bottle> = dao.getAllBottlesSync()

    fun getSubBottlesForBottle(bottleId: Long): Flow<List<SubBottle>> = dao.getSubBottlesForBottle(bottleId)
    suspend fun getSubBottlesForBottleSync(bottleId: Long): List<SubBottle> = dao.getSubBottlesForBottleSync(bottleId)
    suspend fun getSubBottleByNumber(bottleId: Long, number: Int): SubBottle? = dao.getSubBottleByNumber(bottleId, number)

    fun getReviewsForSubBottle(subBottleId: Long): Flow<List<Review>> = dao.getReviewsForSubBottle(subBottleId)
    fun getReviewsForBottle(bottleId: Long): Flow<List<Review>> = dao.getReviewsForBottle(bottleId)

    fun getRevealsForBlind(blindId: Long): Flow<List<BlindReveal>> = dao.getRevealsForBlind(blindId)

    suspend fun insertBottle(bottle: Bottle): Long = dao.insertBottle(bottle)
    suspend fun createBottleWithSubBottles(bottle: Bottle): Long = dao.createBottleWithSubBottles(bottle)

    suspend fun insertSubBottle(subBottle: SubBottle): Long = dao.insertSubBottle(subBottle)
    suspend fun insertReview(review: Review): Long = dao.insertReview(review)
    suspend fun insertBlind(blind: Blind): Long = dao.insertBlind(blind)
    suspend fun insertBlindReveal(reveal: BlindReveal): Long = dao.insertBlindReveal(reveal)

    suspend fun updateBottle(bottle: Bottle) = dao.updateBottle(bottle)
    suspend fun updateSubBottle(subBottle: SubBottle) = dao.updateSubBottle(subBottle)
}
