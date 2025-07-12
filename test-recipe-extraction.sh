#!/bin/bash

# Test script for the new recipe extraction functionality
# This script demonstrates the updated extract-from-url endpoint

echo "Testing Recipe Extraction with OpenAI Integration"
echo "================================================"

# Check if the service is running
if ! curl -s http://localhost:8080/api/v1/health > /dev/null; then
    echo "Error: Service is not running on localhost:8080"
    echo "Please start the service first with: ./gradlew run"
    exit 1
fi

# Test with a sample recipe URL
echo "Testing recipe extraction from URL..."
echo ""

# Example request
curl -X POST http://localhost:8080/api/v1/food-chain/recipes/extract-from-url \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.allrecipes.com/recipe/213742/cheesy-chicken-broccoli-casserole/"}' \
  | jq '.'

echo ""
echo "Expected response format:"
echo "{"
echo '  "url": "https://example.com/recipe",'
echo '  "simplifiedName": "Chicken Broccoli Casserole",'
echo '  "ingredients": ["chicken", "broccoli", "cheese", "rice"],'
echo '  "success": true,'
echo '  "errorMessage": null'
echo "}"
echo ""
echo "Note: This requires a valid OPENAI_API_KEY environment variable to be set."
