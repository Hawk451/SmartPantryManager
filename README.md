# Smart Pantry Manager

A Java Android app that suggests recipes **strictly** from the ingredients you already have at home, to help cut food waste. No shopping trip needed, and no recipe is suggested unless you have everything it needs.

Built for **Mobile App Development 700** (Richfield), Practical Assignment.

---

## Features

- **Pantry management (full CRUD):** add, view, edit and delete ingredients, each with a name, quantity, unit and optional expiry date.
- **Pantry list:** a RecyclerView with a custom adapter (DiffUtil), an empty-state message, delete confirmation and *Undo*.
- **Expiry alerts:** items expiring within 3 days are highlighted orange, and expired items red. This can be switched off in Settings.
- **Suggested Recipes:** runs the strict-matching rule and lists only the recipes you can make right now.
- **"Almost there" (bonus):** a separate, clearly labelled section for recipes missing exactly one ingredient. These are never mixed into the strict suggestions, and the section can be hidden in Settings.
- **Recipe Detail:** the full ingredient list with a ✓ or ✗ next to each ingredient (checked against your current pantry), plus the method.
- **Settings:** preference toggles (stored in SharedPreferences), data counts, and *Clear pantry*.
- **20 pre-loaded recipes**, seeded automatically on first launch.
- **Input validation** on the add/edit form, with every error shown on its own field at once. Both `0.5` and `0,5` are accepted as decimals.
- **Bottom navigation** between Pantry, Recipes and Settings.

The app deliberately uses **no** Google Maps, mapping SDK, GPS or location features, as the brief requires. It requests no permissions.

---

## The strict-matching rule

A recipe is suggested **only if every ingredient it requires is in the pantry in at least the required quantity.**

| Situation | Result |
|---|---|
| All ingredients present in enough quantity | ✅ Suggested |
| 4 of 5 ingredients present | ❌ Not suggested (shown under *Almost there* if exactly 1 is missing) |
| Ingredient present but not enough (2 eggs, recipe needs 3) | ❌ Not suggested |
| 1 kg tomatoes in pantry, recipe needs 500 g | ✅ Units converted |
| "Tomatoes" in pantry, recipe says "tomato" | ✅ Names normalised |
| Two pantry entries for tomato (300 g + 0.5 kg) | ✅ Quantities added together |
| Incompatible units (3 *each* tomatoes vs 200 *g*) | ❌ Not suggested. If the amount can't be proven, the rule treats it as missing |

How it works:

1. **`IngredientNormalizer`** turns names into a matching key: it lowercases, trims and collapses spaces, then singularises the last word (tomatoes → tomato, berries → berry, peaches → peach, bay leaves → bay leaf). Words like *cheese*, *couscous* and *hummus* are left alone.
2. **`UnitConverter`** converts quantities to a base unit: grams (g, kg), millilitres (ml, L, cup, tbsp, tsp) or *each*. It refuses to compare units from different groups.
3. **`RecipeMatcher`** groups the pantry by normalised name in a `HashMap`. For each recipe ingredient it adds up the compatible pantry quantities and compares the total with what the recipe needs. A recipe goes on the strict list only when its list of missing ingredients is empty.

**Assumption:** salt, pepper and water are treated as always available and are not listed as recipe ingredients.

---

## Database choice: SQLite (Room)

I chose **SQLite through the Room persistence library** because:

- **It works offline.** A pantry app has to work in the kitchen without a network connection.
- **The data is private and local.** A user's pantry doesn't need syncing or accounts, so a cloud database (Firebase/PostgreSQL) would add complexity with no benefit.
- **The data is relational.** One recipe has many ingredients, which suits a foreign key with `ON DELETE CASCADE` well.
- **Room checks SQL queries at compile time** and maps rows to Java objects, which removes a lot of boilerplate compared with a raw `SQLiteOpenHelper`.
- **It matches the module content** on persistent on-device storage.

### Schema

```
pantry_items                       recipes
-----------------------------      -----------------------
id              INTEGER PK          id            INTEGER PK
name            TEXT                name          TEXT
normalized_name TEXT  (indexed)     description   TEXT
quantity        REAL                steps         TEXT
unit            TEXT                prep_minutes  INTEGER
expiry_date     INTEGER (nullable)          │ 1
                                            │
                                            │ *
                                   recipe_ingredients
                                   -----------------------
                                   id              INTEGER PK
                                   recipe_id       INTEGER FK → recipes.id (CASCADE, indexed)
                                   name            TEXT
                                   normalized_name TEXT (indexed)
                                   quantity        REAL
                                   unit            TEXT
```

User settings (expiry alerts, show "Almost there") are stored in **SharedPreferences**, because they are simple key/value flags rather than relational data.

---

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
├── data/
│   ├── AppDatabase.java              Room database singleton + first-run seeding
│   ├── entity/                       PantryItem, Recipe, RecipeIngredient (tables)
│   ├── relation/                     RecipeWithIngredients (@Relation query result)
│   ├── dao/                          PantryItemDao (CRUD), RecipeDao
│   └── seed/RecipeSeeder.java        the 20 pre-loaded recipes
├── logic/                            plain Java with no Android code, unit-tested
│   ├── IngredientNormalizer.java
│   ├── UnitConverter.java
│   ├── PantryItemValidator.java
│   └── RecipeMatcher.java            the strict-matching rule
├── ui/
│   ├── common/BottomNavHelper.java
│   ├── pantry/                       PantryListActivity, PantryAdapter, AddEditItemActivity
│   ├── recipes/                      SuggestedRecipesActivity, RecipeAdapter, RecipeDetailActivity
│   └── settings/SettingsActivity.java
└── util/AppPrefs.java                SharedPreferences wrapper

app/src/test/java/com/example/smartpantrymanager/
├── IngredientNormalizerTest.java
├── UnitConverterTest.java
├── PantryItemValidatorTest.java
└── RecipeMatcherTest.java
```

### Screen flow

```
                ┌──────────── Bottom navigation ────────────┐
                ▼                     ▼                     ▼
       PantryListActivity  ◄──►  SuggestedRecipesActivity  ◄──►  SettingsActivity
        │ (+) add  │ tap row            │ tap recipe
        ▼          ▼                    ▼
   AddEditItemActivity           RecipeDetailActivity
   (no extra = add,              (EXTRA_RECIPE_ID)
    EXTRA_ITEM_ID = edit)
```

---

## Setup and run

**Requirements:** Android Studio (Koala or newer), JDK 17, an Android emulator or device running Android 7.0 (API 24) or newer.

1. Clone the repository:
   ```
   git clone https://github.com/<your-username>/SmartPantryManager.git
   ```
2. Open the folder in Android Studio (**File → Open**) and wait for Gradle sync to finish.
3. Check that `app/build.gradle.kts` contains the Room dependencies:
   ```kotlin
   val roomVersion = "2.6.1"
   implementation("androidx.room:room-runtime:$roomVersion")
   annotationProcessor("androidx.room:room-compiler:$roomVersion")
   implementation("androidx.recyclerview:recyclerview:1.3.2")
   ```
4. Select an emulator and press **Run ▶**.
5. On first launch the 20 recipes are seeded automatically. You can see them in **App Inspection → Database Inspector**.

> If you change `RecipeSeeder`, uninstall the app (or clear its storage) before running it again. Seeding only happens when the database is first created.

### Running the unit tests

Right-click `app/src/test/java` and choose **Run 'Tests in …'**, or from a terminal run:

```
./gradlew test
```

---

## Known limitations and future improvements

- **Synonyms aren't matched.** For example, "sunflower oil" won't match "oil". A small synonym map could fix this.
- **Pantry stock isn't deducted.** A "Cook this" button could subtract a recipe's ingredients from the pantry.
- **No push notifications for expiry.** Expiry is shown visually only; WorkManager could schedule reminders.
- **Recipes are read-only.** The recipe collection is fixed; a user-recipe editor could be added.

---

## Acknowledgements

- [Android Developers: Save data in a local database using Room](https://developer.android.com/training/data-storage/room)
- [Android Developers: Create dynamic lists with RecyclerView](https://developer.android.com/develop/ui/views/layout/recyclerview)
- [Material Components for Android](https://m3.material.io/develop/android/mdc-android)
- AI-assisted development (Claude, Anthropic), used with lecturer approval.
