package com.example.smartpantrymanager.ui.recipes;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.dao.PantryItemDao;
import com.example.smartpantrymanager.data.dao.RecipeDao;
import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.data.relation.RecipeWithIngredients;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.ui.pantry.PantryAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shows one recipe in full. The recipe id arrives via an Intent extra.
 * Each ingredient is ticked or crossed against the user's CURRENT pantry,
 * using the same RecipeMatcher rule as the Suggested Recipes screen.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra key: the id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "com.example.smartpantrymanager.EXTRA_RECIPE_ID";

    private static final long NO_ID = -1;

    private RecipeDao recipeDao;
    private PantryItemDao pantryDao;
    private long recipeId;

    private View content;
    private TextView metaText, descriptionText, statusText, stepsText;
    private MaterialCardView statusCard;
    private LinearLayout ingredientsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        content = findViewById(R.id.content);
        metaText = findViewById(R.id.text_detail_meta);
        descriptionText = findViewById(R.id.text_detail_description);
        statusCard = findViewById(R.id.card_status);
        statusText = findViewById(R.id.text_status);
        ingredientsContainer = findViewById(R.id.container_ingredients);
        stepsText = findViewById(R.id.text_steps);

        AppDatabase db = AppDatabase.getInstance(this);
        recipeDao = db.recipeDao();
        pantryDao = db.pantryItemDao();

        // Read which recipe to show from the Intent that started this screen
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, NO_ID);
        if (recipeId == NO_ID) {
            showNotFound();
            return;
        }
        loadRecipe();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ======================================================================
    // Load
    // ======================================================================

    private void loadRecipe() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            RecipeWithIngredients recipe = recipeDao.getById(recipeId);
            List<PantryItem> pantry = pantryDao.getAll();
            List<RecipeIngredient> missing = recipe == null
                    ? null
                    : RecipeMatcher.missingIngredients(recipe, pantry);

            runOnUiThread(() -> show(recipe, missing));
        });
    }

    private void show(@Nullable RecipeWithIngredients r, @Nullable List<RecipeIngredient> missing) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (r == null || missing == null) {
            showNotFound();
            return;
        }

        setTitle(r.recipe.getName());

        int count = r.ingredients.size();
        metaText.setText(getResources().getQuantityString(
                R.plurals.recipe_meta, count, count, r.recipe.getPrepMinutes()));
        descriptionText.setText(r.recipe.getDescription());
        stepsText.setText(r.recipe.getSteps());

        showStatus(missing.size());
        showIngredients(r.ingredients, missing);

        content.setVisibility(View.VISIBLE);
    }

    private void showNotFound() {
        Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
        finish();
    }

    // ======================================================================
    // Status banner
    // ======================================================================

    private void showStatus(int missingCount) {
        int color;
        if (missingCount == 0) {
            statusText.setText(R.string.status_can_make);
            color = ContextCompat.getColor(this, R.color.status_have);
        } else {
            statusText.setText(getResources().getQuantityString(
                    R.plurals.status_missing, missingCount, missingCount));
            color = ContextCompat.getColor(this, R.color.expiry_expired);
        }
        statusText.setTextColor(color);
        statusCard.setStrokeColor(ColorStateList.valueOf(color));
    }

    // ======================================================================
    // Ingredient list
    // ======================================================================

    /**
     * The ingredient list is short and fixed, so rows are inflated straight
     * into a LinearLayout. A RecyclerView would add recycling overhead with
     * no benefit for ~5-8 rows inside a scrolling page.
     */
    private void showIngredients(List<RecipeIngredient> ingredients, List<RecipeIngredient> missing) {
        // Use ids in a Set for fast "is this one missing?" checks
        Set<Long> missingIds = new HashSet<>();
        for (RecipeIngredient m : missing) {
            missingIds.add(m.getId());
        }

        int haveColor = ContextCompat.getColor(this, R.color.status_have);
        int missingColor = ContextCompat.getColor(this, R.color.expiry_expired);

        ingredientsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (RecipeIngredient ingredient : ingredients) {
            View row = inflater.inflate(R.layout.item_ingredient_line, ingredientsContainer, false);
            TextView icon = row.findViewById(R.id.text_ingredient_icon);
            TextView text = row.findViewById(R.id.text_ingredient);

            boolean isMissing = missingIds.contains(ingredient.getId());
            icon.setText(isMissing ? "✗" : "✓");
            icon.setTextColor(isMissing ? missingColor : haveColor);
            icon.setContentDescription(getString(
                    isMissing ? R.string.cd_ingredient_missing : R.string.cd_ingredient_have));

            text.setText(getString(R.string.ingredient_line,
                    PantryAdapter.formatQuantity(ingredient.getQuantity()),
                    ingredient.getUnit(),
                    ingredient.getName()));

            ingredientsContainer.addView(row);
        }
    }
}
