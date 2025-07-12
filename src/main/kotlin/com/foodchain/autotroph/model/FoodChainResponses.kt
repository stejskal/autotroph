package com.foodchain.autotroph.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Response DTOs for food-chain entities
 */

@Serializable
data class IngredientResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val purchaseFrequency: PurchaseFrequency?,
    val storeLocationsCount: Int = 0,
    val storeLocations: List<StoreLocationResponse> = emptyList()
) {
    companion object {
        fun from(ingredient: Ingredient, storeLocations: List<StoreLocationResponse> = emptyList()): IngredientResponse {
            return IngredientResponse(
                id = ingredient.id!!,
                name = ingredient.name,
                description = ingredient.description,
                createdAt = ingredient.createdAt,
                updatedAt = ingredient.updatedAt,
                purchaseFrequency = ingredient.purchaseFrequency,
                storeLocationsCount = ingredient.storeLocations.size,
                storeLocations = storeLocations
            )
        }
    }
}

@Serializable
data class RecipeResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val ingredientsCount: Int = 0,
    val subRecipesCount: Int = 0,
    val cuisinesCount: Int = 0,
    val ingredients: List<IngredientResponse> = emptyList(),
    val subRecipes: List<RecipeResponse> = emptyList(),
    val cuisines: List<CuisineResponse> = emptyList()
) {
    companion object {
        fun from(
            recipe: Recipe,
            ingredients: List<IngredientResponse> = emptyList(),
            subRecipes: List<RecipeResponse> = emptyList(),
            cuisines: List<CuisineResponse> = emptyList()
        ): RecipeResponse {
            return RecipeResponse(
                id = recipe.id!!,
                name = recipe.name,
                description = recipe.description,
                createdAt = recipe.createdAt,
                updatedAt = recipe.updatedAt,
                ingredientsCount = recipe.ingredients.size,
                subRecipesCount = recipe.subRecipes.size,
                cuisinesCount = recipe.cuisines.size,
                ingredients = ingredients,
                subRecipes = subRecipes,
                cuisines = cuisines
            )
        }
    }
}

@Serializable
data class MealResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val recipesCount: Int = 0,
    val ingredientsCount: Int = 0,
    val cuisinesCount: Int = 0,
    val recipes: List<RecipeResponse> = emptyList(),
    val ingredients: List<IngredientResponse> = emptyList(),
    val cuisines: List<CuisineResponse> = emptyList()
) {
    companion object {
        fun from(
            meal: Meal,
            recipes: List<RecipeResponse> = emptyList(),
            ingredients: List<IngredientResponse> = emptyList(),
            cuisines: List<CuisineResponse> = emptyList()
        ): MealResponse {
            return MealResponse(
                id = meal.id!!,
                name = meal.name,
                description = meal.description,
                createdAt = meal.createdAt,
                updatedAt = meal.updatedAt,
                recipesCount = meal.recipes.size,
                ingredientsCount = meal.ingredients.size,
                cuisinesCount = meal.cuisines.size,
                recipes = recipes,
                ingredients = ingredients,
                cuisines = cuisines
            )
        }
    }
}

@Serializable
data class ShoppingListResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val ingredientsCount: Int = 0,
    val recipesCount: Int = 0,
    val mealsCount: Int = 0,
    val mealPlansCount: Int = 0,
    val ingredients: List<IngredientResponse> = emptyList(),
    val recipes: List<RecipeResponse> = emptyList(),
    val meals: List<MealResponse> = emptyList(),
    val mealPlans: List<MealPlanResponse> = emptyList()
) {
    companion object {
        fun from(
            shoppingList: ShoppingList,
            ingredients: List<IngredientResponse> = emptyList(),
            recipes: List<RecipeResponse> = emptyList(),
            meals: List<MealResponse> = emptyList(),
            mealPlans: List<MealPlanResponse> = emptyList()
        ): ShoppingListResponse {
            return ShoppingListResponse(
                id = shoppingList.id!!,
                name = shoppingList.name,
                description = shoppingList.description,
                createdAt = shoppingList.createdAt,
                updatedAt = shoppingList.updatedAt,
                ingredientsCount = shoppingList.ingredients.size,
                recipesCount = shoppingList.recipes.size,
                mealsCount = shoppingList.meals.size,
                mealPlansCount = shoppingList.mealPlans.size,
                ingredients = ingredients,
                recipes = recipes,
                meals = meals,
                mealPlans = mealPlans
            )
        }
    }
}

@Serializable
data class MealPlanResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val ingredientsCount: Int = 0,
    val recipesCount: Int = 0,
    val mealsCount: Int = 0,
    val ingredients: List<IngredientResponse> = emptyList(),
    val recipes: List<RecipeResponse> = emptyList(),
    val meals: List<MealResponse> = emptyList()
) {
    companion object {
        fun from(
            mealPlan: MealPlan,
            ingredients: List<IngredientResponse> = emptyList(),
            recipes: List<RecipeResponse> = emptyList(),
            meals: List<MealResponse> = emptyList()
        ): MealPlanResponse {
            return MealPlanResponse(
                id = mealPlan.id!!,
                name = mealPlan.name,
                description = mealPlan.description,
                createdAt = mealPlan.createdAt,
                updatedAt = mealPlan.updatedAt,
                ingredientsCount = mealPlan.ingredients.size,
                recipesCount = mealPlan.recipes.size,
                mealsCount = mealPlan.meals.size,
                ingredients = ingredients,
                recipes = recipes,
                meals = meals
            )
        }
    }
}

@Serializable
data class CuisineResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(cuisine: Cuisine): CuisineResponse {
            return CuisineResponse(
                id = cuisine.id!!,
                name = cuisine.name,
                description = cuisine.description,
                createdAt = cuisine.createdAt,
                updatedAt = cuisine.updatedAt
            )
        }
    }
}

@Serializable
data class StoreLocationResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(storeLocation: StoreLocation): StoreLocationResponse {
            return StoreLocationResponse(
                id = storeLocation.id!!,
                name = storeLocation.name,
                description = storeLocation.description,
                createdAt = storeLocation.createdAt,
                updatedAt = storeLocation.updatedAt
            )
        }
    }
}

/**
 * Relationship response DTOs
 */

@Serializable
data class RelationshipResponse(
    val id: String, // Composite ID: "fromId-toId-relationshipType"
    val fromEntityId: Long,
    val toEntityId: Long,
    val relationshipType: String,
    val createdAt: Instant,
    val fromEntity: EntitySummary? = null,
    val toEntity: EntitySummary? = null
)

@Serializable
data class EntitySummary(
    val id: Long,
    val name: String,
    val type: String,
    val description: String?
)

/**
 * Aggregated response DTOs
 */

@Serializable
data class EntityWithRelationshipsResponse(
    val entity: EntityResponse,
    val relationships: List<RelationshipResponse>
)

@Serializable
data class MealPlanSummaryResponse(
    val mealPlan: MealPlanResponse,
    val totalIngredients: Int,
    val totalRecipes: Int,
    val totalMeals: Int,
    val dateRange: String?,
    val completionStatus: String // "not_started", "in_progress", "completed"
)

@Serializable
data class ShoppingListSummaryResponse(
    val shoppingList: ShoppingListResponse,
    val totalItems: Int,
    val completedItems: Int,
    val pendingItems: Int,
    val estimatedCost: Double? = null,
    val storeLocations: List<StoreLocationResponse> = emptyList()
)

/**
 * Search and analytics response DTOs
 */

@Serializable
data class SearchResultsResponse<T>(
    val results: List<T>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)

@Serializable
data class EntityStatsResponse(
    val entityType: String,
    val totalCount: Int,
    val createdThisWeek: Int,
    val createdThisMonth: Int,
    val mostPopular: List<EntitySummary> = emptyList()
)

/**
 * Response DTO for ingredient similarity search results
 */
@Serializable
data class SimilarIngredientResponse(
    val id: Long,
    val name: String,
    val similarity: Double
)

/**
 * Response DTO for extracted recipe content from URL
 */
@Serializable
data class ExtractedRecipeResponse(
    val url: String,
    val simplifiedName: String?, // OpenAI-generated simplified recipe name
    val ingredients: List<ExtractedIngredientWithMatches>, // OpenAI-extracted ingredients with similarity matches
    val success: Boolean,
    val errorMessage: String? = null
)

/**
 * Response DTO for an extracted ingredient with its similarity matches
 */
@Serializable
data class ExtractedIngredientWithMatches(
    val name: String, // Original ingredient name from OpenAI
    val similarIngredients: List<SimilarIngredientResponse> // Top 5 similar ingredients from database
)
