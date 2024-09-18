package cz.gennario.gennarioframework.utils.placeholder;

import lombok.Getter;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Placeholder {

    public interface PlaceholderAction {
        String onRequest(OfflinePlayer player, SegmentInstance[] segments);
    }

    private List<PlaceholderSegment> segments;
    private PlaceholderAction action;

    public Placeholder() {
        segments = new ArrayList<>();
    }

    public Placeholder appendStaticSegment(String value) {
        segments.add(new PlaceholderSegment(PlaceholderSegment.SegmentType.STATIC_VALUE, value));
        return this;
    }

    public Placeholder appendDynamicSegment(PlaceholderSegment.SegmentType type) {
        segments.add(new PlaceholderSegment(type));
        return this;
    }

    public Placeholder appendAction(PlaceholderAction action) {
        this.action = action;
        return this;
    }

    public boolean matchPlacehodler(String placeholder) {
        String[] s = placeholder.split("_");
        if (s.length != segments.size()) {
            return false;
        }
        for (int i = 0; i < s.length; i++) {
            if (!segments.get(i).insertedCorrectly(s[i])) {
                return false;
            }
        }
        return true;
    }

    public String initPlaceholder(OfflinePlayer offlinePlayer, String params) {
        String[] s = params.split("_");
        if (!matchPlacehodler(params)) {
            return "Not matching segments";
        }

        SegmentInstance[] instances = new SegmentInstance[s.length];
        for (int i = 0; i < s.length; i++) {
            instances[i] = new SegmentInstance(segments.get(i).getType(), s[i]);
        }

        if (action == null) {
            return "No action not set";
        }

        return action.onRequest(offlinePlayer, instances);
    }

}
