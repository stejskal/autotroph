package com.foodchain.autotroph.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

/**
 * Property enums for food-chain entities
 */
@Serializable
enum class PurchaseFrequency {
    Always,
    Usually,
    Rarely
}

/**
 * Base interface for all food-chain entities
 */
interface FoodChainEntity {
    val id: Long?
    val name: String
    val description: String?
    val createdAt: Instant
    val updatedAt: Instant
}

/**
 * Ingredient entity - Basic food item or component
 */
@Serializable
data class Ingredient(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now(),
    val purchaseFrequency: PurchaseFrequency? = null,
    val storeLocations: List<Long> = emptyList(), // References to StoreLocation entities
    val embedding: List<Double>? = null // Vector embedding for semantic similarity
) : FoodChainEntity

/**
 * Recipe entity - Set of instructions for preparing food
 */
@Serializable
data class Recipe(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now(),
    val ingredients: List<Long> = emptyList(), // References to Ingredient entities
    val subRecipes: List<Long> = emptyList(), // References to other Recipe entities
    val cuisines: List<Long> = emptyList(), // References to Cuisine entities
    val sources: List<Long> = emptyList() // References to Source entities
) : FoodChainEntity

/**
 * Meal entity - Complete eating occasion
 */
@Serializable
data class Meal(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now(),
    val recipes: List<Long> = emptyList(), // References to Recipe entities
    val ingredients: List<Long> = emptyList(), // References to Ingredient entities
    val cuisines: List<Long> = emptyList() // References to Cuisine entities
) : FoodChainEntity

/**
 * Shopping List entity - Collection of items needed for purchase
 */
@Serializable
data class ShoppingList(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now(),
    val ingredients: List<Long> = emptyList(), // References to Ingredient entities
    val recipes: List<Long> = emptyList(), // References to Recipe entities
    val meals: List<Long> = emptyList(), // References to Meal entities
    val mealPlans: List<Long> = emptyList() // References to MealPlan entities
) : FoodChainEntity

/**
 * Meal Plan entity - Structured plan for meals over time
 */
@Serializable
data class MealPlan(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now(),
    val ingredients: List<Long> = emptyList(), // References to Ingredient entities
    val recipes: List<Long> = emptyList(), // References to Recipe entities
    val meals: List<Long> = emptyList() // References to Meal entities
) : FoodChainEntity

/**
 * Cuisine entity - Style or tradition of cooking
 */
@Serializable
data class Cuisine(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now()
) : FoodChainEntity

/**
 * Store Location entity - Where ingredients can be purchased
 */
@Serializable
data class StoreLocation(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now()
) : FoodChainEntity

/**
 * Source entity - Reference or origin of a recipe or another source
 */
@Serializable
data class Source(
    override val id: Long? = null,
    override val name: String,
    override val description: String? = null,
    override val createdAt: Instant = Clock.System.now(),
    override val updatedAt: Instant = Clock.System.now(),
    val sources: List<Long> = emptyList() // References to other Source entities
) : FoodChainEntity

/**
 * Utility functions for entity conversion
 */
object FoodChainEntityConverter {
    
    fun toEntityNode(entity: FoodChainEntity): EntityNode {
        val properties = mutableMapOf<String, JsonElement>()
        
        when (entity) {
            is Ingredient -> {
                entity.purchaseFrequency?.let {
                    properties["purchaseFrequency"] = kotlinx.serialization.json.JsonPrimitive(it.name)
                }
                entity.embedding?.let {
                    properties["embedding"] = kotlinx.serialization.json.JsonArray(it.map { value -> kotlinx.serialization.json.JsonPrimitive(value) })
                }
            }
            is Recipe -> {
                // No additional properties beyond base entity
            }
            is Meal -> {
                // No additional properties beyond base entity
            }
            is ShoppingList -> {
                // No additional properties beyond base entity
            }
            is MealPlan -> {
                // No additional properties beyond base entity
            }
            is Cuisine -> {
                // No additional properties beyond base entity
            }
            is StoreLocation -> {
                // No additional properties beyond base entity
            }
            is Source -> {
                // No additional properties beyond base entity
            }
        }
        
        return EntityNode(
            id = entity.id,
            name = entity.name,
            type = getEntityTypeName(entity),
            description = entity.description,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            properties = properties,
            relatedEntities = getRelatedEntityIds(entity)
        )
    }
    
    private fun getEntityTypeName(entity: FoodChainEntity): String = when (entity) {
        is Ingredient -> "Ingredient"
        is Recipe -> "Recipe"
        is Meal -> "Meal"
        is ShoppingList -> "ShoppingList"
        is MealPlan -> "MealPlan"
        is Cuisine -> "Cuisine"
        is StoreLocation -> "StoreLocation"
        is Source -> "Source"
        else -> "Unknown"
    }
    
    private fun getRelatedEntityIds(entity: FoodChainEntity): List<Long> = when (entity) {
        is Ingredient -> entity.storeLocations
        is Recipe -> entity.ingredients + entity.subRecipes + entity.cuisines + entity.sources
        is Meal -> entity.recipes + entity.ingredients + entity.cuisines
        is ShoppingList -> entity.ingredients + entity.recipes + entity.meals + entity.mealPlans
        is MealPlan -> entity.ingredients + entity.recipes + entity.meals
        is Cuisine -> emptyList()
        is StoreLocation -> emptyList()
        is Source -> entity.sources
        else -> emptyList()
    }
}
