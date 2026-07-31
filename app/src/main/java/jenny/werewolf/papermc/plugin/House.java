package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.block.data.Openable;
import org.bukkit.block.Lectern;
import org.bukkit.util.BoundingBox;
import org.eclipse.aether.version.VersionRange.Bound;
import org.jetbrains.annotations.NotNull;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;



@SerializableAs("House")
public class House implements ConfigurationSerializable  {
    
    public String name;
    public WWPlayer owner;
    public Location location;
    public Location tpout;
    public boolean hovered=false;
    public ArrayList<Door> doors = new ArrayList<Door>();
    public RotatingGhostBeam beam;
    public RotatingGhostBeam claimedBeam;
    public RotatingGhostBeam ownerBeam;
    public RotatingGhostBeam doorBeam;
    public RotatingGhostBeam hoverBeam;
    public ArrayList<Interior > interior=new ArrayList<>();
    public Location lectern;
    
    
    public House(String name, Location location) {
        this.name = name;
        this.location = location;
        this.owner = null;
        hovered=false;
    }
    public House(String name, Location location, WWPlayer claimedBy) {
        this.name = name;
        this.location = location;
        this.owner = claimedBy;
        hovered=false;
    }
    public House() {
        this.name = null;
        this.location = null;
        this.owner = null;
        hovered=false;
    }
    // public boolean contains(Location l){
    //     for(Interior i : interior){
    //         if(i.b.contains(l.getX(),l.getY(),l.getZ()))return true;
    //     }
    //     return false;
    // }
    public boolean contains(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        for (Interior i : interior) {
            BoundingBox box = i.b;

            if (x >= box.getMinX() && x < box.getMaxX() + 1.0
                    && y >= box.getMinY() && y < box.getMaxY() + 1.0
                    && z >= box.getMinZ() && z < box.getMaxZ() + 1.0) {
                return true;
            }
        }

        return false;
    }
    public void openDoors(){
        for(Door d : doors){
            d.open();
        }
    }
    public void closeDoors(){
        for(Door d : doors){
            d.close();
        }
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();


         result.put("name", name);
        result.put("location", location);
        result.put("tpout", tpout);
        result.put("lectern", lectern);

        /*
         * Extract only the essential BoundingBox data
         * from each Interior wrapper.
         */
        List<BoundingBox> savedInteriors = interior.stream()
            .map(Interior::getBoundingBox)
            .toList();

        /*
         * Extract only the essential Location data
         * from each ManagedDoor wrapper.
         */
        List<Location> savedDoors = doors.stream()
            .map(Door::getLocation)
            .toList();

        result.put("interiors", savedInteriors);
        result.put("doors", savedDoors);

        return result;
    }

    public static House deserialize(Map<String, Object> data) {
        String name = requireType(
            data,
            "name",
            String.class
        );

        Location location = requireType(
            data,
            "location",
            Location.class
        );
        Location tpout = requireType(
            data,
            "tpout",
            Location.class
        );

        Location lectern = requireType(
            data,
            "lectern",
            Location.class
        );

        ArrayList<BoundingBox> savedBoundingBoxes =
            readTypedList(
                data,
                "interiors",
                BoundingBox.class
            );

        ArrayList<Location> savedDoorLocations =
            readTypedList(
                data,
                "doors",
                Location.class
            );

        /*
         * Rebuild the runtime wrapper objects.
         */
        ArrayList<Interior> interiors = new ArrayList<>();

        for (BoundingBox box : savedBoundingBoxes) {
            interiors.add(new Interior(box));
        }

        ArrayList<Door> doors = new ArrayList<>();

        for (Location l : savedDoorLocations) {
            doors.add(new Door(l));
        }
        House h= new House();
        h.name=name;
        h.location=location;
        h.tpout=tpout;
        h.lectern=lectern;
        h.interior=interiors;
        h.doors=doors;
        return h;
    }

    private static <T> T requireType(
        Map<String, Object> data,
        String key,
        Class<T> type
    ) {
        Object value = data.get(key);

        if (!type.isInstance(value)) {
            return null;
            // throw new IllegalArgumentException(
            //     "House field '" + key + "' must be a "
            //         + type.getSimpleName()
            // );
        }

        return type.cast(value);
    }

    private static <T> ArrayList<T> readTypedList(
        Map<String, Object> data,
        String key,
        Class<T> type
    ) {
        ArrayList<T> result = new ArrayList<>();

        Object value = data.get(key);

        if (!(value instanceof List<?> list)) {
            return result;
        }

        for (Object element : list) {
            if (type.isInstance(element)) {
                result.add(type.cast(element));
            }
        }

        return result;
    }
}
class Interior{
    BoundingBox b;
    BlockDisplay d;
    public Interior(BoundingBox b){
        this.b=b;
    }
    public BoundingBox getBoundingBox(){
        return b;
    }
    public Interior(BoundingBox b, BlockDisplay d){
        this.b=b;
        this.d=d;
    }
}
class Door{
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
