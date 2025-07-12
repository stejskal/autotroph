package com.foodchain.autotroph.controller

import com.foodchain.autotroph.config.configureSerialization
import com.foodchain.autotroph.model.SchemaResponse
import com.foodchain.autotroph.service.SchemaService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SchemaEndpointTest {

    @Test
    fun `GET schema endpoint returns valid schema information`() = testApplication {
        // Configure DI for testing
        application {
            di {
                bind<SchemaService>() with singleton { SchemaService() }
            }
            configureSerialization()
            routing {
                foodChainRoutes()
            }
        }

        // Test the schema endpoint
        val response = client.get("/api/v1/food-chain/schema")
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())
        
        val schema = response.body<SchemaResponse>()
        
        // Verify entities are present
        assertTrue(schema.entities.isNotEmpty(), "Schema should contain entities")
        assertTrue(schema.entities.any { it.type == "Ingredient" }, "Schema should contain Ingredient entity")
        assertTrue(schema.entities.any { it.type == "Recipe" }, "Schema should contain Recipe entity")
        assertTrue(schema.entities.any { it.type == "Meal" }, "Schema should contain Meal entity")
        assertTrue(schema.entities.any { it.type == "ShoppingList" }, "Schema should contain ShoppingList entity")
        assertTrue(schema.entities.any { it.type == "MealPlan" }, "Schema should contain MealPlan entity")
        assertTrue(schema.entities.any { it.type == "Cuisine" }, "Schema should contain Cuisine entity")
        assertTrue(schema.entities.any { it.type == "StoreLocation" }, "Schema should contain StoreLocation entity")
        assertTrue(schema.entities.any { it.type == "Source" }, "Schema should contain Source entity")
        
        // Verify relationships are present
        assertTrue(schema.relationships.isNotEmpty(), "Schema should contain relationships")
        assertTrue(schema.relationships.any { it.name == "USES" }, "Schema should contain USES relationship")
        assertTrue(schema.relationships.any { it.name == "INCLUDES" }, "Schema should contain INCLUDES relationship")
        assertTrue(schema.relationships.any { it.name == "CONTAINS" }, "Schema should contain CONTAINS relationship")
        assertTrue(schema.relationships.any { it.name == "FROM" }, "Schema should contain FROM relationship")
        
        // Verify relationship matrix is present
        assertTrue(schema.relationshipMatrix.isNotEmpty(), "Schema should contain relationship matrix")
        assertTrue(
            schema.relationshipMatrix.any {
                it.fromEntityType == "Recipe" && it.toEntityType == "Ingredient" && it.relationshipName == "USES"
            },
            "Schema should contain Recipe -> Ingredient USES relationship"
        )
        assertTrue(
            schema.relationshipMatrix.any {
                it.fromEntityType == "Source" && it.toEntityType == "Source" && it.relationshipName == "FROM"
            },
            "Schema should contain Source -> Source FROM relationship"
        )
        assertTrue(
            schema.relationshipMatrix.any {
                it.fromEntityType == "Recipe" && it.toEntityType == "Source" && it.relationshipName == "FROM"
            },
            "Schema should contain Recipe -> Source FROM relationship"
        )
        
        // Verify enums are present
        assertTrue(schema.enums.isNotEmpty(), "Schema should contain enums")
        assertNotNull(schema.enums["PurchaseFrequency"], "Schema should contain PurchaseFrequency enum")
        assertTrue(
            schema.enums["PurchaseFrequency"]?.contains("Always") == true,
            "PurchaseFrequency enum should contain 'Always' value"
        )
        
        // Verify entity properties
        val ingredientEntity = schema.entities.find { it.type == "Ingredient" }
        assertNotNull(ingredientEntity, "Ingredient entity should be present")
        assertTrue(
            ingredientEntity.properties.any { it.name == "purchaseFrequency" && it.enumValues != null },
            "Ingredient should have purchaseFrequency property with enum values"
        )
    }
}
