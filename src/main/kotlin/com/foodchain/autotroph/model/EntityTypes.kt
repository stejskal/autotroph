package com.foodchain.autotroph.model

import kotlinx.serialization.Serializable

/**
 * Sealed class representing all possible entity types in the food-chain system.
 * This provides type safety and prevents invalid entity types.
 */
@Serializable
sealed class EntityType(val label: String, val description: String) {
    @Serializable
    object Ingredient : EntityType("Ingredient", "Basic food item or component used in recipes or meals")

    @Serializable
    object Recipe : EntityType("Recipe", "Set of instructions for preparing food with ingredients and sub-recipes")

    @Serializable
    object Meal : EntityType("Meal", "Complete eating occasion consisting of recipes and/or ingredients")

    @Serializable
    object ShoppingList : EntityType("ShoppingList", "Collection of items needed for purchase")

    @Serializable
    object MealPlan : EntityType("MealPlan", "Structured plan for meals over a period of time")

    @Serializable
    object Cuisine : EntityType("Cuisine", "Style or tradition of cooking with distinctive characteristics")

    @Serializable
    object StoreLocation : EntityType("StoreLocation", "Physical or online location where ingredients can be purchased")

    @Serializable
    object Source : EntityType("Source", "Reference or origin of a recipe or another source")

    @Serializable
    data class Custom(val customLabel: String, val customDescription: String) :
        EntityType(customLabel, customDescription)

    companion object {
        fun fromString(type: String): EntityType = when (type.lowercase()) {
            "ingredient" -> Ingredient
            "recipe" -> Recipe
            "meal" -> Meal
            "shoppinglist", "shopping_list" -> ShoppingList
            "mealplan", "meal_plan" -> MealPlan
            "cuisine" -> Cuisine
            "storelocation", "store_location" -> StoreLocation
            "source" -> Source
            else -> Custom(type, "Custom entity type: $type")
        }

        fun getAllStandardTypes(): List<EntityType> = listOf(
            Ingredient, Recipe, Meal, ShoppingList, MealPlan, Cuisine, StoreLocation, Source
        )
    }
}

/**
 * Sealed class representing all possible relationship types in the food-chain system.
 * This ensures type safety and provides metadata about relationships.
 * All relationships are unidirectional and support 0-to-many cardinality.
 */
@Serializable
sealed class RelationshipType(
    val name: String,
    val description: String,
    val allowedFromTypes: Set<EntityType> = emptySet(),
    val allowedToTypes: Set<EntityType> = emptySet(),
    val isDirectional: Boolean = true
) {
    // Recipe relationships
    @Serializable
    object RecipeUsesIngredient : RelationshipType(
        "USES",
        "Recipe requires specific ingredients",
        allowedFromTypes = setOf(EntityType.Recipe),
        allowedToTypes = setOf(EntityType.Ingredient)
    )

    @Serializable
    object RecipeIncludesRecipe : RelationshipType(
        "INCLUDES",
        "Recipe includes other recipes as components",
        allowedFromTypes = setOf(EntityType.Recipe),
        allowedToTypes = setOf(EntityType.Recipe)
    )

    // Meal relationships
    @Serializable
    object MealContainsRecipe : RelationshipType(
        "CONTAINS",
        "Meal contains one or more recipes",
        allowedFromTypes = setOf(EntityType.Meal),
        allowedToTypes = setOf(EntityType.Recipe)
    )

    @Serializable
    object MealIncludesIngredient : RelationshipType(
        "INCLUDES",
        "Meal includes individual ingredients",
        allowedFromTypes = setOf(EntityType.Meal),
        allowedToTypes = setOf(EntityType.Ingredient)
    )

    // Meal Plan relationships
    @Serializable
    object MealPlanPlansIngredient : RelationshipType(
        "PLANS_FOR",
        "Meal plan directly includes ingredients",
        allowedFromTypes = setOf(EntityType.MealPlan),
        allowedToTypes = setOf(EntityType.Ingredient)
    )

    @Serializable
    object MealPlanPlansRecipe : RelationshipType(
        "PLANS_FOR",
        "Meal plan includes specific recipes",
        allowedFromTypes = setOf(EntityType.MealPlan),
        allowedToTypes = setOf(EntityType.Recipe)
    )

    @Serializable
    object MealPlanSchedulesMeal : RelationshipType(
        "SCHEDULES",
        "Meal plan schedules complete meals",
        allowedFromTypes = setOf(EntityType.MealPlan),
        allowedToTypes = setOf(EntityType.Meal)
    )

    // Shopping List relationships
    @Serializable
    object ShoppingListNeedsIngredient : RelationshipType(
        "NEEDS",
        "Shopping list directly includes ingredients",
        allowedFromTypes = setOf(EntityType.ShoppingList),
        allowedToTypes = setOf(EntityType.Ingredient)
    )

    @Serializable
    object ShoppingListNeedsRecipe : RelationshipType(
        "NEEDS_FOR",
        "Shopping list includes recipes to derive ingredients",
        allowedFromTypes = setOf(EntityType.ShoppingList),
        allowedToTypes = setOf(EntityType.Recipe)
    )

    @Serializable
    object ShoppingListNeedsMeal : RelationshipType(
        "NEEDS_FOR",
        "Shopping list includes meals to derive all components",
        allowedFromTypes = setOf(EntityType.ShoppingList),
        allowedToTypes = setOf(EntityType.Meal)
    )

    @Serializable
    object ShoppingListNeedsMealPlan : RelationshipType(
        "NEEDS_FOR",
        "Shopping list includes meal plans to derive all components",
        allowedFromTypes = setOf(EntityType.ShoppingList),
        allowedToTypes = setOf(EntityType.MealPlan)
    )

    // Location and Cuisine relationships
    @Serializable
    object IngredientAvailableAt : RelationshipType(
        "AVAILABLE_AT",
        "Ingredient can be purchased at specific store locations",
        allowedFromTypes = setOf(EntityType.Ingredient),
        allowedToTypes = setOf(EntityType.StoreLocation)
    )

    @Serializable
    object RecipeBelongsToCuisine : RelationshipType(
        "BELONGS_TO",
        "Recipe is categorized under a specific cuisine type",
        allowedFromTypes = setOf(EntityType.Recipe),
        allowedToTypes = setOf(EntityType.Cuisine)
    )

    @Serializable
    object MealBelongsToCuisine : RelationshipType(
        "BELONGS_TO",
        "Meal is categorized under a specific cuisine type",
        allowedFromTypes = setOf(EntityType.Meal),
        allowedToTypes = setOf(EntityType.Cuisine)
    )

    // Source relationships
    @Serializable
    object SourceFromSource : RelationshipType(
        "FROM",
        "Source references another source as its origin",
        allowedFromTypes = setOf(EntityType.Source),
        allowedToTypes = setOf(EntityType.Source)
    )

    @Serializable
    object RecipeFromSource : RelationshipType(
        "FROM",
        "Recipe references a source as its origin",
        allowedFromTypes = setOf(EntityType.Recipe),
        allowedToTypes = setOf(EntityType.Source)
    )

    @Serializable
    data class Custom(val customName: String, val customDescription: String) :
        RelationshipType(customName, customDescription)

    companion object {
        fun fromString(type: String): RelationshipType = when (type.uppercase()) {
            "USES" -> RecipeUsesIngredient
            "INCLUDES" -> RecipeIncludesRecipe // Note: This handles both Recipe->Recipe and Meal->Ingredient
            "CONTAINS" -> MealContainsRecipe
            "PLANS_FOR" -> MealPlanPlansIngredient // Note: This handles multiple MealPlan relationships
            "SCHEDULES" -> MealPlanSchedulesMeal
            "NEEDS" -> ShoppingListNeedsIngredient
            "NEEDS_FOR" -> ShoppingListNeedsRecipe // Note: This handles multiple ShoppingList relationships
            "AVAILABLE_AT" -> IngredientAvailableAt
            "BELONGS_TO" -> RecipeBelongsToCuisine // Note: This handles both Recipe->Cuisine and Meal->Cuisine
            "FROM" -> SourceFromSource // Note: This handles both Source->Source and Recipe->Source
            else -> Custom(type, "Custom relationship type: $type")
        }

        fun getAllStandardTypes(): List<RelationshipType> = listOf(
            RecipeUsesIngredient, RecipeIncludesRecipe, MealContainsRecipe, MealIncludesIngredient,
            MealPlanPlansIngredient, MealPlanPlansRecipe, MealPlanSchedulesMeal,
            ShoppingListNeedsIngredient, ShoppingListNeedsRecipe, ShoppingListNeedsMeal, ShoppingListNeedsMealPlan,
            IngredientAvailableAt, RecipeBelongsToCuisine, MealBelongsToCuisine,
            SourceFromSource, RecipeFromSource
        )

        /**
         * Get appropriate relationship type based on from and to entity types
         */
        fun getRelationshipType(fromType: EntityType, toType: EntityType): RelationshipType? = when {
            fromType == EntityType.Recipe && toType == EntityType.Ingredient -> RecipeUsesIngredient
            fromType == EntityType.Recipe && toType == EntityType.Recipe -> RecipeIncludesRecipe
            fromType == EntityType.Meal && toType == EntityType.Recipe -> MealContainsRecipe
            fromType == EntityType.Meal && toType == EntityType.Ingredient -> MealIncludesIngredient
            fromType == EntityType.MealPlan && toType == EntityType.Ingredient -> MealPlanPlansIngredient
            fromType == EntityType.MealPlan && toType == EntityType.Recipe -> MealPlanPlansRecipe
            fromType == EntityType.MealPlan && toType == EntityType.Meal -> MealPlanSchedulesMeal
            fromType == EntityType.ShoppingList && toType == EntityType.Ingredient -> ShoppingListNeedsIngredient
            fromType == EntityType.ShoppingList && toType == EntityType.Recipe -> ShoppingListNeedsRecipe
            fromType == EntityType.ShoppingList && toType == EntityType.Meal -> ShoppingListNeedsMeal
            fromType == EntityType.ShoppingList && toType == EntityType.MealPlan -> ShoppingListNeedsMealPlan
            fromType == EntityType.Ingredient && toType == EntityType.StoreLocation -> IngredientAvailableAt
            fromType == EntityType.Recipe && toType == EntityType.Cuisine -> RecipeBelongsToCuisine
            fromType == EntityType.Meal && toType == EntityType.Cuisine -> MealBelongsToCuisine
            fromType == EntityType.Source && toType == EntityType.Source -> SourceFromSource
            fromType == EntityType.Recipe && toType == EntityType.Source -> RecipeFromSource
            else -> null
        }
    }
}

/**
 * Validation helper for relationship constraints
 */
object RelationshipValidator {
    fun isValidRelationship(
        relationshipType: RelationshipType,
        fromEntityType: EntityType,
        toEntityType: EntityType
    ): Boolean {
        // If no constraints are defined, allow any relationship
        if (relationshipType.allowedFromTypes.isEmpty() && relationshipType.allowedToTypes.isEmpty()) {
            return true
        }
        
        val fromValid = relationshipType.allowedFromTypes.isEmpty() || 
                       relationshipType.allowedFromTypes.contains(fromEntityType)
        val toValid = relationshipType.allowedToTypes.isEmpty() || 
                     relationshipType.allowedToTypes.contains(toEntityType)
        
        return fromValid && toValid
    }
    
    fun getValidationError(
        relationshipType: RelationshipType,
        fromEntityType: EntityType,
        toEntityType: EntityType
    ): String? {
        if (isValidRelationship(relationshipType, fromEntityType, toEntityType)) {
            return null
        }
        
        return "Invalid relationship: ${relationshipType.name} cannot connect " +
               "${fromEntityType.label} to ${toEntityType.label}"
    }
}
