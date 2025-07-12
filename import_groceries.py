#!/usr/bin/env python3
"""
Script to import grocery data from our_groceries.json into the food-chain service
via HTTP REST endpoints.

Mapping:
- Recipes in JSON -> Recipes in system
- Items in JSON -> Ingredients in system  
- Category in JSON -> Store Location in system

The script ensures no duplicates by tracking created entities and reusing them.
"""

import json
import requests
import sys
from typing import Dict, Set, Optional
import time

class FoodChainImporter:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()
        
        # Track created entities to avoid duplicates
        self.store_locations: Dict[str, str] = {}  # name -> id
        self.ingredients: Dict[str, str] = {}      # name -> id
        self.recipes: Dict[str, str] = {}          # name -> id
        
        # Track processed items
        self.processed_store_locations: Set[str] = set()
        self.processed_ingredients: Set[str] = set()
        self.processed_recipes: Set[str] = set()

    def make_request(self, method: str, endpoint: str, data: dict = None, retries: int = 3) -> Optional[dict]:
        """Make HTTP request with retry logic"""
        url = f"{self.base_url}{endpoint}"
        
        for attempt in range(retries):
            try:
                if method.upper() == 'GET':
                    response = self.session.get(url)
                elif method.upper() == 'POST':
                    response = self.session.post(url, json=data)
                elif method.upper() == 'PUT':
                    response = self.session.put(url, json=data)
                else:
                    raise ValueError(f"Unsupported HTTP method: {method}")
                
                if response.status_code in [200, 201]:
                    return response.json() if response.content else {}
                elif response.status_code == 409:
                    print(f"Conflict (409) for {endpoint}: Entity may already exist")
                    return None
                else:
                    print(f"HTTP {response.status_code} for {endpoint}: {response.text}")
                    if attempt < retries - 1:
                        time.sleep(1)  # Wait before retry
                        continue
                    return None
                    
            except requests.exceptions.RequestException as e:
                print(f"Request failed (attempt {attempt + 1}): {e}")
                if attempt < retries - 1:
                    time.sleep(1)
                    continue
                return None
        
        return None

    def create_store_location(self, category_name: str) -> Optional[str]:
        """Create a store location and return its ID"""
        if category_name in self.processed_store_locations:
            location_id = self.store_locations.get(category_name)
            if location_id:
                return location_id
            else:
                print(f"⚠ Store location '{category_name}' marked as processed but ID not found")
                # Continue to create the store location
        
        # Map category names to more descriptive store location names
        location_mapping = {
            "Produce": "Produce Section",
            "Meat": "Meat Department",
            "Dairy": "Dairy Section",
            "Cheese": "Cheese Counter",
            "Middle": "Center Aisles",
            "Bakery": "Bakery Department",
            "Deli": "Deli Counter",
            "Frozen Food": "Frozen Foods",
            "": "General Store"  # Handle empty categories
        }

        # Handle empty or None categories
        if not category_name or category_name.strip() == "":
            category_name = ""
        
        location_name = location_mapping.get(category_name, category_name)
        
        store_location_data = {
            "name": location_name,
            "description": f"Store location for {category_name.lower()} items"
        }
        
        print(f"Creating store location: {location_name}")
        result = self.make_request('POST', '/api/v1/food-chain/store-locations', store_location_data)
        
        if result and 'id' in result:
            location_id = result['id']
            self.store_locations[category_name] = location_id
            self.processed_store_locations.add(category_name)
            print(f"✓ Created store location '{location_name}' with ID: {location_id}")
            return location_id
        else:
            print(f"✗ Failed to create store location: {location_name}")
            return None

    def create_ingredient(self, item_name: str, category: str) -> Optional[str]:
        """Create an ingredient and return its ID"""
        if item_name in self.processed_ingredients:
            ingredient_id = self.ingredients.get(item_name)
            if ingredient_id:
                return ingredient_id
            else:
                print(f"⚠ Ingredient '{item_name}' marked as processed but ID not found")
                # Continue to create the ingredient
        
        # Ensure store location exists
        store_location_id = self.create_store_location(category)
        if not store_location_id:
            print(f"✗ Cannot create ingredient '{item_name}' - store location creation failed")
            return None
        
        ingredient_data = {
            "name": item_name,
            "purchaseFrequency": "Usually"  # Default value as per domain model
        }
        
        print(f"Creating ingredient: {item_name}")
        result = self.make_request('POST', '/api/v1/food-chain/ingredients', ingredient_data)
        
        if result and 'id' in result:
            ingredient_id = result['id']
            self.ingredients[item_name] = ingredient_id
            self.processed_ingredients.add(item_name)
            print(f"✓ Created ingredient '{item_name}' with ID: {ingredient_id}")
            
            # Create relationship between ingredient and store location
            self.create_ingredient_store_relationship(ingredient_id, store_location_id)
            return ingredient_id
        else:
            print(f"✗ Failed to create ingredient: {item_name}")
            return None

    def create_ingredient_store_relationship(self, ingredient_id: str, store_location_id: str):
        """Create relationship between ingredient and store location"""
        endpoint = f'/api/v1/food-chain/ingredients/{ingredient_id}/store-locations/{store_location_id}'

        result = self.make_request('POST', endpoint)
        if result:
            print(f"✓ Created ingredient->store location relationship")
        else:
            print(f"✗ Failed to create ingredient->store location relationship")

    def create_recipe(self, recipe_name: str, items: list) -> Optional[str]:
        """Create a recipe and return its ID"""
        if recipe_name in self.processed_recipes:
            recipe_id = self.recipes.get(recipe_name)
            if recipe_id:
                print(f"✓ Recipe '{recipe_name}' already exists with ID: {recipe_id}")
                return recipe_id
            else:
                print(f"⚠ Recipe '{recipe_name}' marked as processed but ID not found")
                # Continue to create the recipe
        
        recipe_data = {
            "name": recipe_name,
            "description": f"Recipe for {recipe_name}"
        }
        
        print(f"Creating recipe: {recipe_name}")
        result = self.make_request('POST', '/api/v1/food-chain/recipes', recipe_data)
        
        if result and 'id' in result:
            recipe_id = result['id']
            self.recipes[recipe_name] = recipe_id
            self.processed_recipes.add(recipe_name)
            print(f"✓ Created recipe '{recipe_name}' with ID: {recipe_id}")
            
            # Create relationships between recipe and ingredients
            self.create_recipe_ingredient_relationships(recipe_id, items)
            return recipe_id
        else:
            print(f"✗ Failed to create recipe: {recipe_name}")
            return None

    def create_recipe_ingredient_relationships(self, recipe_id: str, items: list):
        """Create relationships between recipe and its ingredients"""
        for item in items:
            item_name = item['name']
            category = item['category']
            
            # Ensure ingredient exists
            ingredient_id = self.create_ingredient(item_name, category)
            if ingredient_id:
                endpoint = f'/api/v1/food-chain/recipes/{recipe_id}/ingredients/{ingredient_id}'

                result = self.make_request('POST', endpoint)
                if result:
                    print(f"✓ Created recipe->ingredient relationship: {recipe_id} -> {ingredient_id}")
                else:
                    print(f"✗ Failed to create recipe->ingredient relationship")

    def import_data(self, json_file_path: str):
        """Import all data from the JSON file"""
        try:
            with open(json_file_path, 'r') as f:
                data = json.load(f)
        except Exception as e:
            print(f"Error reading JSON file: {e}")
            return False
        
        recipes = data.get('recipes', [])
        total_recipes = len(recipes)
        
        print(f"Starting import of {total_recipes} recipes...")
        print("=" * 60)
        
        success_count = 0
        for i, recipe in enumerate(recipes, 1):
            recipe_name = recipe.get('name', '')
            items = recipe.get('items', [])
            
            print(f"\n[{i}/{total_recipes}] Processing recipe: {recipe_name}")
            print(f"  Items: {len(items)}")
            
            result = self.create_recipe(recipe_name, items)
            if result is not None:
                success_count += 1
                print(f"✓ SUCCESS COUNT: {success_count}/{total_recipes}")
            else:
                print(f"✗ FAILED: Recipe '{recipe_name}' returned: {result}")
                print(f"✗ SUCCESS COUNT: {success_count}/{total_recipes}")
            
            # Add small delay to avoid overwhelming the server
            time.sleep(0.1)
        
        print("\n" + "=" * 60)
        print(f"Import completed!")
        print(f"Successfully imported: {success_count}/{total_recipes} recipes")
        print(f"Store locations created: {len(self.processed_store_locations)}")
        print(f"Ingredients created: {len(self.processed_ingredients)}")
        
        return success_count == total_recipes

def main():
    if len(sys.argv) > 1:
        base_url = sys.argv[1]
    else:
        base_url = "http://localhost:8080"
    
    print(f"Food Chain Data Importer")
    print(f"Target service: {base_url}")
    print(f"Source file: our_groceries.json")
    print("=" * 60)
    
    importer = FoodChainImporter(base_url)
    success = importer.import_data('our_groceries.json')
    
    if success:
        print("\n🎉 Import completed successfully!")
        sys.exit(0)
    else:
        print("\n❌ Import completed with errors.")
        sys.exit(1)

if __name__ == '__main__':
    main()
