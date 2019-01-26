package palm.recipe.assistant

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_recipe_edit.*
import palm.recipe.assistant.model.Recipe
import palm.recipe.assistant.model.db.DatabaseHelper

/**
 * Activity for editing an Recipe.
 */
class RecipeEditActivity : AppCompatActivity() {

    private val dbHelper = DatabaseHelper(this)
    private var recipeID: Int = 0

    private val nameField by lazy { edit_recipe_name }
    private val yieldField by lazy { edit_recipe_yield }
    private val descriptionField by lazy { edit_recipe_description }

//    private val units by lazy { dbHelper.unitAbbreviations() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_edit)

        // for backwards navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*
        This activity can be used for creating or editing an Recipe.
        When editing, the ID of the recipe to edit is passed as an extra.
        This ID is stored to identify the correct recipe. An ID of 0
        indicates a new Recipe.
        */
        when (intent.action) {
            Intent.ACTION_EDIT -> {
                recipeID = intent.getIntExtra(EXTRA_ID, 0)

                // get the values from the database, and fill the fields
                val recipe = dbHelper.getRecipe(recipeID)
                if (recipe != null) {
                    fillFields(recipe)
                }
            }
            Intent.ACTION_INSERT -> {
                // Maybe something later?
            }
        }

        // Set up the save button
        val saveButton = edit_recipe_save
        saveButton.setOnClickListener {
            if (validate()) {
                val recipe = createRecipe()

                if (recipeID == 0) {
                    if (dbHelper.addRecipe(recipe) != -1) {
                        toast(getString(R.string.toast_msg_recipe_created, recipe.name))
                    } else {
                        toast(getString(R.string.toast_msg_recipe_failed, recipe.name))
                    }
                } else {
                    if (dbHelper.updateRecipe(recipe)) {
                        toast(getString(R.string.toast_msg_recipe_saved, recipe.name))
                    } else {
                        toast(getString(R.string.toast_msg_recipe_failed, recipe.name))
                    }
                }
                finish()
            }
        }
    }

    /**
     * Fill the form fields based on the values in the given Recipe.
     *
     * @param recipe the Recipe to get the values from
     */
    private fun fillFields(recipe: Recipe) {
        nameField.setText(recipe.name)
        yieldField.setText(recipe.yield.toString())
        descriptionField.setText(recipe.description)
    }

    /**
     * Create an Recipe object based on the values in the form fields.
     *
     * @return a new Recipe
     */
    private fun createRecipe() = Recipe(
            recipeID,
            nameField.text.toString(),
            yieldField.text.toString().toDouble(),
            descriptionField.text.toString()
    )

    /**
     * Check that the form fields contain valid data for an Recipe.
     * If not, the user is notified with an error message and help.
     *
     * @return true if valid, false otherwise.
     */
    private fun validate(): Boolean {
        var hasError = false

        if (nameField.text.isBlank()) {
            nameField.error = getString(R.string.edit_recipe_error_name)
            hasError = true
        }

        // TODO when creating a new recipe, check that the name is unique

        val yieldVal = yieldField.text.toString().toDoubleOrNull()
        if (yieldVal == null) {
            yieldField.error = getString(R.string.edit_recipe_error_yield)
            hasError = true
        }

        return !hasError
    }
}
