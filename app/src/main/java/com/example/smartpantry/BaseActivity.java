package com.example.smartpantry;


// this class shares bottom navigation behaviour between screens
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    // Hooks up the bottom nav bar and highlights the current tab
    protected void setupBottomNav(final int selectedId) {
        // Find the nav bar and mark the current tab as selected
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            // Find which tab was tapped
            int id = item.getItemId();
            // Do nothing if that tab is already open
            if (id == selectedId) return true;

            // Pick the screen that matches the tapped tab
            Class<?> target;
            if (id == R.id.nav_pantry) target = PantryActivity.class;
            else if (id == R.id.nav_recipes) target = SuggestedRecipesActivity.class;
            else target = SettingsActivity.class;

            // Create the intent that opens that screen
            Intent intent = new Intent(this, target);
            if (target == PantryActivity.class) {
                // Going "home": clear everything above the pantry screen.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            // Open the chosen screen
            startActivity(intent);
            // Skip the slide animation between tabs
            overridePendingTransition(0, 0);
            // Pantry is the root screen, so it stays on the back stack.
            if (!(this instanceof PantryActivity)) finish();
            return true;
        });
    }
}
