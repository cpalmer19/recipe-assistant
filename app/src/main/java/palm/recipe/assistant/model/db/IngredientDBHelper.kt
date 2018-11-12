package palm.recipe.assistant.model.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import palm.recipe.assistant.model.IngredUnit
import palm.recipe.assistant.model.Ingredient

/**
 * SQLite Helper for the ingredient table
 */
class IngredientDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory? = null)
        : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ingredDB.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_INGREDIENTS = "ingredients"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "ingred_name"
        const val COLUMN_UNIT_COST = "unit_cost"
        const val COLUMN_UNIT = "unit"

        val ALL_COLUMNS = arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_UNIT_COST, COLUMN_UNIT)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // TODO put UNIQUE modifier on COLUMN_NAME
        val sql = "CREATE TABLE $TABLE_INGREDIENTS(" +
                "$COLUMN_ID INTEGER PRIMARY KEY," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_UNIT_COST DOUBLE," +
                "$COLUMN_UNIT TEXT);"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO transfer old data to new table so no data is lost

        db.execSQL("DROP TABLE IF EXISTS $TABLE_INGREDIENTS")
        onCreate(db)
    }

    /**
     * Insert a new ingredient into the table.
     *
     * @param ingredient the Ingredient to add/insert
     */
    fun addIngredient(ingredient: Ingredient) {
        // TODO check that the ingredient name does not already exist

        val values = ContentValues()
        values.put(COLUMN_NAME, ingredient.name)
        values.put(COLUMN_UNIT_COST, ingredient.unitCost)
        values.put(COLUMN_UNIT, ingredient.unit.name)

        val db = this.writableDatabase
        db.insert(TABLE_INGREDIENTS, null, values)
        db.close()
    }

    /**
     * Delete the given ingredient from the table, identified by the ID.
     *
     * @param ingredient the Ingredient to delete
     */
    fun deleteIngredient(ingredient: Ingredient) : Boolean {
        if (ingredient.id == 0) {
            // ingredient not in the database
            return false
        }

        val db = this.writableDatabase
        val whereArgs = arrayOf(ingredient.id.toString())
        db.delete(TABLE_INGREDIENTS, "$COLUMN_ID = ?", whereArgs)
        db.close()
        return true
    }

    /**
     * Update the ingredient identified by the ID to its new values.
     * If the ID is 0, it inserts a new ingredient instead.
     *
     * @param ingredient the Ingredient to update
     */
    fun updateIngredient(ingredient: Ingredient) {
        if (ingredient.id == 0) {
            addIngredient(ingredient)

        } else {
            val values = ContentValues()
            values.put(COLUMN_NAME, ingredient.name)
            values.put(COLUMN_UNIT_COST, ingredient.unitCost)
            values.put(COLUMN_UNIT, ingredient.unit.name)

            val db = this.writableDatabase
            val whereArgs = arrayOf(ingredient.id.toString())
            db.update(TABLE_INGREDIENTS, values, "$COLUMN_ID = ?", whereArgs)
            db.close()
        }
    }

    /**
     * Convert the values from an ingredient search cursor to an Ingredient object.
     *
     * @return an Ingredient object with the current cursor values.
     */
    private fun Cursor.toIngredient(): Ingredient {
        val id = getInt(getColumnIndex(COLUMN_ID))
        val name = getString(getColumnIndex(COLUMN_NAME))
        val unitCost = getDouble(getColumnIndex(COLUMN_UNIT_COST))
        val unit = getString(getColumnIndex(COLUMN_UNIT))
        return Ingredient(id, name, unitCost, IngredUnit.valueOf(unit))
    }

    /**
     * Get the ingredient with the given ID from the database.
     *
     * @param id the ID of the desired Ingredient
     * @return an Ingredient object, or null if no ingredient with that ID.
     */
    fun getIngredient(id: Int): Ingredient? {
        var ingred: Ingredient? = null

        query(TABLE_INGREDIENTS, ALL_COLUMNS, where = "$COLUMN_ID = ?", whereArgs = arrayOf(id.toString())) {
            ingred = this.toIngredient()
        }

        return ingred
    }

    /**
     * Get a list of all the ingredients from the database. The results are
     * ordered by name.
     *
     * @return a List of Ingredients
     */
    fun getAllIngredients(): List<Ingredient> {
        val ingreds = mutableListOf<Ingredient>()

        query(TABLE_INGREDIENTS, ALL_COLUMNS, orderBy = COLUMN_NAME) {
            ingreds += this.toIngredient()
        }

        return ingreds
    }
}

/**
 * A convenience function for making queries from within an SQLiteOpenHelper.
 * Uses a default value of null for the optional query arguments. See
 * SQLiteDatabase.query(...) for argument options.
 *
 * A lambda function 'forEach' must be passed which will be applied to the cursor
 * for each result it finds from the query.
 */
fun SQLiteOpenHelper.query(
        table: String,
        columns: Array<String>,
        where: String? = null,
        whereArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        limit: Int? = null,
        forEach: Cursor.() -> Unit
) {
    val db = readableDatabase
    val cursor = db.query(table, columns, where, whereArgs, groupBy, having, orderBy, limit?.toString())
    cursor.use {
        while (cursor.moveToNext()) {
            cursor.forEach()
        }
    }
}