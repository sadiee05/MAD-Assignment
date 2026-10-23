package com.example.smartpantry;


// this class checks whether pantry ingredients meet recipe requirements
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
 // arecipe is suggested only if EVERY ingredient is in the pantry in at least
public class MatchEngine {

    // lowercase entered ingredient
    public static String normalise(String name) {
        // Trim, lowercase and collapse extra spaces
        String n = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        // Turn plural endings into singular (berries, tomatoes, eggs)
        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";      // berries -> berry
        }
        if (n.endsWith("oes")) {
            return n.substring(0, n.length() - 2);             // tomatoes -> tomato
        }
        if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 3) {
            return n.substring(0, n.length() - 1);             // eggs -> egg
        }
        // Leave the name as it is if no rule applies
        return n;
    }

    // groups units into mass (g), volume (ml) or count (pcs).
    public static String dimension(String unit) {
        // Decide if the unit measures mass, volume or count
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "g": case "kg":
                return "mass";
            case "ml": case "l": case "tsp": case "tbsp": case "cup":
                return "volume";
            default:
                return "count";
        }
    }

    // converts a quantity to its base unit
    public static double toBase(double qty, String unit) {
        // Use the factor that converts this unit to its base unit
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "kg":   return qty * 1000;
            case "l":    return qty * 1000;
            case "tsp":  return qty * 5;
            case "tbsp": return qty * 15;
            case "cup":  return qty * 240;
            default:     return qty;
        }
    }

    private static String keyOf(Ingredient i) {
        // The key joins the clean name and the unit type
        return normalise(i.getName()) + "|" + dimension(i.getUnit());
    }

    // returns only the recipes the user can make right now
    public static List<Recipe> findStrictMatches(List<Recipe> recipes, List<Ingredient> pantry) {
        // Total pantry stock per ingredient (same item added twice is summed).
        Map<String, Double> stock = new HashMap<>();
        // Add up each pantry item into the stock totals
        for (Ingredient p : pantry) {
            String key = keyOf(p);
            double current = stock.containsKey(key) ? stock.get(key) : 0.0;
            stock.put(key, current + toBase(p.getQuantity(), p.getUnit()));
        }

        // Recipes that can be made are collected here
        List<Recipe> matches = new ArrayList<>();
        // Check every recipe one by one
        for (Recipe r : recipes) {
            // Assume the recipe works until an ingredient is missing
            boolean canMake = true;
            // Check each ingredient the recipe needs
            for (Ingredient needed : r.getIngredients()) {
                // Look up how much the user has of it
                Double have = stock.get(keyOf(needed));
                // Work out how much the recipe needs
                double need = toBase(needed.getQuantity(), needed.getUnit());
                if (have == null || have + 0.0001 < need) {
                    canMake = false;   // one missing/insufficient ingredient = excluded
                    break;
                }
            }
            // Keep the recipe only if everything is available
            if (canMake) matches.add(r);
        }
        // Give back the recipes that can be made
        return matches;
    }


    public static String fmt(double q) {
        // Whole numbers drop the decimal part
        return (q == Math.floor(q)) ? String.valueOf((long) q) : String.valueOf(q);
    }
}
