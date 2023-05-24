package com.example.smartlist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.model.Recipe

@Database(
    entities = [
        PurchaseList::class,
        Item::class,
        DishList::class,
        DishComponent::class,
        Recipe::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MyDatabase: RoomDatabase() {

    abstract fun purchaseListDao(): PurchaseListDao

    abstract fun itemDao(): ItemDao

    abstract fun dishListDao(): DishListDao

    abstract fun dishComponentDao(): DishComponentDao

    abstract fun recipeDao(): RecipeDao

    companion object{
        private const val DB_NAME = "database.db"

        private var INSTANCE: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
