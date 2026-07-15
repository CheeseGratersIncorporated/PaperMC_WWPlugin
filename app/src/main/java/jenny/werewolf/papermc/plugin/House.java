package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;

import org.bukkit.Location;

public class House {
    
    public String name;
    public WWPlayer owner;
    public Location location;
    public boolean hovered=false;
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

}
