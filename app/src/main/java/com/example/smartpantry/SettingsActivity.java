package com.example.smartpantry;


// this screen lets the user change app settings
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.switchmaterial.SwitchMaterial;

// settings that show expiring soon alerts on/off, referred units: metric or imperial and option to clear the pantry
public class SettingsActivity extends BaseActivity {

    // Names used to save and read the settings
    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_UNITS = "units";            // "metric" or "imperial"

    // read by other screens
    public static boolean alertsEnabled(Context c) {
        // Alerts are on unless the user turned them off
        return c.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static boolean isImperial(Context c) {
        // True if the user chose imperial units
        return "imperial".equals(c.getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_UNITS, "metric"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the settings layout
        setContentView(R.layout.activity_settings);
        // Set the screen title
        setTitle("Settings");

        // Open the saved settings
        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Expiring soon alerts
        SwitchMaterial alertSwitch = findViewById(R.id.switch_expiry);
        // Show the saved switch state
        alertSwitch.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        // Save the switch whenever it changes
        alertSwitch.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());

        // Preferred units
        RadioGroup unitGroup = findViewById(R.id.radio_units);
        // Select the saved unit choice
        unitGroup.check(isImperial(this) ? R.id.radio_imperial : R.id.radio_metric);
        // Save the unit choice when it changes
        unitGroup.setOnCheckedChangeListener((group, checkedId) ->
                prefs.edit().putString(KEY_UNITS,
                        checkedId == R.id.radio_imperial ? "imperial" : "metric").apply());

        // Clear pantry
        Button clear = findViewById(R.id.btn_clear_pantry);
        // Ask for confirmation before clearing
        clear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clear pantry?")
                .setMessage("This removes every ingredient from your pantry.")
                .setPositiveButton("Clear", (d, w) -> {
                    // Delete every pantry item
                    new DatabaseHelper(this).deleteAllIngredients();
                    Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());

        // Turn on the bottom navigation bar
        setupBottomNav(R.id.nav_settings);
    }
}
