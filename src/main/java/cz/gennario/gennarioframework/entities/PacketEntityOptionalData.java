package cz.gennario.gennarioframework.entities;

import cz.gennario.gennarioframework.entities.types.ItemDisplayPlayerItem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PacketEntityOptionalData {

    private ItemDisplayPlayerItem itemDisplayPlayerItem;

    // Interaction entity optional data
    private float interactionWidth = 1.0f;
    private float interactionHeight = 1.0f;
    private boolean interactionResponsive = false;

}
