package me.seif.nBTTransfer.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.seif.nBTTransfer.NBTTransfer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class FetchPlayerDataCommand implements CommandExecutor {

    private NBTTransfer plugin;

    public FetchPlayerDataCommand(NBTTransfer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            System.out.println("This command can only be run from the console.");
            return true;
        }

        if (args.length < 1) {
            System.out.println("Usage: fetchplayerdata <playerName>");
            return true;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            System.out.println("Player not found or is offline.");
            return true;
        }

        try {
            Map<String, Object> playerData = new HashMap<>();

            // Basic Player Attributes
            playerData.put("Name", player.getName());
            playerData.put("UUID", player.getUniqueId().toString());
            playerData.put("Health", player.getHealth());
            playerData.put("FoodLevel", player.getFoodLevel());
            playerData.put("Level", player.getLevel());
            playerData.put("TotalExperience", player.getTotalExperience());
            playerData.put("GameMode", player.getGameMode().toString());
            playerData.put("IP", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown");

            // Location
            if (player.getLocation() != null) {
                Map<String, Object> location = new HashMap<>();
                location.put("World", player.getWorld().getName());
                location.put("X", player.getLocation().getX());
                location.put("Y", player.getLocation().getY());
                location.put("Z", player.getLocation().getZ());
                location.put("Yaw", player.getLocation().getYaw());
                location.put("Pitch", player.getLocation().getPitch());
                playerData.put("Location", location);
            }

            // Inventory and Ender Chest
            playerData.put("Inventory", serializeItems(player.getInventory().getContents()));
            playerData.put("EnderItems", serializeItems(player.getEnderChest().getContents()));

            // Last Death Location
            if (player.getLastDeathLocation() != null) {
                Map<String, Object> lastDeath = new HashMap<>();
                lastDeath.put("World", player.getLastDeathLocation().getWorld().getName());
                lastDeath.put("Position", player.getLastDeathLocation().toVector());
                playerData.put("LastDeathLocation", lastDeath);
            } else {
                playerData.put("LastDeathLocation", "No death location available");
            }

            // Potion Effects
            Map<String, Object> potionEffects = new HashMap<>();
            player.getActivePotionEffects().forEach(effect -> {
                potionEffects.put(effect.getType().getName(), effect.getAmplifier());
            });
            playerData.put("PotionEffects", potionEffects);

            // Discovered Recipes
            playerData.put("recipeBook", serializeRecipeBook(player.getDiscoveredRecipes()));

            // Bed Spawn Location
            if (player.getBedSpawnLocation() != null) {
                Map<String, Object> bedSpawn = new HashMap<>();
                bedSpawn.put("World", player.getBedSpawnLocation().getWorld().getName());
                bedSpawn.put("Position", player.getBedSpawnLocation().toVector());
                playerData.put("BedSpawnLocation", bedSpawn);
            }

            // Advancements (Requires Manual Parsing if Needed)
            playerData.put("Advancements", "Advancement data can be accessed with custom parsing");

            // Convert to JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(playerData);


            getLogger().info("Player Data for " + playerName + ":");
            getLogger().info(jsonOutput);
            saveJsonToFile(jsonOutput, playerName + ".json");

        } catch (Exception e) {
            System.out.println("An error occurred while fetching player data. Check console for details.");
            e.printStackTrace();
        }

        return true;
    }

    private void saveJsonToFile(String jsonOutput, String fileName) {
        // Get the plugin's data folder
        File dataFolder = plugin.getDataFolder();

        // Ensure the main data folder exists
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        // Create the "playerFetchedData" subfolder inside the plugin's data folder
        File playerDataFolder = new File(dataFolder, "playerFetchedData");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdir();
        }

        // Create a new file in the "playerFetchedData" folder
        File jsonFile = new File(playerDataFolder, fileName);

        if (jsonFile.exists()) {
            getLogger().warning("File " + jsonFile.getAbsolutePath() + " already exists. Overwriting...");
        }

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(jsonOutput);
            getLogger().info("Successfully saved JSON to " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            getLogger().severe("Could not save JSON file: " + e.getMessage());
        }
    }

    private Map<String, Object> serializeRecipeBook(Set<NamespacedKey> discoveredRecipes) {
        Map<String, Object> recipeBook = new HashMap<>();

        // Example hardcoded values; adjust based on actual data if available
        recipeBook.put("isBlastingFurnaceFilteringCraftable", 0);
        recipeBook.put("isGuiOpen", 1);

        // Convert discovered recipes to the desired string format
        List<String> recipeList = new ArrayList<>();
        for (org.bukkit.NamespacedKey recipe : discoveredRecipes) {
            recipeList.add(recipe.getNamespace() + ":" + recipe.getKey());
        }

        recipeBook.put("toBeDisplayed", recipeList);

        return recipeBook;
    }


    private List<Map<String, Object>> serializeItems(ItemStack[] items) {
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("Slot", i);
                itemData.put("id", "minecraft:" + item.getType().name().toLowerCase());
                itemData.put("Count", item.getAmount());

                // Add tag data if the item has metadata
                if (item.hasItemMeta()) {
                    Map<String, Object> tagData = new HashMap<>();

                    // Enchantments
                    if (!item.getEnchantments().isEmpty()) {
                        List<Map<String, Object>> enchantments = new ArrayList<>();
                        item.getEnchantments().forEach((enchantment, level) -> {
                            Map<String, Object> enchantmentData = new HashMap<>();
                            enchantmentData.put("id", "minecraft:" + enchantment.getKey().getKey());
                            enchantmentData.put("lvl", level);
                            enchantments.add(enchantmentData);
                        });
                        tagData.put("Enchantments", enchantments);
                    }

                    // Custom Model Data (if available)
                    if (item.getItemMeta().hasCustomModelData()) {
                        tagData.put("CustomModelData", item.getItemMeta().getCustomModelData());
                    }

                    // Damage (if applicable)
                    if (item.getType().getMaxDurability() > 0 && item.getDurability() > 0) {
                        tagData.put("Damage", (int) item.getDurability());
                    }

                    itemData.put("tag", tagData);
                }

                serializedItems.add(itemData);
            }
        }
        return serializedItems;
    }


}