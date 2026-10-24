package com.example.smartpantry;


// this class formats ingredient quantities in the selected unit system
//display-only unit conversion for the Metric / Imperial setting.
public class UnitFormatter {

    // Conversion factors between metric and imperial
    private static final double G_PER_OZ = 28.3495;
    private static final double ML_PER_FL_OZ = 29.5735;
    private static final double ML_PER_CUP = 236.588;

    // Turns a quantity into text in the chosen unit system
    public static String format(double qty, String unit, boolean imperial) {
        double base = MatchEngine.toBase(qty, unit);   // g, ml or count
        // Convert differently for mass, volume or counts
        switch (MatchEngine.dimension(unit)) {
            case "mass":
                // Imperial mass: show ounces, or pounds once large
                if (imperial) {
                    double oz = base / G_PER_OZ;
                    return oz >= 16 ? num(oz / 16) + " lb" : num(oz) + " oz";
                }
                // Metric mass: show grams, or kg once large
                return base >= 1000 ? num(base / 1000) + " kg" : num(base) + " g";

            case "volume":
                // Imperial volume: show fl oz, or cups once large
                if (imperial) {
                    double flOz = base / ML_PER_FL_OZ;
                    return flOz >= 8 ? num(base / ML_PER_CUP) + " cup" : num(flOz) + " fl oz";
                }
                // Metric volume: show ml, or litres once large
                return base >= 1000 ? num(base / 1000) + " l" : num(base) + " ml";

            default:
                return num(qty) + " " + unit;   // counts (pcs) never change
        }
    }

    // rounds to 1 decimal place
    private static String num(double v) {
        // Round, then format the number
        return MatchEngine.fmt(Math.round(v * 10) / 10.0);
    }
}
