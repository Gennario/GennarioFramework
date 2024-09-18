package cz.gennario.gennarioframework.gui.utils;

import cz.gennario.gennarioframework.gui.GennarioGUI;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Data
public class ClickData {

    private Player player;
    private GennarioGUI gennarioGUI;
    private int slot;
    private int rawSlot;
    private ItemStack itemStack;
    private ClickType clickType;

    public ClickData(GennarioGUI gennarioGUI, InventoryClickEvent event) {
        this.player = event.getWhoClicked().getKiller();
        this.gennarioGUI = gennarioGUI;
        this.slot = event.getSlot();
        this.rawSlot = event.getRawSlot();
        this.itemStack = event.getCurrentItem();
        this.clickType = event.getClick();
    }

}
