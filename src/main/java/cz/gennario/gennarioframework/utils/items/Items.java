package cz.gennario.gennarioframework.utils.items;

import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.lone.itemsadder.api.ItemsAdder;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Items {

    public ItemStack setItem(Section section) {
        return setItem(section, null, new ReplacementPackage());
    }

    public ItemStack setItem(Section section, @Nullable Player player) {
        return setItem(section, player, new ReplacementPackage());
    }

    /**
     * Replacement list
     */
    public ItemStack setItem(Section section, @Nullable Player player, ReplacementPackage replacements) {
        int amount;

        if (replacements != null) {
            amount = section.contains("amount") ? Integer.parseInt(replacements.replace(player, section.getString("amount"))) : 1;
        } else {
            amount = section.contains("amount") ? section.getInt("amount") : 1;
        }

        String materialString = section.getString("material");

        if (materialString.startsWith("<base64>")) {
            String value = replacements.replace(player, materialString.replace("<base64>", ""));
            ItemBuilder item = new ItemBuilder(HeadItem.getSkullByTexture(value));

            updateItemMeta(section, item, player, replacements);
            return item.toItemStack();
        } else if (materialString.startsWith("<nickname>")) {
            String value = replacements.replace(player, materialString.replace("<nickname>", ""));
            ItemBuilder item = new ItemBuilder(Material.PLAYER_HEAD);

            updateItemMeta(section, item, player, replacements);
            item.setSkullOwner(value);
            return item.toItemStack();
        } else if (materialString.startsWith("<url>")) {
            String value = replacements.replace(player, materialString.replace("<url>", ""));
            ItemBuilder item = new ItemBuilder(HeadItem.getSkullByUrl(value));

            updateItemMeta(section, item, player, replacements);
            return item.toItemStack();
        } else if (section.contains("head")) {
            String value = replacements.replace(player, section.getString("head"));
            ItemBuilder item = new ItemBuilder(Material.PLAYER_HEAD);

            updateItemMeta(section, item, player, replacements);
            item.setSkull(value);
            return item.toItemStack();
        } else if (materialString.startsWith("<itemsadder>")) {
            String value = replacements.replace(player, materialString.replaceFirst("<itemsadder>", ""));
            ItemBuilder itemBuilder = new ItemBuilder(ItemsAdder.getCustomItem(value));
            updateItemMeta(section, itemBuilder, player, replacements);
            return itemBuilder.toItemStack();
        } else if (materialString.startsWith("<oraxen>")) {
            String value = replacements.replace(player, materialString.replaceFirst("<oraxen>", ""));
            ItemBuilder itemBuilder = new ItemBuilder(OraxenItems.getItemById(value).build());
            updateItemMeta(section, itemBuilder, player, replacements);
            return itemBuilder.toItemStack();
        }

        ItemBuilder item = new ItemBuilder(Material.valueOf(replacements.replace(player, materialString).toUpperCase()), amount);
        updateItemMeta(section, item, player, replacements);

        return item.toItemStack();
    }


    public void updateItemMeta(Section section, ItemBuilder item, Player player, ReplacementPackage replacements) {
        if (section.contains("durability")) {
            item.setUnbreakable(true);
        }

        // Custom model data
        if (section.contains("custom-model-data")) {
            item.setCustomModelData(section.getInt("custom-model-data"));
        }

        // Unbreakable
        if (section.contains("unbreakable")) {
            item.setUnbreakable(section.getBoolean("unbreakable"));
        }

        // Display name
        if (section.contains("name")) {
            item.setDisplayName(Utils.colorize(player, replacements.replace(player, section.getString("name"))));
        }

        // Item flags
        if (section.contains("itemflags")) {
            for (String itemFlags : section.getStringList("itemflags")) {
                item.addItemFlag(ItemFlag.valueOf(itemFlags));
            }
        }

        // Lore:
        if (section.contains("lore")) {
            item.setLoreList(Utils.colorize(player, replacements.replace(player, section.getStringList("lore"))));
        }

        // Enchants
        if (!section.contains("enchants")) return;

        for (String enchants : section.getStringList("enchants")) {
            String[] args = enchants.split(":");
            if (args.length == 2) {
                String enchantName = args[0];
                int enchantLevel = Integer.parseInt(args[1]);
                item.addEnchant(Enchantment.getByName(enchantName.toUpperCase()), enchantLevel);
            }
        }

        if (section.contains("leather-color")) {
            String string = section.getString("leather-color");
            String[] split = string.split(";");
            if (split.length == 3) {
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                item.setLeatherColor(Color.fromRGB(r, g, b));
            }
        }
    }

}
