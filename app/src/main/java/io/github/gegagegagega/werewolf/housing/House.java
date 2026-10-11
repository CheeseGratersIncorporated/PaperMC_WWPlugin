package io.github.gegagegagega.werewolf.housing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;



@SerializableAs("House")
public class House implements ConfigurationSerializable  {
    
    public String name;
    /** Name of the player who claimed this house, or null. */
    public String owner;
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
    public House(String name, Location location, String claimedBy) {
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
        setDoorsOpen(true);
    }
    public void closeDoors(){
        setDoorsOpen(false);
    }
    /** Opens or closes every door, skipping ones already in that state. */
    public void setDoorsOpen(boolean open){
        for(Door d : doors){
            if(d.isOpen()!=open)d.setOpen(open);
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
