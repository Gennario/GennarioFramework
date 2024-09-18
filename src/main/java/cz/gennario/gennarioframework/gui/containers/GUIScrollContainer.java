package cz.gennario.gennarioframework.gui.containers;

import cz.gennario.gennarioframework.gui.GUIComponent;
import cz.gennario.gennarioframework.gui.GUIContainer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIScrollContainer extends GUIContainer {

    public enum ScrollDirection {
        HORISONTAL, VERTICAL
    }

    private Map<Player, Integer> lineOffset;
    private ScrollDirection scrollDirection;

    private List<List<GUIComponent>> lines;

    public GUIScrollContainer(int rows, int width, ScrollDirection scrollDirection) {
        super(rows, width);

        lineOffset = new HashMap<>();
        this.scrollDirection = scrollDirection;
        lines = new ArrayList<>();
    }

    public void addComponent(GUIComponent guiComponent) {
        int maxLineSize = getMaxLineSize();

        int index = lines.size() > 0 ? lines.size() - 1 : 0;
        List<GUIComponent> guiComponents = lines.size() > 0 ? lines.get(index) : new ArrayList<>();

        if(guiComponents.size() >= maxLineSize) {
            List<GUIComponent> newLine = new ArrayList<>();
            newLine.add(guiComponent);
            lines.add(newLine);
        }else {
            guiComponents.add(guiComponent);
            if(!lines.contains(guiComponents)) {
                lines.add(guiComponents);
            }else {
                lines.set(index, guiComponents);
            }
        }
    }

    public List<GUIComponent> getCurrentLines(Player player) {
        int maxLineSize = getMaxLineSize();

        int maxOffset = getLineOffset(player) + getMaxOffset();

        if(maxOffset > lines.size()) maxOffset = lines.size();

        if(scrollDirection == ScrollDirection.VERTICAL) {
            ArrayList<GUIComponent> objects = new ArrayList<>();
            for (List<GUIComponent> guiComponents : lines.subList(getLineOffset(player), maxOffset)) {
                objects.addAll(guiComponents);
            }
            return objects;
        }else {
            List<List<GUIComponent>> lists = lines.subList(getLineOffset(player), maxOffset);
            List<GUIComponent> guiComponents = new ArrayList<>();

            for (int i = 0; i < maxLineSize; i++) {
                for (int j = 0; j < maxLineSize; j++) {
                    guiComponents.add(lists.get(j).get(i));
                }
            }

            return guiComponents;
        }
    }

    public void nextPage(Player player) {
        if(canNextPage(player)) {
            setLineOffset(player, getLineOffset(player) + 1);
        }
    }

    public void previousPage(Player player) {
        if(canPreviousPage(player)) {
            setLineOffset(player, getLineOffset(player) - 1);
        }
    }

    public void nextPage(Player player, int jump) {
        if(canNextPage(player, jump)) {
            setLineOffset(player, getLineOffset(player) + jump);
        }
    }

    public void previousPage(Player player, int jump) {
        if(canPreviousPage(player, jump)) {
            setLineOffset(player, getLineOffset(player) - jump);
        }
    }

    public boolean canNextPage(Player player) {
        return getLineOffset(player) + getMaxOffset() <= lines.size();
    }

    public boolean canNextPage(Player player, int jump) {
        return getLineOffset(player) + getMaxOffset() + jump <= lines.size();
    }

    public boolean canPreviousPage(Player player) {
        return getLineOffset(player) > 0;
    }

    public boolean canPreviousPage(Player player, int jump) {
        return getLineOffset(player) - jump >= 0;
    }

    public int getMaxLineSize() {
        switch (scrollDirection) {
            case HORISONTAL:
                return getRows();
            case VERTICAL:
                return getWidth();
        }
        return 0;
    }

    public int getMaxOffset() {
        int maxOffset = getRows();
        if(scrollDirection == ScrollDirection.HORISONTAL) maxOffset = getWidth();
        return maxOffset;
    }

    public int getPage(Player player) {
        return getLineOffset(player);
    }

    public int getLineOffset(Player player) {
        return lineOffset.getOrDefault(player, 0);
    }

    public void setLineOffset(Player player, int offset) {
        lineOffset.put(player, offset);
    }

}
