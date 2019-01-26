package palm.recipe.assistant.model

class Recipe (
        val id: Int,
        val name: String,
        val yield: Double,
        val description: String
)

class Measure(
        val id: Int,
        val ingredient: String,
        val measure: Double,
        val unit: String
)