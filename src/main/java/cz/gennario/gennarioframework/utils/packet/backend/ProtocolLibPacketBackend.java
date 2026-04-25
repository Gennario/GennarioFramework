package cz.gennario.gennarioframework.utils.packet.backend;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProtocolLibPacketBackend implements PacketBackend {

    @Override
    public PacketBackendMode mode() {
        return PacketBackendMode.PROTOCOLLIB;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
    }

    @Override
    public void init() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                PacketAdapter.params(Main.getInstance(), PacketType.Play.Client.USE_ENTITY)
                        .optionAsync()) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType().equals(PacketType.Play.Client.USE_ENTITY)) {
                    event.setCancelled(false);
                }
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Main.getInstance(), PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                PacketContainer packet = e.getPacket();
                if (packet.getType() != PacketType.Play.Client.USE_ENTITY) {
                    return;
                }

                int id = packet.getIntegers().read(0);
                if (!PacketUtils.entityClickMap.containsKey(id)) {
                    return;
                }

                List<PacketClickType> clickTypeList = new ArrayList<>();
                EnumWrappers.EntityUseAction action = Utils.versionIsAfter(16)
                        ? packet.getEnumEntityUseActions().readSafely(0).getAction()
                        : packet.getEntityUseActions().readSafely(0);
                boolean isShift = packet.getBooleans().readSafely(0);

                switch (action.compareTo(EnumWrappers.EntityUseAction.INTERACT)) {
                    case 1 -> clickTypeList.add(isShift ? PacketClickType.SHIFT_LEFT : PacketClickType.LEFT);
                    case 2 -> clickTypeList.add(isShift ? PacketClickType.SHIFT_RIGHT : PacketClickType.RIGHT);
                    default -> {
                        return;
                    }
                }

                PacketClickResponse packetClickResponse = PacketUtils.entityClickMap.get(id);
                if (packetClickResponse != null) {
                    packetClickResponse.onClick(clickTypeList, e.getPlayer());
                }
            }
        });
    }

    @Override
    public void sendPacket(Player player, PacketContainer packet) throws Exception {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}


