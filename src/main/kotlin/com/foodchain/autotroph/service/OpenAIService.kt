package com.foodchain.autotroph.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Data classes for OpenAI Chat Completion API
 */
@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.3,
    @SerialName("max_tokens") val maxTokens: Int = 1000
)

@Serializable
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

@Serializable
data class OpenAIChatResponse(
    val choices: List<ChatChoice>,
    val usage: ChatUsage
)

@Serializable
data class ChatChoice(
    val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String
)

@Serializable
data class ChatUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

/**
 * Data class for structured recipe extraction result
 */
@Serializable
data class RecipeExtractionResult(
    val simplifiedName: String,
    val ingredients: List<String>
)

/**
 * Service for interacting with OpenAI's Chat Completion API.
 * Used to extract structured recipe information from raw text content.
 */
class OpenAIService(private val apiKey: String) {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    companion object {
        private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-4o-mini"
        
        private const val SYSTEM_PROMPT = """
You are a recipe extraction assistant. Your task is to analyze text content from a web page and extract:
1. A simplified, concise recipe name (not the verbose title from the webpage)
2. A distinct list of ingredients

Please respond with a JSON object in this exact format:
{
  "simplifiedName": "Simple Recipe Name",
  "ingredients": ["ingredient 1", "ingredient 2", "ingredient 3"]
}

Guidelines:
- For the simplified name: Create a short, clear name that describes the dish (e.g., "Chocolate Chip Cookies" instead of "The Best Ever Super Moist Chocolate Chip Cookies You'll Ever Make")
- For ingredients: Extract only the actual ingredients, not quantities or preparation methods
- Remove duplicates from the ingredient list
- If no clear recipe is found, return empty arrays but still provide the JSON structure
"""
    }

    /**
     * Extracts recipe information from raw text content using OpenAI.
     *
     * @param extractedText The raw text content from a web page
     * @return RecipeExtractionResult containing simplified name and ingredients
     * @throws Exception if the API call fails or response cannot be parsed
     */
    suspend fun extractRecipeFromText(extractedText: String): RecipeExtractionResult {
        logger.info { "Extracting recipe information from text (${extractedText.length} characters)" }

        try {
            val request = OpenAIChatRequest(
                model = MODEL,
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = "Extract recipe information from this text:\n\n$extractedText")
                ),
                temperature = 0.3,
                maxTokens = 1000
            )

            val response = httpClient.post(OPENAI_API_URL) {
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val chatResponse = response.body<OpenAIChatResponse>()
                val content = chatResponse.choices.firstOrNull()?.message?.content
                    ?: throw Exception("No response content received from OpenAI API")

                logger.debug { "OpenAI response: $content" }

                // Parse the JSON response
                val result = try {
                    Json.decodeFromString<RecipeExtractionResult>(content.trim())
                } catch (e: Exception) {
                    logger.warn { "Failed to parse OpenAI response as JSON: $content" }
                    // Fallback: try to extract information manually
                    parseResponseManually(content)
                }

                logger.info { "Successfully extracted recipe: '${result.simplifiedName}' with ${result.ingredients.size} ingredients" }
                return result
            } else {
                val errorBody = response.body<String>()
                throw Exception("OpenAI API error: ${response.status} - $errorBody")
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to extract recipe information from text" }
            throw e
        }
    }

    /**
     * Fallback method to manually parse OpenAI response if JSON parsing fails
     */
    private fun parseResponseManually(content: String): RecipeExtractionResult {
        logger.debug { "Attempting manual parsing of OpenAI response" }
        
        // Try to extract simplified name and ingredients from the response text
        val simplifiedName = extractSimplifiedName(content)
        val ingredients = extractIngredients(content)
        
        return RecipeExtractionResult(
            simplifiedName = simplifiedName,
            ingredients = ingredients
        )
    }

    private fun extractSimplifiedName(content: String): String {
        // Look for patterns like "simplifiedName": "..." or similar
        val nameRegex = """"simplifiedName"\s*:\s*"([^"]+)"""".toRegex()
        val match = nameRegex.find(content)
        return match?.groupValues?.get(1) ?: "Unknown Recipe"
    }

    private fun extractIngredients(content: String): List<String> {
        // Look for ingredients array or list patterns
        val ingredientsRegex = """"ingredients"\s*:\s*\[(.*?)\]""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = ingredientsRegex.find(content)
        
        return if (match != null) {
            val ingredientsString = match.groupValues[1]
            ingredientsString.split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    /**
     * Closes the HTTP client to free up resources.
     * Should be called when the service is no longer needed.
     */
    fun close() {
        httpClient.close()
    }
}
