package cz.gennario.gennarioframework.entities;

import org.bukkit.entity.Player;

public interface PacketVisiblityCondition {
    boolean canSee(Player player);
}