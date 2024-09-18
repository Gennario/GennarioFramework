package cz.gennario.gennarioframework.entities;

import cz.gennario.gennarioframework.entities.types.EntityArmorstand;
import cz.gennario.gennarioframework.entities.types.EntityItemDisplay;
import cz.gennario.gennarioframework.entities.types.EntityTextDisplay;
import cz.gennario.gennarioframework.entities.types.hologram.EntityHologram;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public final class PacketEntityUtils {

    private JavaPlugin plugin;
    private final Map<Integer, PacketEntity> entities;

    private BukkitRunnable ticker;

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
        ticker = new BukkitRunnable() {
            int updateCount = 0;

            @Override
            public void run() {
                synchronized (entities) {
                    entities.forEach((integer, packetEntity) -> {
                        Location location = packetEntity.getLocation().clone();
                        location.setY(0);

                        for (Player player : location.getWorld().getPlayers()) {
                            if (packetEntity.getEntityVisiblity() == PacketEntity.EntityVisiblity.PRIVATE) {
                                if (!packetEntity.getVisiblityList().contains(player.getName())) {
                                    continue;
                                }
                            }

                            Location clone = player.getLocation().clone();
                            clone.setY(0);

                            if (packetEntity.getSpawnedPlayers().contains(player)) {
                                if (packetEntity.isUpdate()) {
                                    if (updateCount % packetEntity.getUpdateTime() == 0) {
                                        packetEntity.update(player);
                                    }
                                }

                                if (location.distance(clone) > packetEntity.getViewDistance()) {
                                    packetEntity.destroy(player);
                                }

                                if(packetEntity.getPacketVisiblityCondition() != null) {
                                    if (!packetEntity.getPacketVisiblityCondition().canSee(player)) {
                                        packetEntity.destroy(player);
                                        return;
                                    }
                                }
                                continue;
                            }

                            if (location.distance(clone) <= packetEntity.getViewDistance()) {
                                packetEntity.spawn(player);
                            }
                        }
                    });

                    updateCount++;
                    if (updateCount >= 20000)
                        updateCount = 0;
                }
            }
        };
        ticker.runTaskTimerAsynchronously(plugin, 0, 1);
    }

}
