package com.example.smartpantry;


// this class displays and filters pantry ingredients
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// shows the pantry items, with search filtering plus edit and delete buttons
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    // Lets the screen react to edit and delete taps
    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final List<Ingredient> allItems;                  // everything in the pantry
    private final List<Ingredient> shownItems = new ArrayList<>(); // after the search filter
    // Display settings and the click listener
    private final boolean showExpiryAlerts;
    private final boolean imperial;
    private final Listener listener;

    // stores the items, the settings and the listener
    public IngredientAdapter(List<Ingredient> items, boolean showExpiryAlerts,
                             boolean imperial, Listener listener) {
        this.allItems = items;
        this.shownItems.addAll(items);
        this.showExpiryAlerts = showExpiryAlerts;
        this.imperial = imperial;
        this.listener = listener;
    }

    // keeps only the items whose name contains the search text
    public void filter(String query) {
        // cleans up the search text
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        // prepare a singular version of the search
        String qNorm = MatchEngine.normalise(q);
        // Start with an empty visible list
        shownItems.clear();
        // Check every pantry item against the search
        for (Ingredient i : allItems) {
            // Compare in lowercase
            String name = i.getName().toLowerCase(Locale.ROOT);
            // Keep the item if there is no search or the name matches
            if (q.isEmpty() || name.contains(q) || MatchEngine.normalise(name).contains(qNorm)) {
                shownItems.add(i);
            }
        }
        // Refresh the list on screen
        notifyDataSetChanged();
    }

    // Holds the views of one pantry row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, expiry;
        ImageButton edit, delete;

        ViewHolder(View v) {
            super(v);
            // Link the fields to the row's views
            name = v.findViewById(R.id.tv_name);
            quantity = v.findViewById(R.id.tv_quantity);
            expiry = v.findViewById(R.id.tv_expiry);
            edit = v.findViewById(R.id.btn_edit);
            delete = v.findViewById(R.id.btn_delete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a row from the item_ingredient layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        // Get the ingredient for this row
        final Ingredient item = shownItems.get(position);
        // Show the name and the quantity in the chosen units
        h.name.setText(item.getName());
        h.quantity.setText(UnitFormatter.format(item.getQuantity(), item.getUnit(), imperial));

        // Read the expiry date
        String expiry = item.getExpiry();
        // Hide the chip if there is no expiry date
        if (expiry == null || expiry.isEmpty()) {
            h.expiry.setVisibility(View.GONE);
        } else {
            h.expiry.setVisibility(View.VISIBLE);
            // Days left until the item expires
            long days = ExpiryUtil.daysUntil(expiry);
            if (showExpiryAlerts && days < 0) {
                // Expired: show a warning
                h.expiry.setText("Expired on " + expiry);
                h.expiry.setBackgroundResource(R.drawable.bg_chip_warning);
            } else if (showExpiryAlerts && days <= ExpiryUtil.SOON_DAYS) {
                // Expiring soon: show a warning
                h.expiry.setText("Expires soon: " + expiry);
                h.expiry.setBackgroundResource(R.drawable.bg_chip_warning);
            } else {
                // Still fresh: show the normal chip
                h.expiry.setText("Expires: " + expiry);
                h.expiry.setBackgroundResource(R.drawable.bg_chip_stock);
            }
        }

        // Tells the screen when edit or delete is tapped
        h.edit.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() { return shownItems.size(); }
}
