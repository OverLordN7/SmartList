package com.example.smartlist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date
import java.util.UUID

@Entity(tableName = "list_table")
data class PurchaseList(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val listSize: Int,
    val year: Int,
    val month: String,
    val monthValue: Int,
    val day: Int,
    @ColumnInfo(name = "drawableId")
    val drawableId: Int = (0..4).random()
)


@Entity(tableName = "item_table",
    foreignKeys = [ForeignKey(
            entity = PurchaseList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE)]
)
data class Item(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    var weight: Float,
    val weightType: String,
    val price : Float,
    val total: Float,
    var isBought: Boolean = false,
    val listId: UUID, //foreign key to purchase list
    var drawableId: Int = (0..2).random(),
    var photoPath: String? = null,
)
