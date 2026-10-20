package com.example.smartpantrymanager.ui.pantry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.dao.PantryItemDao;
import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.logic.IngredientNormalizer;
import com.example.smartpantrymanager.logic.PantryItemValidator;
import com.example.smartpantrymanager.logic.PantryItemValidator.NameError;
import com.example.smartpantrymanager.logic.PantryItemValidator.QuantityError;
import com.example.smartpantrymanager.logic.UnitConverter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * One form for both CREATE and UPDATE.
 *  - Started WITHOUT EXTRA_ITEM_ID -> "Add" mode, inserts a new row.
 *  - Started WITH EXTRA_ITEM_ID    -> "Edit" mode, loads that row and updates it.
 */
public class AddEditItemActivity extends AppCompatActivity {

    /** Intent extra key: present = edit that item, absent = add a new item. */
    public static final String EXTRA_ITEM_ID = "com.example.smartpantrymanager.EXTRA_ITEM_ID";

    private static final long NO_ID = -1;
    private static final String STATE_EXPIRY = "state_expiry";
    private static final String TAG_DATE_PICKER = "expiry_date_picker";

    private TextInputLayout nameLayout, quantityLayout, unitLayout, expiryLayout;
    private TextInputEditText nameEdit, quantityEdit, expiryEdit;
    private MaterialAutoCompleteTextView unitDropdown;
    private MaterialButton saveButton;

    private PantryItemDao pantryDao;
    private long editingItemId = NO_ID;

    /** Selected expiry as local-midnight millis, or null for "no expiry". */
    @Nullable
    private Long expiryMillis;

    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    // ======================================================================
    // Lifecycle
    // ======================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // back arrow
        }

        bindViews();
        pantryDao = AppDatabase.getInstance(this).pantryItemDao();

        // Dropdown of allowed units. setSimpleItems uses a non-filtering adapter,
        // so all units stay visible even after a screen rotation.
        unitDropdown.setSimpleItems(UnitConverter.DISPLAY_UNITS.toArray(new String[0]));

        // Read the Intent to decide which mode we're in
        editingItemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);
        boolean isEditMode = editingItemId != NO_ID;
        setTitle(isEditMode ? R.string.title_edit_item : R.string.title_add_item);

        setUpListeners();

        if (savedInstanceState != null) {
            // Screen was rotated: EditTexts restore their own text automatically,
            // but our expiryMillis field must be restored manually.
            expiryMillis = savedInstanceState.containsKey(STATE_EXPIRY)
                    ? savedInstanceState.getLong(STATE_EXPIRY) : null;
            showExpiry();
            reattachDatePickerIfOpen();
        } else if (isEditMode) {
            loadItem();
        } else {
            unitDropdown.setText(UnitConverter.DISPLAY_UNITS.get(0), false);   // default "g"
            showExpiry();
        }
    }

    /** Saves state that Android won't save for us (called before rotation/process death). */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (expiryMillis != null) {
            outState.putLong(STATE_EXPIRY, expiryMillis);
        }
    }

    /** Toolbar back arrow simply closes this screen and returns to the list. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ======================================================================
    // Setup
    // ======================================================================

    private void bindViews() {
        nameLayout = findViewById(R.id.layout_name);
        quantityLayout = findViewById(R.id.layout_quantity);
        unitLayout = findViewById(R.id.layout_unit);
        expiryLayout = findViewById(R.id.layout_expiry);
        nameEdit = findViewById(R.id.edit_name);
        quantityEdit = findViewById(R.id.edit_quantity);
        expiryEdit = findViewById(R.id.edit_expiry);
        unitDropdown = findViewById(R.id.dropdown_unit);
        saveButton = findViewById(R.id.button_save);
    }

    private void setUpListeners() {
        expiryEdit.setOnClickListener(v -> showDatePicker());
        expiryLayout.setEndIconOnClickListener(v -> setExpiry(null));
        saveButton.setOnClickListener(v -> attemptSave());

        // Clear an error as soon as the user starts fixing it
        clearErrorWhenEdited(nameEdit, nameLayout);
        clearErrorWhenEdited(quantityEdit, quantityLayout);
        unitDropdown.setOnItemClickListener((parent, view, position, id) -> unitLayout.setError(null));
    }

    private static void clearErrorWhenEdited(EditText edit, TextInputLayout layout) {
        edit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { layout.setError(null); }
        });
    }

    // ======================================================================
    // Edit mode: READ the existing item
    // ======================================================================

    private void loadItem() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PantryItem item = pantryDao.getById(editingItemId);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;                                   // user left while we were loading
                }
                if (item == null) {
                    Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                nameEdit.setText(item.getName());
                quantityEdit.setText(PantryAdapter.formatQuantity(item.getQuantity()));
                unitDropdown.setText(item.getUnit(), false);   // false = don't filter the list
                setExpiry(item.getExpiryDate());
            });
        });
    }

    // ======================================================================
    // Expiry date picker
    // ======================================================================

    private void showDatePicker() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_DATE_PICKER) != null) {
            return;                                           // already open - ignore double taps
        }
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())   // no past dates
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_expiry)
                .setSelection(expiryMillis != null
                        ? localMidnightToUtc(expiryMillis)
                        : MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        attachPickerListener(picker);
        picker.show(getSupportFragmentManager(), TAG_DATE_PICKER);
    }

    private void attachPickerListener(MaterialDatePicker<Long> picker) {
        picker.addOnPositiveButtonClickListener(selection ->
                setExpiry(utcToLocalMidnight(selection)));
    }

    /**
     * The picker (a Fragment) survives rotation, but its listener doesn't -
     * so if it was open, find it again and re-attach our listener.
     */
    @SuppressWarnings("unchecked")
    private void reattachDatePickerIfOpen() {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(TAG_DATE_PICKER);
        if (existing instanceof MaterialDatePicker) {
            attachPickerListener((MaterialDatePicker<Long>) existing);
        }
    }

    private void setExpiry(@Nullable Long millis) {
        expiryMillis = millis;
        showExpiry();
    }

    private void showExpiry() {
        expiryEdit.setText(expiryMillis == null ? "" : dateFormat.format(new Date(expiryMillis)));
        expiryLayout.setEndIconVisible(expiryMillis != null);   // only show X when there's a date
    }

    /** MaterialDatePicker works in UTC; convert its date to midnight in the phone's time zone. */
    static long utcToLocalMidnight(long utcMillis) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(utcMillis);
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
        return local.getTimeInMillis();
    }

    /** Reverse of the above, for pre-selecting a stored date in the picker. */
    static long localMidnightToUtc(long localMillis) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(localMillis);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis();
    }

    // ======================================================================
    // Validate + CREATE / UPDATE
    // ======================================================================

    private void attemptSave() {
        String rawName = textOf(nameEdit);
        String rawQuantity = textOf(quantityEdit);
        String unit = unitDropdown.getText().toString().trim();

        // Check ALL fields so the user sees every problem at once, not one at a time
        boolean valid = true;

        NameError nameError = PantryItemValidator.validateName(rawName);
        nameLayout.setError(messageFor(nameError));
        if (nameError != NameError.NONE) valid = false;

        QuantityError quantityError = PantryItemValidator.validateQuantity(rawQuantity);
        quantityLayout.setError(messageFor(quantityError));
        if (quantityError != QuantityError.NONE) valid = false;

        if (!UnitConverter.isKnownUnit(unit)) {
            unitLayout.setError(getString(R.string.error_unit_required));
            valid = false;
        } else {
            unitLayout.setError(null);
        }

        if (!valid) {
            return;
        }

        // Inputs are valid - build the entity
        String name = rawName.trim().replaceAll("\\s+", " ");
        double quantity = PantryItemValidator.parseQuantity(rawQuantity);
        PantryItem item = new PantryItem(
                name, IngredientNormalizer.normalize(name), quantity, unit, expiryMillis);

        saveButton.setEnabled(false);   // stop double-taps creating duplicate rows

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (editingItemId == NO_ID) {
                pantryDao.insert(item);                 // CREATE
            } else {
                item.setId(editingItemId);              // same id -> Room updates that row
                pantryDao.update(item);                 // UPDATE
            }
            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.item_saved, name), Toast.LENGTH_SHORT).show();
                finish();   // back to the list; its onResume() reloads the data
            });
        });
    }

    private static String textOf(EditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString();
    }

    @Nullable
    private String messageFor(NameError error) {
        switch (error) {
            case REQUIRED:           return getString(R.string.error_name_required);
            case TOO_LONG:           return getString(R.string.error_name_too_long);
            case INVALID_CHARACTERS: return getString(R.string.error_name_invalid);
            default:                 return null;   // null clears the error
        }
    }

    @Nullable
    private String messageFor(QuantityError error) {
        switch (error) {
            case REQUIRED:     return getString(R.string.error_quantity_required);
            case NOT_A_NUMBER: return getString(R.string.error_quantity_invalid);
            case NOT_POSITIVE: return getString(R.string.error_quantity_positive);
            case TOO_LARGE:    return getString(R.string.error_quantity_too_large);
            default:           return null;
        }
    }
}
