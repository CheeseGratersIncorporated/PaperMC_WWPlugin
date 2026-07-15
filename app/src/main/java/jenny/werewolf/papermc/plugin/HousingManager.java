package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;


import org.bukkit.entity.Player;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;



public class HousingManager {
    ExamplePlugin plugin;
    Set<Player> viewers = new HashSet<Player>();
    Set<House> houses = new HashSet<House>();
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
            for (Player player : viewers) {
                
                if (house.beam != null) {
                    if(house.owner==null && !house.hovered){
                        house.beam.addViewer(player);
                    } else {
                        house.beam.removeViewer(player);
                    }
                }
                if (house.hoverBeam != null) {
                    if(house.owner==null && house.hovered){
                        house.hoverBeam.addViewer(player);

                    } else {
                        house.hoverBeam.removeViewer(player);
                    }
                }
                if (house.ownerBeam != null ) {
                    if (house.owner!=null&& house.owner.getName()==(player.getName())) {
                        house.ownerBeam.addViewer(player);
                    } else {
                        house.ownerBeam.removeViewer(player);
                    }
                }
                
                if (house.claimedBeam != null) {
                    if (house.owner!=null&&house.owner.getName()!=(player.getName())) {
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
                plugin.gameInstance.getWWPlayer(player.getName()).setHouse(house);
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
                
                plugin.gameInstance.getWWPlayer(player.getName()).setHouse(h);
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
                                                    player.sendMessage(Component.text("trued", NamedTextColor.YELLOW));

                    refreshVisibility();
            }else {
                if(hoverMap.containsKey(name)&&hoverMap.get(name)==house){
                    if(player.getLocation().getBlockX()!=hoverMap.get(name).location.getBlockX()
                        ||player.getLocation().getBlockZ()!=hoverMap.get(name).location.getBlockZ()){
                            
                            hoverMap.remove(name);
                            if(!hoverMap.values().contains(house)){
                                house.hovered=false;
                                player.sendMessage(Component.text("falsed", NamedTextColor.YELLOW));
                    
                            }
                            refreshVisibility();
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