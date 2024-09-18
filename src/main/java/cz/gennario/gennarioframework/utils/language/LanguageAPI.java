package cz.gennario.gennarioframework.utils.language;

import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.replacement.Replacement;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LanguageAPI {

    private Map<String, Language> languages;
    private JavaPlugin instance;

    private File file;

    private Language activeLanguage;

    public LanguageAPI(JavaPlugin plugin) {
        this.languages = new HashMap<>();
        this.instance = plugin;

        this.file = new File(instance.getDataFolder()+"/languages/");
        if(!file.exists()) {
            file.mkdir();
        }

        loadLanguages();
    }

    public List<String> getMessage(String path, @Nullable Player player, @Nullable ReplacementPackage replacement) {
        Object o = getYamlDocument().get(path);

        if(replacement == null) {
            replacement = new ReplacementPackage();
        }

        List<String> stringList = new ArrayList<>();
        if (o instanceof List) {
            List<String> list = new ArrayList<>((List<String>) o);
            for (String s : list) {
                stringList.add(getPrefix() + Utils.colorize(player, replacement.replace(player, s)));
            }
        } else if (o instanceof String) {
            stringList.add(getPrefix() + Utils.colorize(player, replacement.replace(player, (String) o)));
        }

        return stringList;
    }

    public List<String> getMessage(String path, Player player) {
        return getMessage(path, player, new ReplacementPackage());
    }

    public void sendMessage(String path, Player player, @Nullable ReplacementPackage replacement) {
        List<String> stringList = getMessage(path, player, replacement);
        for (String s : stringList) {
            player.sendMessage(s);
        }
    }

    public void sendMessage(String path, Player player) {
        sendMessage(path, player, new ReplacementPackage());
    }

    public String getString(String path) {
        return getYamlDocument().getString(path);
    }

    public String getString(String path, Player player, Replacement replacement) {
        return replacement.replace(player, getYamlDocument().getString(path));
    }

    public String getColoredString(String path, @Nullable Player player) {
        return Utils.colorize(player, getYamlDocument().getString(path));
    }

    public String getColoredString(String path, Player player, Replacement replacement) {
        return replacement.replace(player, Utils.colorize(player, getYamlDocument().getString(path)));
    }

    public List<String> getStringList(String path) {
        return getYamlDocument().getStringList(path);
    }

    public List<String> getStringList(String path, Player player, Replacement replacement) {
        List<String> list = new ArrayList<>();
        for (String s : getYamlDocument().getStringList(path)) {
            list.add(replacement.replace(player, s));
        }
        return list;
    }

    public List<String> getColoredStringList(String path, @Nullable Player player) {
        List<String> list = new ArrayList<>();
        for (String s : getYamlDocument().getStringList(path)) {
            list.add(Utils.colorize(player, s));
        }
        return list;
    }

    public List<String> getColoredStringList(String path, Player player, Replacement replacement) {
        List<String> list = new ArrayList<>();
        for (String s : getYamlDocument().getStringList(path)) {
            list.add(Utils.colorize(player, replacement.replace(player, s)));
        }
        return list;
    }

    public double getDouble(String path) {
        return getYamlDocument().getDouble(path);
    }

    public int getInt(String path) {
        return getYamlDocument().getInt(path);
    }

    public Section getSection(String path) {
        return getYamlDocument().getSection(path);
    }

    public byte getByte(String path) {
        return getYamlDocument().getByte(path);
    }

    public List<?> getList(String path) {
        return getYamlDocument().getList(path);
    }

    public BigInteger getBigInt(String path) {
        return getYamlDocument().getBigInt(path);
    }

    public YamlDocument getYamlDocument() {
        return activeLanguage.getConfig().getYamlDocument();
    }

    public boolean contains(String path) {
        return getYamlDocument().contains(path);
    }

    public String getPrefix() {
        return getColoredString("commands.prefix", null);
    }

    public void setActiveLanguage(String name) {
        if(languages.containsKey(name)) {
            activeLanguage = languages.get(name);
            if(activeLanguage == null) {
                System.out.println("Hooked to language "+name+" for plugin "+instance.getDescription().getName()+ ". This language doesn't exist... Disabling plugin.");
                instance.getPluginLoader().disablePlugin(instance);
            }else {
                Main.getInstance().getPluginUpdater().getData().put("Hooked to language", name);
            }
        }
    }

    public void setActiveLanguage(String name, String defaultName) {
        if(languages.containsKey(name)) {
            activeLanguage = languages.get(name);
            Main.getInstance().getPluginUpdater().getData().put("Hooked to language", name);
        }else {
            activeLanguage = languages.get(defaultName);
            Main.getInstance().getPluginUpdater().getData().put("Tried to hook into the language", name+" | Not exist, replaced by "+defaultName);
        }
    }

    public void loadLanguages() {
        for (File f : file.listFiles()) {
            String name = f.getName().replace(".yml", "");
            if (languages.containsKey(name)) continue;
            Language language = new Language(instance, name);

            languages.put(name, language);
        }
        Main.getInstance().getPluginUpdater().getData().put("Successfully loaded", languages.size()+" languages");
    }

    public void addLanguage(String name) {
        if(exist(name)) return;
        Language language = new Language(instance, name);
        languages.put(name, language);
        Main.getInstance().getPluginUpdater().getData().put("Added new language", name);
    }

    public boolean exist(String name) {
        for (File f : file.listFiles()) {
            if(f.getName().replace(".yml", "").equals("name")) return true;
        }
        if(languages.keySet().contains(name)) return true;
        return false;
    }

    public void reloadLanguages() {
        languages.clear();
        loadLanguages();
    }

}
