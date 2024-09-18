package cz.gennario.gennarioframework.gui;

import lombok.Data;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Data
public class GUISettings {

    public enum OpenMethod {
        ASYNC,
        SYNC
    }

    private boolean preventClose;
    private ArrayList<Player> closeAllowed;
    private OpenMethod openMethod;
    private boolean updateOnOpen;
    private int reopenDelay;

    public GUISettings() {
        this.preventClose = false;
        this.closeAllowed = new ArrayList<>();
        this.openMethod = OpenMethod.ASYNC;
        this.updateOnOpen = false;
        this.reopenDelay = 3;
    }

}
