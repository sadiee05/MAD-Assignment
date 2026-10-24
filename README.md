# Smart Pantry Manager

A Java Android app that reduces food waste by suggesting recipes using **only** the ingredients
you already have. A recipe is shown only if *every* ingredient is in your pantry in at least the
required quantity (the "strict-matching rule").

## Features
- Add / edit / delete pantry items (name, quantity, unit, required future expiry date); duplicates are merged or blocked
- Search box on the pantry list
- Settings: expiring-soon alerts toggle and Metric / Imperial unit preference
- 19 pre-loaded recipes (seeded on first run)
- Suggested Recipes screen using strict matching (handles plurals, case and unit conversion)
- Recipe detail screen (ingredients + method)
- Bottom navigation: Pantry | Recipes | Settings

## Database choice: SQLite (SQLiteOpenHelper)
The app uses SQLite to store pantry items and recipes directly on the device. This means users don't need an account or an internet connection to use the app. Their data is also saved when they close the application.

## Setup / Run
1. Install Android Studio **Koala (2024.1.1) or newer** (required by Android Gradle Plugin 8.5.0) with an SDK for API 34. JDK 17 is bundled with Android Studio.
2. Extract the zip, then `File > Open` and choose the `SmartPantryManager` folder (the one containing `settings.gradle`). Let Gradle sync (internet needed the first time; it downloads Gradle 8.7 automatically).
3. Create an emulator (or plug in a phone with USB debugging) and press **Run**.

## Project structure
- `DatabaseHelper` - SQLite tables, CRUD, recipe seed data
- `MatchEngine` - strict-matching logic
- `PantryActivity`, `AddEditIngredientActivity`, `SuggestedRecipesActivity`, `RecipeDetailActivity`, `SettingsActivity`
- `IngredientAdapter`, `RecipeAdapter` - RecyclerView adapters
