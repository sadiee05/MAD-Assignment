package com.example.smartpantry;


// this screen adds or edits a pantry ingredient
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    // Units the user can choose from
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    // The database and the input fields
    private DatabaseHelper db;
    private EditText nameInput, quantityInput, expiryInput;
    private TextView expiryError;
    private Spinner unitSpinner;
    // -1 means a new ingredient is being added
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the add/edit layout
        setContentView(R.layout.activity_add_edit);
        // Show a back arrow in the top bar
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Open the database and find the input fields
        db = new DatabaseHelper(this);
        nameInput = findViewById(R.id.et_name);
        quantityInput = findViewById(R.id.et_quantity);
        expiryInput = findViewById(R.id.et_expiry);
        expiryError = findViewById(R.id.tv_expiry_error);
        unitSpinner = findViewById(R.id.spinner_unit);
        Button save = findViewById(R.id.btn_save);

        // Fill the unit dropdown
        unitSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        // Did the Intent carry an ingredient id? Then we are editing.
        editId = getIntent().getLongExtra(PantryActivity.EXTRA_INGREDIENT_ID, -1);
        if (editId != -1) {
            setTitle("Edit Ingredient");
            // Load the saved ingredient
            Ingredient existing = db.getIngredient(editId);
            if (existing != null) {
                // Fill the fields with the saved values
                nameInput.setText(existing.getName());
                quantityInput.setText(MatchEngine.fmt(existing.getQuantity()));
                expiryInput.setText(existing.getExpiry());
                // Select the saved unit in the dropdown
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equals(existing.getUnit())) unitSpinner.setSelection(i);
                }
            }
        } else {
            setTitle("Add Ingredient");
        }

        // Tapping the date field opens the date picker
        expiryInput.setOnClickListener(v -> showDatePicker());
        // Tapping Save checks the input and stores the ingredient
        save.setOnClickListener(v -> saveIngredient());
    }

    /** Date picker that does not allow past dates. */
    private void showDatePicker() {
        // Start the picker on today's date
        Calendar c = Calendar.getInstance();
        // If a valid future date is already typed, start there
        Date current = ExpiryUtil.parse(expiryInput.getText().toString());
        if (current != null && current.getTime() >= ExpiryUtil.startOfToday()) {
            c.setTime(current);
        }
        // Write the chosen date into the field as yyyy-MM-dd
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            expiryInput.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d));
            expiryError.setVisibility(View.GONE);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        // Block dates before today
        dialog.getDatePicker().setMinDate(ExpiryUtil.startOfToday());
        // Display the picker
        dialog.show();
    }

    private void showExpiryError(String message) {
        // Show the error message under the date field
        expiryError.setText(message);
        expiryError.setVisibility(View.VISIBLE);
    }

    private void saveIngredient() {
        // Read what the user typed
        String name = nameInput.getText().toString().trim();
        String qtyText = quantityInput.getText().toString().trim();
        String expiry = expiryInput.getText().toString().trim();

        // Input validation
        // Name is required
        if (name.isEmpty()) {
            nameInput.setError("Please enter an ingredient name");
            return;
        }
        // Quantity must be a valid number
        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            quantityInput.setError("Enter a valid number");
            return;
        }
        // Quantity must be above zero
        if (qty <= 0) {
            quantityInput.setError("Quantity must be greater than 0");
            return;
        }
        // Expiry date is required
        if (expiry.isEmpty()) {
            showExpiryError("Please choose an expiry date");
            return;
        }
        // The date must be a real date
        if (ExpiryUtil.parse(expiry) == null) {
            showExpiryError("That is not a valid date");
            return;
        }
        // The date cannot be in the past
        if (ExpiryUtil.daysUntil(expiry) < 0) {
            showExpiryError("The expiry date cannot be in the past");
            return;
        }
        expiryError.setVisibility(View.GONE);

        // Read the chosen unit
        String unit = unitSpinner.getSelectedItem().toString();

        // Duplicate check
        // Look for another item with the same name
        Ingredient dup = db.findDuplicate(name, editId);
        if (dup != null) {
            // Check that both units measure the same kind of thing
            boolean sameKind = MatchEngine.dimension(dup.getUnit()).equals(MatchEngine.dimension(unit));
            // Offer a merge for new items, otherwise block the duplicate
            if (editId == -1 && sameKind) {
                offerMerge(dup, qty, unit, expiry);   // e.g. 500 g rice + 200 g rice
            } else {
                nameInput.setError("\"" + dup.getName() + "\" is already in your pantry. "
                        + "Edit that item instead.");
            }
            return;
        }

        if (editId == -1) {
            // Save a new ingredient
            db.addIngredient(name, qty, unit, expiry);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            // Save changes to the existing ingredient
            db.updateIngredient(editId, name, qty, unit, expiry);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // back to the pantry list
    }

    // if the ingredient already exists
    private void offerMerge(final Ingredient existing, final double qty, final String unit,
                            final String expiry) {
        // Ask whether to merge the amounts
        new AlertDialog.Builder(this)
                .setTitle("Already in your pantry")
                .setMessage("You already have " + MatchEngine.fmt(existing.getQuantity()) + " "
                        + existing.getUnit() + " of " + existing.getName() + ".\n\n"
                        + "Add this amount to it? The earlier expiry date is kept so nothing gets wasted.")
                .setPositiveButton("Merge", (d, w) -> {
                    // Convert the new amount into the existing item's unit, then add it
                    double added = MatchEngine.toBase(qty, unit) / MatchEngine.toBase(1, existing.getUnit());
                    double total = Math.round((existing.getQuantity() + added) * 100) / 100.0;

                    // Keep whichever expiry date is earlier
                    String oldExpiry = existing.getExpiry();
                    String keep = (oldExpiry == null || oldExpiry.isEmpty() || expiry.compareTo(oldExpiry) < 0)
                            ? expiry : oldExpiry;   // yyyy-MM-dd strings compare correctly

                    // Save the merged amount
                    db.updateIngredient(existing.getId(), existing.getName(), total, existing.getUnit(), keep);
                    Toast.makeText(this, "Quantities merged", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
