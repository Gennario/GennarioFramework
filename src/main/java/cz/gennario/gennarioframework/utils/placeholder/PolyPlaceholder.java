package cz.gennario.gennarioframework.utils.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PolyPlaceholder extends PlaceholderExpansion {

    private final String author;
    private final String identifier;
    private final String version;

    private final List<Placeholder> placeholders;

    public PolyPlaceholder(String author, String identifier, String version) {
        this.author = author;
        this.identifier = identifier;
        this.version = version;

        this.placeholders = new ArrayList<>();
    }

    public PolyPlaceholder appendPlaceholder(Placeholder placeholder) {
        placeholders.add(placeholder);
        return this;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        try {
            for (Placeholder placeholder : placeholders) {
                if (placeholder.matchPlacehodler(params)) {
                    return placeholder.initPlaceholder(player, params);
                }
            }
        }catch (Exception e) {
            return "§cloading...";
        }
        return "§cerror";
    }

    public void registerPlaceholder() {
        register();
    }
}
