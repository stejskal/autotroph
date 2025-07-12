package com.foodchain.autotroph.controller

import com.foodchain.autotroph.config.configureDI
import com.foodchain.autotroph.config.configureRouting
import com.foodchain.autotroph.config.configureSerialization
import com.foodchain.autotroph.model.*
import com.foodchain.autotroph.repository.EntityRepository
import com.foodchain.autotroph.service.EmbeddingService
import com.foodchain.autotroph.service.EntityService
import com.foodchain.autotroph.service.FoodChainService
import com.foodchain.autotroph.service.SchemaService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientSimilarityEndpointTest {

    @Test
    fun `POST ingredients similar should return similar ingredients`() = testApplication {
        // Setup mocks
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()

        // Configure test application
        application {
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
            }
            configureSerialization()
            configureRouting()
        }

        // Mock the service response
        val mockSimilarIngredients = listOf(
            SimilarIngredientResponse(
                id = 1L,
                name = "cherry tomato",
                similarity = 0.95
            ),
            SimilarIngredientResponse(
                id = 2L,
                name = "tomato sauce",
                similarity = 0.87
            )
        )

        coEvery { mockFoodChainService.findSimilarIngredients("tomato", 10) } returns mockSimilarIngredients

        // Make the request
        val response = client.post("/api/v1/food-chain/ingredients/similar") {
            contentType(ContentType.Application.Json)
            setBody("""{"ingredientName": "tomato", "topK": 10}""")
        }

        // Verify response
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("cherry tomato"))
        assertTrue(responseBody.contains("tomato sauce"))
        assertTrue(responseBody.contains("0.95"))
        assertTrue(responseBody.contains("0.87"))
    }

    @Test
    fun `POST ingredients similar should return 400 for blank ingredient name`() = testApplication {
        // Setup mocks
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()

        application {
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
            }
            configureSerialization()
            configureRouting()
        }

        // Make the request with blank ingredient name
        val response = client.post("/api/v1/food-chain/ingredients/similar") {
            contentType(ContentType.Application.Json)
            setBody("""{"ingredientName": "", "topK": 10}""")
        }

        // Verify response
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("INVALID_INPUT"))
        assertTrue(responseBody.contains("Ingredient name cannot be blank"))
    }

    @Test
    fun `POST ingredients similar should return 400 for invalid topK`() = testApplication {
        // Setup mocks
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()

        application {
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
            }
            configureSerialization()
            configureRouting()
        }

        // Make the request with invalid topK
        val response = client.post("/api/v1/food-chain/ingredients/similar") {
            contentType(ContentType.Application.Json)
            setBody("""{"ingredientName": "tomato", "topK": 100}""")
        }

        // Verify response
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("INVALID_INPUT"))
        assertTrue(responseBody.contains("topK must be between 1 and 50"))
    }

    @Test
    fun `POST ingredients similar should return empty list when no similar ingredients found`() = testApplication {
        // Setup mocks
        val mockEntityRepository = mockk<EntityRepository>()
        val mockEntityService = mockk<EntityService>()
        val mockEmbeddingService = mockk<EmbeddingService>()
        val mockFoodChainService = mockk<FoodChainService>()
        val mockSchemaService = mockk<SchemaService>()

        application {
            di {
                bind<EntityRepository>() with singleton { mockEntityRepository }
                bind<EntityService>() with singleton { mockEntityService }
                bind<EmbeddingService>() with singleton { mockEmbeddingService }
                bind<FoodChainService>() with singleton { mockFoodChainService }
                bind<SchemaService>() with singleton { mockSchemaService }
            }
            configureSerialization()
            configureRouting()
        }

        // Mock empty response
        coEvery { mockFoodChainService.findSimilarIngredients("unknown", 10) } returns emptyList()

        // Make the request
        val response = client.post("/api/v1/food-chain/ingredients/similar") {
            contentType(ContentType.Application.Json)
            setBody("""{"ingredientName": "unknown", "topK": 10}""")
        }

        // Verify response
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        assertEquals("[]", responseBody)
    }
}
