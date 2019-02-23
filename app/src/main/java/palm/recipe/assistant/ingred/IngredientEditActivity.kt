package palm.recipe.assistant.ingred

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_ingredient_edit.*
import palm.recipe.assistant.R
import palm.recipe.assistant.base.DatabaseHelper
import palm.recipe.assistant.base.EXTRA_ID
import palm.recipe.assistant.base.toast

/**
 * Activity for editing an Ingredient.
 */
class IngredientEditActivity : AppCompatActivity() {

    private val dbHelper = DatabaseHelper(this)
    private var ingredID: Int = 0

    private val nameField by lazy { edit_ingred_name }
    private val unitCostField by lazy { edit_ingred_unitCost }
    private val unitField by lazy { edit_ingred_unit }

    private val units by lazy { dbHelper.unitAbbreviations() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_edit)

        // for backwards navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        val saveButton = edit_ingred_save
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
    }

    /**
     * Create an Ingredient object based on the values in the form fields.
     *
     * @return a new Ingredient
     */
    private fun createIngredient() = Ingredient(
            ingredID,
            nameField.text.toString(),
            unitCostField.text.toString().toDouble(),
            unitField.selectedItem as String
    )

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
