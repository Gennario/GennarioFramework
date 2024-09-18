package cz.gennario.gennarioframework.utils.replacement;

import cz.gennario.gennarioframework.utils.Utils;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReplacementPackage {

    List<Replacement> replacements;

    public ReplacementPackage() {
        replacements = new ArrayList<>();
    }

    public ReplacementPackage colorize() {
        replacements.add(new Replacement(Utils::colorize));
        return this;
    }

    public ReplacementPackage append(String placeholder, Object value) {
        replacements.add(new Replacement((player, text) -> text.replace(placeholder, value + "")));
        return this;
    }

    public ReplacementPackage append(String placeholder, ReplacementHolder holder) {
        replacements.add(new Replacement((player, text) -> text.replace(placeholder, holder.process(player))));
        return this;
    }

    public ReplacementPackage append(Replacement.ReplacementAction action) {
        replacements.add(new Replacement(action));
        return this;
    }

    public ReplacementPackage append(Replacement replacement) {
        replacements.add(replacement);
        return this;
    }

    public String replace(String text) {
        return replace(null, text);
    }

    public String replace(Player player, String text) {
        for (Replacement replacement : replacements) {
            text = replacement.replace(player, text);
        }
        return text;
    }

    public List<String> replace(List<String> text) {
        return replace(null, text);
    }

    public List<String> replace(Player player, List<String> text) {
        for (int i = 0; i < text.size(); i++) {
            text.set(i, replace(player, text.get(i)));
        }
        return text;
    }

    public String[] replace(String... text) {
        return replace(null, text);
    }

    public String[] replace(Player player, String... text) {
        for (int i = 0; i < text.length; i++) {
            text[i] = replace(player, text[i]);
        }
        return text;
    }

    public interface ReplacementHolder {
        String process(Player player);
    }

}
