package cz.gennario.gennarioframework.gui.containers;

import cz.gennario.gennarioframework.gui.GUIContainer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
public class GUITabsContainer extends GUIContainer {

    Map<String, GUIContainer> tabs;
    private Map<Player, String> currentTab;
    private String defaultTab;

    public GUITabsContainer(int rows, int width) {
        super(rows, width);

        tabs = new HashMap<>();
        currentTab = new HashMap<>();
        defaultTab = "";
    }

    public GUITabsContainer appendTab(String name, GUIContainer container) {
        tabs.put(name, container);
        return this;
    }

    public GUITabsContainer setCurrentTab(Player player, String name) {
        if(!tabs.containsKey(name)) throw new IllegalArgumentException("Tab with name " + name + " does not exist");
        currentTab.put(player, name);
        return this;
    }

    public GUITabsContainer setDefaultTab(String name) {
        defaultTab = name;
        return this;
    }

    public GUIContainer getCurrentTab(Player player) {
        return tabs.get(currentTab.getOrDefault(player, defaultTab));
    }

    public String getCurrentTabName(Player player) {
        return currentTab.getOrDefault(player, defaultTab);
    }

}
