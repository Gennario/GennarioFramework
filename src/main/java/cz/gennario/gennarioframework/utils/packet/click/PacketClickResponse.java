package cz.gennario.gennarioframework.utils.packet.click;

import org.bukkit.entity.Player;

import java.util.List;

public interface PacketClickResponse {

    void onClick(List<PacketClickType> clickTypes, Player player);
}
