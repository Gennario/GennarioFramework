package cz.gennario.gennarioframework.gui;

import cz.gennario.gennarioframework.gui.containers.GUIPagedContainer;
import cz.gennario.gennarioframework.gui.containers.GUIScrollContainer;
import cz.gennario.gennarioframework.gui.containers.GUITabsContainer;
import cz.gennario.gennarioframework.gui.utils.InventoryBackgrounding;
import cz.gennario.gennarioframework.utils.Utils;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GGUIHolder {

    private GennarioGUI gui;

    private Player player;
    private Inventory inventory;

    private String title;

    private Map<Player, Map<Integer, GUIComponent>> clickEventsCache;

    private InventoryBackgrounding inventoryBackgrounding;

    public GGUIHolder(Player player, GennarioGUI gui) {
        this.gui = gui;
        this.player = player;
        this.title = gui.getTitle();

        if (gui.getInventoryType().name().contains("CHEST"))
            this.inventory = Bukkit.createInventory(player, gui.getRows() * 9, Utils.colorize(player, gui.getTitle()));
        else
            this.inventory = Bukkit.createInventory(player, gui.getInventoryType(), Utils.colorize(player, gui.getTitle()));

        inventory.setMaxStackSize(128);
        this.clickEventsCache = new HashMap<>();
    }

    public void loadContainer(int startSlot, GUIContainer container, boolean reinit) {
        if (reinit) {
            container = gui.init(player, gui.getContainer().clone());
        }

        List<Integer> slots = calcSlots(startSlot, container);

        container.getComponents().forEach((slot, component) -> {
            if (component != null) {
                setItem(slots.get(slot), component);
                if (component.getBackgroundData() != null) {
                    inventoryBackgrounding.append(slots.get(slot), component.getBackgroundData());
                }
            }
        });

        container.getContainers().forEach((slot, guiContainer) -> {
            if (guiContainer instanceof GUIPagedContainer pagedContainer) {
                loadContainer(slots.get(slot), pagedContainer);
            } else if (guiContainer instanceof GUITabsContainer tabsContainer) {
                loadContainer(slots.get(slot), tabsContainer);
            } else if (guiContainer instanceof GUIScrollContainer scrollContainer) {
                loadContainer(slots.get(slot), scrollContainer);
            } else {
                loadContainer(slots.get(slot), guiContainer, false);
            }
        });

    }

    public void loadContainer(int slot, GUIPagedContainer container) {
        List<Integer> slots = calcSlots(slot, container);

        container.loadItems(player);
        container.getComponents().forEach((integer, guiComponent) -> {
            if (integer >= slots.size()) {
                System.out.println("Item slot is out of range! (slot: " + integer + ", slots: " + slots.size() + ")");
            } else {
                setItem(slots.get(integer), guiComponent);
                if (guiComponent.getBackgroundData() != null) {
                    inventoryBackgrounding.append(slots.get(integer), guiComponent.getBackgroundData());
                }
            }
        });
    }

    public void loadContainer(int slot, GUITabsContainer container) {
        if (container.getCurrentTab(player) == null) {
            container.setCurrentTab(player, container.getDefaultTab());
            if (container.getCurrentTab(player) == null) {
                Bukkit.broadcastMessage((new ArrayList<>(container.getTabs().values()).get(0) == null) + " ? null");
                System.out.println("Current tab is null! (player: " + player.getName() + ") (tab container size: " + container.getTabs().size() + ")");
            }
        }
        loadContainer(slot, container.getCurrentTab(player), false);
    }

    public void loadContainer(int slot, GUIScrollContainer container) {
        List<Integer> slots = calcSlots(slot, container);

        List<GUIComponent> lines = container.getCurrentLines(player);
        container.getComponents().clear();
        for (GUIComponent line : lines) {
            container.appendComponent(line);
        }
        container.getComponents().forEach((integer, guiComponent) -> {
            if (integer >= slots.size()) {
                System.out.println("Item slot is out of range! (slot: " + integer + ", slots: " + slots.size() + ")");
            } else {
                setItem(slots.get(integer), guiComponent);
                if (guiComponent.getBackgroundData() != null) {
                    inventoryBackgrounding.append(slots.get(integer), guiComponent.getBackgroundData());
                }
            }
        });
    }

    public List<Integer> calcSlots(int slot, GUIContainer container) {
        List<Integer> slots = new ArrayList<>();
        int utilitySlot = slot;
        int column = 0;
        for (int i = 0; i < container.getWidth() * container.getRows(); i++) {
            slots.add(utilitySlot);
            if (column + 1 >= container.getWidth()) {
                column = 0;
                utilitySlot += 10 - container.getWidth();
            } else {
                column++;
                utilitySlot++;
            }
        }
        return slots;
    }

    public void setItem(int slot, GUIComponent component) {
        if (component == null) return;
        ItemStack item = component.initStack(player);
        if (item == null) return;
        if (item.getType().isAir()) return;
        if (item.getType().equals(Material.AIR)) {
            inventory.setItem(slot, item);
            return;
        }
        NBTItem nbtItem = new NBTItem(item);

        nbtItem.setInteger("gui-item", slot);
        inventory.setItem(slot, nbtItem.getItem());

        addClickEventCache(player, slot, component);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public GennarioGUI getGui() {
        return gui;
    }

    public Player getPlayer() {
        return player;
    }

    public Map<Player, Map<Integer, GUIComponent>> getClickEventsCache() {
        return clickEventsCache;
    }

    public void clearClickEventsCache(Player player) {
        clickEventsCache.remove(player);
    }

    public void addClickEventCache(Player player, Integer slot, GUIComponent value) {
        Map<Integer, GUIComponent> orDefault = clickEventsCache.getOrDefault(player, new HashMap<>());
        orDefault.put(slot, value);
        clickEventsCache.put(player, orDefault);
    }

    public void removeClickEventCache(Player player, Integer slot) {
        Map<Integer, GUIComponent> orDefault = clickEventsCache.getOrDefault(player, new HashMap<>());
        orDefault.remove(slot);
        clickEventsCache.put(player, orDefault);
    }

    public GUIComponent getClickEventCache(Player player, Integer slot) {
        Map<Integer, GUIComponent> orDefault = clickEventsCache.getOrDefault(player, new HashMap<>());
        return orDefault.get(slot);
    }

    public boolean clickEventCacheContains(Player player, Integer slot) {
        return clickEventsCache.getOrDefault(player, new HashMap<>()).containsKey(slot);
    }
}
