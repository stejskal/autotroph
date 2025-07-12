package com.foodchain.autotroph.controller

import com.foodchain.autotroph.model.*
import com.foodchain.autotroph.service.FoodChainService
import com.foodchain.autotroph.service.SchemaService
import com.foodchain.autotroph.service.RecipeExtractionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

private val logger = KotlinLogging.logger {}

fun Route.foodChainRoutes() {
    val foodChainService by closestDI().instance<FoodChainService>()
    val schemaService by closestDI().instance<SchemaService>()
    val recipeExtractionService by closestDI().instance<RecipeExtractionService>()

    route("/api/v1/food-chain") {

        // ========== Schema Route ==========
        // GET /api/v1/food-chain/schema
        get("/schema") {
            logger.info { "GET /api/v1/food-chain/schema - Fetching schema information" }
            try {
                val schema = schemaService.getSchema()
                call.respond(HttpStatusCode.OK, schema)
            } catch (e: Exception) {
                logger.error(e) { "Error fetching schema" }
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error"))
            }
        }

        // ========== Ingredient Routes ==========
        route("/ingredients") {
            
            // GET /api/v1/food-chain/ingredients/{id}
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid ingredient ID"))
                    return@get
                }
                
                val includeStoreLocations = call.request.queryParameters["includeStoreLocations"]?.toBoolean() ?: false
                val ingredient = foodChainService.getIngredient(id, includeStoreLocations)
                
                if (ingredient != null) {
                    call.respond(HttpStatusCode.OK, ingredient)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Ingredient not found"))
                }
            }
            
            // POST /api/v1/food-chain/ingredients
            post {
                try {
                    val request = call.receive<CreateIngredientRequest>()
                    val ingredient = foodChainService.createIngredient(request)
                    call.respond(HttpStatusCode.Created, ingredient)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating ingredient" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create ingredient"))
                }
            }
            
            // PUT /api/v1/food-chain/ingredients/{id}
            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid ingredient ID"))
                    return@put
                }
                
                try {
                    val request = call.receive<UpdateIngredientRequest>()
                    val ingredient = foodChainService.updateIngredient(id, request)
                    
                    if (ingredient != null) {
                        call.respond(HttpStatusCode.OK, ingredient)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Ingredient not found"))
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Error updating ingredient" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("UPDATE_FAILED", e.message ?: "Failed to update ingredient"))
                }
            }
            
            // DELETE /api/v1/food-chain/ingredients/{id}
            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid ingredient ID"))
                    return@delete
                }
                
                val deleted = foodChainService.deleteIngredient(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Ingredient not found"))
                }
            }
            
            // POST /api/v1/food-chain/ingredients/{id}/store-locations/{storeLocationId}
            post("/{id}/store-locations/{storeLocationId}") {
                val ingredientId = call.parameters["id"]?.toLongOrNull()
                val storeLocationId = call.parameters["storeLocationId"]?.toLongOrNull()

                if (ingredientId == null || storeLocationId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid ingredient or store location ID"))
                    return@post
                }

                val success = foodChainService.addStoreLocationToIngredient(ingredientId, storeLocationId)
                if (success) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Store location added to ingredient"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_FAILED", "Failed to add store location to ingredient"))
                }
            }

            // POST /api/v1/food-chain/ingredients/similar
            post("/similar") {
                logger.info { "POST /api/v1/food-chain/ingredients/similar - Finding similar ingredients" }
                try {
                    val request = call.receive<FindSimilarIngredientsRequest>()

                    if (request.ingredientName.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_INPUT", "Ingredient name cannot be blank"))
                        return@post
                    }

                    if (request.topK <= 0 || request.topK > 50) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_INPUT", "topK must be between 1 and 50"))
                        return@post
                    }

                    val similarIngredients = foodChainService.findSimilarIngredients(request.ingredientName, request.topK)
                    call.respond(HttpStatusCode.OK, similarIngredients)
                } catch (e: Exception) {
                    logger.error(e) { "Error finding similar ingredients" }
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("SEARCH_FAILED", e.message ?: "Failed to find similar ingredients"))
                }
            }
        }
        
        // ========== Recipe Routes ==========
        route("/recipes") {
            
            // GET /api/v1/food-chain/recipes/{id}
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid recipe ID"))
                    return@get
                }
                
                val includeRelated = call.request.queryParameters["includeRelated"]?.toBoolean() ?: false
                val recipe = foodChainService.getRecipe(id, includeRelated)
                
                if (recipe != null) {
                    call.respond(HttpStatusCode.OK, recipe)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Recipe not found"))
                }
            }
            
            // POST /api/v1/food-chain/recipes
            post {
                try {
                    val request = call.receive<CreateRecipeRequest>()
                    val recipe = foodChainService.createRecipe(request)
                    call.respond(HttpStatusCode.Created, recipe)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating recipe" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create recipe"))
                }
            }
            
            // POST /api/v1/food-chain/recipes/{id}/ingredients/{ingredientId}
            post("/{id}/ingredients/{ingredientId}") {
                val recipeId = call.parameters["id"]?.toLongOrNull()
                val ingredientId = call.parameters["ingredientId"]?.toLongOrNull()
                
                if (recipeId == null || ingredientId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid recipe or ingredient ID"))
                    return@post
                }
                
                val success = foodChainService.addIngredientToRecipe(recipeId, ingredientId)
                if (success) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Ingredient added to recipe"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_FAILED", "Failed to add ingredient to recipe"))
                }
            }
            
            // POST /api/v1/food-chain/recipes/{id}/sub-recipes/{subRecipeId}
            post("/{id}/sub-recipes/{subRecipeId}") {
                val parentRecipeId = call.parameters["id"]?.toLongOrNull()
                val subRecipeId = call.parameters["subRecipeId"]?.toLongOrNull()
                
                if (parentRecipeId == null || subRecipeId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid recipe IDs"))
                    return@post
                }
                
                val success = foodChainService.addSubRecipeToRecipe(parentRecipeId, subRecipeId)
                if (success) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Sub-recipe added to recipe"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_FAILED", "Failed to add sub-recipe to recipe"))
                }
            }

            // POST /api/v1/food-chain/recipes/extract-from-url
            post("/extract-from-url") {
                logger.info { "POST /api/v1/food-chain/recipes/extract-from-url - Extracting recipe from URL" }
                try {
                    val request = call.receive<ExtractRecipeFromUrlRequest>()
                    logger.info { "Extracting recipe from URL: ${request.url}" }

                    val extractedRecipe = recipeExtractionService.extractRecipeFromUrl(request.url)
                    call.respond(HttpStatusCode.OK, extractedRecipe)
                } catch (e: Exception) {
                    logger.error(e) { "Error extracting recipe from URL" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("EXTRACTION_FAILED", e.message ?: "Failed to extract recipe from URL"))
                }
            }

            // POST /api/v1/food-chain/recipes/extract-from-text
            post("/extract-from-text") {
                logger.info { "POST /api/v1/food-chain/recipes/extract-from-text - Extracting recipe from text" }
                try {
                    val request = call.receive<ExtractRecipeFromTextRequest>()
                    logger.info { "Extracting recipe from text (${request.text.length} characters)" }

                    val extractedRecipe = recipeExtractionService.extractRecipeFromText(request.text)
                    call.respond(HttpStatusCode.OK, extractedRecipe)
                } catch (e: Exception) {
                    logger.error(e) { "Error extracting recipe from text" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("EXTRACTION_FAILED", e.message ?: "Failed to extract recipe from text"))
                }
            }
        }

        // ========== Meal Routes ==========
        route("/meals") {
            
            // POST /api/v1/food-chain/meals
            post {
                try {
                    val request = call.receive<CreateMealRequest>()
                    val meal = foodChainService.createMeal(request)
                    call.respond(HttpStatusCode.Created, meal)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating meal" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create meal"))
                }
            }
            
            // POST /api/v1/food-chain/meals/{id}/recipes/{recipeId}
            post("/{id}/recipes/{recipeId}") {
                val mealId = call.parameters["id"]?.toLongOrNull()
                val recipeId = call.parameters["recipeId"]?.toLongOrNull()
                
                if (mealId == null || recipeId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid meal or recipe ID"))
                    return@post
                }
                
                val success = foodChainService.addRecipeToMeal(mealId, recipeId)
                if (success) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Recipe added to meal"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_FAILED", "Failed to add recipe to meal"))
                }
            }
            
            // POST /api/v1/food-chain/meals/{id}/ingredients/{ingredientId}
            post("/{id}/ingredients/{ingredientId}") {
                val mealId = call.parameters["id"]?.toLongOrNull()
                val ingredientId = call.parameters["ingredientId"]?.toLongOrNull()
                
                if (mealId == null || ingredientId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid meal or ingredient ID"))
                    return@post
                }
                
                val success = foodChainService.addIngredientToMeal(mealId, ingredientId)
                if (success) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Ingredient added to meal"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_FAILED", "Failed to add ingredient to meal"))
                }
            }
        }
        
        // ========== Shopping List Routes ==========
        route("/shopping-lists") {
            
            // POST /api/v1/food-chain/shopping-lists
            post {
                try {
                    val request = call.receive<CreateShoppingListRequest>()
                    val shoppingList = foodChainService.createShoppingList(request)
                    call.respond(HttpStatusCode.Created, shoppingList)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating shopping list" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create shopping list"))
                }
            }
            
            // POST /api/v1/food-chain/shopping-lists/{id}/items/{itemId}
            post("/{id}/items/{itemId}") {
                val shoppingListId = call.parameters["id"]?.toLongOrNull()
                val itemId = call.parameters["itemId"]?.toLongOrNull()
                
                if (shoppingListId == null || itemId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Invalid shopping list or item ID"))
                    return@post
                }
                
                val success = foodChainService.addItemToShoppingList(shoppingListId, itemId)
                if (success) {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Item added to shopping list"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("RELATIONSHIP_FAILED", "Failed to add item to shopping list"))
                }
            }
        }
        
        // ========== Meal Plan Routes ==========
        route("/meal-plans") {
            
            // POST /api/v1/food-chain/meal-plans
            post {
                try {
                    val request = call.receive<CreateMealPlanRequest>()
                    val mealPlan = foodChainService.createMealPlan(request)
                    call.respond(HttpStatusCode.Created, mealPlan)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating meal plan" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create meal plan"))
                }
            }
        }
        
        // ========== Cuisine Routes ==========
        route("/cuisines") {
            
            // POST /api/v1/food-chain/cuisines
            post {
                try {
                    val request = call.receive<CreateCuisineRequest>()
                    val cuisine = foodChainService.createCuisine(request)
                    call.respond(HttpStatusCode.Created, cuisine)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating cuisine" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create cuisine"))
                }
            }
        }
        
        // ========== Store Location Routes ==========
        route("/store-locations") {
            
            // POST /api/v1/food-chain/store-locations
            post {
                try {
                    val request = call.receive<CreateStoreLocationRequest>()
                    val storeLocation = foodChainService.createStoreLocation(request)
                    call.respond(HttpStatusCode.Created, storeLocation)
                } catch (e: Exception) {
                    logger.error(e) { "Error creating store location" }
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("CREATION_FAILED", e.message ?: "Failed to create store location"))
                }
            }
        }
    }
}
