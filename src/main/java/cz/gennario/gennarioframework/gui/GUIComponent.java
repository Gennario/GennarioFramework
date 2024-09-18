package cz.gennario.gennarioframework.gui;

import cz.gennario.gennarioframework.gui.utils.ClickData;
import cz.gennario.gennarioframework.gui.utils.InventoryBackgrounding;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public abstract class GUIComponent {

    private InventoryBackgrounding.BackgroundData backgroundData;

    public abstract ItemStack initStack(Player player);
    public abstract void onClick(Player player, ClickData clickData);

}
