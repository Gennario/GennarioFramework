package cz.gennario.gennarioframework.commands;

import cz.gennario.gennarioframework.utils.Utils;
import lombok.Data;

/**
 * Configurable messages for the command framework.
 * All messages support color codes via Utils.colorize().
 */
@Data
public class CommandMessages {

    private String noPermission = "&cYou don't have permission to use this command!";
    private String playerOnly = "&cThis command can only be used by players!";
    private String consoleOnly = "&cThis command can only be used from the console!";
    private String usage = "&7Usage: &f/%label% %usage%";
    private String wrongArgument = "&cInvalid argument '&f%value%&c' for &f%name%&c. Expected: &f%type%";
    private String onCooldown = "&cPlease wait &f%time%s &cbefore using this command again!";
    private String conditionFailed = "&cYou cannot use this command right now!";
    private String unknownSubCommand = "&cUnknown sub-command '&f%input%&c'. Use &f/%label% help &cfor available commands.";
    private String helpHeader = "&8&m─────────&8 &6%label% Help &8&m─────────";
    private String helpEntry = " &8▸ &f/%label% %usage% &8- &7%description%";
    private String helpFooter = "&8&m────────────────────────────";
    private String errorOccurred = "&cAn error occurred while executing this command!";

    public String format(String message, String label, String... replacements) {
        String result = message.replace("%label%", label);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return Utils.colorize(result);
    }
}
