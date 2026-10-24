package com.example.smartpantry;


// this screen displays and manages the pantry list
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

// home screen that lists every ingredient in the pantry, with search, add, edit and delete
public class PantryActivity extends BaseActivity {

    // Key used to pass an ingredient id to the edit screen
    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    // The database, views and adapter used by this screen
    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private TextView emptyText, banner;
    private SearchView searchView;
    private IngredientAdapter adapter;
    // How many items are in the pantry before searching
    private int totalItems = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the pantry layout
        setContentView(R.layout.activity_pantry);
        // Set the screen title
        setTitle("My Pantry");

        // Open the database and find the views
        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_pantry);
        emptyText = findViewById(R.id.tv_empty);
        banner = findViewById(R.id.tv_banner);
        searchView = findViewById(R.id.search_view);
        // Show the pantry as a vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Filter the list as the user types
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Hide the keyboard when the search is submitted
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the list on every letter typed
                if (adapter != null) {
                    adapter.filter(newText);
                    updateEmptyState();
                }
                return true;
            }
        });

        // The + button opens the add screen
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        // Turn on the bottom navigation bar
        setupBottomNav(R.id.nav_pantry);
    }

    // reloads every time we come back, so changes made on other screens show up.
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        // Load every pantry item from the database
        List<Ingredient> items = db.getAllIngredients();
        totalItems = items.size();
        // Read the saved settings
        boolean alerts = SettingsActivity.alertsEnabled(this);
        boolean imperial = SettingsActivity.isImperial(this);

        // Build the adapter with edit and delete actions
        adapter = new IngredientAdapter(items, alerts, imperial, new IngredientAdapter.Listener() {
            @Override
            public void onEdit(Ingredient ingredient) {
                // Open the edit screen for this ingredient
                Intent i = new Intent(PantryActivity.this, AddEditIngredientActivity.class);
                i.putExtra(EXTRA_INGREDIENT_ID, ingredient.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(final Ingredient ingredient) {
                // Ask before deleting the ingredient
                new AlertDialog.Builder(PantryActivity.this)
                        .setTitle("Delete ingredient?")
                        .setMessage("Remove " + ingredient.getName() + " from your pantry?")
                        .setPositiveButton("Delete", (d, w) -> {
                            // Delete it, then refresh the list
                            db.deleteIngredient(ingredient.getId());
                            loadPantry();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        // Show the items on screen
        recyclerView.setAdapter(adapter);
        adapter.filter(searchView.getQuery().toString());   // keep any search text

        // Refresh the warning banner and the empty message
        updateBanner(items, alerts);
        updateEmptyState();
    }

    // banner for the "expiring soon alerts" setting
    private void updateBanner(List<Ingredient> items, boolean alerts) {
        // Count items that are expired or expiring soon
        int count = 0;
        for (Ingredient i : items) {
            if (i.getExpiry() != null && !i.getExpiry().isEmpty()
                    && ExpiryUtil.daysUntil(i.getExpiry()) <= ExpiryUtil.SOON_DAYS) {
                count++;
            }
        }
        // Show the banner only if alerts are on and something is expiring
        if (alerts && count > 0) {
            banner.setText(count + (count == 1 ? " item is" : " items are")
                    + " expiring soon or already expired.");
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
        }
    }

    // feedback instead of a blank screen
    private void updateEmptyState() {
        // Pantry empty, no search results, or items to show
        if (totalItems == 0) {
            emptyText.setText("Your pantry is empty.\nTap the + button to add your first ingredient.");
            emptyText.setVisibility(View.VISIBLE);
        } else if (adapter.getItemCount() == 0) {
            emptyText.setText("No ingredients match your search.");
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
}
