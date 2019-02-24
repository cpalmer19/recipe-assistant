package palm.recipe.assistant.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import palm.recipe.assistant.R
import palm.recipe.assistant.ingred.IngredientListActivity
import palm.recipe.assistant.recipe.RecipeListActivity

/**
 * The main Home activity.
 * Contains menu to go to other activities.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button for viewing the ingredient list
        buttonOnClick(R.id.btn_ingredients) {
            launchActivity(IngredientListActivity::class)
        }

        // Button for viewing the recipe list
        buttonOnClick(R.id.btn_recipes) {
            launchActivity(RecipeListActivity::class)
        }
    }
}
