package com.example.smartlist.data

import android.content.Context
import androidx.room.AutoMigration
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer

val MIGRATION_11_12 = object : Migration(11,12){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE dish_component_table ADD COLUMN drawableId INTEGER")
        database.execSQL("ALTER TABLE dish_component_table ADD COLUMN photoPath TEXT")
    }
}
val MIGRATION_14_15 = object : Migration(14,15){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE list_table ADD COLUMN drawableId INTEGER NOT NULL DEFAULT 0")
    }
}
val MIGRATION_15_16 = object : Migration(15,16){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE dished_list ADD COLUMN drawableId INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        PurchaseList::class,
        Item::class,
        DishList::class,
        DishComponent::class,
        Recipe::class,
        Product::class
    ],
    version = 16,
    exportSchema = true,
    //autoMigrations = [AutoMigration(from = 14, to = 15)]
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

        suspend fun initDataFromTextFile(context: Context, db: SupportSQLiteDatabase){
            withContext(Dispatchers.IO){
                // Download data from text file
                val inputStream: InputStream = context.assets.open("data/calTable.txt")
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))

                // insert data into product_table
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    val data = line?.split(",")
                    if (data != null && data.size >= 3) {
                        val product = Product(
                            name = data[0],
                            carb = data[1].toFloat(),
                            fat = data[2].toFloat(),
                            protein = data[3].toFloat(),
                            cal = data[4].toFloat()
                        )

                        val buffer = ByteBuffer.allocate(16)
                        buffer.putLong(product.id.mostSignificantBits)
                        buffer.putLong(product.id.leastSignificantBits)

                        db.execSQL("INSERT INTO product_table (id, name, carb, fat, protein, cal) VALUES ( ?, ?, ?, ?, ?, ?)",
                            arrayOf(buffer.array(), product.name, product.carb, product.fat, product.protein, product.cal))
                    }
                }

                // Closing streams
                bufferedReader.close()
                inputStream.close()
            }
        }

        fun getInstance(context: Context): MyDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_11_12)
                    .addMigrations(MIGRATION_14_15)
                    .addMigrations(MIGRATION_15_16)
                    .addCallback(object : RoomDatabase.Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        CoroutineScope(Dispatchers.IO).launch {
                            initDataFromTextFile(context,db)
                        }

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
