package com.example.smartpantrymanager.ui.recipes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.data.relation.RecipeWithIngredients;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.ui.pantry.PantryAdapter;

/**
 * One RecyclerView, three kinds of row:
 *   TYPE_HEADER - section title ("Ready to cook", "Almost there")
 *   TYPE_RECIPE - a strict match
 *   TYPE_ALMOST - a recipe missing exactly one ingredient
 * getItemViewType() tells RecyclerView which layout/ViewHolder each row needs.
 */
public class RecipeAdapter extends ListAdapter<RecipeAdapter.Row, RecyclerView.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(long recipeId);
    }

    static final int TYPE_HEADER = 0;
    static final int TYPE_RECIPE = 1;
    static final int TYPE_ALMOST = 2;

    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // ======================================================================
    // Row models - what each position in the list represents
    // ======================================================================

    public abstract static class Row {
        abstract int viewType();
        /** Identity for DiffUtil: "is this the same row?" */
        abstract String key();
        /** Contents for DiffUtil: "does it look the same?" */
        abstract boolean sameContentAs(Row other);
    }

    public static final class HeaderRow extends Row {
        private final String id;
        final String title;
        final String subtitle;

        public HeaderRow(String id, String title, String subtitle) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
        }

        @Override int viewType() { return TYPE_HEADER; }
        @Override String key() { return "header:" + id; }
        @Override boolean sameContentAs(Row other) {
            HeaderRow o = (HeaderRow) other;
            return title.equals(o.title) && subtitle.equals(o.subtitle);
        }
    }

    public static final class RecipeRow extends Row {
        final RecipeWithIngredients recipe;

        public RecipeRow(RecipeWithIngredients recipe) { this.recipe = recipe; }

        @Override int viewType() { return TYPE_RECIPE; }
        @Override String key() { return "recipe:" + recipe.recipe.getId(); }
        @Override boolean sameContentAs(Row other) {
            return recipe.recipe.getName().equals(((RecipeRow) other).recipe.recipe.getName());
        }
    }

    public static final class AlmostRow extends Row {
        final RecipeMatcher.AlmostThere almost;

        public AlmostRow(RecipeMatcher.AlmostThere almost) { this.almost = almost; }

        @Override int viewType() { return TYPE_ALMOST; }
        @Override String key() { return "almost:" + almost.recipe.recipe.getId(); }
        @Override boolean sameContentAs(Row other) {
            // Same recipe, but the missing ingredient might have changed
            return almost.missingIngredient.getId()
                    == ((AlmostRow) other).almost.missingIngredient.getId();
        }
    }

    private static final DiffUtil.ItemCallback<Row> DIFF_CALLBACK = new DiffUtil.ItemCallback<Row>() {
        @Override
        public boolean areItemsTheSame(@NonNull Row a, @NonNull Row b) {
            return a.key().equals(b.key());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Row a, @NonNull Row b) {
            return a.sameContentAs(b);   // only called when keys match, so types match too
        }
    };

    // ======================================================================
    // RecyclerView callbacks
    // ======================================================================

    @Override
    public int getItemViewType(int position) {
        return getItem(position).viewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        // TYPE_RECIPE and TYPE_ALMOST share a layout; ALMOST just shows the extra "missing" line
        return new RecipeViewHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = getItem(position);
        switch (row.viewType()) {
            case TYPE_HEADER:
                ((HeaderViewHolder) holder).bind((HeaderRow) row);
                break;
            case TYPE_RECIPE:
                ((RecipeViewHolder) holder).bind(((RecipeRow) row).recipe, null);
                break;
            case TYPE_ALMOST:
                AlmostRow almostRow = (AlmostRow) row;
                ((RecipeViewHolder) holder).bind(almostRow.almost.recipe,
                        almostRow.almost.missingIngredient);
                break;
        }
    }

    // ======================================================================
    // ViewHolders
    // ======================================================================

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_header_title);
            subtitle = itemView.findViewById(R.id.text_header_subtitle);
        }

        void bind(HeaderRow row) {
            title.setText(row.title);
            subtitle.setText(row.subtitle);
        }
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView meta;
        private final TextView description;
        private final TextView missing;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_recipe_name);
            meta = itemView.findViewById(R.id.text_recipe_meta);
            description = itemView.findViewById(R.id.text_recipe_description);
            missing = itemView.findViewById(R.id.text_recipe_missing);
        }

        /** @param missingIngredient null for strict matches; the one missing item for "almost" rows */
        void bind(RecipeWithIngredients r, @Nullable RecipeIngredient missingIngredient) {
            Context ctx = itemView.getContext();
            int count = r.ingredients.size();

            name.setText(r.recipe.getName());
            meta.setText(ctx.getResources().getQuantityString(
                    R.plurals.recipe_meta, count, count, r.recipe.getPrepMinutes()));
            description.setText(r.recipe.getDescription());

            if (missingIngredient == null) {
                missing.setVisibility(View.GONE);      // must reset - ViewHolders are recycled
            } else {
                missing.setVisibility(View.VISIBLE);
                missing.setText(ctx.getString(R.string.missing_ingredient,
                        PantryAdapter.formatQuantity(missingIngredient.getQuantity()),
                        missingIngredient.getUnit(),
                        missingIngredient.getName()));
            }

            itemView.setOnClickListener(v -> listener.onRecipeClick(r.recipe.getId()));
        }
    }
}
