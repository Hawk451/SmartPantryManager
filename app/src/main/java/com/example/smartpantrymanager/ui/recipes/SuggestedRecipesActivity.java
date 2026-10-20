package com.example.smartpantrymanager.ui.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.dao.PantryItemDao;
import com.example.smartpantrymanager.data.dao.RecipeDao;
import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.data.relation.RecipeWithIngredients;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.ui.common.BottomNavHelper;
import com.example.smartpantrymanager.ui.pantry.PantryListActivity;
import com.example.smartpantrymanager.util.AppPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs the strict-matching rule against the current pantry and shows:
 *   1. "Ready to cook" - recipes where EVERY ingredient is available (strict)
 *   2. "Almost there"  - missing exactly one (bonus; can be hidden in Settings)
 *   3. An empty-state message when nothing qualifies
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private static final String TAG = "SuggestedRecipes";

    private RecipeDao recipeDao;
    private PantryItemDao pantryDao;
    private RecipeAdapter adapter;

    private RecyclerView recycler;
    private View emptyState;
    private View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppDatabase db = AppDatabase.getInstance(this);
        recipeDao = db.recipeDao();
        pantryDao = db.pantryItemDao();

        recycler = findViewById(R.id.recycler_recipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);

        emptyState = findViewById(R.id.empty_state);
        progress = findViewById(R.id.progress);

        findViewById(R.id.button_go_to_pantry).setOnClickListener(v -> openPantry());

        BottomNavHelper.setup(this, findViewById(R.id.bottom_nav), R.id.nav_recipes);
    }

    /**
     * Re-run matching every time the screen is shown - the user may have just
     * added or removed an ingredient on the Pantry screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        boolean showAlmostThere = AppPrefs.isShowAlmostThere(this);
        if (adapter.getItemCount() == 0) {
            progress.setVisibility(View.VISIBLE);   // spinner on first load only - avoids flicker
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Background thread: two DB reads + the matching algorithm
            List<RecipeWithIngredients> recipes = recipeDao.getAllWithIngredients();
            List<PantryItem> pantry = pantryDao.getAll();
            RecipeMatcher.MatchResult result = RecipeMatcher.match(recipes, pantry);

            Log.d(TAG, "Matched " + result.canMake.size() + " of " + recipes.size()
                    + " recipes against " + pantry.size() + " pantry items");

            runOnUiThread(() -> showResult(result, showAlmostThere));
        });
    }

    private void showResult(RecipeMatcher.MatchResult result, boolean showAlmostThere) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        progress.setVisibility(View.GONE);

        boolean hasStrict = !result.canMake.isEmpty();
        boolean hasAlmost = showAlmostThere && !result.almostThere.isEmpty();

        // Nothing at all to show -> full-screen feedback instead of a blank list
        if (!hasStrict && !hasAlmost) {
            adapter.submitList(Collections.<RecipeAdapter.Row>emptyList());
            recycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        List<RecipeAdapter.Row> rows = new ArrayList<>();

        // Section 1: strict matches (always shown first, clearly labelled)
        rows.add(new RecipeAdapter.HeaderRow("can_make",
                getString(R.string.header_can_make, result.canMake.size()),
                getString(hasStrict ? R.string.header_can_make_sub : R.string.header_can_make_none)));
        for (RecipeWithIngredients r : result.canMake) {
            rows.add(new RecipeAdapter.RecipeRow(r));
        }

        // Section 2: "Almost there" - visually separate, never mixed into section 1
        if (hasAlmost) {
            rows.add(new RecipeAdapter.HeaderRow("almost",
                    getString(R.string.header_almost, result.almostThere.size()),
                    getString(R.string.header_almost_sub)));
            for (RecipeMatcher.AlmostThere a : result.almostThere) {
                rows.add(new RecipeAdapter.AlmostRow(a));
            }
        }

        adapter.submitList(rows);
    }

    /** Intent with an extra: tell the detail screen WHICH recipe to show. */
    @Override
    public void onRecipeClick(long recipeId) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    private void openPantry() {
        Intent intent = new Intent(this, PantryListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
