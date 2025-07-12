package com.foodchain.autotroph.controller

import com.foodchain.autotroph.config.configureDI
import com.foodchain.autotroph.config.configureRouting
import com.foodchain.autotroph.config.configureSerialization
import com.foodchain.autotroph.model.CreateEntityRequest
import com.foodchain.autotroph.model.EntityResponse
import com.foodchain.autotroph.repository.EntityRepository
import com.foodchain.autotroph.service.EntityService
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

class EntityControllerTest {

    @Test
    fun `should get all entities`() = testApplication {
        // Given
        val mockEntityService = mockk<EntityService>()
        val entities = listOf(
            EntityResponse(
                id = 1L,
                name = "Entity 1",
                type = "Type1",
                description = "Description 1",
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                properties = emptyMap()
            )
        )

        coEvery { mockEntityService.getAllEntities() } returns entities

        application {
            di {
                bind<EntityRepository>() with singleton { mockk<EntityRepository>() }
                bind<EntityService>() with singleton { mockEntityService }
            }
            configureSerialization()
            configureRouting()
        }

        // When & Then
        val response = client.get("/api/v1/entities")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        val responseEntities = json.decodeFromString<List<EntityResponse>>(responseBody)

        assertEquals(1, responseEntities.size)
        assertEquals("Entity 1", responseEntities[0].name)
        assertEquals("Type1", responseEntities[0].type)
    }

    @Test
    fun `should get entity by id`() = testApplication {
        // Given
        val mockEntityService = mockk<EntityService>()
        val entityId = 1L
        val entity = EntityResponse(
            id = entityId,
            name = "Test Entity",
            type = "TestType",
            description = "Test Description",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = emptyMap()
        )

        coEvery { mockEntityService.getEntityById(entityId) } returns entity

        application {
            di {
                bind<EntityRepository>() with singleton { mockk<EntityRepository>() }
                bind<EntityService>() with singleton { mockEntityService }
            }
            configureSerialization()
            configureRouting()
        }

        // When & Then
        val response = client.get("/api/v1/entities/$entityId")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        val responseEntity = json.decodeFromString<EntityResponse>(responseBody)

        assertEquals(entityId, responseEntity.id)
        assertEquals("Test Entity", responseEntity.name)
        assertEquals("TestType", responseEntity.type)
    }

    @Test
    fun `should return 404 when entity not found`() = testApplication {
        // Given
        val mockEntityService = mockk<EntityService>()
        val entityId = 999L
        coEvery { mockEntityService.getEntityById(entityId) } returns null

        application {
            di {
                bind<EntityRepository>() with singleton { mockk<EntityRepository>() }
                bind<EntityService>() with singleton { mockEntityService }
            }
            configureSerialization()
            configureRouting()
        }

        // When & Then
        val response = client.get("/api/v1/entities/$entityId")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should include relatedEntitiesCount in entity response`() = testApplication {
        // Given
        val mockEntityService = mockk<EntityService>()
        val entityId = 1L
        val entity = EntityResponse(
            id = entityId,
            name = "Test Entity",
            type = "TestType",
            description = "Test Description",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            properties = emptyMap(),
            relatedEntitiesCount = 5
        )

        coEvery { mockEntityService.getEntityById(entityId) } returns entity

        application {
            di {
                bind<EntityRepository>() with singleton { mockk<EntityRepository>() }
                bind<EntityService>() with singleton { mockEntityService }
            }
            configureSerialization()
            configureRouting()
        }

        // When & Then
        val response = client.get("/api/v1/entities/$entityId")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        val responseEntity = json.decodeFromString<EntityResponse>(responseBody)

        assertEquals(entityId, responseEntity.id)
        assertEquals("Test Entity", responseEntity.name)
        assertEquals("TestType", responseEntity.type)
        assertEquals(5, responseEntity.relatedEntitiesCount)
    }

    @Test
    fun `should include relatedEntitiesCount in entities list response`() = testApplication {
        // Given
        val mockEntityService = mockk<EntityService>()
        val entities = listOf(
            EntityResponse(
                id = 1L,
                name = "Entity 1",
                type = "Type1",
                description = "Description 1",
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                properties = emptyMap(),
                relatedEntitiesCount = 3
            ),
            EntityResponse(
                id = 2L,
                name = "Entity 2",
                type = "Type2",
                description = "Description 2",
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                properties = emptyMap(),
                relatedEntitiesCount = 0
            )
        )

        coEvery { mockEntityService.getAllEntities() } returns entities

        application {
            di {
                bind<EntityRepository>() with singleton { mockk<EntityRepository>() }
                bind<EntityService>() with singleton { mockEntityService }
            }
            configureSerialization()
            configureRouting()
        }

        // When & Then
        val response = client.get("/api/v1/entities")
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        val responseEntities = json.decodeFromString<List<EntityResponse>>(responseBody)

        assertEquals(2, responseEntities.size)
        assertEquals("Entity 1", responseEntities[0].name)
        assertEquals(3, responseEntities[0].relatedEntitiesCount)
        assertEquals("Entity 2", responseEntities[1].name)
        assertEquals(0, responseEntities[1].relatedEntitiesCount)
    }
}
