package palm.recipe.assistant.model

/**
 * The Ingredient model
 */
class Ingredient(
    val id: Int,            // database ID (or zero)
    val name: String,       // custom name of the Ingredient
    val unitCost: Double,   // unit cost in dollars
    val unit: IngredUnit    // unit of the unit cost ($[unitCost]/[unit])
) {

    override fun toString() = "$name (\$$unitCost/${unit.name})"
}

/**
 * An enum for measurement units.
 * Each unit has a Type (weight or volume), and an abbreviation.
 *
 * Units of the same type can be converted to one another, but different types
 * cannot, unless there is a conversion.
 *
 * Note: 'Unit' is part of the Kotlin language, so had to call 'IngredUnit'.
 */
enum class IngredUnit(val type: Type, val abbreviation: String) {
    G(Type.WEIGHT, "g"),
    KG(Type.WEIGHT, "kg"),
    ML(Type.VOLUME, "mL"),
    L(Type.VOLUME, "L"),
    TSP(Type.VOLUME, "tsp"),
    TBSP(Type.VOLUME, "tbsp"),
    OZ(Type.VOLUME, "oz"),
    C(Type.VOLUME, "c");

    enum class Type {
        VOLUME, WEIGHT
    }

    companion object {
        fun weightUnits() = listOf(G, KG)
        fun volumeUnits() = listOf(ML, L, TSP, TBSP, OZ, C)
    }

    override fun toString() = abbreviation
}