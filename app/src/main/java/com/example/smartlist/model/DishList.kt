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
    tableName = "dish_component_table",
    foreignKeys = [ForeignKey(
        entity = DishList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DishComponent(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val weight: Float,
    val weightType: String,
    val listId:UUID,
)