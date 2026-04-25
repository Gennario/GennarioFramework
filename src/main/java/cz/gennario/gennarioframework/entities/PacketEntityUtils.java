package cz.gennario.gennarioframework.entities;

import cz.gennario.gennarioframework.entities.types.EntityArmorstand;
import cz.gennario.gennarioframework.entities.types.EntityInteraction;
import cz.gennario.gennarioframework.entities.types.EntityItemDisplay;
import cz.gennario.gennarioframework.entities.types.EntityTextDisplay;
import cz.gennario.gennarioframework.entities.types.hologram.EntityHologram;
import cz.gennario.gennarioframework.utils.FoliaScheduler;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public final class PacketEntityUtils {

    private JavaPlugin plugin;
    private final Map<Integer, PacketEntity> entities;

    private FoliaScheduler.WrappedTask ticker;

    public PacketEntityUtils(JavaPlugin plugin) {
        this.plugin = plugin;
        entities = new ConcurrentHashMap<>();

        startTicker();
    }

    public PacketEntity createEntity(PacketEntity.EntityType entityType, Location location, PacketEntity.EntityVisiblity entityVisiblity, PacketEntityOptionalData packetEntityOptionalData) {
        PacketEntity packetEntity = null;
        switch (entityType) {
            case TEXT_DISPLAY: {
                packetEntity = new EntityTextDisplay(location, entityVisiblity);
                break;
            }
            case ARMOR_STAND: {
                packetEntity = new EntityArmorstand(location, entityVisiblity);
                break;
            }
            case ITEM_DISPLAY: {
                packetEntity = new EntityItemDisplay(entityVisiblity, packetEntityOptionalData.getItemDisplayPlayerItem());
                packetEntity.teleport(location);
                ((EntityItemDisplay) packetEntity).getPacketItemDisplay().setLocation(location);
                break;
            }
            case HOLOGRAM: {
                packetEntity = new EntityHologram(this, entityVisiblity, location);
                break;
            }
            case INTERACTION: {
                float width = packetEntityOptionalData.getInteractionWidth();
                float height = packetEntityOptionalData.getInteractionHeight();
                boolean responsive = packetEntityOptionalData.isInteractionResponsive();
                packetEntity = new EntityInteraction(location, entityVisiblity, width, height, responsive);
                break;
            }
        }
        entities.put(packetEntity.getId(), packetEntity);
        return packetEntity;
    }

    public PacketEntity createEntity(PacketEntity.EntityType entityType, Location location, PacketEntity.EntityVisiblity entityVisiblity) {
        return createEntity(entityType, location, entityVisiblity, new PacketEntityOptionalData());
    }

    public void removeEntity(PacketEntity packetEntity) {
        synchronized (entities) {
            entities.remove(packetEntity.getId());
            packetEntity.destroyAll();
        }
    }

    public void removeEntity(int id) {
        synchronized (entities) {
            entities.get(id).destroyAll();
            entities.remove(id);
        }
    }

    public void removeAll() {
        synchronized (entities) {
            entities.forEach((integer, packetEntity) -> {
                packetEntity.destroyAll();
            });
            entities.clear();
        }
    }

    private void startTicker() {
        ticker = FoliaScheduler.runAsyncTimer(plugin, new Runnable() {
            int updateCount = 0;

            @Override
            public void run() {
                synchronized (entities) {
                    entities.forEach((integer, packetEntity) -> {
                        Location location = packetEntity.getLocation().clone();
                        location.setY(0);

                        if (location.getWorld() == null) return;

                        for (Player player : new ArrayList<>(packetEntity.getSpawnedPlayers())) {
                            if (!player.isOnline() || player.getLocation().getWorld() != location.getWorld()) {
                                packetEntity.destroy(player);
                            }
                        }

                        for (Player player : location.getWorld().getPlayers()) {
                            if (packetEntity.getEntityVisiblity() == PacketEntity.EntityVisiblity.PRIVATE) {
                                if (!packetEntity.getVisiblityList().contains(player.getName())) {
                                    if (packetEntity.getSpawnedPlayers().contains(player)) {
                                        packetEntity.destroy(player);
                                    }
                                    continue;
                                }
                            }

                            Location clone = player.getLocation().clone();
                            clone.setY(0);

                            double distance = location.distance(clone);
                            boolean isSpawned = packetEntity.getSpawnedPlayers().contains(player);

                            boolean canSee = true;
                            if (packetEntity.getPacketVisiblityCondition() != null) {
                                canSee = packetEntity.getPacketVisiblityCondition().canSee(player);
                            }

                            if (isSpawned) {
                                if (packetEntity.isUpdate()) {
                                    if (updateCount % packetEntity.getUpdateTime() == 0) {
                                        packetEntity.update(player);
                                    }
                                }

                                if (distance > packetEntity.getViewDistance() || !canSee) {
                                    packetEntity.destroy(player);
                                }
                            } else {
                                if (distance <= packetEntity.getViewDistance() && canSee) {
                                    packetEntity.spawn(player);
                                }
                            }
                        }
                    });

                    updateCount++;
                    if (updateCount >= 20000) {
                        updateCount = 0;
                    }
                }
            }
        }, 0, 1);
    }


//    private void startTicker() {
//        final int[] updateCount = {0};
//        ticker = FoliaScheduler.runSyncTimer(plugin, () -> {
//            synchronized (entities) {
//                entities.forEach((integer, packetEntity) -> {
//                    Location location = packetEntity.getLocation().clone();
//                    location.setY(0);
//
//                    for (Player player : location.getWorld().getPlayers()) {
//                        if (packetEntity.getEntityVisiblity() == PacketEntity.EntityVisiblity.PRIVATE) {
//                            if (!packetEntity.getVisiblityList().contains(player.getName())) {
//                                continue;
//                            }
//                        }
//
//                        Location clone = player.getLocation().clone();
//                        clone.setY(0);
//
//                        if (packetEntity.getSpawnedPlayers().contains(player)) {
//                            if (packetEntity.isUpdate()) {
//                                if (updateCount[0] % packetEntity.getUpdateTime() == 0) {
//                                    packetEntity.update(player);
//                                }
//                            }
//
//                            if (location.distance(clone) > packetEntity.getViewDistance()) {
//                                packetEntity.destroy(player);
//                            }
//
//                            if(packetEntity.getPacketVisiblityCondition() != null) {
//                                if (!packetEntity.getPacketVisiblityCondition().canSee(player)) {
//                                    packetEntity.destroy(player);
//                                    return;
//                                }
//                            }
//                            continue;
//                        }
//
//                        if (location.distance(clone) <= packetEntity.getViewDistance()) {
//                            packetEntity.spawn(player);
//                        }
//                    }
//                });
//
//                updateCount[0]++;
//                if (updateCount[0] >= 20000)
//                    updateCount[0] = 0;
//            }
//        }, 0, 1);
//    }

}
