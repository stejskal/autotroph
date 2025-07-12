package com.foodchain.autotroph.service

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HtmlParserServiceTest {

    private val htmlParserService = HtmlParserService()

    @Test
    fun `parseHtmlFromUrl should handle invalid URL gracefully`() = runBlocking {
        // Given
        val invalidUrl = "not-a-valid-url"

        // When
        val result = htmlParserService.parseHtmlFromUrl(invalidUrl)

        // Then
        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `parseHtmlFromUrl should handle unreachable URL gracefully`() = runBlocking {
        // Given
        val unreachableUrl = "https://this-domain-does-not-exist-12345.com"

        // When
        val result = htmlParserService.parseHtmlFromUrl(unreachableUrl)

        // Then
        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.errorMessage)
    }
}
