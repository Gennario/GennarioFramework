package cz.gennario.gennarioframework.utils.packet.backend;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PacketEventsPacketBackend implements PacketBackend {

    private PacketListenerAbstract clickListener;

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("packetevents")
                || Bukkit.getPluginManager().isPluginEnabled("PacketEvents")
                || Bukkit.getPluginManager().getPlugin("packetevents") != null
                || Bukkit.getPluginManager().getPlugin("PacketEvents") != null;
    }

    @Override
    public void init() {
        if (!isAvailable()) {
            throw new IllegalStateException("PacketEvents plugin is not enabled.");
        }

        if (clickListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(clickListener);
        }

        clickListener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
                    return;
                }

                Object rawPlayer = event.getPlayer();
                if (!(rawPlayer instanceof Player player)) {
                    return;
                }

                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
                int entityId = wrapper.getEntityId();
                PacketClickResponse response = PacketUtils.entityClickMap.get(entityId);
                if (response == null) {
                    return;
                }

                PacketClickType clickType = mapClickType(wrapper);
                if (clickType == null) {
                    return;
                }

                List<PacketClickType> clickTypes = new ArrayList<>(1);
                clickTypes.add(clickType);
                response.onClick(clickTypes, player);
            }
        };

        PacketEvents.getAPI().getEventManager().registerListener(clickListener);
    }

    @Override
    public void shutdown() {
        if (clickListener != null) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(clickListener);
            } catch (Exception ignored) {
            }
            clickListener = null;
        }
    }

    private PacketClickType mapClickType(WrapperPlayClientInteractEntity wrapper) {
        boolean shift = wrapper.isSneaking().orElse(false);
        WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();

        if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return shift ? PacketClickType.SHIFT_LEFT : PacketClickType.LEFT;
        }

        if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT
                || action == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
            InteractionHand hand = wrapper.getHand();
            boolean hasTarget = wrapper.getTarget().isPresent();

            if (hand == null && !hasTarget) {
                return shift ? PacketClickType.SHIFT_LEFT : PacketClickType.LEFT;
            }

            return shift ? PacketClickType.SHIFT_RIGHT : PacketClickType.RIGHT;
        }

        return null;
    }
}
