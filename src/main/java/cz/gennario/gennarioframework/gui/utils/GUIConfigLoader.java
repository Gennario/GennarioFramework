package cz.gennario.gennarioframework.gui.utils;

import cz.gennario.gennarioframework.gui.GUIComponent;
import cz.gennario.gennarioframework.gui.GUIContainer;
import cz.gennario.gennarioframework.utils.items.Items;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIConfigLoader {

    private List<String> layout;
    private Map<Character, GUIComponent> components;

    public GUIConfigLoader(List<String> layout) {
        this.layout = layout;
        this.components = new HashMap<>();
    }

    public GUIConfigLoader loadComponents(Section section) {
        for (String route : section.getRoutesAsStrings(false)) {
            Section componentSection = section.getSection(route);
            GUIComponent guiComponent = new GUIComponent() {
                @Override
                public ItemStack initStack(Player player) {
                    return new Items().setItem(componentSection);
                }

                @Override
                public void onClick(Player player, ClickData clickData) {

                }
            };
            components.put(route.charAt(0), guiComponent);
        }
        return this;
    }

    public void insertComponents(GUIContainer guiContainer) {
        for (Character aChar : getChars()) {
            List<Integer> positions = getCharPositions(aChar);
            GUIComponent guiComponent = components.get(aChar);
            if (guiComponent != null) {
                for (int position : positions) {
                    guiContainer.setComponent(position, guiComponent);
                }
            }
        }
    }

    public List<Character> getChars() {
        List<Character> chars = new ArrayList<>();
        for (String s : layout) {
            for (int i = 0; i < 9; i++) {
                char c = s.charAt(i);
                if (c != ' ' && !chars.contains(c)) {
                    chars.add(c);
                }
            }
        }
        return chars;
    }

    public List<Integer> getCharPositions(char c) {
        int slot = 0;
        List<Integer> positions = new ArrayList<>();
        for (String s : layout) {
            for (int i = 0; i < 9; i++) {
                if (s.charAt(i) == c) {
                    positions.add(slot);
                }
                slot++;
            }
        }
        return positions;
    }

}
