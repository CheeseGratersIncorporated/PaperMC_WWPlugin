package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;
import org.bukkit.*;

public class House {
    
    public String name;
    public WWPlayer owner;
    public Location location;
    public ArrayList<Door> doors = new ArrayList<Door>();
    public RotatingGhostBeam beam;
    public RotatingGhostBeam claimedBeam;
    public RotatingGhostBeam ownerBeam;
    public RotatingGhostBeam doorBeam;
    public RotatingGhostBeam hoverBeam;
    
    
    public House(String name, Location location) {
        this.name = name;
        this.location = location;
        this.owner = null;
    }
    public House(String name, Location location, WWPlayer claimedBy) {
        this.name = name;
        this.location = location;
        this.owner = claimedBy;
    }
    public House() {
        this.name = null;
        this.location = null;
        this.owner = null;
    }

}
