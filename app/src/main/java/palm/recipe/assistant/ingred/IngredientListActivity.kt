package palm.recipe.assistant.ingred

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.widget.ListView
import palm.recipe.assistant.R
import palm.recipe.assistant.base.*

/**
 * An Activity for showing the current list of Ingredients.
 */
class IngredientListActivity : AppCompatActivity() {
    private val listView by viewId<ListView>(R.id.main_ingred_list)

    private val dbHelper = DatabaseHelper(this)
    private var selectedIngredient: Ingredient? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_list)

        // Use a toolbar as the ActionBar
        setSupportActionBar(findViewById(R.id.toolbar))

        // For back navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Floating Action Button for creating a new Ingredient
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            launchActivity(IngredientEditActivity::class) {
                action = Intent.ACTION_INSERT
            }
        }

        /*
        Set up the list view.
        One ingredient at a time can be selected, with an ActionMode showing
        the Edit and Delete options
        */
        listView.setOnItemClickListener { parent, view, position, id ->
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback)
            }
            val ingred = listView.getItemAtPosition(position) as Ingredient
            selectedIngredient = ingred
            actionMode?.title = ingred.name
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    /**
     * Refresh the list of ingredients
     */
    private fun refreshList() {
        // load ingredients from the database
        val ingreds = dbHelper.getAllIngredients()
        val adapter = IngredAdapter(this, ingreds)
        listView.adapter = adapter
    }

    /**
     * Callback for selecting an item from the Ingredient List.
     */
    private val actionModeCallback = createActionModeCallback(
            R.menu.menu_ingred_list,
            itemMappings = mapOf(
                    R.id.menu_ingred_list_edit to { editCurrentItem() },
                    R.id.menu_ingred_list_delete to { deleteCurrentItem() }
            ),
            onDestroy = {
                actionMode = null
                selectedIngredient = null
                listView.clearChoices()

                // hack to get the view to update
                listView.adapter = listView.adapter
            }
    )

    /**
     * Edit the selected ingredient
     */
    private fun editCurrentItem() {
        launchActivity(IngredientEditActivity::class) {
            action = Intent.ACTION_EDIT
            putExtra(EXTRA_ID, selectedIngredient?.id ?: 0)
        }
    }

    /**
     * Delete the selected Ingredient.
     */
    private fun deleteCurrentItem() {
        // Get the user to confirm before deleting
        val msg = getString(R.string.confirm_delete_ingred_question, selectedIngredient?.name)
        confirmDelete(msg) {
            toast(getString(R.string.toast_msg_ingred_deleted, selectedIngredient?.name))
            dbHelper.deleteIngredient(selectedIngredient!!)
            actionMode?.finish()
            refreshList()
        }
    }
}
