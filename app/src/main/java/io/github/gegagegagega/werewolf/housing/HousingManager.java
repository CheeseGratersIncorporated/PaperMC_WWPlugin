package io.github.gegagegagega.werewolf.housing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import org.bukkit.Material;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.joml.Matrix4f;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;



public class HousingManager {
    public static boolean debug = false; 
    public Location open,close;
    public Player follow;
    public BlockDisplay interiorSetDisplay;
    public ArrayList<BlockDisplay> debugBlockDisplays=new ArrayList<>();


    JavaPlugin plugin;
    Set<String> viewers = new HashSet<String>();
    ArrayList<House> houses = new ArrayList<House>();
    Map<String, House> houseMap = new HashMap<>();
    Map<String, House> ownerMap = new HashMap<>();
    Map<String, House> hoverMap = new HashMap<>();
    
    public HousingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Initialize housing manager
    }

    public void toggleViewer(Player player) {
        if (!viewers.contains(player.getName())) {
            viewers.add(player.getName());
        }else {
            viewers.remove(player.getName());
        }
        refreshVisibility();
    }
    public void addViewer(Player player) {
        if (!viewers.contains(player.getName())) {
            viewers.add(player.getName());
        }
        refreshVisibility();
    }
    public void removeViewer(Player player) {
        if (viewers.contains(player.getName())) {
            viewers.remove(player.getName());
        }
        refreshVisibility();
    }
    public void clearViewers() {
        viewers.clear();
        refreshVisibility();
    }

    public void refreshVisibility() {
        for (House house : houses) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                
                if (house.beam != null) {
                    if(house.owner==null && !house.hovered && viewers.contains(player.getName())){
                        house.beam.addViewer(player);
                    } else {
                        house.beam.removeViewer(player);
                    }
                }
                if (house.hoverBeam != null) {
                    if(house.owner==null && house.hovered && viewers.contains(player.getName())){
                        house.hoverBeam.addViewer(player);

                    } else {
                        house.hoverBeam.removeViewer(player);
                    }
                }
                if (house.ownerBeam != null ) {
                    if (house.owner!=null&& house.owner.equals(player.getName()) && viewers.contains(player.getName())) {
                        house.ownerBeam.addViewer(player);
                    } else {
                        house.ownerBeam.removeViewer(player);
                    }
                }
                
                if (house.claimedBeam != null) {
                    if (house.owner!=null&&!house.owner.equals(player.getName()) && viewers.contains(player.getName())) {
                        house.claimedBeam.addViewer(player);
                    } else {
                        house.claimedBeam.removeViewer(player);
                    }
                }
            }
            house.beam.refreshVisibility();
            house.hoverBeam.refreshVisibility();
            house.ownerBeam.refreshVisibility();
            house.claimedBeam.refreshVisibility();
        }
    }

    public Component getHousingInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Housing Info:\n");
        sb.append("Viewers: " + viewers.size() + "\n");
        return Component.text(sb.toString());
    }
    public ArrayList<House> getHouses(){
        return houses;
    }

    public String getPlayerClaim(Player player){
        House house=houseOf(player.getName());
        return house==null?null:house.name;
    }

    /** The house claimed by this player, or null. */
    public House houseOf(String playerName){
        if(playerName==null)return null;
        return ownerMap.get(playerName);
    }

    /** Frees the house claimed by this player, if any. */
    public void release(String playerName){
        House house=ownerMap.remove(playerName);
        if(house!=null){
            house.owner=null;
            refreshVisibility();
        }
    }

    public boolean getHouseClaim(String house){
        if(houseMap.containsKey(house)){
            return houseMap.get(house).owner!=null;
        }else return false;
    }

    public boolean claim(Player player){
        
        if(hoverMap.containsKey(player.getName())){
            
        House house=hoverMap.get(player.getName());
            return processClaim(player, house);
        }
        return false;
    }
    public boolean claim(Player player, String house){
        if(houseMap.containsKey(house)&&(houseMap.get(house).owner==null)){
         House h=houseMap.get(house);
            return processClaim(player, h);
        }
        return false;
    }

    public boolean processClaim(Player player, House house){
        if((house.owner==null)){
                if(ownerMap.containsKey(player.getName())){
                    ownerMap.get(player.getName()).owner=null;
                }
                house.owner=player.getName();
                ownerMap.put(player.getName(),house);
                updateHover(player);
                player.setRespawnLocation(house.location,true);
                refreshVisibility();
                return true;
            }
            return false;
    }
    public void set(Location location, String name){
        House house = new House();
        house.name = name;
        house.location = location;
        house.beam = new RotatingGhostBeam(plugin, location,Material.LIGHT_BLUE_STAINED_GLASS);
        house.hoverBeam = new RotatingGhostBeam(plugin, location,Material.YELLOW_STAINED_GLASS);
        
        house.claimedBeam = new RotatingGhostBeam(plugin, location,Material.RED_STAINED_GLASS);
        house.ownerBeam = new RotatingGhostBeam(plugin, location,Material.LIME_STAINED_GLASS);
        houses.add(house);
        houseMap.put(name, house);
        refreshVisibility();
    }

    public void load(ArrayList<House> loadedHouses){
        for(House h:houses){
            if (h.beam != null) {
                    h.beam.remove();
                }
                if (h.hoverBeam != null) {
                    h.hoverBeam.remove();
                }
                if (h.ownerBeam != null) {
                    h.ownerBeam.remove();
                }
                if (h.claimedBeam != null) {
                    h.claimedBeam.remove();
                }
        }
        for(BlockDisplay b:debugBlockDisplays)b.remove();
        houses=new ArrayList<>();
        houseMap=new HashMap<>();
        ownerMap=new HashMap<>();
        hoverMap=new HashMap<>();
        for(House house:loadedHouses){
            house.beam = new RotatingGhostBeam(plugin, house.location,Material.LIGHT_BLUE_STAINED_GLASS);
            house.hoverBeam = new RotatingGhostBeam(plugin, house.location,Material.YELLOW_STAINED_GLASS);
            
            house.claimedBeam = new RotatingGhostBeam(plugin, house.location,Material.RED_STAINED_GLASS);
            house.ownerBeam = new RotatingGhostBeam(plugin, house.location,Material.LIME_STAINED_GLASS);
            houses.add(house);
            houseMap.put(house.name, house);
            house.location.setPitch(0f);
            house.location.setYaw(0f);
            if(debug){
                setLectern(house.lectern,house.name);
                setTpout(house.tpout, house.name);
                for(Door d:house.doors){
                     d.display=house.location.getWorld().spawn(d.getLocation(), BlockDisplay.class, display -> {
                    display.setBlock(Material.RED_STAINED_GLASS.createBlockData());

                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
            });
            debugBlockDisplays.add(d.display);
                }
                
                
                for(Interior i:house.interior){
                    i.d=house.location.getWorld().spawn(new Location(house.location.getWorld(),i.getBoundingBox().getMinX(),i.getBoundingBox().getMinY(),i.getBoundingBox().getMinZ()), BlockDisplay.class, display -> {
                    display.setBlock(Material.PINK_STAINED_GLASS.createBlockData());
                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
                    Matrix4f matrix = new Matrix4f()
                    .scale((float)i.getBoundingBox().getWidthX()+1f,(float)i.getBoundingBox().getHeight()+1f,(float)i.getBoundingBox().getWidthZ()+1f);
         ;   //.translate((float)((housingManager.close.getX()-housingManager.open.getX())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2) );

                    display.setTransformationMatrix(matrix);
            });
                debugBlockDisplays.add(i.d);
                }
                }
            }
        
        refreshVisibility();
        
    }

    public void delete(String name){
        House house = houseMap.get(name);
                if (house == null) return;
                houses.remove(house);
                houseMap.remove(name);
                if (house.owner != null) ownerMap.remove(house.owner);
                if (house.beam != null) {
                    house.beam.remove();
                }
                if (house.hoverBeam != null) {
                    house.hoverBeam.remove();
                }
                if (house.ownerBeam != null) {
                    house.ownerBeam.remove();
                }
                if (house.claimedBeam != null) {
                    house.claimedBeam.remove();
                }
        refreshVisibility();
        
    }

    public void updateHover(Player player){
        String name=player.getName();
        
        for(House house : houses){
            if(house.owner!=null){
                house.beam.removeViewer(player);
                house.hoverBeam.removeViewer(player);
            }
            if(player.getLocation().getBlockX()==house.location.getBlockX()
                &&player.getLocation().getBlockZ()==house.location.getBlockZ()){
                    
                    hoverMap.put(name,house);
                    house.hovered=true;
                                                    // player.sendMessage(Component.text("trued", NamedTextColor.YELLOW));

                    refreshVisibility();
            }else {
                if(hoverMap.containsKey(name)&&hoverMap.get(name)==house){
                    if(player.getLocation().getBlockX()!=hoverMap.get(name).location.getBlockX()
                        ||player.getLocation().getBlockZ()!=hoverMap.get(name).location.getBlockZ()){
                            
                            hoverMap.remove(name);
                            if(!hoverMap.values().contains(house)){
                                house.hovered=false;
                                // player.sendMessage(Component.text("falsed", NamedTextColor.YELLOW));
                    
                            }
                            refreshVisibility();
                    }
                }
            }
        }
    }

    public boolean setLectern(Location l, String house){
        if(l==null)return false;
        House h=houseMap.get(house);
        if(h==null)
            return false;
        else 
            h.lectern=l;

        if(debug){
             debugBlockDisplays.add(l.getWorld().spawn(l, BlockDisplay.class, display -> {
                    display.setBlock(Material.YELLOW_STAINED_GLASS.createBlockData());

                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
            }));
        }
        return true;
    }
    public boolean setTpout(Location l, String house){
        
        if(l==null)return false;
        House h=houseMap.get(house);
        if(h==null)
            return false;
        else 
            h.tpout=l;

        if(debug){
             debugBlockDisplays.add(l.getWorld().spawn(l, BlockDisplay.class, display -> {
                    display.setBlock(Material.ORANGE_STAINED_GLASS.createBlockData());

                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
            }));
        }
        return true;
    }
    public boolean addInterior(String house){

        House h=houseMap.get(house);
        if(h==null){
            
            return false;
        }
        
        if(debug){
                h.interior.add(new Interior(BoundingBox.of(open,close),interiorSetDisplay));

        }else{
            h.interior.add(new Interior(BoundingBox.of(open,close)));

        }
        return true;
    }
    public void closeAllDoors(){
        for(House h: houses){
            for(Door d:h.doors){
                d.close();
            }
        }
    }
    public boolean deleteInterior(Location l, String house){
        House h=houseMap.get(house);
        if(h==null){
            
            return false;
        }
        for(Interior i:h.interior)
            if(i.b.contains(l.getX(),l.getY(),l.getZ())){
                i.d.remove();
                h.interior.remove(i);
                return true;
            }

        if(debug){
            
        }
        return true;
    }
    public boolean setDoor(Location l, String house){
        
        if(l==null)return false;
        House h=houseMap.get(house);
        if(h==null)return false;
         
            for(Door d : h.doors){
                if(d.location.getBlock().equals(l.getBlock())){
                    if(debug&&d.display!=null)
                    d.display.remove();
                    h.doors.remove(d);
                    return true;
                }
            }
            Door d=new Door(l);

        if(debug){
             d.display=l.getWorld().spawn(l, BlockDisplay.class, display -> {
                    display.setBlock(Material.RED_STAINED_GLASS.createBlockData());

                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
            });
            debugBlockDisplays.add(d.display);
        }
        
            h.doors.add(d);
        return true;
    }
    /** Gives every player without a house a random free one. */
    public void forceClaim(Collection<String> playerNames){
        List<String> homeless=new ArrayList<>(playerNames);
        Collections.shuffle(homeless);
        List<House> free=new ArrayList<>(houses);
        Collections.shuffle(free);
        int i=0;
        for(String name:homeless){
            if(houseOf(name)!=null)continue;

            Bukkit.getServer().broadcast(Component.text("homeless -> "+name));
            while(i<free.size()&&free.get(i).owner!=null)i++;
            if(i>=free.size())break;
            House h=free.get(i);
            h.owner=name;
            ownerMap.put(name,h);
            refreshVisibility();
            Player p=Bukkit.getPlayerExact(name);
            if(p!=null){
                p.setRespawnLocation(h.location,true);
                updateHover(p);
                p.sendMessage(Component.text("You have claimed " + h.name, NamedTextColor.GREEN));
            }
        }
    }

    public Set<String> getHouseNames() {
        
        return houseMap.keySet();
    
    }
}


