package cz.gennario.gennarioframework.entities.types;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.types.display.types.PacketTextDisplay;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EntityTextDisplay extends PacketEntity {

    public abstract static class PerPlayerText {
        public abstract List<String> getText(Player player);
    }

    private Map<Player, PacketTextDisplay> packetTextDisplays;
    private PerPlayerText text;
    private ReplacementPackage replacement;
    private List<Player> hiddenPlayers = new ArrayList<>();

    private Location location;

    // Settings
    private double scale = 1;
    private Display.Billboard billboard = Display.Billboard.CENTER;
    private boolean background = true;
    private Color backgroundColor = null;
    private boolean shadow = false;

    public EntityTextDisplay(Location location, PacketEntity.EntityVisiblity entityVisiblity) {
        super(-1, EntityType.TEXT_DISPLAY, entityVisiblity, false, 0, 40);

        packetTextDisplays = new HashMap<>();
        this.location = location.clone();
        setId(Integer.parseInt(RandomStringUtils.random(8, false, true)));

        text = new PerPlayerText() {
            @Override
            public List<String> getText(Player player) {
                return List.of("not set");
            }
        };
        replacement = new ReplacementPackage().colorize();
    }

    public PacketTextDisplay getPacketTextDisplay(Player player) {
        if (!packetTextDisplays.containsKey(player)) {
            PacketTextDisplay packetTextDisplay = new PacketTextDisplay();
            packetTextDisplay.setLocation(location);
            packetTextDisplay.setText(Utils.colorize(player, text.getText(player)).toArray(new String[0]));

            // settings
            packetTextDisplay.setScale(scale);
            packetTextDisplay.setBillboard(billboard);
            packetTextDisplay.setDefaultBackgroundColor(background);
            if (backgroundColor != null) packetTextDisplay.setBackgroundColor(backgroundColor);
            packetTextDisplay.setShadow(shadow);

            packetTextDisplays.put(player, packetTextDisplay);
        }

        return packetTextDisplays.get(player);
    }

    @Override
    public void spawn(Player player) {
        if(getPacketVisiblityCondition() != null) {
            if (!getPacketVisiblityCondition().canSee(player)) {
                if (new ArrayList<>(hiddenPlayers).contains(player)) {
                    destroy(player);
                }
                return;
            }
        }

        if(new ArrayList<>(hiddenPlayers).contains(player)) {
            if(new ArrayList<>(getSpawnedPlayers()).contains(player)) {
                destroy(player);
            }
            return;
        }

        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        packetTextDisplay.setText(Utils.colorize(player, text.getText(player)).toArray(new String[0]));
        packetTextDisplay.spawn(player);

        packetTextDisplay.updateText(player, Utils.colorize(player, text.getText(player)).toArray(new String[0]));

        if(getPacketEntitySpawnOverwrite() != null) {
            getPacketEntitySpawnOverwrite().spawnOverwrite(player);
        }

        addSpawnedPlayer(player);
    }

    @Override
    public void destroy(Player player) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        packetTextDisplay.delete(player);

        removeSpawnedPlayer(player);
    }

    @Override
    public void teleport(Location location) {
        packetTextDisplays.forEach((player, packetTextDisplay) -> {
            packetTextDisplay.setLocation(location.clone());
            for (Player p : getSpawnedPlayers()) {
                packetTextDisplay.teleport(p, location.clone());
            }
        });
    }

    @Override
    public void teleport(Player player, Location location) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);
        packetTextDisplay.teleport(player, location);
    }

    @Override
    public void update(Player player) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        packetTextDisplay.update(player);
        packetTextDisplay.updateText(player, Utils.colorize(player, text.getText(player)).toArray(new String[0]));
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void updateAllPerPlayer() {
        for (Player player : new ArrayList<>(getSpawnedPlayers())) {
            PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

            packetTextDisplay.setText(Utils.colorize(player, text.getText(player)).toArray(new String[0]));

            packetTextDisplay.update(player);
        }
    }

    public EntityTextDisplay setText(String text) {
        this.text = new PerPlayerText() {
            @Override
            public List<String> getText(Player player) {
                return List.of(text);
            }
        };
        return this;
    }

    public EntityTextDisplay setText(String... text) {
        this.text = new PerPlayerText() {
            @Override
            public List<String> getText(Player player) {
                return List.of(text);
            }
        };
        return this;
    }

    public EntityTextDisplay setText(List<String> text) {
        this.text = new PerPlayerText() {
            @Override
            public List<String> getText(Player player) {
                return text;
            }
        };
        return this;
    }

    public EntityTextDisplay setText(PerPlayerText text) {
        this.text = text;
        return this;
    }

    public EntityTextDisplay setReplacement(ReplacementPackage replacement) {
        this.replacement = replacement;
        return this;
    }

    public void hidePlayer(Player player) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        hiddenPlayers.add(player);
        packetTextDisplay.delete(player);
    }

    public void showPlayer(Player player) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        hiddenPlayers.remove(player);
        packetTextDisplay.spawn(player);
    }
}
