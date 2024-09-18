package cz.gennario.gennarioframework.gui.containers;

import cz.gennario.gennarioframework.gui.GUIComponent;
import cz.gennario.gennarioframework.gui.GUIContainer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class GUIPagedContainer extends GUIContainer {

    private Map<Player, Integer> page;
    Map<Player, List<GUIComponent>> pagedGuiComponents;

    public GUIPagedContainer(int rows, int width) {
        super(rows, width);
        page = new HashMap<>();
        pagedGuiComponents = new HashMap<>();
    }

    public abstract void initItems(Player player, GUIPagedContainer guiPagedContainer);

    public GUIPagedContainer insert(Player player, GUIComponent guiComponent) {
        List<GUIComponent> list = pagedGuiComponents.getOrDefault(player, new ArrayList<>());
        list.add(guiComponent);
        pagedGuiComponents.put(player, list);
        return this;
    }

    public List<GUIComponent> getPageComponents(Player player) {
        List<GUIComponent> orDefault = pagedGuiComponents.getOrDefault(player, new ArrayList<>());
        orDefault.clear();
        pagedGuiComponents.put(player, orDefault);

        initItems(player, this);

        int start = (getPage(player)-1) * getSize();
        int end = start + getSize();
        List<GUIComponent> components = new ArrayList<>();

        int offset = 0;
        for (GUIComponent component : getPagedGuiComponents().get(player)) {
            if(offset >= start && offset < end) {
                components.add(component);
            }
            offset++;
        }

        return components;
    }

    public void loadItems(Player player) {
        getComponents().clear();
        List<GUIComponent> pageComponents = getPageComponents(player);
        pageComponents.forEach(this::appendComponent);
    }

    public int getMaxPage(Player player) {
        if(!pagedGuiComponents.containsKey(player)) return 1;
        return (int) Math.floor(pagedGuiComponents.get(player).size() / getSize())+1;
    }

    public boolean nextPage(Player player) {
        if (!canNextPage(player)) return false;
        setPage(player, getPage(player)+1);
        loadItems(player);
        return true;
    }

    public boolean previousPage(Player player) {
        if (!canPreviousPage(player)) return false;
        setPage(player, getPage(player)-1);
        loadItems(player);
        return true;
    }

    public boolean canNextPage(Player player) {
        return getPage(player) < getMaxPage(player);
    }

    public boolean canPreviousPage(Player player) {
        return getPage(player) > 1;
    }

    public void setPage(Player player, int page) {
        this.page.put(player, page);
    }

    public int getPage(Player player) {
        return page.getOrDefault(player, 1);
    }

}
