package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;


import org.bukkit.entity.Player;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import net.kyori.adventure.text.Component;



public class HousingManager {
    ExamplePlugin plugin;
    ArrayList<Player> viewers = new ArrayList<Player>();
    ArrayList<House> houses = new ArrayList<House>();
    Map<String, House> houseMap = new HashMap<>();
    Map<String, House> ownerMap = new HashMap<>();
    Map<String, House> hoverMap = new HashMap<>();
    
    public HousingManager(ExamplePlugin plugin) {
        this.plugin = plugin;
        // Initialize housing manager
    }

    public void toggleViewer(Player player) {
        if (!viewers.contains(player)) {
            viewers.add(player);
        }else {
            viewers.remove(player);
        }
        refreshVisibility();
    }

    public void refreshVisibility() {
        for (House house : houses) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                
                if (house.beam != null && house.owner==null) {
                    if (viewers.contains(player)) {
                        house.beam.addViewer(player);
                    } else {
                        house.beam.removeViewer(player);
                    }
                }
                if (house.ownerBeam != null ) {
                    if (house.owner!=null&&viewers.contains(player)&& house.owner.getName()==(player.getName())) {
                        house.ownerBeam.addViewer(player);
                    } else {
                        house.ownerBeam.removeViewer(player);
                    }
                }
                
                if (house.claimedBeam != null) {
                    if (house.owner!=null&&viewers.contains(player)&&house.owner.getName()!=(player.getName())) {
                        house.claimedBeam.addViewer(player);
                    } else {
                        house.claimedBeam.removeViewer(player);
                    }
                }
            }
        }
    }

    public Component getHousingInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Housing Info:\n");
        sb.append("Viewers: " + viewers.size() + "\n");
        return Component.text(sb.toString());
    }

    public String getPlayerClaim(Player player){
        if(ownerMap.containsKey(player.getName())){
            return ownerMap.get(player.getName()).name;
        }else return null;
    }

    public boolean getHouseClaim(String house){
        if(houseMap.containsKey(house)){
            return houseMap.get(house).owner!=null;
        }else return false;
    }

    public boolean claim(Player player){
        
        if(hoverMap.containsKey(player.getName())){
            
        House house=hoverMap.get(player.getName());
            if((house.owner==null)){
                if(ownerMap.containsKey(player.getName())){
                    ownerMap.get(player.getName()).owner=null;
                }
                house.owner=plugin.gameInstance.getWWPlayer(player.getName());
                ownerMap.put(player.getName(),house);
                updateHover(player);
                refreshVisibility();
                return true;
            }
        }
        return false;
    }
    public boolean claim(Player player, String house){
        if((houseMap.get(house).owner==null)){
         House h=houseMap.get(house);
            if((h.owner==null)){
                if(ownerMap.containsKey(player.getName())){
                    ownerMap.get(player.getName()).owner=null;
                }
                h.owner=plugin.gameInstance.getWWPlayer(player.getName());;
                ownerMap.put(player.getName(),h);
                updateHover(player);
                
                refreshVisibility();
                return true;
            }
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

    public void delete(String name){
        House house = houseMap.get(name);
                houses.remove(house);
                houseMap.remove(name);
                if (house.beam != null) {
                    house.beam.remove();
                }
                if (house.hoverBeam != null) {
                    house.hoverBeam.remove();
                }
        refreshVisibility();
        
    }

    public void updateHover(Player player){
        String uuid=player.getName();
        
        for(House house : houses){
            if(house.owner!=null){
                house.beam.removeViewer(player);
                house.hoverBeam.removeViewer(player);
            }
            if(player.getLocation().getBlockX()==house.location.getBlockX()
                &&player.getLocation().getBlockZ()==house.location.getBlockZ()){
                    if(hoverMap.containsKey(uuid)&&hoverMap.get(uuid).name!=house.name){
                            hoverMap.get(uuid).hoverBeam.removeViewer(player);
                            if(viewers.contains(player))
                            hoverMap.get(uuid).beam.addViewer(player);
                        }
                    hoverMap.put(uuid,house);
                    if(viewers.contains(player)){
                        
                        house.hoverBeam.addViewer(player);
                        house.beam.removeViewer(player);
                    }
            }else {
                if(hoverMap.containsKey(uuid)){
                    if(player.getLocation().getBlockX()!=hoverMap.get(uuid).location.getBlockX()
                        ||player.getLocation().getBlockZ()!=hoverMap.get(uuid).location.getBlockZ()){
                            if(viewers.contains(player)){
                                
                                hoverMap.get(uuid).hoverBeam.removeViewer(player);
                                hoverMap.get(uuid).beam.addViewer(player);
                            }
                            hoverMap.remove(uuid);
                    }
                }
            }
        }
    }


    public void forceClaim(){}

    public Set<String> getHouseNames() {
        
        return houseMap.keySet();
    
    }
}

class Door{
    public Location location;
}


class HousingDataManager {

    private final ExamplePlugin plugin;
    private final File file;
    private FileConfiguration data;

    public HousingDataManager(ExamplePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "housing.yml");

        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Could not create housing.yml", e);
            }
        }

        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not save housing.yml", e);
        }
    }

    public FileConfiguration getData() {
        return data;
    }
}