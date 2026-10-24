package com.example.smartpantry;


// this screen shows recipes that match the pantry ingredients
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// runs the strict-matching logic and lists the recipes the user can cook right now
public class SuggestedRecipesActivity extends BaseActivity {

    // Key used to pass the recipe id to the detail screen
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the screen layout
        setContentView(R.layout.activity_suggested);
        // Set the screen title
        setTitle("Suggested Recipes");

        // Open the database
        DatabaseHelper db = new DatabaseHelper(this);
        // Keep only the recipes that can be made with the pantry
        List<Recipe> matches = MatchEngine.findStrictMatches(db.getAllRecipes(), db.getAllIngredients());

        // Find the list and the empty message
        RecyclerView list = findViewById(R.id.recycler_recipes);
        TextView empty = findViewById(R.id.tv_no_matches);
        // Show recipes in a vertical list
        list.setLayoutManager(new LinearLayoutManager(this));
        // Fill the list; tapping a recipe opens its details
        list.setAdapter(new RecipeAdapter(matches, recipe -> {
            // Open the detail screen for the tapped recipe
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(EXTRA_RECIPE_ID, recipe.getId());
            startActivity(i);
        }));

        // Feedback when nothing matches
        empty.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);

        // Turn on the bottom navigation bar
        setupBottomNav(R.id.nav_recipes);
    }
}
