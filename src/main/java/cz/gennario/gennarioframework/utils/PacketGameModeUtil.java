package cz.gennario.gennarioframework.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class PacketGameModeUtil {

    public static void setPacketGameModeForEveryone(Player manipulated, GameMode gameMode) {
        setPacketGameMode(manipulated, gameMode, Bukkit.getOnlinePlayers().toArray(new Player[0]));
    }

    public static void setPacketGameMode(Player manipulated, GameMode gameMode, Player... players) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoActions().writeSafely(0, Collections.singleton(EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(manipulated);
        PlayerInfoData playerInfoData = new PlayerInfoData(
                profile,
                0,
                EnumWrappers.NativeGameMode.fromBukkit(gameMode),
                WrappedChatComponent.fromText(manipulated.getDisplayName())
        );

        List<PlayerInfoData> list = List.of(playerInfoData);
        packet.getPlayerInfoDataLists().write(1, list);

        for (Player onlinePlayer : players) {
            if(onlinePlayer != manipulated)
                protocolManager.sendServerPacket(onlinePlayer, packet);
        }
    }

}
