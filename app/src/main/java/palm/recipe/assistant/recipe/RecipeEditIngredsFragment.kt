package palm.recipe.assistant.recipe

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import palm.recipe.assistant.R
import palm.recipe.assistant.base.*
import palm.recipe.assistant.ingred.IngredAdapter

internal class RecipeEditIngredsFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private var recipeId: Int = 0

    private val addButton by viewId<Button>(R.id.edit_recipe_add_ingred)
    private val editButton by viewId<Button>(R.id.edit_recipe_edit_ingred)
    private val deleteButton by viewId<Button>(R.id.edit_recipe_delete_ingred)
    private val measureList by viewId<ListView>(R.id.edit_recipe_ingredient_list)

    private val _measures = mutableListOf<Measure>()
    private var selectedMeasure: Measure? = null

    var measures: List<Measure>
        get() = _measures
        set(value) {
            _measures.clear()
            _measures.addAll(value)
            refreshList()
        }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dbHelper = DatabaseHelper(context)
        recipeId = arguments.getInt("id")
        return inflater?.inflate(R.layout.content_recipe_edit_ingreds, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        addButton.onClick(::addMeasure)
        editButton.onClick(::editSelectedMeasure)
        deleteButton.onClick(::deleteSelectedMeasure)

        if (recipeId != 0) {
            measures = dbHelper.getMeasuresForRecipe(recipeId)
        }

        measureList.setOnItemClickListener { _, _, position, _ ->
            val measure = measureList.getItemAtPosition(position) as Measure
            selectedMeasure = measure
            editButton.isEnabled = true
            deleteButton.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    private fun refreshList() {
        measureList.adapter = MeasureAdapter(context, _measures)
        editButton.isEnabled = false
        deleteButton.isEnabled = false
    }

    fun addMeasure() {
        // TODO how to add/edit measures?
    }

    fun editSelectedMeasure() {

    }

    private fun deleteSelectedMeasure() {
        val msg = getString(R.string.confirm_delete_measure_question, selectedMeasure?.ingredient)
        context.confirmDelete(msg) {
            _measures.remove(selectedMeasure)
            refreshList()
        }
    }

    /**
     * Adapter for showing a list of Measures in a ListView.
     */
    private class MeasureAdapter(context: Context, measures: List<Measure>)
        : ArrayAdapter<Measure>(context, 0, measures) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val measure = getItem(position)

            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_row_measure, parent, false)

            val titleView = view.findViewById<TextView>(R.id.measure_row_name)
            titleView.text = measure.ingredient

            // details are '$measure $units'
            val detailView = view.findViewById<TextView>(R.id.measure_row_details)
            val details = "${measure.measure} ${measure.unit}"
            detailView.text = details

            return view
        }
    }
}