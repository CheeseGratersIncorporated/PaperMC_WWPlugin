package io.github.gegagegagega.werewolf.housing;

import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.BoundingBox;

/** One box of a house's inside. A house can be made of several. */
public class Interior {
    BoundingBox b;
    BlockDisplay d;

    public Interior(BoundingBox b) {
        this.b = b;
    }

    public Interior(BoundingBox b, BlockDisplay d) {
        this.b = b;
        this.d = d;
    }

    public BoundingBox getBoundingBox() {
        return b;
    }
}
