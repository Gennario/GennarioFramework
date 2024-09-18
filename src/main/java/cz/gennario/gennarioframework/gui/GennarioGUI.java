package cz.gennario.gennarioframework.gui;

import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.gui.containers.GUIPagedContainer;
import cz.gennario.gennarioframework.gui.utils.ClickData;
import cz.gennario.gennarioframework.gui.utils.InventoryBackgrounding;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class GennarioGUI implements Listener {

    private Map<Player, GGUIHolder> holders;

    private GUIContainer container;

    private InventoryType inventoryType;
    private String title;
    private int rows;

    private GUISettings settings;

    private String backgroundingData;

    public GennarioGUI(String title, int rows) {
        this.holders = new HashMap<>();
        this.inventoryType = InventoryType.CHEST;
        this.title = title;
        this.rows = rows;

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

        container = new GUIContainer(rows, 9);
        settings = new GUISettings();
    }

    public GennarioGUI(InventoryType inventoryType, String title) {
        this.holders = new HashMap<>();
        this.inventoryType = inventoryType;
        this.title = title;

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

        container = new GUIContainer(1, inventoryType.getDefaultSize());
        settings = new GUISettings();
    }

    public GennarioGUI(InventoryType inventoryType, String title, int rows) {
        this.holders = new HashMap<>();
        this.inventoryType = inventoryType;
        this.title = title;
        this.rows = rows;

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

        container = new GUIContainer(rows, inventoryType.getDefaultSize());
        settings = new GUISettings();
    }

    public abstract GUIContainer init(Player player, GUIContainer container);

    public abstract void onOpen(InventoryOpenEvent event);
    public abstract void onClose(InventoryCloseEvent event);

    public void autoUpdate(int time) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : holders.keySet()) {
                    if(player.getOpenInventory().getTopInventory().equals(holders.get(player).getInventory())) {
                        update(player, false);
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, time);
    }

    public void open(Player player) {
        if(holders.containsKey(player)) {
            update(player, true);
            return;
        }

        GGUIHolder holder = new GGUIHolder(player, this);
        if(backgroundingData != null) {
            String[] split = backgroundingData.split(":---:");
            holder.setInventoryBackgrounding(new InventoryBackgrounding(rows, split[0], split[1]));
        }
        holder.clearClickEventsCache(player);
        if(settings.getOpenMethod().equals(GUISettings.OpenMethod.ASYNC)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    holder.loadContainer(0, container.clone(), true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            InventoryView inventoryView = player.openInventory(holder.getInventory());
                            if(holder.getInventoryBackgrounding() != null) {
                                inventoryView.setTitle(holder.getInventoryBackgrounding().generateBackground());
                            }else {
                                inventoryView.setTitle(holder.getTitle());
                            }
                            holders.put(player, holder);
                        }
                    }.runTask(Main.getInstance());
                }
            }.runTaskAsynchronously(Main.getInstance());
        } else {
            holder.loadContainer(0, container.clone(), true);
            InventoryView inventoryView = player.openInventory(holder.getInventory());
            if(holder.getInventoryBackgrounding() != null) {
                inventoryView.setTitle(holder.getInventoryBackgrounding().generateBackground());
            }else {
                inventoryView.setTitle(holder.getTitle());
            }
            holders.put(player, holder);
        }
    }

    public void update(Player player) {
        update(player, true);
    }

    public void update(Player player, boolean clear) {
        if(holders.containsKey(player)) {
            GGUIHolder holder = holders.get(player);
            if(holder.getInventoryBackgrounding() != null) holder.getInventoryBackgrounding().clear();
            holder.clearClickEventsCache(player);

            if(!clear) {
                container.getContainers().forEach((integer, guiContainer) -> {
                    if(guiContainer instanceof GUIPagedContainer) {
                        for (Integer slot : guiContainer.getSlots()) {
                            holder.getInventory().setItem(slot, null);
                        }
                    }
                });
            }else {
                holder.getInventory().clear();
            }


            if(settings.getOpenMethod().equals(GUISettings.OpenMethod.ASYNC)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        holder.loadContainer(0, container.clone(), false);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                InventoryView inventoryView = player.openInventory(holder.getInventory());
                                if(holder.getInventoryBackgrounding() != null) {
                                    inventoryView.setTitle(holder.getInventoryBackgrounding().generateBackground());
                                }else {
                                    inventoryView.setTitle(holder.getTitle());
                                }
                            }
                        }.runTask(Main.getInstance());
                    }
                }.runTaskAsynchronously(Main.getInstance());
            } else {
                holder.loadContainer(0, container.clone(), false);
                InventoryView inventoryView = player.openInventory(holder.getInventory());//player.openInventory(holder.getInventory());
                if(holder.getInventoryBackgrounding() != null) {
                    inventoryView.setTitle(holder.getInventoryBackgrounding().generateBackground());
                }else {
                    inventoryView.setTitle(holder.getTitle());
                }
            }
        }
    }

    public void updatePlayerTitle(Player player, String title) {
        GGUIHolder gguiHolder = holders.get(player);
        gguiHolder.setTitle(title);
    }

    public void allowBackgrounding(String prebackground, String title) {
        backgroundingData = prebackground + ":---:" + title;
    }

    @EventHandler
    public void onInteract(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        if(event.getClickedInventory().getHolder() == null) return;

        if(event.getWhoClicked() instanceof Player player) {
            if (holders.containsKey(player)) {
                GGUIHolder holder = holders.get(player);
                if (holder.getInventory().equals(event.getClickedInventory())) {
                    event.setCancelled(true);
                    if(event.getCurrentItem() == null) return;
                    NBTItem nbtItem = new NBTItem(event.getCurrentItem());
                    if(nbtItem.hasTag("gui-item")) {
                        if (holder.clickEventCacheContains(player, nbtItem.getInteger("gui-item"))) {
                            holder.getClickEventCache(player, nbtItem.getInteger("gui-item")).onClick(player, new ClickData(this, event));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCloseInv(InventoryCloseEvent event) {
        if(event.getPlayer() instanceof Player player) {
            if (holders.containsKey(player)) {
                GGUIHolder holder = holders.get(player);
                if (holder.getInventory().equals(event.getInventory())) {
                    onClose(event);
                    if(settings.isPreventClose()) {
                        if(settings.getCloseAllowed().contains(player)) {
                            settings.getCloseAllowed().remove(player);
                        }else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    open(player);
                                }
                            }.runTaskLater(Main.getInstance(), settings.getReopenDelay());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onOpenInv(InventoryOpenEvent event) {
        if(event.getPlayer() instanceof Player player) {
            if (holders.containsKey(player)) {
                GGUIHolder holder = holders.get(player);
                if (holder.getInventory().equals(event.getInventory())) {
                    onOpen(event);
                }
            }
        }
    }

    @EventHandler
    public void onItemDrag(InventoryDragEvent event) {
        if(event.getWhoClicked() instanceof Player player) {
            if (holders.containsKey(player)) {
                GGUIHolder holder = holders.get(player);
                if (holder.getInventory().equals(event.getInventory())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public GUISettings getSettings() {
        return settings;
    }
}