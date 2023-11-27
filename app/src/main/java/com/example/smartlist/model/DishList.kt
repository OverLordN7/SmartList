package com.example.smartlist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "dished_list")
data class DishList(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val listSize: Int,
    val year: Int,
    val month: String,
    val day: Int,
)
@Entity(
    tableName = "recipe_table",
    foreignKeys = [ForeignKey(
        entity = DishList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Recipe(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val listId: UUID,
    val name: String,
    val portions: Int,
    val photoPath: String? = null,
    @ColumnInfo(name = "description", defaultValue = "null")
    var description: String? = null,
)

@Entity(
    tableName = "dish_component_table",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DishComponent(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val recipeId:UUID,
    val name: String,
    var weight: Float,
    val weightType: String,
    val price: Float = 0.0f,
    var total: Float = 0.0f,
    var carbs: Float = 0.0f,
    var fat: Float = 0.0f,
    var protein: Float = 0.0f,
    var cal: Float = 0.0f,
    var drawableId: Int = (0..2).random(),
    @ColumnInfo(name = "photoPath", defaultValue = "null")
    var photoPath: String? = null,
)

