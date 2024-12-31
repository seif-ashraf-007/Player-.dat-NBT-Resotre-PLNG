package me.seif.nBTTransfer.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FetchPlayerDataCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command is executed by the console only
        if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            System.out.println("This command can only be run from the console.");
            return true;
        }

        // Check if the correct number of arguments is provided
        if (args.length < 1) {
            System.out.println("Usage: fetchplayerdata <playerName>");
            return true;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);

        // Validate player
        if (player == null) {
            System.out.println("Player not found or is offline.");
            return true;
        }

        try {
            // Prepare data to fetch
            Map<String, Object> playerData = new HashMap<>();

            // Inventory
            ItemStack[] inventoryItems = player.getInventory().getContents();
            playerData.put("Inventory", serializeItems(inventoryItems));

            // Ender Chest Items
            ItemStack[] enderItems = player.getEnderChest().getContents();
            playerData.put("EnderItems", serializeItems(enderItems));

            // XP Level
            playerData.put("XpLevel", player.getLevel());

            // Last Death Location
            if (player.getLastDeathLocation() != null) {
                Map<String, Object> lastDeath = new HashMap<>();
                lastDeath.put("dimension", player.getLastDeathLocation().getWorld().getName());
                lastDeath.put("pos", player.getLastDeathLocation().toVector());
                playerData.put("LastDeathLocation", lastDeath);
            } else {
                playerData.put("LastDeathLocation", "No death location available");
            }

            // Recipe Book
            Map<String, Object> recipeBookData = new HashMap<>();
            recipeBookData.put("recipes", player.getDiscoveredRecipes());
            playerData.put("recipeBook", recipeBookData);

            // Convert to JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(playerData);

            // Log data to console
            Bukkit.getLogger().info("Player Data for " + playerName + ":");
            Bukkit.getLogger().info(jsonOutput);

        } catch (Exception e) {
            System.out.println("An error occurred while fetching player data. Check console for details.");
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Serialize ItemStack array to a simplified format.
     */
    private Map<Integer, Map<String, Object>> serializeItems(ItemStack[] items) {
        Map<Integer, Map<String, Object>> serializedItems = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getType().toString());
                itemData.put("Count", item.getAmount());
                itemData.put("Slot", i);

                if (item.hasItemMeta()) {
                    Map<String, Object> metaData = new HashMap<>();
                    if (item.getItemMeta().hasDisplayName()) {
                        metaData.put("Name", item.getItemMeta().getDisplayName());
                    }
                    if (item.getItemMeta().hasLore()) {
                        metaData.put("Lore", item.getItemMeta().getLore());
                    }
                    metaData.put("Enchantments", item.getEnchantments());
                    itemData.put("tag", metaData);
                }

                serializedItems.put(i, itemData);
            }
        }
        return serializedItems;
    }
}