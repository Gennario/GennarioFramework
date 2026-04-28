package cz.gennario.gennarioframework.utils.packet.types.display.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.types.display.PacketDisplay;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class PacketTextDisplay extends PacketDisplay {

    protected String saveText;
    protected int saveBackgroundColor;

    private String text;
    private int lineWidth, backgroundColor;
    private byte textOpacity;
    private boolean shadow, seeThrough, defaultBackgroundColor;
    private TextDisplay.TextAlignment textAlignment;

    public PacketTextDisplay() {
        super();
    }

    public void spawn(Player player) {
        List<EntityData<?>> metadata = getDisplay(player, EntityType.TEXT_DISPLAY);
        update(player, metadata);
    }

    private byte getPacketTextAlignment() {
        if (textAlignment == TextDisplay.TextAlignment.LEFT) return (byte) 0x08;  // bit 3
        if (textAlignment == TextDisplay.TextAlignment.RIGHT) return (byte) 0x10; // bit 4
        return 0x00; // CENTER = no alignment bits set
    }

    private byte buildFlagsByte() {
        byte flags = 0;
        if (shadow) flags |= 0x01;
        if (seeThrough) flags |= 0x02;
        if (defaultBackgroundColor) flags |= 0x04;
        if (textAlignment != null) flags |= getPacketTextAlignment();
        return flags;
    }

    public void update(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        update(player, metadata);
    }

    private void update(Player player, List<EntityData<?>> metadata) {
        int o = versionOverwrite(player);

        if (text != null) {
            try {
                Component component = LegacyComponentSerializer.legacySection().deserialize(text);
                PacketUtils.addMetadata(metadata, 23+o, EntityDataTypes.ADV_COMPONENT, component);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (lineWidth != 0) PacketUtils.addMetadata(metadata, 24+o, EntityDataTypes.INT, lineWidth);
        PacketUtils.addMetadata(metadata, 25+o, EntityDataTypes.INT, backgroundColor);
        if (textOpacity != 0) PacketUtils.addMetadata(metadata, 26+o, EntityDataTypes.BYTE, textOpacity);

        PacketUtils.addMetadata(metadata, 27+o, EntityDataTypes.BYTE, buildFlagsByte());

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    public void hideDisplay(Player player) {
        this.saveText = text;
        this.saveBackgroundColor = backgroundColor;
        updateText(player, "");
        updateBackgroundColor(player, 0);
    }

    public void showDisplay(Player player) {
        updateText(player, saveText);
        updateBackgroundColor(player, saveBackgroundColor);
    }

    public PacketTextDisplay updateText(Player player) {
        return updateText(player, text);
    }

    public PacketTextDisplay updateTextList(Player player, List<String> textList) {
        return updateText(player, String.join("\n", textList));
    }

    public PacketTextDisplay updateText(Player player, String... lines) {
        return updateTextList(player, Arrays.asList(lines));
    }

    public PacketTextDisplay updateText(Player player, String text) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (text != null) {
            try {
                Component component = LegacyComponentSerializer.legacySection().deserialize(text);
                PacketUtils.addMetadata(metadata, 23+versionOverwrite(player), EntityDataTypes.ADV_COMPONENT, component);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateLineWidth(Player player) {
        return updateLineWidth(player, lineWidth);
    }

    public PacketTextDisplay updateLineWidth(Player player, int lineWidth) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (lineWidth != 0) PacketUtils.addMetadata(metadata, 24+versionOverwrite(player), EntityDataTypes.INT, lineWidth);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateBackgroundColor(Player player) {
        return updateBackgroundColor(player, backgroundColor);
    }

    public PacketTextDisplay updateBackgroundColor(Player player, Color color) {
        return updateBackgroundColor(player, color.getRGB());
    }

    public PacketTextDisplay updateBackgroundColor(Player player, int backgroundColor) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (backgroundColor != 0) PacketUtils.addMetadata(metadata, 25+versionOverwrite(player), EntityDataTypes.INT, backgroundColor);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateTextOpacity(Player player) {
        return updateTextOpacity(player, textOpacity);
    }

    public PacketTextDisplay updateTextOpacity(Player player, byte textOpacity) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (textOpacity != 0) PacketUtils.addMetadata(metadata, 26+versionOverwrite(player), EntityDataTypes.BYTE, textOpacity);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateTextDisplayFlags(Player player) {
        return updateTextDisplayFlags(player, shadow, seeThrough, defaultBackgroundColor, textAlignment);
    }

    public PacketTextDisplay updateTextDisplayFlags(Player player, boolean shadow, boolean seeThrough, boolean defaultBackgroundColor, TextDisplay.TextAlignment textAlignment) {
        this.shadow = shadow;
        this.seeThrough = seeThrough;
        this.defaultBackgroundColor = defaultBackgroundColor;
        this.textAlignment = textAlignment;

        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 27+versionOverwrite(player), EntityDataTypes.BYTE, buildFlagsByte());
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateShadow(Player player) {
        return updateShadow(player, shadow);
    }

    public PacketTextDisplay updateShadow(Player player, boolean shadow) {
        this.shadow = shadow;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 27+versionOverwrite(player), EntityDataTypes.BYTE, buildFlagsByte());
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateSeeThrough(Player player) {
        return updateSeeThrough(player, seeThrough);
    }

    public PacketTextDisplay updateSeeThrough(Player player, boolean seeThrough) {
        this.seeThrough = seeThrough;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 27+versionOverwrite(player), EntityDataTypes.BYTE, buildFlagsByte());
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateDefaultBackgroundColor(Player player) {
        return updateDefaultBackgroundColor(player, defaultBackgroundColor);
    }

    public PacketTextDisplay updateDefaultBackgroundColor(Player player, boolean defaultBackgroundColor) {
        this.defaultBackgroundColor = defaultBackgroundColor;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 27+versionOverwrite(player), EntityDataTypes.BYTE, buildFlagsByte());
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay updateTextAlignment(Player player) {
        return updateTextAlignment(player, textAlignment);
    }

    public PacketTextDisplay updateTextAlignment(Player player, TextDisplay.TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 27+versionOverwrite(player), EntityDataTypes.BYTE, buildFlagsByte());
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketTextDisplay setText(String... lines) {
        return setTextList(Arrays.asList(lines));
    }

    public PacketTextDisplay setTextList(List<String> lines) {
        return setText(String.join("\n", lines));
    }

    public PacketTextDisplay setText(String text) {
        this.text = text;
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
        return text;
    }

    public List<String> getTextList() {
        if (text == null) return List.of();
        return Arrays.asList(text.split("\n"));
    }

    public int versionOverwrite() {
        return PacketUtils.getDisplayMetadataOffset();
    }
}
