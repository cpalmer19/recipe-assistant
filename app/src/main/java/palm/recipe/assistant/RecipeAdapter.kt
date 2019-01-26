package palm.recipe.assistant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import palm.recipe.assistant.model.Recipe

/**
 * Adapter for showing a list of Ingredients in a ListView.
 */
class RecipeAdapter(context: Context, recipes: List<Recipe>) : ArrayAdapter<Recipe>(context, 0, recipes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val recipe = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_row_ingredient, parent, false)

        val titleView = view.findViewById<TextView>(R.id.ingred_row_name)
        titleView.text = recipe.name

        // details are 'yield'
        val detailView = view.findViewById<TextView>(R.id.ingred_row_details)
        detailView.text = "Yields ${recipe.yield}"

        return view
    }
}