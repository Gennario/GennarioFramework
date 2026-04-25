package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Player argument - resolves to an online Player.
 */
public class PlayerArgument extends Argument<Player> {

    public PlayerArgument(String name) {
        super(name, Player.class);
    }

    @Override
    public Player parse(String input) throws ArgumentParseException {
        Player player = Bukkit.getPlayer(input);
        if (player == null) {
            throw new ArgumentParseException(input, "online player name");
        }
        return player;
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
