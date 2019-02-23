package palm.recipe.assistant.recipe

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import palm.recipe.assistant.R
import palm.recipe.assistant.base.DatabaseHelper
import palm.recipe.assistant.base.confirmDelete
import palm.recipe.assistant.base.onClick

internal class RecipeEditIngredsFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private var recipeId: Int = 0

    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var measureList: ListView
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
        view!!.findViewById<Button>(R.id.edit_recipe_add_ingred).apply {
            onClick(::addMeasure)
        }

        editButton = view.findViewById<Button>(R.id.edit_recipe_edit_ingred).apply {
            isEnabled = false
            onClick(::editSelectedMeasure)
        }

        deleteButton = view.findViewById<Button>(R.id.edit_recipe_delete_ingred).apply {
            isEnabled = false
            onClick(::deleteSelectedMeasure)
        }

        measureList = view.findViewById(R.id.edit_recipe_ingredient_list)
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

    fun refreshList() {
        measureList.adapter = MeasureAdapter(context, _measures)
        editButton.isEnabled = false
        deleteButton.isEnabled = false
    }

    fun addMeasure() {
        // TODO how to add/edit measures?
    }

    fun editSelectedMeasure() {

    }

    fun deleteSelectedMeasure() {
        val msg = getString(R.string.confirm_delete_measure_question, selectedMeasure?.ingredient)
        confirmDelete(msg) {
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