package cz.gennario.gennarioframework.utils.placeholder;

import lombok.Data;

import java.text.SimpleDateFormat;

@Data
public class PlaceholderSegment {

    public enum SegmentType {
        STATIC_VALUE("Static string (not dynamic insert column"),
        DYNAMIC_STRING("Dynamic string (dynamic insert column)"),
        DYNAMIC_PLAYER("Dynamic string (dynamic insert column) - witch check for player"),
        DYNAMIC_NUMBER("Dynamic number (dynamic insert column) - witch check for int"),
        DYNAMIC_DECIMAL("Dynamic decimal (dynamic insert column) - witch check for double"),
        DYNAMIC_BOOLEAN("Dynamic date (dynamic insert column) - witch check for boolean"),
        DYNAMIC_DATE("Dynamic time (dynamic insert column) - witch check for date");

        private String description;

        SegmentType(String description) {
            this.description = description;
        }
    }

    private SegmentType type;
    private String staticValue;

    public PlaceholderSegment(SegmentType type) {
        this.type = type;
        this.staticValue = null;
    }

    /*
     * This constructor is used for static segments
     */
    public PlaceholderSegment(SegmentType type, String staticValue) {
        this.type = type;
        this.staticValue = staticValue;
    }

    public boolean insertedCorrectly(String value) {
        switch (type) {
            case STATIC_VALUE:
                return staticValue.equals(value);
            case DYNAMIC_STRING:
                return true;
            case DYNAMIC_PLAYER:
                return value != null;
            case DYNAMIC_NUMBER:
                try {
                    Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case DYNAMIC_DECIMAL:
                try {
                    Double.parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case DYNAMIC_BOOLEAN:
                return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            case DYNAMIC_DATE:
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                try {
                    sdf.parse(value);
                    return true;
                } catch (Exception e) {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yyyy");
                    try {
                        sdf2.parse(value);
                        return true;
                    } catch (Exception e2) {
                        return false;
                    }
                }
            default:
                return false;
        }
    }

}
