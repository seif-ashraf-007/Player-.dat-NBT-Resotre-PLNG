package me.seif.nBTTransfer.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipeBookUtils {

    private static final Logger LOGGER = Logger.getLogger(RecipeBookUtils.class.getName());

    public static void transferRecipeBook(Player player, Map<String, Object> recipeBookData) {
        List<String> recipes = (List<String>) recipeBookData.get("recipes");
        if (recipes != null) {
            for (String recipe : recipes) {
                try {
                    // Normalize the recipe name
                    recipe = normalizeRecipeName(recipe);

                    // Validate the recipe key format
                    if (!isValidRecipeKey(recipe)) {
                        LOGGER.warning("Invalid recipe key format: " + recipe);
                        continue;
                    }

                    // Create NamespacedKey
                    NamespacedKey key = NamespacedKey.fromString(recipe);

                    // Discover the recipe if it exists
                    if (key != null && player.getServer().getRecipe(key) != null) {
                        player.discoverRecipe(key);
                    } else {
                        LOGGER.warning("Invalid recipe: " + recipe);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error discovering recipe: " + recipe, e);
                }
            }
        }
    }

    private static String normalizeRecipeName(String recipe) {
        // Ensure the recipe name is lowercase and prefixed with "minecraft:"
        if (!recipe.contains(":")) {
            recipe = "minecraft:" + recipe;
            LOGGER.info("Recipe name normalized: " + recipe);
        }
        return recipe.toLowerCase();
    }

    private static boolean isValidRecipeKey(String recipe) {
        // Validate format with namespace
        return recipe.matches("^[a-z0-9_./-]+:[a-z0-9_./-]+$");
    }
}
