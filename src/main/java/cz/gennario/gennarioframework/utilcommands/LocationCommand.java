package cz.gennario.gennarioframework.utilcommands;

import cz.gennario.gennarioframework.utils.LocationUtils;
import cz.gennario.gennarioframework.utils.TextComponentUtils;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.commands.CommandAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LocationCommand extends CommandAPI {


    public LocationCommand(JavaPlugin plugin) {
        super(plugin, "stringifyLocation");

        setPermission("gennarioframework.location");
        setHelp(false);
        setEmptyCommandResponse((sender, label, commandArgs) -> {

            if (sender instanceof Player) {
                Player player = (Player) sender;
                String string = LocationUtils.locationToString(player.getLocation());

                TextComponent textComponent = TextComponentUtils.create(Utils.colorize("&dGennarioFramework &7- &fLocation: &e" + string + " &7(Click to copy)"));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponentUtils.create(Utils.colorize(string+" - click copy")).getExtra().toArray(new BaseComponent[0])));
                player.spigot().sendMessage(textComponent);

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        });

        addCommand("--center")
                .setPermission("gennarioframework.location.center")
                .setDescription("Get location with center")
                .setAllowConsoleSender(false)
                .setResponse((sender, label, commandArgs) -> {
                    Player player = (Player) sender;
                    String string = LocationUtils.locationToStringCenter(player.getLocation());

                    TextComponent textComponent = TextComponentUtils.create(Utils.colorize("&dGennarioFramework &7- &fLocation: &e" + string + " &7(Click to copy)"));
                    textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string));
                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponentUtils.create(Utils.colorize(string+" - click copy")).getExtra().toArray(new BaseComponent[0])));
                    player.spigot().sendMessage(textComponent);

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                });

        buildCommand();
    }
}
