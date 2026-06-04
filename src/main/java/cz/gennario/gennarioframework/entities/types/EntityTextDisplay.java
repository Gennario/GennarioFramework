package cz.gennario.gennarioframework.entities.types;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.types.display.types.PacketTextDisplay;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class EntityTextDisplay extends PacketEntity {

    public abstract static class PerPlayerText {
        public abstract List<String> getText(Player player);
    }

    private Map<Player, PacketTextDisplay> packetTextDisplays;
    private Map<Player, PlayerOutlineInstance> playerOutlineInstances = new ConcurrentHashMap<>();
    private PerPlayerText text;
    private ReplacementPackage replacement;
    private List<Player> hiddenPlayers = new ArrayList<>();

    private Location location;

    // Base display transformation (applied to the main text and also to outline displays)
    @Setter(AccessLevel.NONE)
    private Vector3f translation = null;
    @Setter(AccessLevel.NONE)
    private Quaternionf rotationLeft = null;
    @Setter(AccessLevel.NONE)
    private Quaternionf rotationRight = null;
    @Setter(AccessLevel.NONE)
    private Vector3f displayScale = null;

    // Settings
    private double scale = 1;
    private Display.Billboard billboard = Display.Billboard.CENTER;
    private boolean background = true;
    private Color backgroundColor = null;
    private boolean shadow = false;
    private boolean outline = false;
    private String outlineColor = "§0";

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
                applyDisplaySettings(packetTextDisplay, null);
                packetTextDisplay.setText(Utils.colorize(player, text.getText(player)).toArray(new String[0]));

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

        applyDisplaySettings(packetTextDisplay, null);
        packetTextDisplay.setText(Utils.colorize(player, text.getText(player)).toArray(new String[0]));
        packetTextDisplay.spawn(player);

        packetTextDisplay.updateText(player, Utils.colorize(player, text.getText(player)).toArray(new String[0]));

        if(getPacketEntitySpawnOverwrite() != null) {
            getPacketEntitySpawnOverwrite().spawnOverwrite(player);
        }

        // outline
        if (outline) {
            if (!playerOutlineInstances.containsKey(player)) {
                PlayerOutlineInstance playerOutlineInstance = new PlayerOutlineInstance(this, player);
                playerOutlineInstance.spawn();
                playerOutlineInstances.put(player, playerOutlineInstance);
            } else {
                PlayerOutlineInstance playerOutlineInstance = playerOutlineInstances.get(player);
                playerOutlineInstance.spawn();
            }
        }

        addSpawnedPlayer(player);
    }

    @Override
    public void destroy(Player player) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        packetTextDisplay.delete(player);

        removeSpawnedPlayer(player);

        if (outline) {
            if (playerOutlineInstances.containsKey(player)) {
                PlayerOutlineInstance playerOutlineInstance = playerOutlineInstances.get(player);
                playerOutlineInstance.destroy();
            }
        }
    }

    @Override
    public void teleport(Location location) {
        packetTextDisplays.forEach((player, packetTextDisplay) -> {
            packetTextDisplay.setLocation(location.clone());
            for (Player p : getSpawnedPlayers()) {
                packetTextDisplay.teleport(p, location.clone());
            }
        });

        if (outline) {
            playerOutlineInstances.forEach((player, playerOutlineInstance) -> playerOutlineInstance.teleport());
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);
        packetTextDisplay.teleport(player, location);

        if (outline) {
            if (playerOutlineInstances.containsKey(player)) {
                PlayerOutlineInstance playerOutlineInstance = playerOutlineInstances.get(player);
                playerOutlineInstance.teleport();
            }
        }
    }

    @Override
    public void update(Player player) {
        PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

        applyDisplaySettings(packetTextDisplay, null);
        packetTextDisplay.update(player);
        packetTextDisplay.updateText(player, Utils.colorize(player, text.getText(player)).toArray(new String[0]));

        if (outline) {
            if (playerOutlineInstances.containsKey(player)) {
                PlayerOutlineInstance playerOutlineInstance = playerOutlineInstances.get(player);
                playerOutlineInstance.update();
            }
        }
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void updateAllPerPlayer() {
        for (Player player : new ArrayList<>(getSpawnedPlayers())) {
            PacketTextDisplay packetTextDisplay = getPacketTextDisplay(player);

            applyDisplaySettings(packetTextDisplay, null);
            packetTextDisplay.setText(Utils.colorize(player, text.getText(player)).toArray(new String[0]));

            packetTextDisplay.update(player);
        }
    }

    public EntityTextDisplay setTranslation(Vector3f translation) {
        this.translation = translation == null ? null : new Vector3f(translation);
        return this;
    }

    public EntityTextDisplay setTranslation(float x, float y, float z) {
        return setTranslation(new Vector3f(x, y, z));
    }

    public EntityTextDisplay setRotationLeft(Quaternionf rotationLeft) {
        this.rotationLeft = rotationLeft == null ? null : new Quaternionf(rotationLeft);
        return this;
    }

    public EntityTextDisplay setRotationRight(Quaternionf rotationRight) {
        this.rotationRight = rotationRight == null ? null : new Quaternionf(rotationRight);
        return this;
    }

    public EntityTextDisplay setDisplayScale(Vector3f displayScale) {
        this.displayScale = displayScale == null ? null : new Vector3f(displayScale);
        return this;
    }

    public EntityTextDisplay setDisplayScale(float x, float y, float z) {
        return setDisplayScale(new Vector3f(x, y, z));
    }

    public EntityTextDisplay setDisplayScale(double x, double y, double z) {
        return setDisplayScale((float) x, (float) y, (float) z);
    }

    public EntityTextDisplay transformation(Vector3f translation, Quaternionf rotationLeft, Vector3f displayScale, Quaternionf rotationRight) {
        return setTranslation(translation)
                .setRotationLeft(rotationLeft)
                .setDisplayScale(displayScale)
                .setRotationRight(rotationRight);
    }

    private void applyDisplaySettings(PacketTextDisplay packetTextDisplay, Vector3f extraTranslation) {
        if (displayScale != null) {
            packetTextDisplay.setScale(new Vector3f(displayScale));
        } else {
            packetTextDisplay.setScale(scale);
        }
        packetTextDisplay.setBillboard(billboard);
        packetTextDisplay.setDefaultBackgroundColor(background);
        if (backgroundColor != null) packetTextDisplay.setBackgroundColor(backgroundColor);
        packetTextDisplay.setShadow(shadow);

        Vector3f finalTranslation = null;
        if (translation != null) {
            finalTranslation = new Vector3f(translation);
        }
        if (extraTranslation != null) {
            if (finalTranslation == null) {
                finalTranslation = new Vector3f(extraTranslation);
            } else {
                finalTranslation.add(extraTranslation);
            }
        }
        packetTextDisplay.setTranslation(finalTranslation);
        packetTextDisplay.setRotationLeft(rotationLeft == null ? null : new Quaternionf(rotationLeft));
        packetTextDisplay.setRotationRight(rotationRight == null ? null : new Quaternionf(rotationRight));
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

    public static class PlayerOutlineInstance {
        private final EntityTextDisplay entityTextDisplay;
        private final Player player;
        private final List<PacketTextDisplay> outlineDisplays;

        public PlayerOutlineInstance(EntityTextDisplay entityTextDisplay, Player player) {
            this.entityTextDisplay = entityTextDisplay;
            this.player = player;
            this.outlineDisplays = new ArrayList<>();
            build();
        }

        private void build() {
            double scale = entityTextDisplay.getScale();
            float offset = (float) (scale * 0.02f);
            Location location1 = entityTextDisplay.getLocation();

            List<String> texts = new ArrayList<>();
            List<String> playerText = entityTextDisplay.getText().getText(player);
            // remove all colors from playerText
            for (String s : playerText) {
                String stripped = s
                        .replaceAll("§x(§[0-9a-fA-F]){6}", "")
                        .replaceAll("<#[0-9a-fA-F]{6}>", "")
                        .replaceAll("<gradient[^>]*>", "")
                        .replaceAll("<rainbow[^>]*>", "")
                        .replaceAll("</(?!bold)[^>]+>", "")        // closing tagy kromě </bold>
                        .replaceAll("§[0-9a-fk-or&&[^l]]", "");
                texts.add(Utils.colorize(entityTextDisplay.getOutlineColor() + stripped));
            }

            for (int i = 0; i < 4; i++) {
                PacketTextDisplay packetTextDisplay = new PacketTextDisplay();
                packetTextDisplay.setLocation(location1.clone());
                packetTextDisplay.setText(texts.toArray(new String[0]));
                entityTextDisplay.applyDisplaySettings(packetTextDisplay, outlineOffset(i, offset));

                outlineDisplays.add(packetTextDisplay);
            }

            updateText();
        }

        public void spawn() {
            for (PacketTextDisplay packetTextDisplay : outlineDisplays) {
                packetTextDisplay.spawn(player);
            }
        }

        public void destroy() {
            for (PacketTextDisplay packetTextDisplay : outlineDisplays) {
                packetTextDisplay.delete(player);
            }
        }

        public void update() {
            updateText();
        }

        public void teleport() {
            Location location = entityTextDisplay.getLocation();
            for (PacketTextDisplay packetTextDisplay : outlineDisplays) {
                packetTextDisplay.setLocation(location.clone());
                packetTextDisplay.teleport(player, location.clone());
            }
        }

        public void updateText() {
            List<String> texts = new ArrayList<>();
            List<String> playerText = entityTextDisplay.getText().getText(player);
            // remove all colors from playerText
            for (String s : playerText) {
                String stripped = s
                        .replaceAll("§x(§[0-9a-fA-F]){6}", "")
                        .replaceAll("<#[0-9a-fA-F]{6}>", "")
                        .replaceAll("<gradient[^>]*>", "")
                        .replaceAll("<rainbow[^>]*>", "")
                        .replaceAll("</(?!bold)[^>]+>", "")        // closing tagy kromě </bold>
                        .replaceAll("§[0-9a-fk-or&&[^l]]", "");   // všechny § kromě §l

                texts.add(Utils.colorize(entityTextDisplay.getOutlineColor()+stripped));
            }
            for (int i = 0; i < outlineDisplays.size(); i++) {
                PacketTextDisplay outlineDisplay = outlineDisplays.get(i);
                outlineDisplay.setText(texts.toArray(new String[0]));
                entityTextDisplay.applyDisplaySettings(outlineDisplay, outlineOffset(i, (float) (entityTextDisplay.getScale() * 0.02f)));
                outlineDisplay.update(player);
            }
        }

        private Vector3f outlineOffset(int index, float offset) {
            return switch (index) {
                case 0 -> new Vector3f(offset, 0f, -0.025f);
                case 1 -> new Vector3f(-offset, 0f, -0.025f);
                case 2 -> new Vector3f(0f, offset, -0.025f);
                case 3 -> new Vector3f(0f, -offset, -0.025f);
                default -> new Vector3f(0f, 0f, -0.025f);
            };
        }

    }

}
