package com.foodchain.autotroph.controller

import com.foodchain.autotroph.config.configureDI
import com.foodchain.autotroph.config.configureRouting
import com.foodchain.autotroph.config.configureSerialization
import com.foodchain.autotroph.model.*
import com.foodchain.autotroph.repository.EntityRepository
import com.foodchain.autotroph.service.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeExtractionEndpointTest {

    @Test
    fun `test extract recipe from URL endpoint with mocked service`() = testApplication {
        // Mock dependencies
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()
        val mockOpenAIService = mockk<OpenAIService>()
        val mockHtmlParserService = mockk<HtmlParserService>()
        val mockRecipeExtractionService = mockk<RecipeExtractionService>()

        // Configure the test application with mocked dependencies
        application {
            configureSerialization()
            
            // Override DI with mocks
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
                bind<OpenAIService>() with singleton { mockOpenAIService }
                bind<HtmlParserService>() with singleton { mockHtmlParserService }
                bind<RecipeExtractionService>() with singleton { mockRecipeExtractionService }
            }
            
            configureRouting()
        }

        // Mock the recipe extraction service response
        val mockResponse = ExtractedRecipeResponse(
            url = "https://example.com/recipe",
            simplifiedName = "Test Recipe",
            ingredients = listOf(
                ExtractedIngredientWithMatches(
                    name = "flour",
                    similarIngredients = emptyList()
                ),
                ExtractedIngredientWithMatches(
                    name = "eggs",
                    similarIngredients = emptyList()
                ),
                ExtractedIngredientWithMatches(
                    name = "milk",
                    similarIngredients = emptyList()
                )
            ),
            success = true,
            errorMessage = null
        )

        coEvery { mockRecipeExtractionService.extractRecipeFromUrl("https://example.com/recipe") } returns mockResponse

        // Test the endpoint
        val response = client.post("/api/v1/food-chain/recipes/extract-from-url") {
            contentType(ContentType.Application.Json)
            setBody("""{"url": "https://example.com/recipe"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        val extractedRecipe = Json.decodeFromString<ExtractedRecipeResponse>(responseBody)
        
        assertEquals("https://example.com/recipe", extractedRecipe.url)
        assertEquals("Test Recipe", extractedRecipe.simplifiedName)
        assertEquals(3, extractedRecipe.ingredients.size)
        assertTrue(extractedRecipe.success)
    }

    @Test
    fun `test extract recipe from URL endpoint with invalid request`() = testApplication {
        // Mock dependencies
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()
        val mockOpenAIService = mockk<OpenAIService>()
        val mockHtmlParserService = mockk<HtmlParserService>()
        val mockRecipeExtractionService = mockk<RecipeExtractionService>()

        // Configure the test application with mocked dependencies
        application {
            configureSerialization()

            // Override DI with mocks
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
                bind<OpenAIService>() with singleton { mockOpenAIService }
                bind<HtmlParserService>() with singleton { mockHtmlParserService }
                bind<RecipeExtractionService>() with singleton { mockRecipeExtractionService }
            }
            
            configureRouting()
        }

        // Test with invalid JSON
        val response = client.post("/api/v1/food-chain/recipes/extract-from-url") {
            contentType(ContentType.Application.Json)
            setBody("""{"invalid": "request"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `test extract recipe from URL endpoint with service error`() = testApplication {
        // Mock dependencies
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()
        val mockRecipeExtractionService = mockk<RecipeExtractionService>()

        // Configure the test application with mocked dependencies
        application {
            configureSerialization()
            
            // Override DI with mocks
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
                bind<RecipeExtractionService>() with singleton { mockRecipeExtractionService }
            }
            
            configureRouting()
        }

        // Mock the service to throw an exception
        coEvery { mockRecipeExtractionService.extractRecipeFromUrl(any()) } throws RuntimeException("Network error")

        // Test the endpoint
        val response = client.post("/api/v1/food-chain/recipes/extract-from-url") {
            contentType(ContentType.Application.Json)
            setBody("""{"url": "https://invalid-url.com"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("EXTRACTION_FAILED"))
    }

    @Test
    fun `test extract recipe from text endpoint success`() = testApplication {
        // Mock dependencies
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()
        val mockOpenAIService = mockk<OpenAIService>()
        val mockHtmlParserService = mockk<HtmlParserService>()
        val mockRecipeExtractionService = mockk<RecipeExtractionService>()

        // Configure the test application with mocked dependencies
        application {
            configureSerialization()

            // Override DI with mocks
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
                bind<OpenAIService>() with singleton { mockOpenAIService }
                bind<HtmlParserService>() with singleton { mockHtmlParserService }
                bind<RecipeExtractionService>() with singleton { mockRecipeExtractionService }
            }

            configureRouting()
        }

        // Mock the recipe extraction service response
        val mockResponse = ExtractedRecipeResponse(
            url = "", // Empty URL for text-based extraction
            simplifiedName = "Test Recipe from Text",
            ingredients = listOf(
                ExtractedIngredientWithMatches(
                    name = "flour",
                    similarIngredients = emptyList()
                ),
                ExtractedIngredientWithMatches(
                    name = "eggs",
                    similarIngredients = emptyList()
                ),
                ExtractedIngredientWithMatches(
                    name = "milk",
                    similarIngredients = emptyList()
                )
            ),
            success = true,
            errorMessage = null
        )

        val testText = "Recipe: Pancakes\nIngredients: 2 cups flour, 2 eggs, 1 cup milk\nInstructions: Mix ingredients and cook on griddle."

        coEvery { mockRecipeExtractionService.extractRecipeFromText(testText) } returns mockResponse

        // Test the endpoint
        val response = client.post("/api/v1/food-chain/recipes/extract-from-text") {
            contentType(ContentType.Application.Json)
            setBody("""{"text": "$testText"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val extractedRecipe = Json.decodeFromString<ExtractedRecipeResponse>(responseBody)

        assertEquals("", extractedRecipe.url) // Should be empty for text extraction
        assertEquals("Test Recipe from Text", extractedRecipe.simplifiedName)
        assertEquals(3, extractedRecipe.ingredients.size)
        assertTrue(extractedRecipe.success)
    }

    @Test
    fun `test extract recipe from text endpoint with invalid request`() = testApplication {
        // Mock dependencies
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()
        val mockOpenAIService = mockk<OpenAIService>()
        val mockHtmlParserService = mockk<HtmlParserService>()
        val mockRecipeExtractionService = mockk<RecipeExtractionService>()

        // Configure the test application with mocked dependencies
        application {
            configureSerialization()

            // Override DI with mocks
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
                bind<OpenAIService>() with singleton { mockOpenAIService }
                bind<HtmlParserService>() with singleton { mockHtmlParserService }
                bind<RecipeExtractionService>() with singleton { mockRecipeExtractionService }
            }

            configureRouting()
        }

        // Test with invalid JSON
        val response = client.post("/api/v1/food-chain/recipes/extract-from-text") {
            contentType(ContentType.Application.Json)
            setBody("""{"invalid": "request"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
