package palm.recipe.assistant.ingred

/**
 * The Ingredient model
 */
data class Ingredient(
    val id: Int,            // database ID (or zero)
    val name: String,       // custom name of the Ingredient
    val unitCost: Double,   // unit cost in dollars
    val unit: String        // unit of the unit cost ($[unitCost]/[unit])
) {

    override fun toString() = "$name (\$$unitCost/$unit)"
}