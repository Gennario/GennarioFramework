package cz.gennario.gennarioframework.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;

public final class PacketGameModeUtil {

    public static void setPacketGameModeForEveryone(Player manipulated, org.bukkit.GameMode gameMode) {
        setPacketGameMode(manipulated, gameMode, Bukkit.getOnlinePlayers().toArray(new Player[0]));
    }

    public static void setPacketGameMode(Player manipulated, org.bukkit.GameMode gameMode, Player... players) {
        UserProfile profile = new UserProfile(manipulated.getUniqueId(), manipulated.getName());

        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile,
                true,
                manipulated.getPing(),
                SpigotConversionUtil.fromBukkitGameMode(gameMode),
                null,
                null
        );

        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE),
                List.of(info)
        );

        for (Player observer : players) {
            if (observer != manipulated) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
            }
        }
    }
}
