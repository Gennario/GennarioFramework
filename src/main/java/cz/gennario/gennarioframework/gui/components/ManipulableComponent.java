package cz.gennario.gennarioframework.gui.components;

import cz.gennario.gennarioframework.gui.GUIComponent;
import cz.gennario.gennarioframework.gui.utils.ClickData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ManipulableComponent extends GUIComponent {

    private ItemStack staticItem, activeItem, cursorItem;
    private Map<Player, Boolean> active;

    public ManipulableComponent(ItemStack staticItem) {

        this.staticItem = staticItem;
        this.activeItem = staticItem;
        this.cursorItem = staticItem;

        active = new HashMap<>();
    }

    public ManipulableComponent(ItemStack staticItem, ItemStack activeItem) {

        this.staticItem = staticItem;
        this.activeItem = activeItem;
        this.cursorItem = activeItem;

        active = new HashMap<>();
    }

    public ManipulableComponent(ItemStack staticItem, ItemStack activeItem, ItemStack cursorItem) {

        this.staticItem = staticItem;
        this.activeItem = activeItem;
        this.cursorItem = cursorItem;

        active = new HashMap<>();
    }

    @Override
    public ItemStack initStack(Player player) {
        if (isActive(player)) return activeItem;
        return staticItem;
    }

    @Override
    public void onClick(Player player, ClickData clickData) {
        if (isActive(player)) {
            setActive(player, false);

            player.setItemOnCursor(null);
            clickData.getGennarioGUI().update(player);
        } else {
            setActive(player, true);

            clickData.getGennarioGUI().update(player);
            player.setItemOnCursor(cursorItem);
        }
    }

    public void setActive(Player player, boolean active) {
        this.active.put(player, active);
    }

    public boolean isActive(Player player) {
        return active.getOrDefault(player, false);
    }
}
