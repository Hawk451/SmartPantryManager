package com.example.smartpantrymanager.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.smartpantrymanager.data.dao.PantryItemDao;
import com.example.smartpantrymanager.data.dao.RecipeDao;
import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.data.entity.Recipe;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.data.seed.RecipeSeeder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
          version = 1,
          exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PantryItemDao pantryItemDao();
    public abstract RecipeDao recipeDao();

    /** Room forbids DB work on the main (UI) thread, so all queries go through this. */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    private static volatile AppDatabase INSTANCE;

    /** Singleton: the whole app shares one database connection. */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "smart_pantry.db")
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /** onCreate runs ONCE - the very first time the database file is created. */
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() ->
                    RecipeSeeder.seed(INSTANCE.recipeDao()));
        }
    };
}
