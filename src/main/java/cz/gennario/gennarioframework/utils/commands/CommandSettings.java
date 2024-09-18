package cz.gennario.gennarioframework.utils.commands;

import cz.gennario.gennarioframework.utils.Utils;
import lombok.Data;

@Data
public class CommandSettings {

    public String usage = Utils.colorize("{#1ca4ed}ℹ &8|&f &fUse: {#8bcdf0}/%label% %help%");
    public String noPermission = Utils.colorize("{#d11717}❌ &8|&f &cYou don''t have enough permissions to do this!");
    public String wrongUsage = Utils.colorize("{#d11717}❌ &8|&f &cWrong inserted value at arg: &f%value%&c!");
    public String disabledConsole = Utils.colorize("{#d11717}❌ &8|&f &cThis command is only for players!");

}
