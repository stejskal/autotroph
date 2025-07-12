package com.foodchain.autotroph.config

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import org.kodein.di.instance
import org.kodein.di.ktor.di
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class Neo4jConfigTest {

    @Test
    fun `should create Neo4j configuration with default values`() {
        // Given
        val mockEnvironment = mockk<ApplicationEnvironment>()
        val mockConfig = mockk<ApplicationConfig>()
        
        every { mockEnvironment.config } returns mockConfig
        every { mockConfig.propertyOrNull("neo4j.uri") } returns null
        every { mockConfig.propertyOrNull("neo4j.username") } returns null
        every { mockConfig.propertyOrNull("neo4j.password") } returns null
        every { mockConfig.propertyOrNull("neo4j.database") } returns null
        
        // When
        val config = createNeo4jConfiguration(mockEnvironment)
        
        // Then
        assertEquals("bolt://localhost:7687", config.uri)
        assertEquals("neo4j", config.username)
        assertEquals("password", config.password)
        assertEquals("neo4j", config.database)
    }

    @Test
    fun `should create Neo4j configuration with custom values`() {
        // Given
        val mockEnvironment = mockk<ApplicationEnvironment>()
        val mockConfig = mockk<ApplicationConfig>()
        val mockUriProperty = mockk<ApplicationConfigValue>()
        val mockUsernameProperty = mockk<ApplicationConfigValue>()
        val mockPasswordProperty = mockk<ApplicationConfigValue>()
        val mockDatabaseProperty = mockk<ApplicationConfigValue>()
        
        every { mockEnvironment.config } returns mockConfig
        every { mockConfig.propertyOrNull("neo4j.uri") } returns mockUriProperty
        every { mockConfig.propertyOrNull("neo4j.username") } returns mockUsernameProperty
        every { mockConfig.propertyOrNull("neo4j.password") } returns mockPasswordProperty
        every { mockConfig.propertyOrNull("neo4j.database") } returns mockDatabaseProperty
        
        every { mockUriProperty.getString() } returns "neo4j+s://test.databases.neo4j.io"
        every { mockUsernameProperty.getString() } returns "testuser"
        every { mockPasswordProperty.getString() } returns "testpassword"
        every { mockDatabaseProperty.getString() } returns "testdb"
        
        // When
        val config = createNeo4jConfiguration(mockEnvironment)
        
        // Then
        assertEquals("neo4j+s://test.databases.neo4j.io", config.uri)
        assertEquals("testuser", config.username)
        assertEquals("testpassword", config.password)
        assertEquals("testdb", config.database)
    }

    @Test
    fun `should create Neo4j configuration with Aura values`() {
        // Given
        val mockEnvironment = mockk<ApplicationEnvironment>()
        val mockConfig = mockk<ApplicationConfig>()
        val mockUriProperty = mockk<ApplicationConfigValue>()
        val mockUsernameProperty = mockk<ApplicationConfigValue>()
        val mockPasswordProperty = mockk<ApplicationConfigValue>()
        val mockDatabaseProperty = mockk<ApplicationConfigValue>()
        
        every { mockEnvironment.config } returns mockConfig
        every { mockConfig.propertyOrNull("neo4j.uri") } returns mockUriProperty
        every { mockConfig.propertyOrNull("neo4j.username") } returns mockUsernameProperty
        every { mockConfig.propertyOrNull("neo4j.password") } returns mockPasswordProperty
        every { mockConfig.propertyOrNull("neo4j.database") } returns mockDatabaseProperty
        
        every { mockUriProperty.getString() } returns "neo4j+s://718597e1.databases.neo4j.io"
        every { mockUsernameProperty.getString() } returns "neo4j"
        every { mockPasswordProperty.getString() } returns "NUsvoQAU2Almeb7d2-T6_k3NeFyWAcXrHwhoAwjGuLM"
        every { mockDatabaseProperty.getString() } returns "neo4j"
        
        // When
        val config = createNeo4jConfiguration(mockEnvironment)
        
        // Then
        assertEquals("neo4j+s://718597e1.databases.neo4j.io", config.uri)
        assertEquals("neo4j", config.username)
        assertEquals("NUsvoQAU2Almeb7d2-T6_k3NeFyWAcXrHwhoAwjGuLM", config.password)
        assertEquals("neo4j", config.database)
    }
}

// Make the private function accessible for testing
private fun createNeo4jConfiguration(environment: ApplicationEnvironment): Neo4jConfiguration {
    val uri = environment.config.propertyOrNull("neo4j.uri")?.getString() ?: "bolt://localhost:7687"
    val username = environment.config.propertyOrNull("neo4j.username")?.getString() ?: "neo4j"
    val password = environment.config.propertyOrNull("neo4j.password")?.getString() ?: "password"
    val database = environment.config.propertyOrNull("neo4j.database")?.getString() ?: "neo4j"
    
    return Neo4jConfiguration(
        uri = uri,
        username = username,
        password = password,
        database = database
    )
}
