package com.example.smartlist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "list_table")
data class PurchaseList(
    @PrimaryKey val id: Int,
    val name: String,
    val listSize: Int,
    val date: Date
)


@Entity(tableName = "item_table",
    foreignKeys = [ForeignKey(
            entity = PurchaseList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE)]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val weight: Float,
    val price : Float,
    val total: Float,
    val listId: Int, //foreign key to purchase list
)
