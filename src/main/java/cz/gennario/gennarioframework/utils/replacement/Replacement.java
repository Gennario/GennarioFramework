package cz.gennario.gennarioframework.utils.replacement;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public class Replacement {

    private ReplacementAction value;

    public Replacement(ReplacementAction value) {
        this.value = value;
    }

    public String replace(Player player, String text) {
        text = value.replace(player, text);
        return text;
    }

    public List<String> replace(Player player, List<String> text) {
        for (int i = 0; i < text.size(); i++) {
            text.set(i, replace(player, text.get(i)));
        }
        return text;
    }

    public String[] replace(Player player, String... text) {
        for (int i = 0; i < text.length; i++) {
            text[i] = replace(player, text[i]);
        }
        return text;
    }

    public interface ReplacementAction {
        String replace(Player player, String text);
    }
}
