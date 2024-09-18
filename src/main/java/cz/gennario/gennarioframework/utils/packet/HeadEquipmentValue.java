package cz.gennario.gennarioframework.utils.packet;

import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.items.Items;
import cz.gennario.gennarioframework.utils.replacement.Replacement;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Getter
@Setter
public class HeadEquipmentValue {

    public enum HeadEquipmentType {
        SYSTEM,
        CONFIG
    }

    private final Items items;
    private HeadEquipmentType type;
    private Section configData;
    private ReplacementPackage replacement;
    private ItemStack systemStack;

    public HeadEquipmentValue(@NotNull HeadEquipmentType type) {
        this.type = type;
        this.items = new Items();
        this.replacement = new ReplacementPackage().append(new Replacement((player, text) -> Utils.colorize(text)));
    }

    public @Nullable ItemStack convert(@Nullable Player player) {
        switch (type) {
            case CONFIG:
                return items.setItem(configData, player, replacement);
            case SYSTEM:
                return systemStack;
        }
        return null;
    }

}
