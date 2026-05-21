package cz.gennario.gennarioframework.utils.packet.backend;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PacketEventsPacketBackend implements PacketBackend {

    private PacketListenerAbstract clickListener;

    // Deduplication for right click — older MC versions send both INTERACT_AT and INTERACT per click
    private final ConcurrentHashMap<String, Long> lastRightClickTime = new ConcurrentHashMap<>();
    private static final long RIGHT_CLICK_DEDUP_MS = 50L;

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
                Object rawPlayer = event.getPlayer();
                if (!(rawPlayer instanceof Player player)) {
                    return;
                }

                // 26.1.X+: left click uses a dedicated ATTACK packet instead of INTERACT_ENTITY
                if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
                    WrapperPlayClientAttack wrapper = new WrapperPlayClientAttack(event);
                    PacketClickResponse response = PacketUtils.entityClickMap.get(wrapper.getEntityId());
                    if (response == null) {
                        return;
                    }
                    PacketClickType clickType = player.isSneaking() ? PacketClickType.SHIFT_LEFT : PacketClickType.LEFT;
                    response.onClick(List.of(clickType), player);
                    return;
                }

                if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
                    return;
                }

                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
                int entityId = wrapper.getEntityId();
                PacketClickResponse response = PacketUtils.entityClickMap.get(entityId);
                if (response == null) {
                    return;
                }

                PacketClickType clickType = mapClickType(wrapper, player);
                if (clickType == null) {
                    return;
                }

                // Dedup right clicks — older versions send both INTERACT_AT and INTERACT per click
                if (clickType == PacketClickType.RIGHT || clickType == PacketClickType.SHIFT_RIGHT) {
                    String dedupKey = player.getUniqueId() + ":" + entityId;
                    long now = System.currentTimeMillis();
                    Long last = lastRightClickTime.get(dedupKey);
                    if (last != null && now - last < RIGHT_CLICK_DEDUP_MS) {
                        return;
                    }
                    lastRightClickTime.put(dedupKey, now);
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

    private PacketClickType mapClickType(WrapperPlayClientInteractEntity wrapper, Player player) {
        boolean shift = wrapper.isSneaking().orElse(player.isSneaking());
        WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();

        // Older versions: left click sends ATTACK action inside INTERACT_ENTITY
        if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return shift ? PacketClickType.SHIFT_LEFT : PacketClickType.LEFT;
        }

        // INTERACT and INTERACT_AT both represent right click.
        // 26.1.X+ hardcodes INTERACT_AT for all INTERACT_ENTITY packets.
        // Older versions send both INTERACT_AT and INTERACT — dedup handles the double fire.
        if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT
                || action == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
            if (wrapper.getHand() == InteractionHand.OFF_HAND) {
                return null;
            }
            return shift ? PacketClickType.SHIFT_RIGHT : PacketClickType.RIGHT;
        }

        return null;
    }
}
