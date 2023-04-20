package com.example.smartlist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList

@Database(
    entities = [
        PurchaseList::class,
        Item::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MyDatabase: RoomDatabase() {

    abstract fun purchaseListDao(): PurchaseListDao

    abstract fun itemDao(): ItemDao

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

val MIGRATION_1_2 = object : Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE list_table
            RENAME TO list_table_old
        """)
        database.execSQL("""
            CREATE TABLE list_table (
             id UUID PRIMARY KEY,
             name TEXT NOT NULL,
             listSize INTEGER NOT NULL,
             year INTEGER NOT NULL,
             month TEXT NOT NULL,
             day INTEGER NOT NULL
             )
        """)

        database.execSQL("""
             INSERT INTO list_table (id,name,listSize,year,month,day)
             SELECT CAST(id AS UUID), name, listSize,year,month,day
             FROM list_table_old
        """)
        database.execSQL("""
             ALTER TABLE item_table
             RENAME TO item_table_old
        """)

        database.execSQL("""
            CREATE TABLE item_table (
             id UUID PRIMARY KEY,
             name TEXT NOT NULL,
             weight FLOAT NOT NULL,
             price FLOAT NOT NULL,
             total FLOAT NOT NULL,
             listId UUID NOT NULL
             )
        """)

        database.execSQL("""
             INSERT INTO item_table (id,name,weight,price,total,listId)
             SELECT CAST(id AS UUID), name, weight,price,total,CAST(listId AS UUID)
             FROM list_table_old
        """)
    }
}