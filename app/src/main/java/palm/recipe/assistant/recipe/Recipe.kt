package palm.recipe.assistant.recipe

data class Recipe (
        val id: Int,
        val name: String,
        val yield: Double,
        val description: String
)

data class Measure(
        val ingredient: String,
        val measure: Double,
        val unit: String
)