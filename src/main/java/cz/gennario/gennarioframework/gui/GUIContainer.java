package cz.gennario.gennarioframework.gui;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GUIContainer {

    private int rows;
    private int width;

    private Map<Integer, GUIContainer> containers;
    private Map<Integer, GUIComponent> components;

    public GUIContainer(int rows, int width) {
        this.rows = rows;
        this.width = width;

        this.containers = new HashMap<>();
        this.components = new HashMap<>();
    }

    public GUIContainer appendContainer(GUIContainer container) {
        int slot = -1;
        for (int i = 0; i < rows * width; i++) {
            if(components.containsKey(i)) continue;
            if(containers.containsKey(i)) continue;
            slot = i;
            break;
        }
        if(slot == -1) {
            throw new IllegalArgumentException("Cannot append container to container, because there is no free slot");
        }

        containers.put(slot, container);
        return this;
    }

    public GUIContainer setContainer(int slot, GUIContainer container) {
        containers.put(slot, container);
        return this;
    }

    public GUIContainer setContainer(int row, int column, GUIContainer container) {
        int skip = 0;
        if(row > 1) skip = (row-1) * width;
        containers.put(skip+column, container);
        return this;
    }

    public GUIContainer appendComponent(GUIComponent component) {
        int slot = -1;
        for (int i = 0; i < rows * width; i++) {
            if(components.containsKey(i)) continue;
            if(containers.containsKey(i)) continue;
            slot = i;
            break;
        }
        if(slot == -1) {
            throw new IllegalArgumentException("Cannot append component to container, because there is no free slot");
        }

        components.put(slot, component);
        return this;
    }

    public GUIContainer setComponent(int slot, GUIComponent component) {
        components.put(slot, component);
        return this;
    }

    public GUIContainer setComponent(int row, int column, GUIComponent component) {
        int skip = 0;
        if(row > 1) skip = (row-1) * width;
        components.put(skip+column, component);
        return this;
    }

    public GUIContainer fillComponent(GUIComponent component) {
        for (int i = 0; i < width * rows; i++) {
            components.put(i, component);
        }
        return this;
    }

    public int getWidth() {
        return width;
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return width * rows;
    }

    public int componentsAmount() {
        return components.size();
    }

    public int containersAmount() {
        int amount = 0;
        for (GUIContainer container : containers.values()) {
            amount += container.globalAmount();
        }
        return amount;
    }

    public int globalAmount() {
        return containersAmount() + componentsAmount();
    }

    public List<Integer> getSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            slots.add(i);
        }
        return slots;
    }

    public List<Integer> getFreeSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            if(containers.containsKey(i)) continue;
            if(components.containsKey(i)) continue;
            slots.add(i);
        }
        return slots;
    }

    public List<Integer> getComponentSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            if(components.containsKey(i)) slots.add(i);
        }
        return slots;
    }

    public List<Integer> getContainerSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            if(containers.containsKey(i)) slots.add(i);
        }
        return slots;
    }

    public boolean isEmpty() {
        return components.isEmpty() && containers.isEmpty();
    }

    public boolean isFull() {
        return components.size() + containers.size() == getSize();
    }

    public Map<Integer, GUIComponent> getComponents() {
        return components;
    }

    public Map<Integer, GUIContainer> getContainers() {
        return containers;
    }

    public GUIContainer clone() {
        GUIContainer container = new GUIContainer(rows, width);
        container.setComponents(components);
        container.setContainers(containers);
        return container;
    }
}
