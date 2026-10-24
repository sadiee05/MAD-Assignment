package com.example.smartpantry;


// this class connects recipe data to the recipe list
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// shows the suggested recipes
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    // Lets the screen know when a recipe row is tapped
    public interface Listener { void onClick(Recipe recipe); }

    // The recipes to show and the tap listener
    private final List<Recipe> recipes;
    private final Listener listener;

    // Stores the list and the click listener
    public RecipeAdapter(List<Recipe> recipes, Listener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    // Holds the views of one recipe row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, count;
        ViewHolder(View v) {
            super(v);
            // Link the fields to the row's text views
            name = v.findViewById(R.id.tv_recipe_name);
            count = v.findViewById(R.id.tv_recipe_count);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a row from the item_recipe layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        // Get the recipe for this row
        final Recipe r = recipes.get(position);
        // Show the recipe name
        h.name.setText(r.getName());
        // Show how many ingredients it uses
        h.count.setText(r.getIngredients().size() + " ingredients - you have them all");
        // Tell the screen when this row is tapped
        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() { return recipes.size(); }
}
