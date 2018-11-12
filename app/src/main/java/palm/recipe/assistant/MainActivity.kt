package palm.recipe.assistant

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

/**
 * The main Home activity.
 * Contains menu to go to other activities.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button for viewing the ingredient list
        val ingredButton: Button = findViewById(R.id.btn_ingredients)
        ingredButton.setOnClickListener {
            launchActivity(IngredientListActivity::class)
        }

        // Button for viewing the recipe list
        val recipeButton: Button = findViewById(R.id.btn_recipes)
        recipeButton.setOnClickListener {
            // TODO create RecipeListActivity
            toast("Not yet implemented")
        }
    }
}
