package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.*
import com.foodchain.autotroph.repository.EntityRepository
import kotlinx.datetime.Clock
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Service layer for food-chain specific operations.
 * This service provides high-level operations for managing food-chain entities
 * and their relationships, built on top of the generic EntityService.
 */
class FoodChainService(
    private val entityRepository: EntityRepository,
    private val entityService: EntityService,
    private val embeddingService: EmbeddingService
) {

    // ========== Ingredient Operations ==========

    suspend fun createIngredient(request: CreateIngredientRequest): IngredientResponse {
        logger.info { "Creating ingredient: ${request.name}" }

        // Generate embedding for the ingredient name
        val embedding = embeddingService.generateEmbedding(request.name)
        logger.debug { "Generated embedding for ingredient '${request.name}' with ${embedding.size} dimensions" }

        val ingredient = Ingredient(
            name = request.name,
            description = request.description,
            purchaseFrequency = request.purchaseFrequency,
            embedding = embedding
        )

        val entityNode = FoodChainEntityConverter.toEntityNode(ingredient)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )

        return IngredientResponse.from(
            ingredient.copy(id = createdEntity.id)
        )
    }

    suspend fun getIngredient(id: Long, includeStoreLocations: Boolean = false): IngredientResponse? {
        logger.info { "Fetching ingredient with id: $id" }

        val entity = entityService.getEntityById(id) ?: return null
        if (entity.type != "Ingredient") return null

        val ingredient = entityNodeToIngredient(entity)
        val storeLocations = if (includeStoreLocations) {
            getStoreLocationsForIngredient(id)
        } else emptyList()

        return IngredientResponse.from(ingredient, storeLocations)
    }

    suspend fun updateIngredient(id: Long, request: UpdateIngredientRequest): IngredientResponse? {
        logger.info { "Updating ingredient with id: $id" }

        val existingEntity = entityService.getEntityById(id) ?: return null
        if (existingEntity.type != "Ingredient") return null
        
        val updateRequest = UpdateEntityRequest(
            name = request.name,
            description = request.description,
            properties = buildMap {
                putAll(existingEntity.properties)
                request.purchaseFrequency?.let { 
                    put("purchaseFrequency", kotlinx.serialization.json.JsonPrimitive(it.name))
                }
            }
        )
        
        val updatedEntity = entityService.updateEntity(id, updateRequest) ?: return null
        val ingredient = entityNodeToIngredient(updatedEntity)
        
        return IngredientResponse.from(ingredient)
    }

    suspend fun deleteIngredient(id: Long): Boolean {
        logger.info { "Deleting ingredient with id: $id" }
        return entityService.deleteEntity(id)
    }

    suspend fun addStoreLocationToIngredient(ingredientId: Long, storeLocationId: Long): Boolean {
        logger.info { "Adding store location $storeLocationId to ingredient $ingredientId" }
        return entityService.createRelationship(ingredientId, storeLocationId)
    }

    suspend fun findSimilarIngredients(ingredientName: String, topK: Int = 10): List<SimilarIngredientResponse> {
        logger.info { "Finding similar ingredients for: '$ingredientName' (top $topK)" }

        // Generate embedding for the input ingredient name
        val targetEmbedding = embeddingService.generateEmbedding(ingredientName)
        logger.debug { "Generated target embedding with ${targetEmbedding.size} dimensions" }

        // Get all ingredients from the database
        val allIngredientEntities = entityService.getEntitiesByType("Ingredient")
        logger.debug { "Found ${allIngredientEntities.size} ingredients in database" }

        // Convert to ingredients with embeddings, filtering out those without embeddings
        val ingredientsWithEmbeddings = allIngredientEntities.mapNotNull { entity ->
            val ingredient = entityNodeToIngredient(entity)
            if (ingredient.embedding != null && ingredient.embedding.isNotEmpty()) {
                ingredient to ingredient.embedding
            } else {
                logger.debug { "Skipping ingredient '${ingredient.name}' - no embedding found" }
                null
            }
        }

        logger.debug { "Found ${ingredientsWithEmbeddings.size} ingredients with embeddings" }

        if (ingredientsWithEmbeddings.isEmpty()) {
            logger.warn { "No ingredients with embeddings found" }
            return emptyList()
        }

        // Find most similar ingredients using embedding service
        val similarityResults = embeddingService.findMostSimilar(
            targetEmbedding = targetEmbedding,
            candidateEmbeddings = ingredientsWithEmbeddings,
            topK = topK
        )

        // Convert to response DTOs
        return similarityResults.map { result ->
            SimilarIngredientResponse(
                id = result.data.id!!,
                name = result.data.name,
                similarity = result.similarity
            )
        }
    }

    // ========== Recipe Operations ==========

    suspend fun createRecipe(request: CreateRecipeRequest): RecipeResponse {
        logger.info { "Creating recipe: ${request.name}" }

        val recipe = Recipe(
            name = request.name,
            description = request.description
        )
        
        val entityNode = FoodChainEntityConverter.toEntityNode(recipe)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )
        
        return RecipeResponse.from(
            recipe.copy(id = createdEntity.id)
        )
    }

    suspend fun getRecipe(id: Long, includeRelated: Boolean = false): RecipeResponse? {
        logger.info { "Fetching recipe with id: $id" }

        val entity = entityService.getEntityById(id) ?: return null
        if (entity.type != "Recipe") return null
        
        val recipe = entityNodeToRecipe(entity)
        
        if (!includeRelated) {
            return RecipeResponse.from(recipe)
        }
        
        val ingredients = getIngredientsForRecipe(id)
        val subRecipes = getSubRecipesForRecipe(id)
        val cuisines = getCuisinesForRecipe(id)
        
        return RecipeResponse.from(recipe, ingredients, subRecipes, cuisines)
    }

    suspend fun addIngredientToRecipe(recipeId: Long, ingredientId: Long): Boolean {
        logger.info { "Adding ingredient $ingredientId to recipe $recipeId" }
        return entityService.createRelationship(recipeId, ingredientId)
    }

    suspend fun addSubRecipeToRecipe(parentRecipeId: Long, subRecipeId: Long): Boolean {
        logger.info { "Adding sub-recipe $subRecipeId to recipe $parentRecipeId" }
        return entityService.createRelationship(parentRecipeId, subRecipeId)
    }

    // ========== Meal Operations ==========

    suspend fun createMeal(request: CreateMealRequest): MealResponse {
        logger.info { "Creating meal: ${request.name}" }

        val meal = Meal(
            name = request.name,
            description = request.description
        )
        
        val entityNode = FoodChainEntityConverter.toEntityNode(meal)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )
        
        return MealResponse.from(
            meal.copy(id = createdEntity.id)
        )
    }

    suspend fun addRecipeToMeal(mealId: Long, recipeId: Long): Boolean {
        logger.info { "Adding recipe $recipeId to meal $mealId" }
        return entityService.createRelationship(mealId, recipeId)
    }

    suspend fun addIngredientToMeal(mealId: Long, ingredientId: Long): Boolean {
        logger.info { "Adding ingredient $ingredientId to meal $mealId" }
        return entityService.createRelationship(mealId, ingredientId)
    }

    // ========== Shopping List Operations ==========

    suspend fun createShoppingList(request: CreateShoppingListRequest): ShoppingListResponse {
        logger.info { "Creating shopping list: ${request.name}" }

        val shoppingList = ShoppingList(
            name = request.name,
            description = request.description
        )
        
        val entityNode = FoodChainEntityConverter.toEntityNode(shoppingList)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )
        
        return ShoppingListResponse.from(
            shoppingList.copy(id = createdEntity.id)
        )
    }

    suspend fun addItemToShoppingList(shoppingListId: Long, itemId: Long): Boolean {
        logger.info { "Adding item $itemId to shopping list $shoppingListId" }
        return entityService.createRelationship(shoppingListId, itemId)
    }

    // ========== Meal Plan Operations ==========

    suspend fun createMealPlan(request: CreateMealPlanRequest): MealPlanResponse {
        logger.info { "Creating meal plan: ${request.name}" }

        val mealPlan = MealPlan(
            name = request.name,
            description = request.description
        )
        
        val entityNode = FoodChainEntityConverter.toEntityNode(mealPlan)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )
        
        return MealPlanResponse.from(
            mealPlan.copy(id = createdEntity.id)
        )
    }

    // ========== Cuisine Operations ==========

    suspend fun createCuisine(request: CreateCuisineRequest): CuisineResponse {
        logger.info { "Creating cuisine: ${request.name}" }

        val cuisine = Cuisine(
            name = request.name,
            description = request.description
        )
        
        val entityNode = FoodChainEntityConverter.toEntityNode(cuisine)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )
        
        return CuisineResponse.from(
            cuisine.copy(id = createdEntity.id)
        )
    }

    // ========== Store Location Operations ==========

    suspend fun createStoreLocation(request: CreateStoreLocationRequest): StoreLocationResponse {
        logger.info { "Creating store location: ${request.name}" }

        val storeLocation = StoreLocation(
            name = request.name,
            description = request.description
        )
        
        val entityNode = FoodChainEntityConverter.toEntityNode(storeLocation)
        val createdEntity = entityService.createEntity(
            CreateEntityRequest(
                name = entityNode.name,
                type = entityNode.type,
                description = entityNode.description,
                properties = entityNode.properties
            )
        )
        
        return StoreLocationResponse.from(
            storeLocation.copy(id = createdEntity.id)
        )
    }

    // ========== Helper Methods ==========

    private fun entityNodeToIngredient(entity: EntityResponse): Ingredient {
        val purchaseFrequency = entity.properties["purchaseFrequency"]?.let {
            PurchaseFrequency.valueOf(it.toString().trim('"'))
        }

        val embedding = entity.properties["embedding"]?.let { embeddingJson ->
            try {
                // Parse JsonArray to List<Double>
                if (embeddingJson is kotlinx.serialization.json.JsonArray) {
                    embeddingJson.map { it.toString().toDouble() }
                } else {
                    logger.warn { "Embedding property is not a JsonArray for ingredient ${entity.name}" }
                    null
                }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to parse embedding for ingredient ${entity.name}" }
                null
            }
        }

        return Ingredient(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            purchaseFrequency = purchaseFrequency,
            embedding = embedding
        )
    }

    private fun entityNodeToRecipe(entity: EntityResponse): Recipe {
        return Recipe(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private suspend fun getStoreLocationsForIngredient(ingredientId: Long): List<StoreLocationResponse> {
        // This would need to be implemented with proper relationship traversal
        return emptyList()
    }

    private suspend fun getIngredientsForRecipe(recipeId: Long): List<IngredientResponse> {
        // This would need to be implemented with proper relationship traversal
        return emptyList()
    }

    private suspend fun getSubRecipesForRecipe(recipeId: Long): List<RecipeResponse> {
        // This would need to be implemented with proper relationship traversal
        return emptyList()
    }

    private suspend fun getCuisinesForRecipe(recipeId: Long): List<CuisineResponse> {
        // This would need to be implemented with proper relationship traversal
        return emptyList()
    }
}
