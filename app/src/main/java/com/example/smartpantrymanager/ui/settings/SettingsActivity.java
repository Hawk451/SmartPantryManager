package com.example.smartpantrymanager.ui.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.dao.PantryItemDao;
import com.example.smartpantrymanager.data.dao.RecipeDao;
import com.example.smartpantrymanager.ui.common.BottomNavHelper;
import com.example.smartpantrymanager.util.AppPrefs;

/**
 * Settings screen.
 *  - Preferences are stored in SharedPreferences (via AppPrefs), because they
 *    are simple key/value flags - not relational data that belongs in SQLite.
 *  - Also shows how much data is stored and offers "Clear pantry".
 */
public class SettingsActivity extends AppCompatActivity {

    private PantryItemDao pantryDao;
    private RecipeDao recipeDao;

    private MaterialSwitch expirySwitch;
    private MaterialSwitch almostThereSwitch;
    private TextView statsText;
    private MaterialButton clearPantryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppDatabase db = AppDatabase.getInstance(this);
        pantryDao = db.pantryItemDao();
        recipeDao = db.recipeDao();

        expirySwitch = findViewById(R.id.switch_expiry_alerts);
        almostThereSwitch = findViewById(R.id.switch_almost_there);
        statsText = findViewById(R.id.text_stats);
        clearPantryButton = findViewById(R.id.button_clear_pantry);
        TextView versionText = findViewById(R.id.text_version);

        // Set the saved values FIRST, then attach listeners - otherwise setting
        // the initial state would trigger a pointless save.
        expirySwitch.setChecked(AppPrefs.isExpiryAlertsEnabled(this));
        almostThereSwitch.setChecked(AppPrefs.isShowAlmostThere(this));

        expirySwitch.setOnCheckedChangeListener((button, isChecked) ->
                AppPrefs.setExpiryAlertsEnabled(this, isChecked));
        almostThereSwitch.setOnCheckedChangeListener((button, isChecked) ->
                AppPrefs.setShowAlmostThere(this, isChecked));

        clearPantryButton.setOnClickListener(v -> confirmClearPantry());

        versionText.setText(getString(R.string.settings_version, appVersionName()));

        BottomNavHelper.setup(this, findViewById(R.id.bottom_nav), R.id.nav_settings);
    }

    /** Refresh the counts every time the screen is shown (the pantry may have changed). */
    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    // ======================================================================
    // Data stats
    // ======================================================================

    private void loadStats() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int pantryCount = pantryDao.count();
            int recipeCount = recipeDao.count();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                statsText.setText(getResources().getQuantityString(
                        R.plurals.settings_stats, pantryCount, pantryCount, recipeCount));
                clearPantryButton.setEnabled(pantryCount > 0);   // nothing to clear
            });
        });
    }

    // ======================================================================
    // Clear pantry (bulk DELETE)
    // ======================================================================

    private void confirmClearPantry() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_pantry_title)
                .setMessage(R.string.clear_pantry_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.settings_clear_pantry, (dialog, which) -> clearPantry())
                .show();
    }

    private void clearPantry() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int removed = pantryDao.deleteAll();
            runOnUiThread(() -> {
                Snackbar.make(findViewById(R.id.settings_root),
                                getResources().getQuantityString(
                                        R.plurals.pantry_cleared, removed, removed),
                                Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.bottom_nav)
                        .show();
                loadStats();
            });
        });
    }

    // ======================================================================
    // Helpers
    // ======================================================================

    /** Reads the versionName from build.gradle via the PackageManager. */
    @SuppressWarnings("deprecation")
    private String appVersionName() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName != null ? info.versionName : "1.0";
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}
