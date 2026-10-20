package com.example.smartpantrymanager.ui.pantry;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.dao.PantryItemDao;
import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.ui.common.BottomNavHelper;
import com.example.smartpantrymanager.util.AppPrefs;

import java.util.List;

/**
 * Launcher screen: shows every pantry item (READ), and is the entry point
 * for Create (FAB), Update (tap a row) and Delete (bin icon).
 */
public class PantryListActivity extends AppCompatActivity
        implements PantryAdapter.OnItemActionListener {

    private static final String TAG = "PantryListActivity";

    private PantryItemDao pantryDao;
    private PantryAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        Log.d(TAG, "onCreate - building views (runs once)");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pantryDao = AppDatabase.getInstance(this).pantryItemDao();

        // RecyclerView = LayoutManager (how rows are arranged) + Adapter (what goes in them)
        RecyclerView recycler = findViewById(R.id.recycler_pantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recycler.setAdapter(adapter);

        emptyText = findViewById(R.id.text_empty);

        // CREATE: open the form with no extras = "add" mode
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditItemActivity.class)));

        BottomNavHelper.setup(this, findViewById(R.id.bottom_nav), R.id.nav_pantry);
    }

    /**
     * onResume runs every time this screen becomes visible again - including
     * after returning from Add/Edit - so reloading here keeps the list current.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - reloading pantry from database");
        adapter.setExpiryAlertsEnabled(AppPrefs.isExpiryAlertsEnabled(this));
        loadPantry();
    }

    /** READ: query on a background thread, then update the UI on the main thread. */
    private void loadPantry() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> items = pantryDao.getAll();
            runOnUiThread(() -> showItems(items));
        });
    }

    private void showItems(List<PantryItem> items) {
        adapter.submitList(items);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---------- Adapter callbacks ----------

    /** UPDATE: pass the item's id to the form via an Intent extra = "edit" mode. */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    /** DELETE: confirm first so a mis-tap can't lose data. */
    @Override
    public void onDeleteClick(PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_item_title)
                .setMessage(getString(R.string.delete_item_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .show();
    }

    private void deleteItem(PantryItem item) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.delete(item);
            runOnUiThread(() -> {
                loadPantry();
                Snackbar.make(findViewById(R.id.recycler_pantry),
                                getString(R.string.item_deleted, item.getName()),
                                Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.bottom_nav)
                        .setAction(R.string.undo, v -> undoDelete(item))
                        .show();
            });
        });
    }

    /** Re-inserts the deleted object; it still has its old id, so the same row comes back. */
    private void undoDelete(PantryItem item) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pantryDao.insert(item);
            runOnUiThread(this::loadPantry);
        });
    }
}
