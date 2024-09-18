package cz.gennario.gennarioframework.gui.components;

import cz.gennario.gennarioframework.gui.GUIComponent;
import cz.gennario.gennarioframework.gui.utils.ClickData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SimpleComponent extends GUIComponent {

    private ItemStack item;

    public SimpleComponent(ItemStack item) {
        this.item = item;
    }

    @Override
    public ItemStack initStack(Player player) {
        return item;
    }

    @Override
    public void onClick(Player player, ClickData clickData) {
    }

}
