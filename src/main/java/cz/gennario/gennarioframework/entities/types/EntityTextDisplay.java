package cz.gennario.gennarioframework.entities.types;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.types.display.types.PacketTextDisplay;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityTextDisplay extends PacketEntity {

    public abstract static class PerPlayerText {
        public abstract List<String> getText(Player player);
    }

    private PacketTextDisplay packetTextDisplay;
    private PerPlayerText text;
    private ReplacementPackage replacement;
    private List<Player> hiddenPlayers = new ArrayList<>();

    public EntityTextDisplay(Location location, PacketEntity.EntityVisiblity entityVisiblity) {
        super(-1, EntityType.TEXT_DISPLAY, entityVisiblity, false, 0, 40);

        packetTextDisplay = new PacketTextDisplay();
        setId(packetTextDisplay.getEntityId());
        packetTextDisplay.setLocation(location);

        text = new PerPlayerText() {
            @Override
            public List<String> getText(Player player) {
                return List.of("not set");
            }
        };
        replacement = new ReplacementPackage().colorize();
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

        packetTextDisplay.setText(List.of("§cloading...").toArray(new String[0]));
        packetTextDisplay.spawn(player);

        packetTextDisplay.updateText(player, Utils.colorize(player, text.getText(player)).toArray(new String[0]));

        if(getPacketEntitySpawnOverwrite() != null) {
            getPacketEntitySpawnOverwrite().spawnOverwrite(player);
        }

        addSpawnedPlayer(player);
    }

    @Override
    public void destroy(Player player) {
        packetTextDisplay.delete(player);

        removeSpawnedPlayer(player);
    }

    @Override
    public void teleport(Location location) {
        packetTextDisplay.setLocation(location.clone());
        for (Player player : getSpawnedPlayers()) {
            packetTextDisplay.teleport(player, location.clone());
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        packetTextDisplay.teleport(player, location);
    }

    @Override
    public void update(Player player) {
        packetTextDisplay.update(player);
        packetTextDisplay.updateText(player, Utils.colorize(player, text.getText(player)).toArray(new String[0]));
    }

    @Override
    public Location getLocation() {
        return packetTextDisplay.getLocation();
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
        hiddenPlayers.add(player);
        packetTextDisplay.delete(player);
    }

    public void showPlayer(Player player) {
        hiddenPlayers.remove(player);
        packetTextDisplay.spawn(player);
    }
}
