package palm.recipe.assistant.model.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import palm.recipe.assistant.model.Ingredient
import palm.recipe.assistant.model.Measure
import palm.recipe.assistant.model.Recipe


/**
 * SQLite Helper for the core database
 */
class DatabaseHelper(context: Context)
        : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "recipe_manager.db"
        const val DATABASE_VERSION = 2

        const val TABLE_INGREDIENTS = "ingredients"
        const val TABLE_RECIPES = "recipes"
        const val TABLE_MEASURES = "measures"
        const val TABLE_UNITS = "units"

        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_UNIT_COST = "unit_cost"
        const val COLUMN_UNIT = "unit"
        const val COLUMN_YIELD = "yield"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_MEASURE = "measure"
        const val COLUMN_INGRED_ID = "ingred_id"
        const val COLUMN_RECIPE_ID = "recipe_id"

        const val COLUMN_ABBR = "abbreviation"
        const val COLUMN_TYPE = "type"

        const val TAG = "DB"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val unitSql = "CREATE TABLE $TABLE_UNITS(" +
                "$COLUMN_ID INTEGER PRIMARY KEY," +
                "$COLUMN_ABBR TEXT UNIQUE," +
                "$COLUMN_TYPE TEXT CHECK($COLUMN_TYPE IN ('W', 'V')));"
        db.execSQL(unitSql)
        initUnits(db)

        // TODO Add conversion table

        val ingredSql = "CREATE TABLE $TABLE_INGREDIENTS(" +
                "$COLUMN_ID INTEGER PRIMARY KEY," +
                "$COLUMN_NAME TEXT UNIQUE," +
                "$COLUMN_UNIT_COST DOUBLE," +
                "$COLUMN_UNIT TEXT REFERENCES $TABLE_UNITS($COLUMN_ABBR));"
        db.execSQL(ingredSql)

        val recipeSql = "CREATE TABLE $TABLE_RECIPES(" +
                "$COLUMN_ID INTEGER PRIMARY KEY," +
                "$COLUMN_NAME TEXT UNIQUE," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_YIELD DOUBLE);"
        db.execSQL(recipeSql)

        val measureSql = "CREATE TABLE $TABLE_MEASURES(" +
                "$COLUMN_RECIPE_ID INTEGER REFERENCES $TABLE_RECIPES($COLUMN_ID) ON DELETE CASCADE," +
                "$COLUMN_INGRED_ID INTEGER REFERENCES $TABLE_INGREDIENTS($COLUMN_ID) ON DELETE CASCADE," +
                "$COLUMN_MEASURE DOUBLE," +
                "$COLUMN_UNIT TEXT REFERENCES $TABLE_UNITS($COLUMN_ABBR)," +
                "PRIMARY KEY($COLUMN_RECIPE_ID, $COLUMN_INGRED_ID));"
        db.execSQL(measureSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val tempTableIngred = "_${TABLE_INGREDIENTS}_old"
        val tempTableRecipe = "_${TABLE_RECIPES}_old"

        db.run {
            execSQL("ALTER TABLE $TABLE_INGREDIENTS RENAME TO $tempTableIngred")
            execSQL("ALTER TABLE $TABLE_RECIPES RENAME TO $tempTableRecipe")

            execSQL("DROP TABLE IF EXISTS $TABLE_UNITS")
            execSQL("DROP TABLE IF EXISTS $TABLE_INGREDIENTS")
            execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
            execSQL("DROP TABLE IF EXISTS $TABLE_MEASURES")
        }

        onCreate(db)

        db.run {
            execSQL("INSERT INTO $TABLE_INGREDIENTS SELECT * FROM $tempTableIngred")
            execSQL("INSERT INTO $TABLE_RECIPES SELECT * FROM $tempTableRecipe")
            execSQL("DROP TABLE $tempTableIngred")
            execSQL("DROP TABLE $tempTableRecipe")
        }
    }

    //-----------------------------------------------------------------------
    // Unit Methods

    private fun initUnits(db: SQLiteDatabase) {
        initUnit(db, "g", "W")
        initUnit(db, "kg", "W")
        initUnit(db, "mL", "V")
        initUnit(db, "L", "V")
        initUnit(db, "tsp", "V")
        initUnit(db, "tbsp", "V")
        initUnit(db, "oz", "V")
        initUnit(db, "c", "V")
    }

    private fun initUnit(db: SQLiteDatabase, abbr: String, type: String) {
        db.insert(TABLE_UNITS, null, unitContentValues(abbr, type))
    }

    private fun unitContentValues(abbr: String, type: String) = ContentValues().apply {
        put(COLUMN_ABBR, abbr)
        put(COLUMN_TYPE, type)
    }

    fun unitAbbreviations(): List<String> {
        return query(
                TABLE_UNITS,
                mapper = { getString(COLUMN_ABBR) }
        )
    }

    //-----------------------------------------------------------------------
    // Ingredient Methods

    /**
     * Convert the values from an ingredient search cursor to an Ingredient object.
     *
     * @return an Ingredient object with the current cursor values.
     */
    private fun Cursor.getIngredient() = Ingredient(
            getInt(COLUMN_ID),
            getString(COLUMN_NAME),
            getDouble(COLUMN_UNIT_COST),
            getString(COLUMN_UNIT)
    )

    /**
     * Get ContentValues that apply to a particular Ingredient object
     */
    private fun Ingredient.toContentValues() = ContentValues().apply {
        put(COLUMN_NAME, name)
        put(COLUMN_UNIT_COST, unitCost)
        put(COLUMN_UNIT, unit)
    }

    /**
     * Insert a new ingredient into the table.
     *
     * @param ingredient the Ingredient to add/insert
     */
    fun addIngredient(ingredient: Ingredient): Int {
        // TODO check that the ingredient name does not already exist

        return addEntry(TABLE_INGREDIENTS, ingredient.toContentValues())
    }

    /**
     * Delete the given ingredient from the table, identified by the ID.
     *
     * @param ingredient the Ingredient to delete
     */
    fun deleteIngredient(ingredient: Ingredient) = deleteEntry(TABLE_INGREDIENTS, ingredient.id)

    /**
     * Update the ingredient identified by the ID to its new values.
     * It assumes the Ingredient exists and its ID is not 0.
     *
     * @param ingredient the Ingredient to update
     */
    fun updateIngredient(ingredient: Ingredient): Boolean {
        // TODO only update the parts that have changed

        return updateEntry(TABLE_INGREDIENTS, ingredient.id, ingredient.toContentValues())
    }

    /**
     * Get the ingredient with the given ID from the database.
     *
     * @param id the ID of the desired Ingredient
     * @return an Ingredient object, or null if no ingredient with that ID.
     */
    fun getIngredient(id: Int): Ingredient? {
        return query(
                TABLE_INGREDIENTS,
                where = "$COLUMN_ID = ?",
                whereArgs = arrayOf(id.toString()),
                mapper = { getIngredient() }
        ).firstOrNull()
    }

    /**
     * Get a list of all the ingredients from the database. The results are
     * ordered by name.
     *
     * @return a List of Ingredients
     */
    fun getAllIngredients(): List<Ingredient> {
        return query(
                TABLE_INGREDIENTS,
                orderBy = COLUMN_NAME,
                mapper = { getIngredient() }
        )
    }

    fun ingredientExists(name: String): Boolean {
        return query(
                TABLE_INGREDIENTS,
                where = "$COLUMN_NAME = ?",
                whereArgs = arrayOf(name),
                mapper = { true }
        ).isNotEmpty()
    }

    //----------------------------------------------------------------------------------
    // Recipe Methods

    /**
     * Create a Recipe from a database cursor resulting from a search.
     *
     * @return a Recipe object
     */
    private fun Cursor.getRecipe() = Recipe(
            getInt(COLUMN_ID),
            getString(COLUMN_NAME),
            getDouble(COLUMN_YIELD),
            getString(COLUMN_DESCRIPTION)
    )

    /**
     * Get ContentValues that apply to a particular Recipe object
     */
    private fun Recipe.toContentValues() = ContentValues().apply {
        put(COLUMN_NAME, name)
        put(COLUMN_YIELD, this@toContentValues.yield)
        put(COLUMN_DESCRIPTION, description)
    }

    fun addRecipe(recipe: Recipe): Int {
        // TODO check that the recipe name does not already exist

        return addEntry(TABLE_RECIPES, recipe.toContentValues())
    }

    fun deleteRecipe(recipe: Recipe) = deleteEntry(TABLE_RECIPES, recipe.id)

    fun updateRecipe(recipe: Recipe): Boolean {
        // TODO only update the parts that have changed

        return updateEntry(TABLE_RECIPES, recipe.id, recipe.toContentValues())
    }

    fun getRecipe(id: Int): Recipe? {
        return query(
                TABLE_RECIPES,
                where = "$COLUMN_ID = ?",
                whereArgs = arrayOf(id.toString()),
                mapper = { getRecipe() }
        ).firstOrNull()
    }

    fun getAllRecipes(): List<Recipe> {
        return query(TABLE_RECIPES, orderBy = COLUMN_NAME, mapper = { getRecipe() })
    }

    fun recipeExists(name: String): Boolean {
        return query(
                TABLE_RECIPES,
                where = "$COLUMN_NAME = ?",
                whereArgs = arrayOf(name),
                mapper = { true }
        ).isNotEmpty()
    }

    //----------------------------------------------------------------------------------
    // Measure Methods

    /**
     * Create a Measure from a database cursor after a search/query
     *
     * @return a Measure Object
     */
    private fun Cursor.getMeasure() = Measure(
                getString(COLUMN_NAME),
                getDouble(COLUMN_MEASURE),
                getString(COLUMN_UNIT)
        )

    /**
     * Get ContentValues that apply to a particular Measure object
     */
    private fun Measure.toContentValues(recipeID: Int): ContentValues {
        val ingredID = query(
                TABLE_INGREDIENTS,
                columns = arrayOf(COLUMN_ID),
                where = "$COLUMN_NAME = ?",
                whereArgs = arrayOf(ingredient),
                mapper = { getInt(COLUMN_ID) }
        ).first()

        return ContentValues().apply {
            put(COLUMN_RECIPE_ID, recipeID)
            put(COLUMN_INGRED_ID, ingredID)
            put(COLUMN_MEASURE, measure)
            put(COLUMN_UNIT, unit)
        }
    }

    fun updateRecipeMeasures(recipeID: Int, measures: List<Measure>) {
        deleteEntries(TABLE_MEASURES, "$COLUMN_RECIPE_ID = ?", arrayOf(recipeID.toString()))
        addAllEntries(TABLE_MEASURES, measures.map { it.toContentValues(recipeID) })
    }

    fun getMeasuresForRecipe(recipeID: Int): List<Measure> {
        return rawQuery(
                "SELECT $TABLE_INGREDIENTS.$COLUMN_NAME, $TABLE_MEASURES.$COLUMN_MEASURE, " +
                        "$TABLE_MEASURES.$COLUMN_UNIT " +
                        "FROM $TABLE_MEASURES INNER JOIN $TABLE_INGREDIENTS " +
                        "ON $TABLE_MEASURES.$COLUMN_INGRED_ID = $TABLE_INGREDIENTS.$COLUMN_ID " +
                        "WHERE $TABLE_MEASURES.$COLUMN_RECIPE_ID = ?",
                whereArgs = arrayOf(recipeID.toString()),
                mapper = { getMeasure() }
        )
    }

    //----------------------------------------------------------------------
    // General database manipulation methods

    private fun addEntry(table: String, values: ContentValues): Int {
        return writableDatabase.use {
            insertEntry(it, table, values)
        }
    }

    private fun addAllEntries(table: String, entries: List<ContentValues>) {
        writableDatabase.use {
            for (values in entries) {
                insertEntry(it, table, values)
            }
        }
    }

    private fun insertEntry(db: SQLiteDatabase, table: String, values: ContentValues): Int {
        return try {
            val newId = db.insertOrThrow(table, null, values).toInt()
            Log.d(TAG, "New $table inserted with ID $newId")
            newId
        } catch (e: SQLException) {
            Log.w(TAG, "Error inserting $table: ${e.message}")
            -1
        }
    }

    private fun deleteEntry(table: String, id: Int): Boolean {
        if (id == 0) return false       // entry not currently in database

        return writableDatabase.use {
            val whereArgs = arrayOf(id.toString())
            val result = it.delete(table, "$COLUMN_ID = ?", whereArgs)
            result == 1
        }
    }

    private fun deleteEntries(table: String, where: String, whereArgs: Array<String>): Int {
        return writableDatabase.use {
            it.delete(table, where, whereArgs)
        }
    }

    private fun updateEntry(table: String, id: Int, values: ContentValues): Boolean {
        if (id == 0) return false       // entry not currently in database

        return writableDatabase.use {
            val whereArgs = arrayOf(id.toString())
            try {
                val result = it.update(table, values, "$COLUMN_ID = ?", whereArgs)
                result == 1
            } catch (e: SQLException) {
                Log.w(TAG, "Error updating $table: ${e.message}")
                false
            }

        }
    }

}

/**
 * A convenience function for making queries from within an SQLiteOpenHelper.
 * Uses a default value of null for the optional query arguments. See
 * SQLiteDatabase.query(...) for argument options.
 *
 * A mapper function is passed as the last argument to convert the cursor into
 * the desired result type.
 *
 * @return a List of results of the type mapped to by the mapper function
 */
fun <T> SQLiteOpenHelper.query(
        table: String,
        columns: Array<String>? = null,
        where: String? = null,
        whereArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = null,
        limit: Int? = null,
        mapper: Cursor.() -> T
): List<T> {
    val db = readableDatabase
    val cursor = db.query(table, columns, where, whereArgs, groupBy, having, orderBy, limit?.toString())
    val results = mutableListOf<T>()
    cursor.use {
        while (cursor.moveToNext()) {
            results += cursor.mapper()
        }
    }
    return results
}

fun <T> SQLiteOpenHelper.rawQuery(
        query: String,
        whereArgs: Array<String>? = null,
        mapper: Cursor.() -> T
): List<T> {
    val db = readableDatabase
    val cursor = db.rawQuery(query, whereArgs)
    val results = mutableListOf<T>()
    cursor.use {
        while (cursor.moveToNext()) {
            results += cursor.mapper()
        }
    }
    return results
}

fun Cursor.getInt(columnName: String): Int = getInt(getColumnIndex(columnName))

fun Cursor.getDouble(columnName: String): Double = getDouble(getColumnIndex(columnName))

fun Cursor.getString(columnName: String): String = getString(getColumnIndex(columnName))
