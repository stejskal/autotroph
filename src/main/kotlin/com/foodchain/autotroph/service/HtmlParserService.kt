package com.foodchain.autotroph.service

import com.microsoft.playwright.*
import com.microsoft.playwright.options.LoadState
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}

/**
 * Data class for HTML parsing result
 */
data class HtmlParsingResult(
    val title: String?,
    val description: String?,
    val extractedContent: String,
    val success: Boolean,
    val errorMessage: String? = null
)

/**
 * Service for parsing HTML content from web pages using Playwright and Jsoup.
 * Uses Playwright to render JavaScript-heavy pages and Jsoup to parse and extract content.
 */
class HtmlParserService {

    /**
     * Extracts text content from a given URL.
     * 
     * @param url The URL to extract content from
     * @return HtmlParsingResult containing the extracted data
     */
    suspend fun parseHtmlFromUrl(url: String): HtmlParsingResult {
        logger.info { "Parsing HTML from URL: $url" }
        
        try {
            // Use Playwright to render the page
            val renderedHtml = renderPageWithPlaywright(url)
            
            // Use Jsoup to parse and extract content
            val result = extractContentWithJsoup(renderedHtml)
            
            logger.info { "Successfully parsed HTML from $url" }
            return result
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse HTML from URL: $url" }
            return HtmlParsingResult(
                title = null,
                description = null,
                extractedContent = "",
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Uses Playwright to render a web page and return the HTML content.
     * This handles JavaScript-rendered content that static scrapers would miss.
     */
    private fun renderPageWithPlaywright(url: String): String {
        logger.debug { "Rendering page with Playwright: $url" }
        
        return Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(true)
            )
            
            browser.use { browser ->
                val context = browser.newContext(
                    Browser.NewContextOptions()
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                )
                
                context.use { context ->
                    val page = context.newPage()
                    
                    // Set a reasonable timeout
                    page.setDefaultTimeout(30000.0) // 30 seconds
                    
                    // Navigate to the page
                    page.navigate(url)

                    // Wait for the DOM to be ready (more reliable for ad-heavy sites)
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED)

                    // Give a moment for JavaScript to render recipe content
                    page.waitForTimeout(2000.0) // 2 seconds

                    // Get the rendered HTML content
                    val content = page.content()
                    logger.debug { "Successfully rendered page content (${content.length} characters)" }
                    
                    content
                }
            }
        }
    }

    /**
     * Uses Jsoup to parse HTML and extract text content.
     */
    private fun extractContentWithJsoup(html: String): HtmlParsingResult {
        logger.debug { "Parsing HTML with Jsoup (${html.length} characters)" }

        val doc = Jsoup.parse(html)

        // Extract basic page information
        val title = doc.title().takeIf { it.isNotBlank() }
        val description = doc.selectFirst("meta[name=description]")?.attr("content")?.takeIf { it.isNotBlank() }

        // Get all text content in simple format
        val allTextContent = getSimpleTextContent(doc)

        return HtmlParsingResult(
            title = title,
            description = description,
            extractedContent = allTextContent,
            success = allTextContent.isNotBlank(),
            errorMessage = if (allTextContent.isBlank()) "No content found" else null
        )
    }

    /**
     * Extracts simple text content from the document
     */
    private fun getSimpleTextContent(doc: Document): String {
        // Remove scripts, styles, and other non-content elements
        doc.select("script, style, nav, header, footer, aside, .advertisement, .ads, noscript").remove()

        // Get all text content from the body
        val bodyText = doc.body()?.text() ?: ""

        // Clean up whitespace and return
        return bodyText.replace(Regex("\\s+"), " ").trim()
    }
}
