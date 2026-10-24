package com.example.smartpantry;


// this class creates the database and manages stored pantry and recipe data
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database file name and version
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        // Tell SQLite the file name and version
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table for the user's pantry items
        db.execSQL("CREATE TABLE ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        // Table for the recipes
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        // Table linking each recipe to its ingredients
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        // Fill the recipes table with starter recipes
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete the old tables and rebuild them
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        onCreate(db);
    }



    // creating ingredient in the db
    public long addIngredient(String name, double qty, String unit, String expiry) {
        // Collect the values to save
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("quantity", qty);
        cv.put("unit", unit);
        cv.put("expiry", expiry);
        // Insert the row and return its new id
        return getWritableDatabase().insert("ingredients", null, cv);
    }

    // reading ingredients in db
    public List<Ingredient> getAllIngredients() {
        // Query all pantry items sorted by name
        List<Ingredient> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, quantity, unit, expiry FROM ingredients ORDER BY name COLLATE NOCASE", null);
        // Turn each row into an Ingredient object
        while (c.moveToNext()) {
            list.add(new Ingredient(c.getLong(0), c.getString(1), c.getDouble(2),
                    c.getString(3), c.getString(4)));
        }
        c.close();
        return list;
    }

    // getting one ingredient from the db
    public Ingredient getIngredient(long id) {
        // Query one pantry item by id
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, quantity, unit, expiry FROM ingredients WHERE id = ?",
                new String[]{String.valueOf(id)});
        Ingredient result = null;
        // Build the object if a row was found
        if (c.moveToFirst()) {
            result = new Ingredient(c.getLong(0), c.getString(1), c.getDouble(2),
                    c.getString(3), c.getString(4));
        }
        c.close();
        return result;
    }

    // editing ingredient in the db
    public int updateIngredient(long id, String name, double qty, String unit, String expiry) {
        // Collect the new values to save
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("quantity", qty);
        cv.put("unit", unit);
        cv.put("expiry", expiry);
        // Update the row with this id
        return getWritableDatabase().update("ingredients", cv, "id = ?", new String[]{String.valueOf(id)});
    }

    // deleting ingredient from the db
    public void deleteIngredient(long id) {
        // Delete the row with this id
        getWritableDatabase().delete("ingredients", "id = ?", new String[]{String.valueOf(id)});
    }

    // searching for duplicate ingredients in the db
    public Ingredient findDuplicate(String name, long excludeId) {
        // Compare names after removing case and plurals
        String key = MatchEngine.normalise(name);
        // Return the first other item with the same name
        for (Ingredient i : getAllIngredients()) {
            if (i.getId() != excludeId && MatchEngine.normalise(i.getName()).equals(key)) {
                return i;
            }
        }
        return null;
    }

    public void deleteAllIngredients() {
        // Delete every row in the pantry table
        getWritableDatabase().delete("ingredients", null, null);
    }

    // ===================== RECIPES =====================

    public List<Recipe> getAllRecipes() {
        // Query all recipes sorted by name
        List<Recipe> recipes = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, steps FROM recipes ORDER BY name", null);
        while (c.moveToNext()) {
            // Read the id so we can load this recipe's ingredients
            long id = c.getLong(0);
            recipes.add(new Recipe(id, c.getString(1), c.getString(2), getRecipeIngredients(id)));
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        // Query one recipe by id
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, steps FROM recipes WHERE id = ?", new String[]{String.valueOf(id)});
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(c.getLong(0), c.getString(1), c.getString(2), getRecipeIngredients(id));
        }
        c.close();
        return r;
    }

    private List<Ingredient> getRecipeIngredients(long recipeId) {
        // Query the ingredients of one recipe
        List<Ingredient> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, quantity, unit FROM recipe_ingredients WHERE recipe_id = ?",
                new String[]{String.valueOf(recipeId)});
        while (c.moveToNext()) {
            list.add(new Ingredient(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), null));
        }
        c.close();
        return list;
    }

    // inserts recipe
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... items) {
        // Save the recipe name and steps
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert("recipes", null, rv);
        // Save each ingredient linked to this recipe
        for (String item : items) {
            // Split "name|quantity|unit" into its parts
            String[] p = item.split("\\|");
            // Put this ingredient's values together
            ContentValues cv = new ContentValues();
            cv.put("recipe_id", recipeId);
            cv.put("name", p[0]);
            cv.put("quantity", Double.parseDouble(p[1]));
            cv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, cv);
        }
    }

    // embeded recipes
    private void seedRecipes(SQLiteDatabase db) {
        // Starter recipes added on first run
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs.\n3. Melt butter in a pan and scramble the eggs on low heat.\n4. Pile onto the toast.",
                "egg|2|pcs", "bread|2|pcs", "butter|10|g");
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt butter in a pan and pour in the eggs.\n3. Add grated cheese, fold in half and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry garlic in olive oil, add chopped tomatoes and simmer 10 minutes.\n3. Toss with the pasta.",
                "pasta|200|g", "tomato|4|pcs", "olive oil|2|tbsp", "garlic|2|pcs");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion until soft.\n2. Add cooked rice and soy sauce.\n3. Push rice aside, scramble the eggs, then mix everything together.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp");
        addRecipe(db, "Fluffy Pancakes",
                "1. Mix flour, sugar, milk and egg into a smooth batter.\n2. Melt butter in a pan.\n3. Cook spoonfuls of batter until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|2|tbsp", "butter|20|g");
        addRecipe(db, "Banana Oat Porridge",
                "1. Simmer oats in milk for 5 minutes, stirring.\n2. Slice the banana on top and serve warm.",
                "oat|80|g", "milk|250|ml", "banana|1|pcs");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place cheese between the slices.\n3. Fry on both sides until golden and melted.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");
        addRecipe(db, "Creamy Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter and warm milk.",
                "potato|500|g", "butter|30|g", "milk|100|ml");
        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion in olive oil.\n3. Stir in the tuna and toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "olive oil|1|tbsp");
        addRecipe(db, "Vegetable Stir Fry",
                "1. Slice all vegetables.\n2. Stir-fry in a hot pan for 5 minutes.\n3. Add soy sauce and serve over cooked rice.",
                "carrot|2|pcs", "onion|1|pcs", "bell pepper|1|pcs", "soy sauce|2|tbsp", "rice|150|g");
        addRecipe(db, "Cheesy Baked Potatoes",
                "1. Bake the potatoes at 200C for 50 minutes.\n2. Split open, add butter and cheese.\n3. Return to the oven for 5 minutes.",
                "potato|2|pcs", "cheese|80|g", "butter|20|g");
        addRecipe(db, "Lentil Soup",
                "1. Fry onion, carrot and garlic in olive oil.\n2. Add lentils and cover with water.\n3. Simmer for 25 minutes.",
                "lentil|200|g", "onion|1|pcs", "carrot|2|pcs", "garlic|2|pcs", "olive oil|1|tbsp");
        addRecipe(db, "Chicken and Rice",
                "1. Cut the chicken into pieces and brown with onion and garlic.\n2. Add rice and water.\n3. Cover and cook for 20 minutes.",
                "chicken breast|300|g", "rice|200|g", "onion|1|pcs", "garlic|2|pcs");
        addRecipe(db, "French Toast",
                "1. Whisk egg, milk and sugar.\n2. Soak the bread slices.\n3. Fry in butter until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|10|g");
        addRecipe(db, "Simple Tomato Soup",
                "1. Fry onion and garlic in olive oil.\n2. Add chopped tomatoes and simmer 20 minutes.\n3. Blend until smooth.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|1|tbsp");
        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread peanut butter.\n3. Top with sliced banana.",
                "bread|2|pcs", "peanut butter|2|tbsp", "banana|1|pcs");
        addRecipe(db, "Beans on Toast",
                "1. Warm the beans in a small pot.\n2. Toast and butter the bread.\n3. Spoon the beans on top.",
                "baked beans|400|g", "bread|2|pcs", "butter|10|g");
        addRecipe(db, "Avocado Toast",
                "1. Toast the bread.\n2. Mash the avocado with a squeeze of lemon.\n3. Spread on the toast.",
                "avocado|1|pcs", "bread|2|pcs", "lemon|1|pcs");
        addRecipe(db, "Garlic and Oil Pasta",
                "1. Boil the pasta.\n2. Gently fry sliced garlic in olive oil.\n3. Toss the pasta in the oil and serve.",
                "pasta|200|g", "garlic|4|pcs", "olive oil|3|tbsp");
    }
}
