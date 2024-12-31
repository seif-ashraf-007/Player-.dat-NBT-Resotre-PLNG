package me.seif.nBTTransfer.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemTransferUtils {

    public static void transferItems(ItemStack[] playerItems, List<Map<String, Object>> itemsData) {
        for (Map<String, Object> itemData : itemsData) {
            try {
                // Validate required keys
                if (!itemData.containsKey("Slot") || !itemData.containsKey("id") || !itemData.containsKey("Count")) {
                    System.err.println("Missing required keys in item data: " + itemData);
                    continue;
                }

                int slot = ((Double) itemData.get("Slot")).intValue();

                // Dynamically resize inventory if slot exceeds array length
                if (slot >= playerItems.length) {
                    playerItems = Arrays.copyOf(playerItems, slot + 1);
                }

                String id = (String) itemData.get("id");
                int count = ((Double) itemData.get("Count")).intValue();

                Material material = getMaterial(id);
                if (material == null) {
                    System.err.println("Invalid material: " + id);
                    continue;
                }

                ItemStack item = new ItemStack(material, count);
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    System.err.println("Unable to retrieve ItemMeta for material: " + material);
                    continue;
                }

                Map<String, Object> tag = (Map<String, Object>) itemData.get("tag");

                if (tag != null) {
                    // Process Enchantments
                    if (tag.containsKey("Enchantments")) {
                        List<Map<String, Object>> enchantments = (List<Map<String, Object>>) tag.get("Enchantments");
                        for (Map<String, Object> enchant : enchantments) {
                            String enchantId = (String) enchant.get("id");
                            if (enchantId != null) {
                                if (enchantId.startsWith("minecraft:")) {
                                    enchantId = enchantId.substring("minecraft:".length());
                                }
                                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantId));
                                if (enchantment != null) {
                                    int level = ((Double) enchant.get("lvl")).intValue();
                                    meta.addEnchant(enchantment, level, true);
                                } else {
                                    System.err.println("Unknown enchantment ID: " + enchantId);
                                }
                            }
                        }
                    }

                    // Process display name and lore
                    if (tag.containsKey("display")) {
                        Map<String, Object> display = (Map<String, Object>) tag.get("display");

                        // Handle Name field
                        if (display.containsKey("Name")) {
                            Object nameObj = display.get("Name");
                            if (nameObj instanceof String) {
                                // Direct string (fallback)
                                meta.setDisplayName((String) nameObj);
                            } else if (nameObj instanceof Map) {
                                // Parse JSON-like structure for Name
                                Map<String, Object> nameMap = (Map<String, Object>) nameObj;
                                if (nameMap.containsKey("extra")) {
                                    List<Map<String, Object>> extra = (List<Map<String, Object>>) nameMap.get("extra");
                                    StringBuilder nameBuilder = new StringBuilder();
                                    for (Map<String, Object> part : extra) {
                                        nameBuilder.append(part.getOrDefault("text", ""));
                                    }
                                    meta.setDisplayName(nameBuilder.toString());
                                }
                            }
                        }

                        // Handle Lore field
                        if (display.containsKey("Lore")) {
                            Object loreObj = display.get("Lore");
                            List<String> lore = extractLore(loreObj);
                            meta.setLore(lore);
                        }
                    }
                }

                item.setItemMeta(meta);
                playerItems[slot] = item;
            } catch (Exception e) {
                System.err.println("Error transferring item: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static Material getMaterial(String id) {
        try {
            if (id.startsWith("minecraft:")) {
                id = id.substring("minecraft:".length());
            }
            return Material.valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid material: " + id);
            return null;
        }
    }

    private static List<String> extractLore(Object loreObj) {
        List<String> lore = new ArrayList<>();
        if (loreObj instanceof List) {
            for (Object loreEntry : (List<?>) loreObj) {
                if (loreEntry instanceof String) {
                    lore.add((String) loreEntry);
                } else if (loreEntry instanceof Map) {
                    // Parse JSON-like structure for Lore
                    Map<String, Object> loreMap = (Map<String, Object>) loreEntry;
                    lore.add((String) loreMap.getOrDefault("text", ""));
                }
            }
        }
        return lore;
    }
}
