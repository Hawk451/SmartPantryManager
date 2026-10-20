package com.example.smartpantrymanager.ui.pantry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.entity.PantryItem;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Custom RecyclerView adapter for the pantry list.
 * Extends ListAdapter so DiffUtil works out exactly which rows changed,
 * instead of redrawing the whole list on every update.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    /** The Activity implements this so the adapter can report clicks without knowing about screens. */
    public interface OnItemActionListener {
        void onItemClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    private static final int EXPIRY_WARNING_DAYS = 3;
    private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

    private final OnItemActionListener listener;
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private boolean expiryAlertsEnabled = true;

    public PantryAdapter(OnItemActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /** Called from the Activity when the Settings toggle changes. */
    @SuppressLint("NotifyDataSetChanged")   // every row's colouring depends on this flag
    public void setExpiryAlertsEnabled(boolean enabled) {
        if (expiryAlertsEnabled != enabled) {
            expiryAlertsEnabled = enabled;
            notifyDataSetChanged();
        }
    }

    // ---------- DiffUtil: how to compare old vs new list ----------
    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PantryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
                    return a.getId() == b.getId();                 // same database row?
                }

                @Override
                public boolean areContentsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
                    return a.getName().equals(b.getName())         // anything visible changed?
                            && a.getQuantity() == b.getQuantity()
                            && a.getUnit().equals(b.getUnit())
                            && Objects.equals(a.getExpiryDate(), b.getExpiryDate());
                }
            };

    // ---------- RecyclerView callbacks ----------
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // ---------- ViewHolder: caches the views for one row ----------
    class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView quantityText;
        private final TextView expiryText;
        private final ImageButton deleteButton;
        private final ColorStateList defaultExpiryColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_name);
            quantityText = itemView.findViewById(R.id.text_quantity);
            expiryText = itemView.findViewById(R.id.text_expiry);
            deleteButton = itemView.findViewById(R.id.button_delete);
            defaultExpiryColor = expiryText.getTextColors();
        }

        void bind(PantryItem item) {
            Context ctx = itemView.getContext();
            nameText.setText(item.getName());
            quantityText.setText(ctx.getString(R.string.quantity_with_unit,
                    formatQuantity(item.getQuantity()), item.getUnit()));
            bindExpiry(ctx, item.getExpiryDate());

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
        }

        private void bindExpiry(Context ctx, Long expiryMillis) {
            if (expiryMillis == null) {
                expiryText.setVisibility(View.GONE);
                return;
            }
            expiryText.setVisibility(View.VISIBLE);
            String date = dateFormat.format(new Date(expiryMillis));
            long daysLeft = daysUntil(expiryMillis);

            if (expiryAlertsEnabled && daysLeft < 0) {
                expiryText.setText(ctx.getString(R.string.expired_on, date));
                expiryText.setTextColor(ContextCompat.getColor(ctx, R.color.expiry_expired));
            } else if (expiryAlertsEnabled && daysLeft <= EXPIRY_WARNING_DAYS) {
                expiryText.setText(ctx.getString(R.string.expires_soon, date));
                expiryText.setTextColor(ContextCompat.getColor(ctx, R.color.expiry_soon));
            } else {
                expiryText.setText(ctx.getString(R.string.expires_on, date));
                expiryText.setTextColor(defaultExpiryColor);  // reset: ViewHolders are recycled!
            }
        }
    }

    // ---------- Static helpers (re-used by other screens) ----------

    /** 2.0 -> "2", 0.5 -> "0.5" (uses the phone's locale for the decimal separator). */
    public static String formatQuantity(double quantity) {
        return new DecimalFormat("0.##").format(quantity);
    }

    /** Whole calendar days from today until the given date (negative = already expired). */
    static long daysUntil(long expiryMillis) {
        long diff = startOfDay(expiryMillis) - startOfDay(System.currentTimeMillis());
        return Math.round(diff / (double) DAY_MILLIS);
    }

    private static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
