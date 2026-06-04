package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "bottles")
data class Bottle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nickname: String,
    val distillery: String,
    val age: String,
    val proof: String,
    val category: String,
    val finish: String,
    val other: String
)

@Entity(
    tableName = "sub_bottles",
    foreignKeys = [
        ForeignKey(
            entity = Bottle::class,
            parentColumns = ["id"],
            childColumns = ["bottleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bottleId"])]
)
data class SubBottle(
    @PrimaryKey(autoGenerate = true) val subBottleId: Long = 0,
    val bottleId: Long,
    val subBottleNumber: Int, // 0 = unowned, 1, 2, ... owned sequentially
    val sizeMl: String,
    val price: String,
    val boughtWhen: String,
    val boughtWhere: String,
    val other: String
)

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = SubBottle::class,
            parentColumns = ["subBottleId"],
            childColumns = ["subBottleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subBottleId"])]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val reviewId: Long = 0,
    val subBottleId: Long,
    val whenDate: String,
    val wherePlace: String,
    val price: String,
    val drunkScore: Int,
    val amountOz: String,
    val amountMl: String,
    val glass: String,
    val ice: Boolean,
    val otherPour: String,
    
    val noseNotes: String,
    val noseScore: Int,
    
    val palateNotes: String,
    val palateScore: Int,
    
    val finishNotes: String,
    val finishScore: Int,
    
    // Flavor profile dimensions (1-7)
    val savory: Int,
    val sweet: Int,
    val sour: Int,
    val smokey: Int,
    val stoneFruity: Int,
    val citrusy: Int,
    val herbal: Int,
    val heat: Int,
    val woody: Int,
    val nutty: Int,
    
    val overallNotes: String,
    val overallScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blinds")
data class Blind(
    @PrimaryKey(autoGenerate = true) val blindId: Long = 0,
    val date: String,
    val wherePlace: String,
    val numberOfPours: Int,
    val theme: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blind_reveals",
    foreignKeys = [
        ForeignKey(
            entity = Blind::class,
            parentColumns = ["blindId"],
            childColumns = ["blindId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["blindId"])]
)
data class BlindReveal(
    @PrimaryKey(autoGenerate = true) val revealId: Long = 0,
    val blindId: Long,
    val pourNumber: Int,
    
    // Guess page
    val guessDistillery: String,
    val guessProof: String,
    val guessCategory: String,
    val guessNoseScore: Int,
    val guessPalateScore: Int,
    val guessFinishScore: Int,
    val guessNotes: String,
    
    // Rank
    val rank: Int,
    
    // Reveal page
    val revealedName: String,
    val revealedDistillery: String,
    val revealedProof: String,
    val revealedCategory: String,
    val revealedNoseScore: Int,
    val revealedPalateScore: Int,
    val revealedFinishScore: Int,
    val revealedNotes: String
)
