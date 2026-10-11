package io.github.gegagegagega.werewolf.housing;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.BlockDisplay;

public class Door {
    Location location;
    BlockDisplay display;
    private final int x;
    private final int y;
    private final int z;
    
    public Door(Location location) {
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Location must have a world");
        }

        Block block = location.getBlock();

        if (!(block.getBlockData() instanceof Openable)) {
            throw new IllegalArgumentException(
                block.getType() + " cannot be opened or closed"
            );
        }
        this.location=location;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public Location getLocation(){
        return location;
    }
    public Block getBlock() {
        World world = location.getWorld();

        if (world == null) {
            return null;
        }

        return world.getBlockAt(x, y, z);
    }

    public boolean isOpen() {
        Block block = getBlock();

        if (block == null) {
            return false;
        }

        if (!(block.getBlockData() instanceof Openable openable)) {
            return false;
        }

        return openable.isOpen();
    }

    public boolean open() {
        return setOpen(true);
    }

    public boolean close() {
        return setOpen(false);
    }

    public boolean setOpen(boolean open) {
        Block block = getBlock();

        if (block == null) {
            return false;
        }

        BlockData data = block.getBlockData();

        if (!(data instanceof Openable openable)) {
            return false;
        }

        openable.setOpen(open);
        block.setBlockData(openable);

        return true;
    }
}
