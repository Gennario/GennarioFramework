package cz.gennario.gennarioframework.utilcommands;

import com.nexomc.nexo.api.NexoItems;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.DefaultFolderCreator;
import cz.gennario.gennarioframework.utils.commands.CommandAPI;
import cz.gennario.gennarioframework.utils.commands.SubCommandArg;
import cz.gennario.gennarioframework.utils.config.Config;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.papermc.paper.datacomponent.DataComponentTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DMFormatCommand extends CommandAPI implements Listener {

    private Map<Player, InventorySetupInstance> playerInventories;

    public DMFormatCommand(JavaPlugin plugin) {
        super(plugin, "dmformat");

        playerInventories = new ConcurrentHashMap<>();

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

        File dmformat = new File(Main.getInstance().getDataFolder(), "dmformat");
        if (!dmformat.exists()) {
            dmformat.mkdirs();
            new DefaultFolderCreator(Main.getInstance(), "dmformat", "example_format", Main.getInstance().getResource("dmformat/example_format.yml"));
        }

        File outputs = new File(dmformat, "outputs");
        if (!outputs.exists()) {
            outputs.mkdirs();
        }

        addCommand("format")
                .setPermission("gennarioframework.dmformat.format")
                .setDescription("Format DMFormat message")
                .addArg("template", SubCommandArg.CommandArgType.REQUIRED, SubCommandArg.CommandArgValue.STRING, () -> {
                    List<String> formats = new java.util.ArrayList<>();
                    for (File file : new File(Main.getInstance().getDataFolder(), "dmformat").listFiles()) {
                        if (file.isFile() && file.getName().endsWith(".yml")) {
                            formats.add(file.getName().substring(0, file.getName().length() - 4));
                        }
                    }
                    return formats;
                })
                .addArg("arg1", SubCommandArg.CommandArgType.OPTIONAL, SubCommandArg.CommandArgValue.STRING)
                .addArg("arg2", SubCommandArg.CommandArgType.OPTIONAL, SubCommandArg.CommandArgValue.STRING)
                .addArg("arg3", SubCommandArg.CommandArgType.OPTIONAL, SubCommandArg.CommandArgValue.STRING)
                .addArg("arg4", SubCommandArg.CommandArgType.OPTIONAL, SubCommandArg.CommandArgValue.STRING)
                .setAllowConsoleSender(false)
                .setResponse((sender, label, commandArgs) -> {
                    String template = commandArgs[0].getAsString();

                    String arg1 = "";
                    String arg2 = "";
                    String arg3 = "";
                    String arg4 = "";

                    if (commandArgs.length > 1) arg1 = commandArgs[1].getAsString();
                    if (commandArgs.length > 2) arg2 = commandArgs[2].getAsString();
                    if (commandArgs.length > 3) arg3 = commandArgs[3].getAsString();
                    if (commandArgs.length > 4) arg4 = commandArgs[4].getAsString();

                    // create bukkit inventory
                    Player player = (Player) sender;
                    Inventory inventory = Bukkit.createInventory(player, 54, "DMFormat - " + template);

                    InventorySetupInstance instance = new InventorySetupInstance(player, inventory, getTemplateYaml(template), arg1, arg2, arg3, arg4);
                    playerInventories.put(player, instance);

                    player.openInventory(inventory);
                    player.sendMessage("§aInventory opened! Now you can setup your DMFormat message by placing items in the inventory and then closing it.");
                });

        buildCommand();
    }

    public YamlDocument getTemplateYaml(String template) {
        Config config = new Config(Main.getInstance(), "dmformat", template, false);
        config.setUpdate(false);
        try {
            config.loadIfNotExist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config.getYamlDocument();
    }


    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (playerInventories.containsKey(player)) {
                InventorySetupInstance instance = playerInventories.remove(player);
                Inventory inventory = instance.getInventory();

                if (inventory != event.getInventory()) {
                    return;
                }

                if (inventory.isEmpty()) {
                    player.sendMessage("§cYou closed the inventory without placing any items. DMFormat message creation cancelled.");
                    return;
                }

                String outputId = instance.getTemplate().getFile().getName().replace(".yml", "") + "_" + RandomStringUtils.random(5, true, true);

                Config config = new Config(Main.getInstance(), "dmformat/outputs", outputId, false);
                config.setUpdate(false);
                try {
                    config.loadIfNotExist();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                YamlDocument templateYAML = instance.getTemplate();
                YamlDocument outputYAML = config.getYamlDocument();

                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    org.bukkit.inventory.ItemStack item = inventory.getItem(slot);
                    if (item == null || item.getType().isAir()) continue;

                    if (!NexoItems.exists(item)) {
                        continue;
                    }
                    String itemId = NexoItems.idFromItem(item);

                    if (templateYAML.contains("format")) {
                        String itemPath = "items.generated_item_" + slot;

                        // Rekurzivní kopírování celé sekce
                        copySection(templateYAML.getSection("format"), outputYAML, itemPath, instance, item, getItemDisplayName(item, itemId), itemId, slot);

                        outputYAML.set(itemPath + ".material", item.getType().name());
                        outputYAML.set(itemPath + ".model_data", NexoItems.itemFromId(itemId).getCustomModelData());
                        outputYAML.set(itemPath + ".slot", slot);
                    }
                }

                try {
                    outputYAML.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage("§aDMFormat has been formatted and saved to §f/dmformat/outputs/" + outputId + ".yml");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
    }

    private void copySection(dev.dejvokep.boostedyaml.block.implementation.Section source, YamlDocument target, String targetPath, InventorySetupInstance instance, org.bukkit.inventory.ItemStack item, String itemName, String nexoId, int slot) {
        for (Object keyObj : source.getKeys()) {
            String key = keyObj.toString();
            Object value = source.get(key);
            String newPath = targetPath + "." + key;

            if (value instanceof dev.dejvokep.boostedyaml.block.implementation.Section) {
                // Rekurzivně zpracuj vnořenou sekci
                copySection((dev.dejvokep.boostedyaml.block.implementation.Section) value, target, newPath, instance, item, itemName, nexoId, slot);
            } else {
                // Nahraď placeholdery a nastav hodnotu
                Object replaced = replacePlaceholders(value, instance, item, itemName, nexoId, slot);
                target.set(newPath, replaced);
            }
        }
    }

    private Object replacePlaceholders(Object value, InventorySetupInstance instance, org.bukkit.inventory.ItemStack item, String itemName, String nexoId, int slot) {
        if (value instanceof String) {
            String str = (String) value;
            str = str.replace("%var1%", instance.getArg1());
            str = str.replace("%var2%", instance.getArg2());
            str = str.replace("%var3%", instance.getArg3());
            str = str.replace("%var4%", instance.getArg4());

            // Formátované čísla
            try {
                str = str.replace("%var1_formatted%", formatNumber(instance.getArg1()));
                str = str.replace("%var2_formatted%", formatNumber(instance.getArg2()));
                str = str.replace("%var3_formatted%", formatNumber(instance.getArg3()));
                str = str.replace("%var4_formatted%", formatNumber(instance.getArg4()));
            } catch (NumberFormatException ignored) {}

            // Item placeholdery
            str = str.replace("%item_name%", itemName);
            str = str.replace("%item_id%", nexoId);

            return str;
        } else if (value instanceof List) {
            List<Object> newList = new java.util.ArrayList<>();
            for (Object obj : (List<?>) value) {
                newList.add(replacePlaceholders(obj, instance, item, itemName, nexoId, slot));
            }
            return newList;
        }
        return value;
    }

    private String formatNumber(String number) {
        try {
            double num = Double.parseDouble(number);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            df.setDecimalFormatSymbols(symbols);
            return df.format(num);
        } catch (NumberFormatException e) {
            return number;
        }
    }

    private String getItemDisplayName(org.bukkit.inventory.ItemStack item, String nexoId) {
        String serialize = LegacyComponentSerializer.legacyAmpersand()
                .serialize(item.displayName());
        return serialize.replace("&f[", "").replace("&f]", "");

    }


    @Getter
    @AllArgsConstructor
    public class InventorySetupInstance {
        private Player player;
        private Inventory inventory;

        private YamlDocument template;
        private String arg1;
        private String arg2;
        private String arg3;
        private String arg4;
    }

}
