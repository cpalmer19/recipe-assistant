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

    private fun addMeasure() {
        // have the user select an ingredient
        val ingredients = dbHelper.getAllIngredients().filter {
            val ingred = it
            measures.none { it.ingredient == ingred.name }
        }
        val adapter = IngredAdapter(context, ingredients)

        context.showDialog {
            setTitle(R.string.dialog_title_select_ingredient)
            setAdapter(adapter) { _, which ->
                val ingredName = adapter.getItem(which).name
                val newMeasure = Measure(ingredName, 1.0, "c")
                editMeasure(newMeasure)
            }
        }
    }

    private fun editMeasure(measure: Measure) {
        context.showDialog {
            setTitle(getString(R.string.dialog_title_edit_measure, measure.ingredient))

            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_measure, null)
            val amountText = dialogView.findViewById<EditText>(R.id.edit_measure_amount)
            val unitSpinner = dialogView.findViewById<Spinner>(R.id.edit_measure_unit)
            setView(dialogView)

            amountText.setText(measure.measure.toString())

            val units = dbHelper.unitAbbreviations
            unitSpinner.adapter = ArrayAdapter(context, R.layout.spinner_view_unit, units)
            unitSpinner.setSelection(units.indexOf(measure.unit))

            setPositiveButton(R.string.ok) { _, _ ->
                val newMeasure = Measure(
                        measure.ingredient,
                        amountText.text.toString().toDoubleOrNull() ?: 1.0,
                        unitSpinner.selectedItem as String
                )
                val index = _measures.indexOfFirst { it.ingredient == measure.ingredient }
                if (index != -1) {
                    _measures[index] = newMeasure
                } else {
                    _measures += newMeasure
                }
                refreshList()
            }
            setNegativeButton(R.string.cancel) { _, _ -> }
        }
    }

    private fun editSelectedMeasure() {
        editMeasure(selectedMeasure!!)
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