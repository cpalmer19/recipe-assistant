package palm.recipe.assistant

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import palm.recipe.assistant.model.Ingredient
import palm.recipe.assistant.model.db.DatabaseHelper

/**
 * Activity for editing an Ingredient.
 */
class IngredientEditActivity : AppCompatActivity() {

    private val dbHelper = DatabaseHelper(this)
    private var ingredID: Int = 0

    private lateinit var nameField: EditText
    private lateinit var unitCostField: EditText
    private lateinit var unitField: Spinner

    private val units by lazy { dbHelper.unitAbbreviations() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_edit)

        // for backwards navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameField = findViewById(R.id.edit_ingred_name)
        unitCostField = findViewById(R.id.edit_ingred_unitCost)

        unitField = findViewById(R.id.edit_ingred_unit)
        unitField.adapter = ArrayAdapter(this, R.layout.spinner_view_unit, units)

        /*
        This activity can be used for creating or editing an Ingredient.
        When editing, the ID of the ingredient to edit is passed as an extra.
        This ID is stored to identify the correct ingredient. An ID of 0
        indicates a new Ingredient.
        */
        when (intent.action) {
            Intent.ACTION_EDIT -> {
                ingredID = intent.getIntExtra(EXTRA_ID, 0)

                // get the values from the database, and fill the fields
                val ingred = dbHelper.getIngredient(ingredID)
                if (ingred != null) {
                    fillFields(ingred)
                }
            }
            Intent.ACTION_INSERT -> {
                // Maybe something later?
            }
        }

        // Set up the save button
        val saveButton = findViewById<Button>(R.id.edit_ingred_save)
        saveButton.setOnClickListener {
            if (validate()) {
                val ingred = createIngredient()

                if (ingredID == 0) {
                    if (dbHelper.addIngredient(ingred) != -1) {
                        toast(getString(R.string.toast_msg_ingred_created, ingred.name))
                    } else {
                        toast(getString(R.string.toast_msg_ingred_failed, ingred.name))
                    }
                } else {
                    if (dbHelper.updateIngredient(ingred)) {
                        toast(getString(R.string.toast_msg_ingred_saved, ingred.name))
                    } else {
                        toast(getString(R.string.toast_msg_ingred_failed, ingred.name))
                    }
                }
                finish()
            }
        }
    }

    /**
     * Fill the form fields based on the values in the given Ingredient.
     *
     * @param ingred the Ingredient to get the values from
     */
    private fun fillFields(ingred: Ingredient) {
        nameField.setText(ingred.name)
        unitCostField.setText(ingred.unitCost.toString())
        unitField.setSelection(units.indexOf(ingred.unit))
//        unitField.setSelection(ingred.unit.ordinal)
    }

    /**
     * Create an Ingredient object based on the values in the form fields.
     *
     * @return a new Ingredient
     */
    private fun createIngredient(): Ingredient {
        return Ingredient(
                ingredID,
                nameField.text.toString(),
                unitCostField.text.toString().toDouble(),
                unitField.selectedItem as String
        )
    }

    /**
     * Check that the form fields contain valid data for an Ingredient.
     * If not, the user is notified with an error message and help.
     *
     * @return true if valid, false otherwise.
     */
    private fun validate(): Boolean {
        var hasError = false

        if (nameField.text.isBlank()) {
            nameField.error = getString(R.string.edit_ingred_error_name)
            hasError = true
        }

        // TODO when creating a new ingredient, check that the name is unique

        val unitCost = unitCostField.text.toString().toDoubleOrNull()
        if (unitCost == null) {
            unitCostField.error = getString(R.string.edit_ingred_error_cost)
            hasError = true
        }

        return !hasError
    }
}
