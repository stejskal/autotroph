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
 * Data classes for OpenAI Embeddings API
 */
@Serializable
data class OpenAIEmbeddingRequest(
    val model: String,
    val input: String
)

@Serializable
data class OpenAIEmbeddingResponse(
    val data: List<EmbeddingData>,
    val model: String,
    val usage: Usage
)

@Serializable
data class EmbeddingData(
    val embedding: List<Double>,
    val index: Int
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

/**
 * Service for generating embeddings for text content using OpenAI's API.
 * Uses the text-embedding-3-small model to generate embeddings.
 */
class EmbeddingService(private val apiKey: String) {

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
        private const val OPENAI_API_URL = "https://api.openai.com/v1/embeddings"
        private const val MODEL = "text-embedding-3-small"
    }

    /**
     * Generates an embedding vector for the given text using OpenAI's API.
     *
     * @param text The input text to generate an embedding for
     * @return A list of doubles representing the embedding vector
     * @throws Exception if the API call fails
     */
    suspend fun generateEmbedding(text: String): List<Double> {
        logger.info { "Generating embedding for text: '$text'" }

        try {
            val request = OpenAIEmbeddingRequest(
                model = MODEL,
                input = text
            )

            val response = httpClient.post(OPENAI_API_URL) {
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val embeddingResponse = response.body<OpenAIEmbeddingResponse>()
                val embedding = embeddingResponse.data.firstOrNull()?.embedding
                    ?: throw Exception("No embedding data received from OpenAI API")

                logger.debug { "Generated embedding of size ${embedding.size} for text: '$text'" }
                return embedding
            } else {
                val errorBody = response.body<String>()
                throw Exception("OpenAI API error: ${response.status} - $errorBody")
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate embedding for text: '$text'" }
            throw e
        }
    }

    /**
     * Calculates cosine similarity between two embedding vectors.
     * Returns a value between -1 and 1, where 1 means identical vectors.
     *
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Cosine similarity score
     */
    fun calculateCosineSimilarity(embedding1: List<Double>, embedding2: List<Double>): Double {
        if (embedding1.size != embedding2.size) {
            logger.warn { "Embedding dimensions don't match: ${embedding1.size} vs ${embedding2.size}" }
            return 0.0
        }

        if (embedding1.isEmpty() || embedding2.isEmpty()) {
            return 0.0
        }

        val dotProduct = embedding1.zip(embedding2).sumOf { (a, b) -> a * b }
        val magnitude1 = kotlin.math.sqrt(embedding1.sumOf { it * it })
        val magnitude2 = kotlin.math.sqrt(embedding2.sumOf { it * it })

        return if (magnitude1 > 0 && magnitude2 > 0) {
            dotProduct / (magnitude1 * magnitude2)
        } else {
            0.0
        }
    }

    /**
     * Finds the most similar embeddings to a target embedding from a list of candidates.
     *
     * @param targetEmbedding The embedding to find similarities for
     * @param candidateEmbeddings List of candidate embeddings with their associated data
     * @param topK Number of top similar results to return
     * @return List of similarity results sorted by similarity score (highest first)
     */
    fun <T> findMostSimilar(
        targetEmbedding: List<Double>,
        candidateEmbeddings: List<Pair<T, List<Double>>>,
        topK: Int = 10
    ): List<SimilarityResult<T>> {
        logger.info { "Finding top $topK similar embeddings from ${candidateEmbeddings.size} candidates" }

        val similarities = candidateEmbeddings.map { (data, embedding) ->
            val similarity = calculateCosineSimilarity(targetEmbedding, embedding)
            SimilarityResult(data, similarity)
        }

        return similarities
            .sortedByDescending { it.similarity }
            .take(topK)
    }

    /**
     * Closes the HTTP client to free up resources.
     * Should be called when the service is no longer needed.
     */
    fun close() {
        httpClient.close()
    }
}

/**
 * Data class representing a similarity search result
 */
data class SimilarityResult<T>(
    val data: T,
    val similarity: Double
)
