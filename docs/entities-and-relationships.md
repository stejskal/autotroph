# Food Chain Application - Entities and Relationships

## Overview

The food-chain application is built around eight core domain entities that represent different aspects of meal planning, cooking, and shopping. All relationships between entities are unidirectional, and each entity can have zero or more relationships of each supported type.

## Core Entities

### Ingredient
Represents a basic food item or component that can be used in recipes or consumed directly in meals.

**Purpose**: The fundamental building block of the food system, representing individual food items, spices, liquids, or any consumable component.

**Key Characteristics**:
- Can be used directly in meals
- Can be components of recipes
- Can be added to shopping lists and meal plans
- Represents the atomic unit of the food domain

**Properties**:
- **Purchase_Frequency**: Indicates how often this ingredient is typically purchased
  - Always
  - Usually
  - Rarely

**Example Use Cases**:
- Raw ingredients like "tomatoes", "flour", "olive oil"
- Processed items like "canned beans", "pasta"
- Seasonings and spices like "salt", "oregano"

### Recipe
Represents a list of ingredients (not instructions), which may include ingredients and/or other recipes as components.

**Purpose**: Defines ingredients and sub-recipes to create a dish or food item.

**Key Characteristics**:
- Can reference multiple ingredients as components
- Can reference other recipes as sub-components (composition/nesting)
- Can be included in meals, meal plans, and shopping lists
- Represents prepared or semi-prepared food items

**Example Use Cases**:
- Simple recipes like "scrambled eggs" (uses ingredients: eggs, butter, salt)
- Complex recipes like "lasagna" (uses ingredients + sub-recipes like "tomato sauce", "bechamel sauce")
- Base recipes like "pizza dough" that are used in other recipes

### Meal
Represents a complete meal with mains and sides that can consist of recipes and/or individual ingredients.

**Purpose**: Groups together all the food items for a single eating occasion.

**Key Characteristics**:
- Can include multiple recipes
- Can include individual ingredients (for simple items or garnishes)
- Can be part of meal plans and shopping lists
- Represents a complete meal whose pairing is likely to be used repeatedly

**Example Use Cases**:
- "Roast Pork Loin with Mashed Potatoes and Green Beans": represents the combination of those recipes that are often consumed together 
- "Thanksgiving Dinner": Includes all the traditional recipes for thanksgiving day meal.  
- "Basic Friend Gathering": Common snacks, Burger recipe drinks used when friends come over. 

### Meal Plan
Represents a structured plan for meals over a period of time, organizing what will be eaten when.

**Purpose**: Provides organization and planning for meals across days, weeks, or other time periods.

**Key Characteristics**:
- Can directly reference ingredients for simple planning
- Can reference recipes for planned cooking
- Can reference complete meals for comprehensive planning
- Can be used to generate shopping lists
- Represents temporal organization of food consumption

**Example Use Cases**:
- Weekly meal plan with specific meals for each day
- Monthly dinner plan focusing only on evening meals
- Special event planning (holiday meals, party menus)
- All the food for snacks, breakfasts, lunch and dinner for the duration of a visitor

### Shopping List
Represents a collection of items needed for purchase, derived from various sources in the food planning system.

**Purpose**: Aggregates all the items needed for shopping based on planned meals, recipes, and direct ingredient needs.

**Key Characteristics**:
- Can include individual ingredients directly
- Can derive ingredients from recipes
- Can derive all components from complete meals
- Can derive all components from meal plans
- Represents the procurement aspect of food planning

**Example Use Cases**:
- Weekly grocery list generated from a meal plan
- Special shopping list for a dinner party (from specific meals)
- Ingredient list for trying new recipes

### Cuisine
Represents a style or tradition of cooking characterized by distinctive ingredients, techniques, and dishes.

**Purpose**: Categorizes recipes and meals by their cultural, regional, or stylistic cooking traditions.

**Key Characteristics**:
- Provides cultural and stylistic context for recipes and meals
- Helps with organization and discovery of food items
- Represents cooking traditions and flavor profiles
- Can be referenced by recipes and meals for categorization

**Example Use Cases**:
- Cultural cuisines like "Italian", "Chinese", "Mexican"
- Regional styles like "Mediterranean", "Southern", "Cajun"
- Cooking styles like "Vegetarian", "Vegan", "Gluten-Free"

### Store Location
Represents the broad section within a typical grocery store where the item is found.  Usually used to suggest a path through a grocery store during shopping

**Purpose**: Tracks where specific ingredients can be found for shopping and procurement planning.

**Key Characteristics**:
- Represents the typical location, in broad terms, within a grocery store
- Helps with shopping trip planning and optimization

**Example Use Cases**:
- Broad category locations within grocery stores "Dairy", "Butcher", "Produce"

### Source
Represents a reference or origin of a recipe or another source, allowing for attribution and traceability of culinary knowledge.

**Purpose**: Tracks the origin, attribution, or reference for recipes and other sources, enabling a hierarchical system of culinary knowledge and proper attribution.

**Key Characteristics**:
- Can reference other sources, creating a chain of attribution
- Provides traceability for recipe origins
- Supports hierarchical organization of culinary knowledge
- Enables proper attribution and citation

**Example Use Cases**:
- Cookbook references: "The Joy of Cooking", "Mastering the Art of French Cooking"
- Website sources: "AllRecipes.com", "Food Network"
- Personal sources: "Grandmother's Recipe Collection", "Chef Mario's Notes"
- Academic sources: "Culinary Institute of America Textbook"
- Chain of attribution: Recipe from cookbook that references another cookbook

## Entity Properties

This section defines the available properties and their possible values for each entity type.

### Ingredient Properties
```
Ingredient:
  Purchase_Frequency:
    Always
    Usually
    Rarely
```

### Recipe Properties
```
Recipe:
  (No specific properties defined yet)
```

### Meal Properties
```
Meal:
  (No specific properties defined yet)
```

### Meal Plan Properties
```
Meal Plan:
  (No specific properties defined yet)
```

### Shopping List Properties
```
Shopping List:
  (No specific properties defined yet)
```

### Cuisine Properties
```
Cuisine:
  (No specific properties defined yet)
```

### Store Location Properties
```
Store Location:
  (No specific properties defined yet)
```

### Source Properties
```
Source:
  (No specific properties defined yet)
```

## Relationship Matrix

| From Entity  | To Entity    | Relationship Type | Description |
|-------------|-------------|-------------------|-------------|
| Recipe      | Ingredient  | requires          | Recipe requires specific ingredients |
| Recipe      | Recipe      | composed-of       | Recipe can include other recipes as components |
| Meal        | Recipe      | composed-of       | Meal contains one or more recipes |
| Meal        | Ingredient  | requires          | Meal includes individual ingredients |
| Meal Plan   | Ingredient  | requires          | Meal plan directly includes ingredients |
| Meal Plan   | Recipe      | composed-of       | Meal plan includes specific recipes |
| Meal Plan   | Meal        | composed-of       | Meal plan schedules complete meals |
| Shopping List | Ingredient | needs             | Shopping list directly includes ingredients |
| Shopping List | Recipe     | needs-for         | Shopping list includes recipes (to derive ingredients) |
| Shopping List | Meal       | needs-for         | Shopping list includes meals (to derive all components) |
| Shopping List | Meal Plan  | needs-for         | Shopping list includes meal plans (to derive all components) |
| Ingredient    | Store Location | available-at      | Ingredient can be purchased at specific store locations |
| Recipe        | Cuisine    | belongs-to        | Recipe is categorized under a specific cuisine type |
| Meal          | Cuisine    | belongs-to        | Meal is categorized under a specific cuisine type |
| Source        | Source     | from              | Source references another source as its origin |
| Recipe        | Source     | from              | Recipe references a source as its origin |

## Relationship Characteristics

### Cardinality
- All relationships support **0 to many** (0..*) cardinality
- An entity can exist without any outgoing relationships
- An entity can have multiple relationships of the same type

### Directionality
- All relationships are **unidirectional**
- Relationships flow from the containing/planning entity to the contained/planned entity
- No automatic reverse relationships are implied

### Composition vs Reference
- **Recipe → Ingredient**: Composition (recipe defines how to use ingredients)
- **Recipe → Recipe**: Composition (sub-recipes are part of the main recipe)
- **Meal → Recipe/Ingredient**: Aggregation (meal groups items for consumption)
- **Meal Plan → All**: Reference (meal plan references items for scheduling)
- **Shopping List → All**: Reference (shopping list references items for procurement)
- **Ingredient → Store Location**: Reference (ingredient references where it can be purchased)
- **Recipe → Cuisine**: Reference (recipe references its cuisine classification)
- **Meal → Cuisine**: Reference (meal references its cuisine classification)

## Implementation Considerations

### Data Structure Implications
- Each entity should maintain collections of references to related entities
- Relationships should be stored as lists/collections to support multiple items
- Consider using identifiers/references rather than embedded objects for flexibility

### Traversal Patterns
- **Ingredient Aggregation**: Follow relationships to collect all required ingredients
- **Dependency Resolution**: Resolve recipe dependencies before meal preparation
- **Shopping List Generation**: Traverse from high-level plans down to individual ingredients

### Business Logic Considerations
- **Circular Dependencies**: Recipe → Recipe relationships should prevent cycles
- **Quantity Management**: Relationships may need to include quantity/portion information
- **Substitution Support**: Consider how ingredient substitutions affect relationships
- **Temporal Aspects**: Meal plans may need temporal ordering of relationships

## Usage Patterns

### Common Workflows
1. **Recipe Creation**: Recipe → Ingredients + optional Recipe → Recipe
2. **Meal Planning**: Meal Plan → Meals → Recipes → Ingredients
3. **Shopping List Generation**: Shopping List → (Meal Plans | Meals | Recipes) → Ingredients
4. **Meal Preparation**: Meal → Recipes → Ingredients (with dependency resolution)

### Data Flow Examples
- **Weekly Planning**: Meal Plan references 7 Meals, each Meal references multiple Recipes, Recipes reference Ingredients
- **Shopping Preparation**: Shopping List references Meal Plan, system traverses to collect all unique Ingredients
- **Recipe Development**: Complex Recipe references simpler Recipes and base Ingredients
