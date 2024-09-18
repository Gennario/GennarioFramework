package cz.gennario.gennarioframework.utils.items;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

public class ItemStackToConfig {

    public ItemStackToConfig(ItemStack itemStack, Section section) {
        if (itemStack == null || section == null) {
            return;
        }

        // Set material and amount
        section.set("material", itemStack.getType().toString());
        section.set("amount", itemStack.getAmount());

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // Check if item is a player head
            if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;

                String base64Texture = getBase64Texture(skullMeta);
                if (base64Texture != null) {
                    section.set("material", "<base64>" + base64Texture);
                } else if (skullMeta.hasOwner()) {
                    section.set("material", "<nickname>" + skullMeta.getOwner());
                }
            }

            // Set custom model data if present
            if (meta.hasCustomModelData()) {
                section.set("custom-model-data", meta.getCustomModelData());
            }

            // Set unbreakable
            section.set("unbreakable", meta.isUnbreakable());

            // Set display name
            if (meta.hasDisplayName()) {
                section.set("name", meta.getDisplayName());
            }

            // Set lore
            if (meta.hasLore()) {
                section.set("lore", meta.getLore());
            }

            // Set item flags
            if (!meta.getItemFlags().isEmpty()) {
                section.set("itemflags", meta.getItemFlags().stream()
                        .map(ItemFlag::name)
                        .collect(Collectors.toList()));
            }

            // Set enchantments
            if (!meta.getEnchants().isEmpty()) {
                section.set("enchants", meta.getEnchants().entrySet().stream()
                        .map(e -> e.getKey().getName() + ":" + e.getValue())
                        .collect(Collectors.toList()));
            }

            // Set leather color if it's a leather armor piece
            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                int r = leatherMeta.getColor().getRed();
                int g = leatherMeta.getColor().getGreen();
                int b = leatherMeta.getColor().getBlue();
                section.set("leather-color", r + ";" + g + ";" + b);
            }
        }
    }

    private String getBase64Texture(SkullMeta skullMeta) {
        try {
            // Use reflection to access the private field "profile"
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(skullMeta);

            if (profile != null) {
                for (Property property : profile.getProperties().get("textures")) {
                    return property.value(); // Return the first texture found
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
