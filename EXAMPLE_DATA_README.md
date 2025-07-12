# Food Chain Example Data

This document describes the example data that has been created for the food-chain application and how to interact with it.

## Overview

We have successfully created a comprehensive dataset representing a realistic food-chain scenario with ingredients, recipes, meals, shopping lists, meal plans, cuisines, and store locations.

## Created Data Summary

### Entity Counts
- **Ingredients**: 15 (including tomatoes, onions, garlic, chicken breast, pasta, etc.)
- **Recipes**: 5 (Spaghetti Marinara, Chicken Stir Fry, Beef Tacos, Grilled Salmon, Test Pasta)
- **Meals**: 3 (Italian Dinner, Healthy Lunch, Taco Tuesday)
- **Cuisines**: 5 (Italian, Mexican, Asian, American, Test Italian)
- **Store Locations**: 3 (Whole Foods Market, Trader Joes, Downtown Farmers Market)
- **Shopping Lists**: 2 (Weekly Groceries, Dinner Party Supplies)
- **Meal Plans**: 1 (This Week Meal Plan)

**Total Entities**: 34

## Example Entities

### Ingredients
- Tomatoes (ID: 11) - Fresh red tomatoes, purchase frequency: Usually
- Onions (ID: 12) - Yellow cooking onions, purchase frequency: Always
- Chicken Breast (ID: 15) - Boneless skinless chicken breast
- Pasta (ID: 18) - Spaghetti pasta
- Olive Oil (ID: 20) - Extra virgin olive oil

### Recipes
- **Spaghetti Marinara** (ID: 24) - Classic Italian pasta with tomato sauce
- **Chicken Stir Fry** (ID: 25) - Quick and healthy chicken and vegetable stir fry
- **Beef Tacos** (ID: 26) - Traditional Mexican beef tacos with spices
- **Grilled Salmon** (ID: 27) - Simple grilled salmon with herbs

### Meals
- **Italian Dinner** (ID: 28) - A complete Italian dinner experience
- **Healthy Lunch** (ID: 29) - Light and nutritious lunch
- **Taco Tuesday** (ID: 30) - Weekly taco night celebration

## API Endpoints to Explore

### General Entity Endpoints
```bash
# Get all entities
curl http://localhost:8080/api/v1/entities

# Get entity by ID
curl http://localhost:8080/api/v1/entities/11

# Search entities by type
curl "http://localhost:8080/api/v1/entities/search?type=Recipe"

# Search entities by name fragment
curl "http://localhost:8080/api/v1/entities/search?nameFragment=Chicken"
```

### Food-Chain Specific Endpoints
```bash
# Get ingredient details
curl http://localhost:8080/api/v1/food-chain/ingredients/11

# Get recipe details
curl http://localhost:8080/api/v1/food-chain/recipes/24

# Create new ingredient
curl -X POST -H "Content-Type: application/json" \
  -d '{"name": "New Ingredient", "description": "Test ingredient"}' \
  http://localhost:8080/api/v1/food-chain/ingredients

# Add ingredient to recipe
curl -X POST http://localhost:8080/api/v1/food-chain/recipes/24/ingredients/11
```

## Neo4j Browser Access

You can explore the data visually using the Neo4j Browser:

- **URL**: http://localhost:7474
- **Username**: neo4j
- **Password**: password

### Useful Cypher Queries

```cypher
// Show all nodes and relationships
MATCH (n)-[r]-(m) RETURN n, r, m LIMIT 50

// Show all ingredients
MATCH (n:Entity {type: 'Ingredient'}) RETURN n

// Show all recipes and their ingredients
MATCH (recipe:Entity {type: 'Recipe'})-[r]-(ingredient:Entity {type: 'Ingredient'})
RETURN recipe.name, ingredient.name

// Show meal plans and their components
MATCH (plan:Entity {type: 'MealPlan'})-[r*1..3]-(component)
RETURN plan.name, component.name, component.type
```

## Scripts Available

### 1. `scripts/test_api.sh`
Quick API test script that creates a few test entities and verifies basic functionality.

```bash
./scripts/test_api.sh
```

### 2. `scripts/create_example_data.sh`
Comprehensive script that creates the full example dataset with realistic food-chain data.

```bash
./scripts/create_example_data.sh
```

### 3. `scripts/show_data.sh`
Display script that shows a summary of all created data.

```bash
./scripts/show_data.sh
```

## Data Relationships

The example data includes realistic relationships between entities:

- **Recipes → Ingredients**: Spaghetti Marinara uses tomatoes, pasta, garlic, basil, olive oil
- **Recipes → Cuisines**: Each recipe is associated with appropriate cuisines
- **Meals → Recipes**: Meals contain one or more recipes
- **Shopping Lists → Ingredients/Recipes**: Shopping lists reference items to purchase
- **Meal Plans → Meals**: Meal plans organize meals over time
- **Ingredients → Store Locations**: Ingredients are available at specific stores

## Next Steps

1. **Explore the Neo4j Browser** to visualize the data relationships
2. **Test the API endpoints** to understand the data structure
3. **Create additional entities** using the API
4. **Build relationships** between entities
5. **Query the data** using both REST API and Cypher queries

## Troubleshooting

If you encounter issues:

1. **Service not running**: Start with `./gradlew run`
2. **Neo4j not available**: Start with `docker compose up -d neo4j`
3. **Connection issues**: Check that ports 8080 (service) and 7474/7687 (Neo4j) are available
4. **Data not visible**: Verify entities were created with `curl http://localhost:8080/api/v1/entities`

The example data provides a solid foundation for testing and developing your food-chain application!
