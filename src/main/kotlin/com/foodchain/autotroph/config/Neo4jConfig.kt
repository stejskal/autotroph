package com.foodchain.autotroph.config

import com.foodchain.autotroph.controller.entityRoutes
import com.foodchain.autotroph.controller.foodChainRoutes
import com.foodchain.autotroph.repository.EntityRepository
import com.foodchain.autotroph.service.EntityService
import com.foodchain.autotroph.service.EmbeddingService
import com.foodchain.autotroph.service.FoodChainService
import com.foodchain.autotroph.service.SchemaService
import com.foodchain.autotroph.service.RecipeExtractionService
import com.foodchain.autotroph.service.OpenAIService
import com.foodchain.autotroph.service.HtmlParserService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.*
import org.kodein.di.ktor.di
import org.neo4j.driver.*

private val logger = KotlinLogging.logger {}

/**
 * Neo4j configuration data class to hold connection parameters
 */
data class Neo4jConfiguration(
    val uri: String,
    val username: String,
    val password: String,
    val database: String
)

fun Application.configureDI() {
    di {
        bind<Neo4jConfiguration>() with singleton {
            createNeo4jConfiguration(environment)
        }

        bind<Driver>() with singleton {
            val config = instance<Neo4jConfiguration>()
            createNeo4jDriver(config)
        }

        bind<EntityRepository>() with singleton { EntityRepository(instance()) }
        bind<EntityService>() with singleton { EntityService(instance()) }
        bind<EmbeddingService>() with singleton {
            val apiKey = environment.config.propertyOrNull("openai.api_key")?.getString()
                ?: throw IllegalStateException("OpenAI API key not configured. Set OPENAI_API_KEY environment variable.")
            EmbeddingService(apiKey)
        }
        bind<OpenAIService>() with singleton {
            val apiKey = environment.config.propertyOrNull("openai.api_key")?.getString()
                ?: throw IllegalStateException("OpenAI API key not configured. Set OPENAI_API_KEY environment variable.")
            OpenAIService(apiKey)
        }
        bind<HtmlParserService>() with singleton { HtmlParserService() }
        bind<FoodChainService>() with singleton { FoodChainService(instance(), instance(), instance()) }
        bind<SchemaService>() with singleton { SchemaService() }
        bind<RecipeExtractionService>() with singleton { RecipeExtractionService(instance(), instance(), instance(), instance()) }
    }
}

/**
 * Creates Neo4j configuration from application environment
 * Reads from application.yml with environment variable substitution
 */
private fun createNeo4jConfiguration(environment: ApplicationEnvironment): Neo4jConfiguration {
    val uri = environment.config.propertyOrNull("neo4j.uri")?.getString() ?: "bolt://localhost:7687"
    val username = environment.config.propertyOrNull("neo4j.username")?.getString() ?: "neo4j"
    val password = environment.config.propertyOrNull("neo4j.password")?.getString() ?: "password"
    val database = environment.config.propertyOrNull("neo4j.database")?.getString() ?: "neo4j"

    logger.info { "Neo4j Configuration loaded:" }
    logger.info { "  URI: $uri" }
    logger.info { "  Username: $username" }
    logger.info { "  Database: $database" }

    return Neo4jConfiguration(
        uri = uri,
        username = username,
        password = password,
        database = database
    )
}

/**
 * Creates and configures Neo4j driver with proper settings for Aura and local connections
 */
private fun createNeo4jDriver(config: Neo4jConfiguration): Driver {
    logger.info { "Connecting to Neo4j at ${config.uri} (database: ${config.database})" }

    try {
        val driver = GraphDatabase.driver(
            config.uri,
            AuthTokens.basic(config.username, config.password)
        )

        // Test the connection
        driver.verifyConnectivity()
        logger.info { "Successfully connected to Neo4j database '${config.database}'" }

        return driver
    } catch (exception: Exception) {
        logger.error(exception) { "Failed to connect to Neo4j at ${config.uri}" }
        throw RuntimeException("Neo4j connection failed: ${exception.message}", exception)
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "INTERNAL_ERROR", "message" to (cause.message ?: "Unknown error"))
            )
        }
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Autotroph Neo4j Service is running!")
        }

        route("/api/v1") {
            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
            }
        }

        entityRoutes()
        foodChainRoutes()
    }
}
