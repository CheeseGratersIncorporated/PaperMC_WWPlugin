package io.github.gegagegagega.werewolf.scene;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Named locations for the map (wolf den, trial stage, ...), saved in a YAML
 * file. See {@link SceneKeys} for the names the game uses.
 */
public class SceneLocations {

    private final JavaPlugin plugin;
    private final File file;

    private final Map<String, Location> locations = new HashMap<>();

    public SceneLocations(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;

        if (!fileName.endsWith(".yml")) {
            fileName += ".yml";
        }

        this.file = new File(plugin.getDataFolder(), fileName);
    }

    /*
     * Add or replace a named location.
     */
    public void setLocation(String name, Location location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name cannot be blank");
        }

        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException(
                    "Location and its world cannot be null"
            );
        }

        locations.put(name, location.clone());
    }

    /*
     * Returns a copy so outside code cannot accidentally modify the stored
     * Location without calling setLocation().
     */
    public Location getLocation(String name) {
        Location location = locations.get(name);

        return location == null ? null : location.clone();
    }

    public boolean hasLocation(String name) {
        return locations.containsKey(name);
    }

    public Location removeLocation(String name) {
        Location removed = locations.remove(name);

        return removed == null ? null : removed.clone();
    }

    public void clearLocations() {
        locations.clear();
    }

    /*
     * Read-only view of the map.
     *
     * The Location objects themselves are copied.
     */
    public Map<String, Location> getLocations() {
        Map<String, Location> copy = new HashMap<>();

        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().clone());
        }

        return Collections.unmodifiableMap(copy);
    }

    /*
     * Save all locations to the YAML file.
     */
    public void save() {
        createDataFolder();

        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            String name = entry.getKey();
            Location location = entry.getValue();
            World world = location.getWorld();

            if (world == null) {
                plugin.getLogger().warning(
                        "Could not save scene location '" + name
                                + "' because its world is null."
                );
                continue;
            }

            String path = "locations." + name;

            config.set(path + ".world", world.getName());
            config.set(path + ".world-uuid", world.getUID().toString());

            config.set(path + ".x", location.getX());
            config.set(path + ".y", location.getY());
            config.set(path + ".z", location.getZ());

            config.set(path + ".yaw", location.getYaw());
            config.set(path + ".pitch", location.getPitch());
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Failed to save scene file: " + file.getName()
            );
            exception.printStackTrace();
        }
    }

    /*
     * Replace the current map with locations from the YAML file.
     */
    public void load() {
        locations.clear();

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        ConfigurationSection locationsSection =
                config.getConfigurationSection("locations");

        if (locationsSection == null) {
            return;
        }

        for (String name : locationsSection.getKeys(false)) {
            String path = "locations." + name;

            World world = loadWorld(config, path);

            if (world == null) {
                plugin.getLogger().warning(
                        "Could not load scene location '" + name
                                + "' because its world does not exist."
                );
                continue;
            }

            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");

            float yaw = (float) config.getDouble(path + ".yaw");
            float pitch = (float) config.getDouble(path + ".pitch");

            Location location = new Location(
                    world,
                    x,
                    y,
                    z,
                    yaw,
                    pitch
            );

            locations.put(name, location);
        }
    }

    private World loadWorld(
            YamlConfiguration config,
            String locationPath
    ) {
        String uuidText =
                config.getString(locationPath + ".world-uuid");

        if (uuidText != null) {
            try {
                UUID uuid = UUID.fromString(uuidText);
                World world = Bukkit.getWorld(uuid);

                if (world != null) {
                    return world;
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid UUID; fall back to the world name.
            }
        }

        String worldName =
                config.getString(locationPath + ".world");

        if (worldName == null) {
            return null;
        }

        return Bukkit.getWorld(worldName);
    }

    private void createDataFolder() {
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning(
                    "Could not create plugin data folder."
            );
        }
    }
}