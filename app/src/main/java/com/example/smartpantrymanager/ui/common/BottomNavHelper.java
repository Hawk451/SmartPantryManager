package com.example.smartpantrymanager.ui.common;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.ui.pantry.PantryListActivity;
import com.example.smartpantrymanager.ui.recipes.SuggestedRecipesActivity;
import com.example.smartpantrymanager.ui.settings.SettingsActivity;

/**
 * Shared bottom-navigation behaviour for the three top-level screens.
 * Each screen calls setup() with its own menu item id.
 */
public final class BottomNavHelper {

    private BottomNavHelper() { }

    @SuppressWarnings("deprecation")   // overridePendingTransition is fine for our minSdk
    public static void setup(Activity activity, BottomNavigationView nav, int currentItemId) {
        nav.setSelectedItemId(currentItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true;                          // already here - do nothing
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = PantryListActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else if (id == R.id.nav_settings) {
                target = SettingsActivity.class;
            } else {
                return false;
            }

            Intent intent = new Intent(activity, target);
            // Re-use the existing screen if it's already open instead of stacking duplicates
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0); // instant switch, feels like tabs

            // Return false so THIS screen keeps its own tab highlighted
            // if the user comes back to it later.
            return false;
        });
    }
}
