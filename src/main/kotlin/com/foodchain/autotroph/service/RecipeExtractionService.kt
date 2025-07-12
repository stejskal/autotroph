package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.ExtractedRecipeResponse
import com.foodchain.autotroph.model.ExtractedIngredientWithMatches
import com.foodchain.autotroph.model.SimilarIngredientResponse
import com.foodchain.autotroph.model.IngredientResponse
import com.foodchain.autotroph.model.Ingredient
import com.foodchain.autotroph.model.PurchaseFrequency
import com.foodchain.autotroph.model.EntityResponse
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Service for extracting recipe content from web pages.
 * Coordinates between HtmlParserService for content extraction, OpenAIService for recipe analysis,
 * and EmbeddingService for ingredient similarity matching.
 */
class RecipeExtractionService(
    private val htmlParserService: HtmlParserService,
    private val openAIService: OpenAIService,
    private val embeddingService: EmbeddingService,
    private val entityService: EntityService
) {

    /**
     * Extracts recipe content from raw text.
     *
     * @param text The raw text content to extract recipe from
     * @return ExtractedRecipeResponse containing the extracted recipe data with ingredient similarity matches
     */
    suspend fun extractRecipeFromText(text: String): ExtractedRecipeResponse {
        logger.info { "Extracting recipe from text (${text.length} characters)" }

        try {
            // Step 1: Send text directly to OpenAI for recipe analysis (skip HTML parsing)
            val recipeResult = openAIService.extractRecipeFromText(text)

            logger.info { "Successfully extracted recipe from text: '${recipeResult.simplifiedName}' with ${recipeResult.ingredients.size} ingredients" }

            // Step 2: For each ingredient, find similar ingredients using embeddings
            val ingredientsWithMatches = findSimilarIngredientsForList(recipeResult.ingredients)

            return ExtractedRecipeResponse(
                url = "", // No URL for text-based extraction
                simplifiedName = recipeResult.simplifiedName,
                ingredients = ingredientsWithMatches,
                success = true,
                errorMessage = null
            )

        } catch (e: Exception) {
            logger.error(e) { "Failed to extract recipe from text" }
            return ExtractedRecipeResponse(
                url = "",
                simplifiedName = null,
                ingredients = emptyList(),
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Extracts recipe content from a given URL.
     *
     * @param url The URL to extract recipe content from
     * @return ExtractedRecipeResponse containing the extracted recipe data with ingredient similarity matches
     */
    suspend fun extractRecipeFromUrl(url: String): ExtractedRecipeResponse {
        logger.info { "Extracting recipe from URL: $url" }

        try {
            // Step 1: Parse HTML content from the URL
            val htmlResult = htmlParserService.parseHtmlFromUrl(url)

            if (!htmlResult.success) {
                return ExtractedRecipeResponse(
                    url = url,
                    simplifiedName = null,
                    ingredients = emptyList(),
                    success = false,
                    errorMessage = htmlResult.errorMessage ?: "Failed to parse HTML content"
                )
            }

            // Step 2: Send extracted text to OpenAI for recipe analysis
            val recipeResult = openAIService.extractRecipeFromText(htmlResult.extractedContent)

            logger.info { "Successfully extracted recipe from $url: '${recipeResult.simplifiedName}' with ${recipeResult.ingredients.size} ingredients" }

            // Step 3: For each ingredient, find similar ingredients using embeddings
            val ingredientsWithMatches = findSimilarIngredientsForList(recipeResult.ingredients)

            return ExtractedRecipeResponse(
                url = url,
                simplifiedName = recipeResult.simplifiedName,
                ingredients = ingredientsWithMatches,
                success = true,
                errorMessage = null
            )

        } catch (e: Exception) {
            logger.error(e) { "Failed to extract recipe from URL: $url" }
            return ExtractedRecipeResponse(
                url = url,
                simplifiedName = null,
                ingredients = emptyList(),
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Finds similar ingredients for a list of ingredient names using embeddings.
     *
     * @param ingredientNames List of ingredient names from OpenAI extraction
     * @return List of ExtractedIngredientWithMatches containing similarity matches
     */
    private suspend fun findSimilarIngredientsForList(ingredientNames: List<String>): List<ExtractedIngredientWithMatches> {
        logger.info { "Finding similar ingredients for ${ingredientNames.size} extracted ingredients" }

        // Get all ingredients from the database once to avoid repeated queries
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
            logger.warn { "No ingredients with embeddings found in database" }
            return ingredientNames.map { name ->
                ExtractedIngredientWithMatches(
                    name = name,
                    similarIngredients = emptyList()
                )
            }
        }

        // Process each ingredient name
        return ingredientNames.map { ingredientName ->
            logger.debug { "Finding similar ingredients for: '$ingredientName'" }

            try {
                // Generate embedding for the ingredient name
                val targetEmbedding = embeddingService.generateEmbedding(ingredientName)
                logger.debug { "Generated target embedding with ${targetEmbedding.size} dimensions for '$ingredientName'" }

                // Find most similar ingredients using embedding service (top 5)
                val similarityResults = embeddingService.findMostSimilar(
                    targetEmbedding = targetEmbedding,
                    candidateEmbeddings = ingredientsWithEmbeddings,
                    topK = 5
                )

                // Convert to response DTOs
                val similarIngredients = similarityResults.map { result ->
                    SimilarIngredientResponse(
                        id = result.data.id!!,
                        name = result.data.name,
                        similarity = result.similarity
                    )
                }

                logger.debug { "Found ${similarIngredients.size} similar ingredients for '$ingredientName'" }

                ExtractedIngredientWithMatches(
                    name = ingredientName,
                    similarIngredients = similarIngredients
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to find similar ingredients for '$ingredientName'" }
                ExtractedIngredientWithMatches(
                    name = ingredientName,
                    similarIngredients = emptyList()
                )
            }
        }
    }

    /**
     * Converts an EntityResponse to an Ingredient object.
     * This is a copy of the method from FoodChainService to avoid circular dependencies.
     */
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

}
