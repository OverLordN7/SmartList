package com.example.smartlist.model

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
    val name: String,
    val recipeId:UUID,
    val weight: Float,
    val weightType: String,
)

