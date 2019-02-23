package palm.recipe.assistant.recipe

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import palm.recipe.assistant.R
import palm.recipe.assistant.base.DatabaseHelper
import palm.recipe.assistant.base.EXTRA_ID
import palm.recipe.assistant.base.createBundle
import palm.recipe.assistant.base.toast

/**
 * Activity for editing an Recipe.
 */
class RecipeEditActivity : AppCompatActivity() {

    private val dbHelper = DatabaseHelper(this)
    private var recipeID: Int = 0

    private lateinit var tabs: TabAdapter
    private val viewPager by lazy { findViewById<ViewPager>(R.id.edit_recipe_pager) }
    private val tabLayout by lazy { findViewById<TabLayout>(R.id.edit_recipe_tablayout) }

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
                val recipe = dbHelper.getRecipe(recipeID)
                if (recipe != null) {
                    title = recipe.name
                }
            }
            Intent.ACTION_INSERT -> {
                // Maybe something later?
            }
        }

        tabs = TabAdapter(recipeID, supportFragmentManager)
        viewPager.adapter = tabs
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_editor_save -> {
                if (tabs.details.validate() && saveRecipe()) {
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class TabAdapter(recipeId: Int, fm: FragmentManager): FragmentPagerAdapter(fm) {
        val details = RecipeEditDetailFragment()
        val ingreds = RecipeEditIngredsFragment()

        init {
            val bundle = createBundle("id" to recipeId)
            details.arguments = bundle
            ingreds.arguments = bundle
        }

        override fun getItem(position: Int): Fragment? = when (position) {
            0 -> details
            1 -> ingreds
            else -> null
        }

        override fun getPageTitle(position: Int): CharSequence? = when(position) {
            0 -> "Details"
            1 -> "Ingredients"
            else -> null
        }

        override fun getCount(): Int = 2
    }

    private fun saveRecipe(): Boolean {
        val recipe = tabs.details.createRecipe(recipeID)

        val createNew: Boolean = recipeID == 0

        val success: Boolean = if (createNew) {
            recipeID = dbHelper.addRecipe(recipe)
            recipeID != -1
        } else {
            dbHelper.updateRecipe(recipe)
        }

        if (success) {
            dbHelper.updateRecipeMeasures(recipeID, tabs.ingreds.measures)

            if (createNew) {
                toast(getString(R.string.toast_msg_recipe_created, recipe.name))
            } else {
                toast(getString(R.string.toast_msg_recipe_saved, recipe.name))
            }

        } else {
            toast(getString(R.string.toast_msg_recipe_failed, recipe.name))
        }

        return success
    }
}
