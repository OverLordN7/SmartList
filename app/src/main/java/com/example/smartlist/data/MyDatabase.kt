package com.example.smartlist.data

import android.content.Context
import android.os.Environment
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Item
import com.example.smartlist.model.Product
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.model.Recipe
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

@Database(
    entities = [
        PurchaseList::class,
        Item::class,
        DishList::class,
        DishComponent::class,
        Recipe::class,
        Product::class
    ],
    version = 7,
    exportSchema = false
)
abstract class MyDatabase: RoomDatabase() {

    abstract fun purchaseListDao(): PurchaseListDao

    abstract fun itemDao(): ItemDao

    abstract fun dishListDao(): DishListDao

    abstract fun dishComponentDao(): DishComponentDao

    abstract fun recipeDao(): RecipeDao

    abstract fun productDao(): ProductDao

    companion object{
        private const val DB_NAME = "my-database"

        private var INSTANCE: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        // Download data from text file
                        val inputStream: InputStream = context.assets.open("data/calTable.txt")
                        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

                        // insert data into product_table
                        var line: String?
                        while (bufferedReader.readLine().also { line = it } != null) {
                            val data = line?.split(",")
                            if (data != null && data.size >= 3) {
                                val product = Product(
                                    id = UUID.randomUUID(),
                                    name = data[0],
                                    carb = data[1].toFloat(),
                                    fat = data[2].toFloat(),
                                    protein = data[3].toFloat(),
                                    cal = data[4].toFloat()
                                )
                                db.execSQL("INSERT INTO product_table (id, name, carb, fat, protein, cal) VALUES (?, ?, ?, ?, ?, ?)",
                                    arrayOf(product.id, product.name, product.carb, product.fat, product.protein, product.cal))
                            }
                        }

                        // Closing streams
                        bufferedReader.close()
                        inputStream.close()

                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)

                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
