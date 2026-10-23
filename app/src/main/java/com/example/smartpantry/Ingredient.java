package com.example.smartpantry;


// this class stores the details of one pantry ingredient
public class Ingredient {
    // Fields that describe one ingredient
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiry; // yyyy-MM-dd, may be empty (pantry items only)

    // Builds an ingredient from its values
    public Ingredient(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }

    // Simple getters to read the fields
    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiry() { return expiry; }
}
