package palm.recipe.assistant.recipe

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import palm.recipe.assistant.R
import palm.recipe.assistant.base.DatabaseHelper

internal class RecipeEditDetailFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private var recipeId: Int = 0

    private lateinit var nameField: EditText
    private lateinit var yieldField: EditText
    private lateinit var descriptionField: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dbHelper = DatabaseHelper(context)
        recipeId = arguments.getInt("id")
        return inflater?.inflate(R.layout.content_recipe_edit_details, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        nameField = view!!.findViewById(R.id.edit_recipe_name)
        yieldField = view.findViewById(R.id.edit_recipe_yield)
        descriptionField = view.findViewById(R.id.edit_recipe_description)

        if (recipeId != 0) {
            val recipe = dbHelper.getRecipe(recipeId)
            if (recipe != null) {
                fillFields(recipe)
            }
        }
    }

    fun fillFields(recipe: Recipe) {
        nameField.setText(recipe.name)
        yieldField.setText(recipe.yield.toString())
        descriptionField.setText(recipe.description)
    }

    fun createRecipe(id: Int) = Recipe(
            id,
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
    fun validate(): Boolean {
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