package cz.gennario.gennarioframework.utils.placeholder;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class SegmentInstance {

    private PlaceholderSegment.SegmentType type;
    private Object value;

    public SegmentInstance(PlaceholderSegment.SegmentType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String getAsString() {
        return value.toString();
    }

    public Player getAsPlayer() {
        return Bukkit.getPlayer(value.toString());
    }

    public int getAsInt() {
        return Integer.parseInt(value.toString());
    }

    public double getAsDouble() {
        return Double.parseDouble(value.toString());
    }

    public boolean getAsBoolean() {
        return Boolean.parseBoolean(value.toString());
    }

    public Date getAsDate() {
        SimpleDateFormat version1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        SimpleDateFormat version2 = new SimpleDateFormat("dd.MM.yyyy");

        try {
            return version1.parse(value.toString());
        } catch (Exception e) {
            try {
                return version2.parse(value.toString());
            } catch (Exception e2) {
                return null;
            }
        }
    }

}
