# Food Chain Data Import Script

This script imports grocery data from `our_groceries.json` into the food-chain service via HTTP REST endpoints.

## Data Mapping

| JSON Field | System Entity | Notes |
|------------|---------------|-------|
| `recipes[].name` | Recipe | Recipe entity in the system |
| `recipes[].items[].name` | Ingredient | Ingredient entity in the system |
| `recipes[].items[].category` | Store Location | Maps to store location where ingredient is found |

## Features

- **Duplicate Prevention**: Tracks created entities to avoid duplicates
- **Relationship Management**: Creates proper relationships between entities
- **Error Handling**: Includes retry logic and comprehensive error reporting
- **Progress Tracking**: Shows detailed progress during import

## Store Location Mapping

The script maps grocery categories to descriptive store locations:

| Category | Store Location |
|----------|----------------|
| Produce | Produce Section |
| Meat | Meat Department |
| Dairy | Dairy Section |
| Cheese | Cheese Counter |
| Middle | Center Aisles |
| Bakery | Bakery Department |
| Deli | Deli Counter |
| Frozen Food | Frozen Foods |

## Usage

### Basic Usage (localhost:8080)
```bash
python3 import_groceries.py
```

### Custom Service URL
```bash
python3 import_groceries.py http://your-service-url:port
```

### Make it executable and run
```bash
chmod +x import_groceries.py
./import_groceries.py
```

## Prerequisites

1. **Python 3.6+** with `requests` library
2. **Food-chain service** running and accessible
3. **our_groceries.json** file in the same directory

## Install Dependencies

```bash
pip install requests
```

## Expected API Endpoints

The script expects these REST endpoints to be available:

- `POST /api/store-locations` - Create store location
- `POST /api/ingredients` - Create ingredient  
- `POST /api/recipes` - Create recipe
- `POST /api/relationships` - Create entity relationships

## Relationships Created

1. **Ingredient → Store Location**: `LOCATED_AT` relationship
2. **Recipe → Ingredient**: `CONTAINS` relationship

## Output

The script provides detailed output including:
- Progress for each recipe being processed
- Success/failure status for each entity creation
- Summary statistics at completion
- Error messages for debugging

## Error Handling

- **HTTP Errors**: Retries failed requests up to 3 times
- **Conflicts (409)**: Handles duplicate entity creation gracefully
- **Network Issues**: Includes connection retry logic
- **Data Validation**: Checks for required fields before API calls

## Example Output

```
Food Chain Data Importer
Target service: http://localhost:8080
Source file: our_groceries.json
============================================================

Starting import of 150 recipes...

[1/150] Processing recipe: Babaganoosh
  Items: 4
Creating store location: Produce Section
✓ Created store location 'Produce Section' with ID: store-loc-1
Creating ingredient: Eggplant
✓ Created ingredient 'Eggplant' with ID: ingredient-1
✓ Created ingredient->store location relationship
...

============================================================
Import completed!
Successfully imported: 150/150 recipes
Store locations created: 8
Ingredients created: 342

🎉 Import completed successfully!
```

## Troubleshooting

1. **Connection Refused**: Ensure the food-chain service is running
2. **404 Errors**: Verify the API endpoints match your service implementation
3. **Permission Errors**: Check if the service requires authentication
4. **Timeout Issues**: The script includes delays to avoid overwhelming the server

## Data Statistics

Based on the current `our_groceries.json`:
- **Recipes**: ~150 recipes
- **Unique Ingredients**: ~300+ unique ingredients
- **Store Locations**: 8 categories/locations
- **Relationships**: ~1000+ relationships will be created
