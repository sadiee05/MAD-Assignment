package com.example.smartpantry;


// this class stores recipe details and ingredients
import java.util.List;

// a recipe with its required ingredients and preparation steps
public class Recipe {
    // Fields that describe one recipe
    private long id;
    private String name;
    private String steps;
    private List<Ingredient> ingredients;

    // Builds a recipe from its values
    public Recipe(long id, String name, String steps, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    // Simple getters to read the fields
    public long getId() { return id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }
    public List<Ingredient> getIngredients() { return ingredients; }
}
