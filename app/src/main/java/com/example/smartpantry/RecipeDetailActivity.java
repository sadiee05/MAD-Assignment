package com.example.smartpantry;


// this screen displays a recipe and its preparation steps
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// shows the full ingredient list and the numbered preparation steps for one recipe
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the recipe detail layout
        setContentView(R.layout.activity_recipe_detail);
        // Show a back arrow in the top bar
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the recipe id sent from the previous screen
        long id = getIntent().getLongExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        // Load the recipe from the database
        Recipe recipe = new DatabaseHelper(this).getRecipe(id);
        // Close the screen if the recipe is not found
        if (recipe == null) { finish(); return; }

        // Show the recipe name as the title and heading
        setTitle(recipe.getName());
        ((TextView) findViewById(R.id.tv_detail_name)).setText(recipe.getName());

        // Ingredients, shown in the user's preferred units
        boolean imperial = SettingsActivity.isImperial(this);
        // Build the ingredient list as text
        StringBuilder sb = new StringBuilder();
        for (Ingredient i : recipe.getIngredients()) {
            // Add one bullet line per ingredient
            sb.append("\u2022 ").append(UnitFormatter.format(i.getQuantity(), i.getUnit(), imperial))
              .append(" ").append(i.getName()).append("\n");
        }
        // Show the finished ingredient list
        ((TextView) findViewById(R.id.tv_detail_ingredients)).setText(sb.toString().trim());

        // Numbered steps: one row per line, with a number badge (1, 2, 3...)
        LinearLayout container = findViewById(R.id.steps_container);
        // Split the steps into separate lines
        String[] lines = recipe.getSteps().split("\n");
        int number = 1;
        for (String line : lines) {
            String text = line.replaceFirst("^\\s*\\d+[.)]\\s*", "").trim(); // drop any old "1." prefix
            // Skip blank lines
            if (text.isEmpty()) continue;
            // Create a row and fill in its number and text
            View row = LayoutInflater.from(this).inflate(R.layout.item_step, container, false);
            ((TextView) row.findViewById(R.id.tv_step_number)).setText(String.valueOf(number++));
            ((TextView) row.findViewById(R.id.tv_step_text)).setText(text);
            // Add the row to the screen
            container.addView(row);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Go back when the back arrow is tapped
        finish();
        return true;
    }
}
