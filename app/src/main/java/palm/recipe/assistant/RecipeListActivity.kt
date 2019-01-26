package palm.recipe.assistant

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import kotlinx.android.synthetic.main.activity_recipe_list.*
import kotlinx.android.synthetic.main.content_recipe_list.*
import palm.recipe.assistant.model.Recipe
import palm.recipe.assistant.model.db.DatabaseHelper

class RecipeListActivity : AppCompatActivity() {

    private val listView by lazy { main_recipe_list }

    private val dbHelper = DatabaseHelper(this)
    private var selectedRecipe: Recipe? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        // Use a toolbar as the ActionBar
        setSupportActionBar(toolbar)

        // For back navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Floating Action Button for creating a new Recipe
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
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.confirm_delete_recipe_question, selectedRecipe?.name))

            setPositiveButton(R.string.confirm_delete_delete) { dialog, which ->
                toast(getString(R.string.toast_msg_recipe_deleted, selectedRecipe?.name))
                dbHelper.deleteRecipe(selectedRecipe!!)
                actionMode?.finish()
                refreshList()
            }
            setNegativeButton(R.string.confirm_delete_cancel) { dialog, which ->  }

            show()
        }
    }

}

