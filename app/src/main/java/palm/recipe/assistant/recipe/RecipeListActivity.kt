package palm.recipe.assistant.recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.content_recipe_list.*
import palm.recipe.assistant.R
import palm.recipe.assistant.base.*

class RecipeListActivity : AppCompatActivity() {

    private val listView by lazy { main_recipe_list }

    private val dbHelper = DatabaseHelper(this)
    private var selectedRecipe: Recipe? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        // Use a toolbar as the ActionBar
        setSupportActionBar(findViewById(R.id.toolbar))

        // For back navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Floating Action Button for creating a new Recipe
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            launchActivity(RecipeEditActivity::class) {
                action = Intent.ACTION_INSERT
            }
        }

        /*
        Set up the list view.
        One recipe at a time can be selected, with an ActionMode showing
        the Edit and Delete options
        */
        listView.setOnItemClickListener { parent, view, position, id ->
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback)
            }
            val recipe = listView.getItemAtPosition(position) as Recipe
            selectedRecipe = recipe
            actionMode?.title = recipe.name
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    /**
     * Refresh the list of recipes
     */
    private fun refreshList() {
        // load recipes from the database
        val recipes = dbHelper.getAllRecipes()
        val adapter = RecipeAdapter(this, recipes)
        listView.adapter = adapter
    }

    /**
     * Callback for selecting an item from the Recipe List.
     */
    private val actionModeCallback = createActionModeCallback(
            R.menu.menu_recipe_list,
            itemMappings = mapOf(
                    R.id.menu_recipe_list_edit to { editCurrentItem() },
                    R.id.menu_recipe_list_delete to { deleteCurrentItem() }
            ),
            onDestroy = {
                actionMode = null
                selectedRecipe = null
                listView.clearChoices()

                // hack to get the view to update
                listView.adapter = listView.adapter
            }
    )

    /**
     * Edit the selected Recipe
     */
    private fun editCurrentItem() {
        launchActivity(RecipeEditActivity::class) {
            action = Intent.ACTION_EDIT
            putExtra(EXTRA_ID, selectedRecipe?.id ?: 0)
        }
    }

    /**
     * Delete the selected Recipe.
     */
    private fun deleteCurrentItem() {
        // Get the user to confirm before deleting
        val msg = getString(R.string.confirm_delete_recipe_question, selectedRecipe?.name)
        confirmDelete(msg) {
            toast(getString(R.string.toast_msg_recipe_deleted, selectedRecipe?.name))
            dbHelper.deleteRecipe(selectedRecipe!!)
            actionMode?.finish()
            refreshList()
        }
    }

    /**
     * Adapter for showing a list of Ingredients in a ListView.
     */
    private class RecipeAdapter(context: Context, recipes: List<Recipe>)
        : ArrayAdapter<Recipe>(context, 0, recipes) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val recipe = getItem(position)

            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_row_recipe, parent, false)

            val titleView = view.findViewById<TextView>(R.id.recipe_row_name)
            titleView.text = recipe.name

            // details are 'yield'
            val descriptionView = view.findViewById<TextView>(R.id.recipe_row_description)
            if (recipe.description.isNotBlank()) {
                descriptionView.text = recipe.description
            } else {
                descriptionView.visibility = View.GONE
            }

            val detailView = view.findViewById<TextView>(R.id.recipe_row_details)
            detailView.text = "Yields ${recipe.yield}"

            return view
        }
    }
}

