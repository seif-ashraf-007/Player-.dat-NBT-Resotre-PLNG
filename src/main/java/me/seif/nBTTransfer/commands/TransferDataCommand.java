package me.seif.nBTTransfer.commands;

import com.google.gson.Gson;
import me.seif.nBTTransfer.NBTTransfer;
import me.seif.nBTTransfer.utils.ItemTransferUtils;
import me.seif.nBTTransfer.utils.RecipeBookUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class TransferDataCommand implements CommandExecutor {
    private final NBTTransfer plugin;

    public TransferDataCommand(NBTTransfer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            System.out.println("This command can only be run from the console.");
            return true;
        }

        if (args.length < 2) {
            System.out.println("Usage: transferdata <playerName> <jsonFileName>");
            return true;
        }

        String playerName = args[0];
        String jsonFileName = args[1];

        Player player = Bukkit.getServer().getPlayer(playerName);
        if (player == null) {
            System.out.println("Player not found.");
            return true;
        }

        File jsonFile = new File(plugin.getDataFolder(), jsonFileName);
        if (!jsonFile.exists()) {
            System.out.println("JSON file not found in the plugin directory!");
            return true;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Gson gson = new Gson();
            Map<String, Object> data = gson.fromJson(reader, Map.class);

            // Transfer Inventory
            if (data.containsKey("Inventory")) {
                ItemStack[] inventoryContents = player.getInventory().getContents();
                ItemTransferUtils.transferItems(inventoryContents, (List<Map<String, Object>>) data.get("Inventory"));
                player.getInventory().setContents(inventoryContents);
                System.out.println("Inventory items transferred successfully.");
            }

            // Transfer Ender Chest Items
            if (data.containsKey("EnderItems")) {
                ItemStack[] enderChestContents = player.getEnderChest().getContents();
                ItemTransferUtils.transferItems(enderChestContents, (List<Map<String, Object>>) data.get("EnderItems"));
                player.getEnderChest().setContents(enderChestContents);
                System.out.println("Ender Chest items transferred successfully.");
            }

            // Transfer XP Level
            if (data.containsKey("XpLevel")) {
                player.setLevel(((Double) data.get("XpLevel")).intValue());
                System.out.println("XP Level transferred successfully.");
            }

            // Transfer Recipe Book
            if (data.containsKey("recipeBook")) {
               RecipeBookUtils.transferRecipeBook(player, (Map<String, Object>) data.get("recipeBook"));
            }

            System.out.println("Data transferred successfully to " + player.getName());
        } catch (Exception e) {
            System.out.println("An error occurred while transferring data. Check console for details.");
            e.printStackTrace();
        }

        return true;
    }

}
