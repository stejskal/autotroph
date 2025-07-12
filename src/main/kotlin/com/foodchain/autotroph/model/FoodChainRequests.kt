package com.foodchain.autotroph.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Request DTOs for creating food-chain entities
 */

@Serializable
data class CreateIngredientRequest(
    val name: String,
    val description: String? = null,
    val purchaseFrequency: PurchaseFrequency? = null
)

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateMealRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateShoppingListRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateMealPlanRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateCuisineRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateStoreLocationRequest(
    val name: String,
    val description: String? = null
)

/**
 * Request DTOs for updating food-chain entities
 */

@Serializable
data class UpdateIngredientRequest(
    val name: String? = null,
    val description: String? = null,
    val purchaseFrequency: PurchaseFrequency? = null
)

@Serializable
data class UpdateRecipeRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateMealRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateShoppingListRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateMealPlanRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateCuisineRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateStoreLocationRequest(
    val name: String? = null,
    val description: String? = null
)

/**
 * Request DTOs for managing relationships
 */

@Serializable
data class AddRelationshipRequest(
    val toEntityId: Long,
    val relationshipType: String? = null // Optional, will be inferred from entity types if not provided
)

@Serializable
data class RemoveRelationshipRequest(
    val toEntityId: Long,
    val relationshipType: String? = null
)

@Serializable
data class BulkAddRelationshipsRequest(
    val relationships: List<AddRelationshipRequest>
)

@Serializable
data class BulkRemoveRelationshipsRequest(
    val relationships: List<RemoveRelationshipRequest>
)

/**
 * Specialized requests for common operations
 */

@Serializable
data class AddIngredientToRecipeRequest(
    val ingredientId: Long,
    val quantity: String? = null,
    val unit: String? = null,
    val notes: String? = null
)

@Serializable
data class AddRecipeToMealRequest(
    val recipeId: Long,
    val servings: Int? = null,
    val notes: String? = null
)

@Serializable
data class AddItemToShoppingListRequest(
    val itemType: String, // "ingredient", "recipe", "meal", "mealplan"
    val itemId: Long,
    val quantity: String? = null,
    val priority: String? = null, // "high", "medium", "low"
    val notes: String? = null
)

@Serializable
data class ScheduleMealRequest(
    val mealId: Long,
    val scheduledFor: Instant,
    val notes: String? = null
)

/**
 * Query/Filter requests
 */

@Serializable
data class EntityFilterRequest(
    val entityType: String? = null,
    val name: String? = null,
    val createdAfter: Instant? = null,
    val createdBefore: Instant? = null,
    val hasRelationshipTo: Long? = null,
    val limit: Int = 50,
    val offset: Int = 0
)

@Serializable
data class RelationshipFilterRequest(
    val fromEntityId: Long? = null,
    val toEntityId: Long? = null,
    val relationshipType: String? = null,
    val limit: Int = 50,
    val offset: Int = 0
)

/**
 * Bulk operation requests
 */

@Serializable
data class BulkCreateEntitiesRequest(
    val entities: List<CreateEntityRequest>
)

@Serializable
data class BulkUpdateEntitiesRequest(
    val updates: Map<Long, UpdateEntityRequest>
)

@Serializable
data class BulkDeleteEntitiesRequest(
    val entityIds: List<Long>
)

/**
 * Import/Export requests
 */

@Serializable
data class ImportRecipeRequest(
    val name: String,
    val description: String? = null,
    val instructions: String,
    val ingredients: List<String>, // Ingredient names that will be created if they don't exist
    val servings: Int? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val cuisineName: String? = null
)

@Serializable
data class ExportMealPlanRequest(
    val mealPlanId: Long,
    val includeIngredients: Boolean = true,
    val includeRecipes: Boolean = true,
    val includeShoppingList: Boolean = true,
    val format: String = "json" // "json", "csv", "pdf"
)

/**
 * Request DTO for finding similar ingredients
 */
@Serializable
data class FindSimilarIngredientsRequest(
    val ingredientName: String,
    val topK: Int = 10
)

/**
 * Request DTO for extracting recipe from URL
 */
@Serializable
data class ExtractRecipeFromUrlRequest(
    val url: String
)

/**
 * Request DTO for extracting recipe from text
 */
@Serializable
data class ExtractRecipeFromTextRequest(
    val text: String
)
