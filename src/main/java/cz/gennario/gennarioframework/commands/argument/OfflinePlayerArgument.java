package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OfflinePlayer argument - resolves to an OfflinePlayer.
 * Tab completion shows online players.
 */
public class OfflinePlayerArgument extends Argument<OfflinePlayer> {

    public OfflinePlayerArgument(String name) {
        super(name, OfflinePlayer.class);
    }

    @Override
    public OfflinePlayer parse(String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            throw new ArgumentParseException(input, "player name");
        }
        // Try online first
        Player online = Bukkit.getPlayer(input);
        if (online != null) return online;
        // Fallback to offline
        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(input);
        return offline;
    }

    @Override
    public String getTypeName() {
        return "player";
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
