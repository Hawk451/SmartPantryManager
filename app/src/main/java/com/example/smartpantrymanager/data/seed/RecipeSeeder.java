package com.example.smartpantrymanager.data.seed;

import com.example.smartpantrymanager.data.dao.RecipeDao;
import com.example.smartpantrymanager.data.entity.Recipe;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.logic.IngredientNormalizer;

import java.util.Arrays;
import java.util.List;

/**
 * Pre-loads the recipe collection the first time the database is created
 * (called from AppDatabase's onCreate callback).
 *
 * Design choices:
 *  - Recipes deliberately SHARE common ingredients (egg, onion, tomato, cheese,
 *    potato, rice, milk, bread) so that adding or removing one pantry item
 *    visibly changes several suggestions - useful for testing and the demo.
 *  - Units match how a user would naturally record each item:
 *    "each" for counted items (eggs, onions, slices of bread, tins),
 *    "g" for dry goods, "ml" for liquids.
 *  - Salt, pepper and water are assumed to always be available and are NOT
 *    listed, otherwise almost nothing would ever strictly match.
 */
public final class RecipeSeeder {

    private RecipeSeeder() { }   // utility class - no instances

    public static void seed(RecipeDao dao) {

        // ---------------- Breakfast & eggs ----------------

        add(dao, "Tomato Omelette",
                "A quick, filling breakfast that uses up ripe tomatoes.",
                "1. Beat the eggs with the milk.\n"
                        + "2. Dice the tomato.\n"
                        + "3. Pour the eggs into a hot greased pan and cook until almost set.\n"
                        + "4. Add the tomato and grated cheese, fold in half and serve.",
                10,
                ing("Eggs", 3, "each"),
                ing("Tomato", 200, "g"),
                ing("Milk", 50, "ml"),
                ing("Cheese", 30, "g"));

        add(dao, "Scrambled Eggs on Toast",
                "Soft, creamy scrambled eggs on buttered toast.",
                "1. Toast the bread and butter it.\n"
                        + "2. Whisk the eggs with the milk.\n"
                        + "3. Cook slowly in a pan over low heat, stirring, until just set.\n"
                        + "4. Spoon onto the toast and serve.",
                10,
                ing("Eggs", 2, "each"),
                ing("Bread", 2, "each"),
                ing("Butter", 10, "g"),
                ing("Milk", 30, "ml"));

        add(dao, "Cheese Toastie",
                "A golden toasted cheese sandwich - the ultimate leftover-bread rescue.",
                "1. Butter one side of each slice of bread.\n"
                        + "2. Grate the cheese and place it between the slices, butter facing out.\n"
                        + "3. Toast in a pan or sandwich maker until golden and melted.",
                8,
                ing("Bread", 2, "each"),
                ing("Cheese", 50, "g"),
                ing("Butter", 10, "g"));

        add(dao, "French Toast",
                "Sweet eggy bread - perfect for slightly stale slices.",
                "1. Whisk the eggs, milk and sugar in a shallow dish.\n"
                        + "2. Soak each slice of bread in the mixture.\n"
                        + "3. Fry in a little butter until golden on both sides.",
                15,
                ing("Bread", 4, "each"),
                ing("Eggs", 2, "each"),
                ing("Milk", 100, "ml"),
                ing("Sugar", 10, "g"),
                ing("Butter", 10, "g"));

        add(dao, "Flapjacks",
                "Thick South African-style pancakes.",
                "1. Mix the flour and sugar in a bowl.\n"
                        + "2. Whisk in the eggs and milk to form a smooth, thick batter.\n"
                        + "3. Melt a little butter in a pan and drop in spoonfuls of batter.\n"
                        + "4. Flip when bubbles appear and cook until golden.",
                20,
                ing("Flour", 200, "g"),
                ing("Eggs", 2, "each"),
                ing("Milk", 250, "ml"),
                ing("Sugar", 20, "g"),
                ing("Butter", 20, "g"));

        add(dao, "Spanish Omelette",
                "A thick potato and onion omelette, great hot or cold.",
                "1. Thinly slice the potatoes and onion.\n"
                        + "2. Fry slowly in the oil until soft (about 15 minutes).\n"
                        + "3. Beat the eggs, pour over the potatoes and cook on low heat until set.\n"
                        + "4. Flip onto a plate, slide back into the pan and cook the other side.",
                30,
                ing("Potatoes", 2, "each"),
                ing("Onion", 1, "each"),
                ing("Eggs", 4, "each"),
                ing("Oil", 30, "ml"));

        // ---------------- Rice, pap & pasta ----------------

        add(dao, "Tomato and Onion Rice",
                "Simple, flavourful rice cooked with tomato and onion.",
                "1. Fry the chopped onion in the oil until soft.\n"
                        + "2. Add the chopped tomato and cook for 5 minutes.\n"
                        + "3. Add the rice and 500 ml water, cover and simmer for 18 minutes.",
                30,
                ing("Rice", 200, "g"),
                ing("Tomato", 200, "g"),
                ing("Onion", 1, "each"),
                ing("Oil", 15, "ml"));

        add(dao, "Egg Fried Rice",
                "The best way to use leftover rice.",
                "1. Fry the chopped onion in the oil.\n"
                        + "2. Add the cooked rice and peas and stir-fry for 3 minutes.\n"
                        + "3. Push the rice aside, scramble the eggs in the pan, then mix through.",
                15,
                ing("Rice", 200, "g"),
                ing("Eggs", 2, "each"),
                ing("Onion", 1, "each"),
                ing("Peas", 100, "g"),
                ing("Oil", 15, "ml"));

        add(dao, "Pap and Tomato Gravy",
                "Stiff mealie pap served with a simple tomato and onion sauce (sous).",
                "1. Bring 750 ml water to the boil, stir in the maize meal and cook covered for 20 minutes, stirring occasionally.\n"
                        + "2. Meanwhile fry the onion in the oil until soft.\n"
                        + "3. Add the chopped tomatoes and simmer for 10 minutes into a thick sauce.\n"
                        + "4. Serve the sauce over the pap.",
                30,
                ing("Maize meal", 250, "g"),
                ing("Tomatoes", 300, "g"),
                ing("Onion", 1, "each"),
                ing("Oil", 15, "ml"));

        add(dao, "Pasta with Tomato Sauce",
                "A classic quick pasta from store-cupboard basics.",
                "1. Cook the pasta in boiling water until tender.\n"
                        + "2. Fry the onion and crushed garlic in the oil.\n"
                        + "3. Add the chopped tomatoes and simmer for 10 minutes.\n"
                        + "4. Toss the drained pasta in the sauce.",
                25,
                ing("Pasta", 250, "g"),
                ing("Tomatoes", 400, "g"),
                ing("Onion", 1, "each"),
                ing("Garlic", 2, "each"),
                ing("Oil", 15, "ml"));

        add(dao, "Macaroni and Cheese",
                "Creamy, cheesy baked pasta.",
                "1. Cook the pasta until just tender and drain.\n"
                        + "2. Melt the butter, stir in the flour, then slowly whisk in the milk to make a white sauce.\n"
                        + "3. Stir in most of the cheese, then the pasta.\n"
                        + "4. Top with the remaining cheese and bake at 180 °C for 20 minutes.",
                40,
                ing("Pasta", 250, "g"),
                ing("Cheese", 150, "g"),
                ing("Milk", 400, "ml"),
                ing("Butter", 30, "g"),
                ing("Flour", 30, "g"));

        // ---------------- Potatoes & vegetables ----------------

        add(dao, "Mashed Potatoes",
                "Smooth, buttery mash.",
                "1. Peel and quarter the potatoes and boil until soft (about 20 minutes).\n"
                        + "2. Drain, then mash with the butter and warm milk until smooth.",
                25,
                ing("Potatoes", 4, "each"),
                ing("Butter", 30, "g"),
                ing("Milk", 100, "ml"));

        add(dao, "Crispy Potato Wedges",
                "Oven-baked wedges with garlic.",
                "1. Cut the potatoes into wedges.\n"
                        + "2. Toss with the oil and crushed garlic.\n"
                        + "3. Roast at 200 °C for 35-40 minutes, turning halfway.",
                45,
                ing("Potatoes", 4, "each"),
                ing("Oil", 30, "ml"),
                ing("Garlic", 2, "each"));

        add(dao, "Cheesy Potato Bake",
                "Layers of potato and onion baked in cheesy milk.",
                "1. Thinly slice the potatoes and onion.\n"
                        + "2. Layer them in a greased dish with most of the cheese.\n"
                        + "3. Pour over the milk, top with the remaining cheese.\n"
                        + "4. Bake at 180 °C for 50 minutes until golden and tender.",
                60,
                ing("Potatoes", 4, "each"),
                ing("Onion", 1, "each"),
                ing("Cheese", 100, "g"),
                ing("Milk", 250, "ml"));

        add(dao, "Vegetable Soup",
                "A hearty soup for using up tired vegetables.",
                "1. Chop the onion, carrots, potatoes and tomato.\n"
                        + "2. Put everything in a pot with the stock cube and 1 litre of water.\n"
                        + "3. Simmer for 30 minutes, then blend or serve chunky.",
                40,
                ing("Onion", 1, "each"),
                ing("Carrots", 2, "each"),
                ing("Potatoes", 2, "each"),
                ing("Tomato", 200, "g"),
                ing("Stock cube", 1, "each"));

        add(dao, "Chakalaka",
                "A spicy South African vegetable relish.",
                "1. Fry the onion in the oil until soft.\n"
                        + "2. Add the curry powder and grated carrots and cook for 3 minutes.\n"
                        + "3. Add the chopped tomato and baked beans and simmer for 10 minutes.",
                25,
                ing("Onion", 1, "each"),
                ing("Carrots", 2, "each"),
                ing("Tomato", 200, "g"),
                ing("Baked beans", 1, "each"),
                ing("Curry powder", 10, "g"),
                ing("Oil", 15, "ml"));

        // ---------------- Chicken ----------------

        add(dao, "Chicken Curry with Rice",
                "A mild, comforting chicken curry.",
                "1. Cook the rice.\n"
                        + "2. Fry the onion in the oil, add the curry powder and cook for 1 minute.\n"
                        + "3. Add the diced chicken and brown it.\n"
                        + "4. Add the chopped tomato and 150 ml water and simmer for 20 minutes.\n"
                        + "5. Serve over the rice.",
                40,
                ing("Chicken breast", 400, "g"),
                ing("Onion", 1, "each"),
                ing("Tomato", 200, "g"),
                ing("Curry powder", 10, "g"),
                ing("Rice", 200, "g"),
                ing("Oil", 15, "ml"));

        add(dao, "Chicken Stir-Fry",
                "Fast, colourful stir-fry served with rice.",
                "1. Cook the rice.\n"
                        + "2. Slice the chicken, carrot and onion into thin strips.\n"
                        + "3. Stir-fry the chicken in the oil until cooked, then add the vegetables for 3 minutes.\n"
                        + "4. Add the soy sauce, toss and serve over the rice.",
                25,
                ing("Chicken breast", 300, "g"),
                ing("Rice", 200, "g"),
                ing("Carrot", 1, "each"),
                ing("Onion", 1, "each"),
                ing("Soy sauce", 30, "ml"),
                ing("Oil", 15, "ml"));

        // ---------------- Sweet ----------------

        add(dao, "Banana Bread",
                "Moist loaf that turns over-ripe bananas into a treat.",
                "1. Mash the bananas.\n"
                        + "2. Mix in the melted butter, sugar and eggs.\n"
                        + "3. Fold in the flour.\n"
                        + "4. Pour into a greased loaf tin and bake at 180 °C for 55 minutes.",
                70,
                ing("Bananas", 3, "each"),
                ing("Flour", 250, "g"),
                ing("Sugar", 100, "g"),
                ing("Eggs", 2, "each"),
                ing("Butter", 80, "g"));

        add(dao, "Banana Smoothie",
                "A quick breakfast drink for spotty bananas.",
                "1. Peel and slice the bananas.\n"
                        + "2. Blend with the milk and yoghurt until smooth.\n"
                        + "3. Serve immediately.",
                5,
                ing("Bananas", 2, "each"),
                ing("Milk", 250, "ml"),
                ing("Yoghurt", 125, "g"));
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    /** Inserts one recipe and its ingredients in a single transaction. */
    private static void add(RecipeDao dao, String name, String description, String steps,
                            int prepMinutes, RecipeIngredient... ingredients) {
        List<RecipeIngredient> list = Arrays.asList(ingredients);
        dao.insertRecipeWithIngredients(new Recipe(name, description, steps, prepMinutes), list);
    }

    /**
     * Builds one ingredient line. The name is normalized with the SAME method
     * used for pantry items, so "Tomatoes" here matches "tomato" in the pantry.
     * recipeId is set later by RecipeDao.insertRecipeWithIngredients().
     */
    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(0, name, IngredientNormalizer.normalize(name), quantity, unit);
    }
}
