#!/bin/bash

# Test script for the recipe extraction from text endpoint
# This script tests the /api/v1/food-chain/recipes/extract-from-text endpoint

set -e

echo "=========================================="
echo "Testing Recipe Extraction from Text API"
echo "=========================================="

# Check if server is running
echo "Checking if server is running on localhost:8080..."
if ! curl -s http://localhost:8080/api/v1/food-chain/schema > /dev/null; then
    echo "Error: Server is not running on localhost:8080"
    echo "Please start the server first with: ./gradlew run"
    exit 1
fi

# Test with a sample recipe text
echo "Testing recipe extraction from text..."
echo ""

# Example request with recipe text
curl -X POST http://localhost:8080/api/v1/food-chain/recipes/extract-from-text \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Recipe: Classic Chocolate Chip Cookies\n\nIngredients:\n- 2 1/4 cups all-purpose flour\n- 1 tsp baking soda\n- 1 tsp salt\n- 1 cup butter, softened\n- 3/4 cup granulated sugar\n- 3/4 cup packed brown sugar\n- 2 large eggs\n- 2 tsp vanilla extract\n- 2 cups chocolate chips\n\nInstructions:\n1. Preheat oven to 375°F\n2. Mix flour, baking soda and salt in bowl\n3. Beat butter and sugars until creamy\n4. Add eggs and vanilla\n5. Gradually blend in flour mixture\n6. Stir in chocolate chips\n7. Drop rounded tablespoons onto ungreased cookie sheets\n8. Bake 9-11 minutes until golden brown"
  }' \
  | jq '.'

echo ""
echo "Expected response format:"
echo "{"
echo '  "url": "",'
echo '  "simplifiedName": "Chocolate Chip Cookies",'
echo '  "ingredients": ['
echo '    {'
echo '      "name": "flour",'
echo '      "similarIngredients": [...]'
echo '    },'
echo '    {'
echo '      "name": "chocolate chips",'
echo '      "similarIngredients": [...]'
echo '    }'
echo '  ],'
echo '  "success": true,'
echo '  "errorMessage": null'
echo "}"
echo ""
echo "Note: This requires a valid OPENAI_API_KEY environment variable to be set."
