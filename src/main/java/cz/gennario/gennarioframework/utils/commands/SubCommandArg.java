package cz.gennario.gennarioframework.utils.commands;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class SubCommandArg {

    private String name;
    private CommandArgType type;
    private CommandArgValue value;

    private CommandArgDynamicTabComplete customTabCompleteArgs;

    public SubCommandArg(String name, CommandArgType type, CommandArgValue value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.customTabCompleteArgs = ArrayList::new;
    }

    public SubCommandArg setCustomTabCompleteArgs(List<String> customTabCompleteArgs) {
        this.customTabCompleteArgs = () -> customTabCompleteArgs;
        return this;
    }

    public SubCommandArg setCustomTabCompleteArgs(String... customTabCompleteArgs) {
        this.customTabCompleteArgs = () -> Arrays.asList(customTabCompleteArgs);
        return this;
    }

    public SubCommandArg setCustomTabCompleteArgs(CommandArgDynamicTabComplete tabCompleteArgs) {
        this.customTabCompleteArgs = tabCompleteArgs;
        return this;
    }

    @Deprecated
    public SubCommandArg addCustomTabCompleteArg(String string) {
        return this;
    }

    public enum CommandArgType {
        OPTIONAL,
        REQUIRED
    }

    public enum CommandArgValue {
        STRING,
        INT,
        DOUBLE,
        FLOAT,
        LONG,
        PLAYER,
        OFFLINE_PLAYER,
        ENTITY,
        MATERIAL,
        LOCATION,
        HEAD
    }

    public interface CommandArgDynamicTabComplete {
        List<String> getTabCompleteArgs();
    }

}
