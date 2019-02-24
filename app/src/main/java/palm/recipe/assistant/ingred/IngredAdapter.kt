package palm.recipe.assistant.ingred

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import palm.recipe.assistant.R

/**
 * Adapter for showing a list of Ingredients in a ListView.
 */
class IngredAdapter(context: Context, ingreds: List<Ingredient>)
    : ArrayAdapter<Ingredient>(context, 0, ingreds) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val ingred = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_row_ingredient, parent, false)

        val titleView = view.findViewById<TextView>(R.id.ingred_row_name)
        titleView.text = ingred.name

        // details are '$UNIT_COST / UNIT'
        val detailView = view.findViewById<TextView>(R.id.measure_row_details)
        val details = "\$${ingred.unitCost} / ${ingred.unit}"
        detailView.text = details

        return view
    }
}