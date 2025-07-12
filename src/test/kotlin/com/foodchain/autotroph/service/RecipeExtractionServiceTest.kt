package com.foodchain.autotroph.service

import com.foodchain.autotroph.model.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeExtractionServiceTest {

    private val mockHtmlParserService = mockk<HtmlParserService>()
    private val mockOpenAIService = mockk<OpenAIService>()
    private val mockEmbeddingService = mockk<EmbeddingService>()
    private val mockEntityService = mockk<EntityService>()

    private val recipeExtractionService = RecipeExtractionService(
        htmlParserService = mockHtmlParserService,
        openAIService = mockOpenAIService,
        embeddingService = mockEmbeddingService,
        entityService = mockEntityService
    )

    @Test
    fun `extractRecipeFromUrl should return ingredients with similarity matches`() = runBlocking {
        // Given
        val url = "https://example.com/recipe"
        val htmlContent = "Recipe content with ingredients"
        
        // Mock HTML parsing
        val htmlResult = HtmlParsingResult(
            title = "Test Recipe Page",
            description = "A test recipe page",
            extractedContent = htmlContent,
            success = true,
            errorMessage = null
        )
        coEvery { mockHtmlParserService.parseHtmlFromUrl(url) } returns htmlResult

        // Mock OpenAI extraction
        val openAIResult = RecipeExtractionResult(
            simplifiedName = "Chocolate Chip Cookies",
            ingredients = listOf("flour", "sugar", "eggs")
        )
        coEvery { mockOpenAIService.extractRecipeFromText(htmlContent) } returns openAIResult

        // Mock existing ingredients in database
        val existingIngredients = listOf(
            createMockEntityResponse(1L, "all-purpose flour", "Ingredient"),
            createMockEntityResponse(2L, "white sugar", "Ingredient"),
            createMockEntityResponse(3L, "brown sugar", "Ingredient")
        )
        coEvery { mockEntityService.getEntitiesByType("Ingredient") } returns existingIngredients

        // Mock embeddings
        val flourEmbedding = listOf(0.1, 0.2, 0.3)
        val sugarEmbedding = listOf(0.4, 0.5, 0.6)
        val eggsEmbedding = listOf(0.7, 0.8, 0.9)
        
        coEvery { mockEmbeddingService.generateEmbedding("flour") } returns flourEmbedding
        coEvery { mockEmbeddingService.generateEmbedding("sugar") } returns sugarEmbedding
        coEvery { mockEmbeddingService.generateEmbedding("eggs") } returns eggsEmbedding

        // Mock similarity results
        val flourSimilarityResults = listOf(
            SimilarityResult(createMockIngredient(1L, "all-purpose flour"), 0.95),
            SimilarityResult(createMockIngredient(2L, "white sugar"), 0.3)
        )
        val sugarSimilarityResults = listOf(
            SimilarityResult(createMockIngredient(2L, "white sugar"), 0.9),
            SimilarityResult(createMockIngredient(3L, "brown sugar"), 0.85)
        )
        val eggsSimilarityResults = listOf(
            SimilarityResult(createMockIngredient(1L, "all-purpose flour"), 0.2)
        )

        coEvery { 
            mockEmbeddingService.findMostSimilar(
                targetEmbedding = flourEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = 5
            ) 
        } returns flourSimilarityResults

        coEvery { 
            mockEmbeddingService.findMostSimilar(
                targetEmbedding = sugarEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = 5
            ) 
        } returns sugarSimilarityResults

        coEvery { 
            mockEmbeddingService.findMostSimilar(
                targetEmbedding = eggsEmbedding,
                candidateEmbeddings = any<List<Pair<Ingredient, List<Double>>>>(),
                topK = 5
            ) 
        } returns eggsSimilarityResults

        // When
        val result = recipeExtractionService.extractRecipeFromUrl(url)

        // Then
        assertTrue(result.success)
        assertEquals(url, result.url)
        assertEquals("Chocolate Chip Cookies", result.simplifiedName)
        assertEquals(3, result.ingredients.size)

        // Check first ingredient (flour)
        val flourIngredient = result.ingredients[0]
        assertEquals("flour", flourIngredient.name)
        assertEquals(2, flourIngredient.similarIngredients.size)
        assertEquals(1L, flourIngredient.similarIngredients[0].id)
        assertEquals("all-purpose flour", flourIngredient.similarIngredients[0].name)
        assertEquals(0.95, flourIngredient.similarIngredients[0].similarity)

        // Check second ingredient (sugar)
        val sugarIngredient = result.ingredients[1]
        assertEquals("sugar", sugarIngredient.name)
        assertEquals(2, sugarIngredient.similarIngredients.size)
        assertEquals(2L, sugarIngredient.similarIngredients[0].id)
        assertEquals("white sugar", sugarIngredient.similarIngredients[0].name)
        assertEquals(0.9, sugarIngredient.similarIngredients[0].similarity)

        // Check third ingredient (eggs)
        val eggsIngredient = result.ingredients[2]
        assertEquals("eggs", eggsIngredient.name)
        assertEquals(1, eggsIngredient.similarIngredients.size)
    }

    private fun createMockEntityResponse(id: Long, name: String, type: String): EntityResponse {
        return EntityResponse(
            id = id,
            name = name,
            type = type,
            description = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = mapOf(
                "embedding" to kotlinx.serialization.json.JsonArray(
                    listOf(0.1, 0.2, 0.3).map { kotlinx.serialization.json.JsonPrimitive(it) }
                )
            )
        )
    }

    private fun createMockIngredient(id: Long, name: String): Ingredient {
        return Ingredient(
            id = id,
            name = name,
            description = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            purchaseFrequency = null,
            embedding = listOf(0.1, 0.2, 0.3)
        )
    }
}
