package cz.gennario.gennarioframework.utilcommands;

import cz.gennario.gennarioframework.commands.GennarioCommand;
import cz.gennario.gennarioframework.utils.LocationUtils;
import cz.gennario.gennarioframework.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LocationCommand {

    public LocationCommand(JavaPlugin plugin) {
        // Main command: /stringifyLocation
        GennarioCommand.create("stringifyLocation", plugin)
                .description("Get your current location or location with center")
                .permission("gennarioframework.location")
                .playerOnly()
                .executes(ctx -> {
                    Player player = (Player) ctx.getSender();
                    String locationString = LocationUtils.locationToString(player.getLocation());

                    Component message = Component.text("GennarioFramework ", NamedTextColor.LIGHT_PURPLE)
                            .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                            .clickEvent(ClickEvent.copyToClipboard(locationString))
                            .append(Component.text("- ", NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text("Location: ", NamedTextColor.WHITE)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text(locationString, NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text(" ", NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text("(Click to copy)", NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)));

                    player.sendMessage(message);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                })
                // Subcommand: /stringifyLocation center
                .subCommand("center")
                .description("Get your location with centered coordinates")
                .permission("gennarioframework.location.center")
                .playerOnly()
                .executes(ctx -> {
                    Player player = (Player) ctx.getSender();
                    String locationString = LocationUtils.locationToStringCenter(player.getLocation());

                    Component message = Component.text("GennarioFramework ", NamedTextColor.LIGHT_PURPLE)
                            .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                            .clickEvent(ClickEvent.copyToClipboard(locationString))
                            .append(Component.text("- ", NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text("Location: ", NamedTextColor.WHITE)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text(locationString, NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text(" ", NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)))
                            .append(Component.text("(Click to copy)", NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Component.text(locationString, NamedTextColor.YELLOW)))
                                    .clickEvent(ClickEvent.copyToClipboard(locationString)));

                    player.sendMessage(message);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                })
                .done()
                .register();
    }
}
