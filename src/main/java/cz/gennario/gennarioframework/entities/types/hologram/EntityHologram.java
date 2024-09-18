package cz.gennario.gennarioframework.entities.types.hologram;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.entities.PacketEntityOptionalData;
import cz.gennario.gennarioframework.entities.PacketEntityUtils;
import cz.gennario.gennarioframework.entities.types.EntityTextDisplay;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntityHologram extends PacketEntity {

    public class HologramLine {
        private String text;
        private double space;
        private double scale;
        private EntityTextDisplay entityTextDisplay;

        public HologramLine(String text, double space, double scale, int viewDistance) {
            this.text = text;
            this.space = space;
            this.scale = scale;

            PacketEntityOptionalData packetEntityOptionalData = new PacketEntityOptionalData();
            entityTextDisplay = (EntityTextDisplay) packetEntityUtils.createEntity(EntityType.TEXT_DISPLAY, location.clone(), EntityVisiblity.PUBLIC, packetEntityOptionalData);
            entityTextDisplay.setText(text);
            entityTextDisplay.setReplacement(replacement);
            entityTextDisplay.setViewDistance(viewDistance);
            entityTextDisplay.getPacketTextDisplay().setScale(scale);
            entityTextDisplay.getPacketTextDisplay().setBillboard(Display.Billboard.VERTICAL);
        }
    }

    private PacketEntityUtils packetEntityUtils;
    private Location location;
    private boolean attachBottom = false;
    private List<HologramLine> lines;
    private ReplacementPackage replacement;

    public EntityHologram(PacketEntityUtils packetEntityUtils, EntityVisiblity entityVisiblity, Location location) {
        super(-1, EntityType.ITEM_DISPLAY, entityVisiblity, false, 0, 50);

        this.packetEntityUtils = packetEntityUtils;
        this.location = location.clone();
        this.replacement = new ReplacementPackage().colorize();

        lines = new ArrayList<>();
    }

    public EntityHologram addLine(String text, double space, double scale) {
        lines.add(new HologramLine(text, space, scale, getViewDistance()));
        return this;
    }

    public EntityHologram addLine(String text, double space, double scale, int viewDistance) {
        lines.add(new HologramLine(text, space, scale, viewDistance));
        return this;
    }

    public void build() {
        if(attachBottom) {
            double offset = 0;
            for (HologramLine line : lines) {
                offset = offset+line.space;
            }
            for (HologramLine line : lines) {
                line.entityTextDisplay.getPacketTextDisplay().setLocation(getLocation().clone().add(0, offset, 0).clone());
                line.entityTextDisplay.updateAll();
                offset = offset-line.space;
            }
        }else {
            double offset = 0;
            for (HologramLine line : lines) {
                line.entityTextDisplay.getPacketTextDisplay().setLocation(getLocation().clone().add(0, offset, 0).clone());
                line.entityTextDisplay.updateAll();
                offset = offset+line.space;
            }
        }
    }

    @Override
    public void spawn(Player player) {
    }

    @Override
    public void destroy(Player player) {
        lines.forEach(hologramLine -> hologramLine.entityTextDisplay.destroy(player));
    }

    @Override
    public void teleport(Location location) {
        this.location = location.clone();
        build();
    }

    @Override
    public void teleport(Player player, Location location) {
        this.location = location.clone();
        build();
    }

    @Override
    public void update(Player player) {
        lines.forEach(hologramLine -> hologramLine.entityTextDisplay.update(player));
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }
}
