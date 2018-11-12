package palm.recipe.assistant

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_ingredient_list.*
import palm.recipe.assistant.model.Ingredient
import palm.recipe.assistant.model.db.IngredientDBHelper

/**
 * An Activity for showing the current list of Ingredients.
 */
class IngredientListActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    private val dbHelper = IngredientDBHelper(this)
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
        listView = findViewById(R.id.main_ingred_list)
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
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater : MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.menu_ingred_list, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_ingred_list_edit -> {
                    editCurrentItem()
                    true
                }
                R.id.menu_ingred_list_delete -> {
                    deleteCurrentItem()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            selectedIngredient = null
            listView.clearChoices()

            // hack to get the view to update
            listView.adapter = listView.adapter
        }
    }

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
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.confirm_delete_ingred_question, selectedIngredient?.name))

            setPositiveButton(R.string.confirm_delete_delete) { dialog, which ->
                toast(getString(R.string.toast_msg_ingred_deleted, selectedIngredient?.name))
                dbHelper.deleteIngredient(selectedIngredient!!)
                actionMode?.finish()
                refreshList()
            }
            setNegativeButton(R.string.confirm_delete_cancel) { dialog, which ->  }

            show()
        }
    }

}