package cz.gennario.gennarioframework.utils.packet.types.display.types;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.gson.Gson;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.types.display.PacketDisplay;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class PacketTextDisplay extends PacketDisplay {

    /* For hide & show entity */
    protected String saveText;
    protected int saveBackgroundColor;

    /* Other */
    private String text;
    private int lineWidth, backgroundColor;
    private byte textOpacity;
    private boolean shadow, seeThrough, defaultBackgroundColor;
    private TextDisplay.TextAlignment textAlignment;

    public PacketTextDisplay() {
        super();
    }

    public void spawn(Player player) {
        /* DATA WATCHER */
        WrappedDataWatcher dataWatcher = getDisplay(player, EntityType.TEXT_DISPLAY);
        update(player, dataWatcher);
    }

    private byte getPacketTextAlignment() {
        if (textAlignment == TextDisplay.TextAlignment.LEFT) {
            return 1;
        } else if (textAlignment == TextDisplay.TextAlignment.CENTER) {
            return 0;
        } else if (textAlignment == TextDisplay.TextAlignment.RIGHT) {
            return 2;
        }

        return 0;
    }


    /* UPDATE ENTITY */
    public void update(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        update(player, dataWatcher);
    }

    private void update(Player player, WrappedDataWatcher dataWatcher) {
        /* DATA WATCHER */

        /* Set text */
        if (text != null) {
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getChatComponentSerializer();

            if (PacketUtils.VERSION_1_20_4_AFTER_OR_EQUAL) {
                TextComponent textComponent = new TextComponent();
                if (text.contains("\n")) {
                    String[] split = text.split("\n");

                    for (String line : split) {
                        BaseComponent[] components = TextComponent.fromLegacyText(Utils.colorize(player, line));
                        for (BaseComponent component : components) {
                            textComponent.addExtra(component);
                        }

                        if (!line.equals(split[split.length - 1])) {
                            textComponent.addExtra("\n");
                        }
                    }
                } else {
                    textComponent = new TextComponent(TextComponent.fromLegacyText(Utils.colorize(player, text)));
                }

                String jsonText = ComponentSerializer.toString(textComponent);
                PacketUtils.setMetadata(dataWatcher, 23 + versionOverwrite(), serializer, WrappedChatComponent.fromJson(jsonText).getHandle());
            } else {
                PacketUtils.setMetadata(dataWatcher, 23 + versionOverwrite(), serializer, WrappedChatComponent.fromJson(Utils.colorize(player, this.text)).getHandle());
            }
        }

        /* Set line width */
        if (lineWidth != 0) PacketUtils.setMetadata(dataWatcher, 24 + versionOverwrite(), Integer.class, lineWidth);
        /* Set background color */
        PacketUtils.setMetadata(dataWatcher, 25 + versionOverwrite(), Integer.class, backgroundColor);
        /* Set text opacity */
        if (textOpacity != 0) PacketUtils.setMetadata(dataWatcher, 26 + versionOverwrite(), Byte.class, textOpacity);

        /* Set flags */
        byte flags = 0;
        if (shadow) flags += 0x01;
        if (seeThrough) flags += 0x02;
        if (defaultBackgroundColor) flags += 0x04;
        flags += (getPacketTextAlignment() << 3);

        PacketUtils.setMetadata(dataWatcher, 27 + versionOverwrite(), Byte.class, flags);


        /* SEND PACKET */
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
    }

    /* HIDE TEXT DISPLAY */
    public void hideDisplay(Player player) {
        this.saveText = text;
        this.saveBackgroundColor = backgroundColor;

        updateText(player, "");
        updateBackgroundColor(player,0);
    }

    /* SHOW TEXT DISPLAY */
    public void showDisplay(Player player) {
        updateText(player, saveText);
        updateBackgroundColor(player, saveBackgroundColor);
    }


    /*
     * UPDATE SPECIFIC THINGS
     */


    /* TEXT */
    public PacketTextDisplay updateText(Player player) {
        return updateText(player, text);
    }

    public PacketTextDisplay updateTextList(Player player, List<String> text) {
        return updateText(player, String.join("\n", text));
    }

    public PacketTextDisplay updateText(Player player, String... text) {
        return updateTextList(player, Arrays.asList(text));
    }

    public PacketTextDisplay updateText(Player player, String text) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (text != null) {
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getChatComponentSerializer();

            if (PacketUtils.VERSION_1_20_4_AFTER_OR_EQUAL) {
                TextComponent textComponent = new TextComponent();
                if (text.contains("\n")) {
                    String[] split = text.split("\n");

                    for (String line : split) {
                        BaseComponent[] components = TextComponent.fromLegacyText(Utils.colorize(player, line));
                        for (BaseComponent component : components) {
                            textComponent.addExtra(component);
                        }

                        if (!line.equals(split[split.length - 1])) {
                            textComponent.addExtra("\n");
                        }
                    }
                } else {
                    textComponent = new TextComponent(TextComponent.fromLegacyText(Utils.colorize(player, text)));
                }

                String jsonText = ComponentSerializer.toString(textComponent);
                PacketUtils.setMetadata(dataWatcher, 23 + versionOverwrite(), serializer, WrappedChatComponent.fromJson(jsonText).getHandle());
            } else {
                PacketUtils.setMetadata(dataWatcher, 23 + versionOverwrite(), serializer, WrappedChatComponent.fromJson(Utils.colorize(player, this.text)).getHandle());
            }
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* LINE WIDTH */
    public PacketTextDisplay updateLineWidth(Player player) {
        return updateLineWidth(player, lineWidth);
    }

    public PacketTextDisplay updateLineWidth(Player player, int lineWidth) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (lineWidth != 0) PacketUtils.setMetadata(dataWatcher, 24 + versionOverwrite(), Integer.class, lineWidth);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* BACKGROUND COLOR */
    public PacketTextDisplay updateBackgroundColor(Player player) {
        return updateBackgroundColor(player, backgroundColor);
    }

    public PacketTextDisplay updateBackgroundColor(Player player, Color color) {
        return updateBackgroundColor(player, color.getRGB());
    }

    public PacketTextDisplay updateBackgroundColor(Player player, int backgroundColor) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (backgroundColor != 0)
            PacketUtils.setMetadata(dataWatcher, 25 + versionOverwrite(), Integer.class, backgroundColor);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* TEXT OPACITY */
    public PacketTextDisplay updateTextOpacity(Player player) {
        return updateTextOpacity(player, textOpacity);
    }

    public PacketTextDisplay updateTextOpacity(Player player, byte textOpacity) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (textOpacity != 0) PacketUtils.setMetadata(dataWatcher, 26 + versionOverwrite(), Byte.class, textOpacity);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* FLAGS */
    public PacketTextDisplay updateTextDisplayFlags(Player player) {
        return updateTextDisplayFlags(player, shadow, seeThrough, defaultBackgroundColor, textAlignment);
    }

    public PacketTextDisplay updateTextDisplayFlags(Player player, boolean shadow, boolean seeThrough, boolean defaultBackgroundColor, TextDisplay.TextAlignment textAlignment) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        byte flags = 0;
        if (shadow) flags += 0x01;
        if (seeThrough) flags += 0x02;
        if (defaultBackgroundColor) flags += 0x04;
        flags += (getPacketTextAlignment() << 3);

        PacketUtils.setMetadata(dataWatcher, 27 + versionOverwrite(), Byte.class, flags);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE SHADOW FLAG */
    public PacketTextDisplay updateShadow(Player player) {
        return updateShadow(player, shadow);
    }

    public PacketTextDisplay updateShadow(Player player, boolean shadow) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        PacketUtils.setMetadata(dataWatcher, 27 + versionOverwrite(), Byte.class, (byte) (shadow ? (byte) 0x01 : 0));
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE SEE-THROUGH FLAG */
    public PacketTextDisplay updateSeeThrough(Player player) {
        return updateSeeThrough(player, seeThrough);
    }

    public PacketTextDisplay updateSeeThrough(Player player, boolean seeThrough) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        PacketUtils.setMetadata(dataWatcher, 27 + versionOverwrite(), Byte.class, (byte) (seeThrough ? (byte) 0x02 : 0));
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE DEFAULT-BACKGROUND-COLOR FLAG */
    public PacketTextDisplay updateDefaultBackgroundColor(Player player) {
        return updateDefaultBackgroundColor(player, defaultBackgroundColor);
    }

    public PacketTextDisplay updateDefaultBackgroundColor(Player player, boolean defaultBackgroundColor) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        PacketUtils.setMetadata(dataWatcher, 27 + versionOverwrite(), Byte.class, (byte) (defaultBackgroundColor ? (byte) 0x04 : 0));
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE TEXT-ALIGNMENT FLAG */
    public PacketTextDisplay updateTextAlignment(Player player) {
        return updateTextAlignment(player, textAlignment);
    }

    public PacketTextDisplay updateTextAlignment(Player player, TextDisplay.TextAlignment textAlignment) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        PacketUtils.setMetadata(dataWatcher, 27 + versionOverwrite(), Byte.class, getPacketTextAlignment());
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* SETTER */
    public PacketTextDisplay setText(String... text) {
        return setTextList(Arrays.asList(text));
    }

    public PacketTextDisplay setTextList(List<String> text) {
        if (PacketUtils.VERSION_1_20_4_AFTER_OR_EQUAL) {
            this.text = String.join("\n", text);
            return this;
        }

        this.text = new Gson().toJson(String.join("\n", text));
        return this;
    }

    public PacketTextDisplay setText(String text) {
        if (PacketUtils.VERSION_1_20_4_AFTER_OR_EQUAL) {
            this.text = text;
            return this;
        }
        this.text = new Gson().toJson(text);
        return this;
    }


    public PacketTextDisplay setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public PacketTextDisplay setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public PacketTextDisplay setBackgroundColor(Color color) {
        this.backgroundColor = color.getRGB();
        return this;
    }

    public PacketTextDisplay setBackgroundColor(int red, int green, int blue) {
        return setBackgroundColor(new Color(red, green, blue));
    }

    public PacketTextDisplay setTextOpacity(byte textOpacity) {
        this.textOpacity = textOpacity;
        return this;
    }

    public PacketTextDisplay setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public PacketTextDisplay setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        return this;
    }

    public PacketTextDisplay setDefaultBackgroundColor(boolean defaultBackgroundColor) {
        this.defaultBackgroundColor = defaultBackgroundColor;
        return this;
    }

    public PacketTextDisplay setTextAlignment(TextDisplay.TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }


    public String getTextJson() {
        return text;
    }

    public String getText() {
        return new Gson().fromJson(text, String.class);
    }

    public List<String> getTextList() {
        return Arrays.asList(new Gson().fromJson(this.text, String[].class));
    }

}