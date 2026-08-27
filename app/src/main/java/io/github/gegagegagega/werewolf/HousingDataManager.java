package io.github.gegagegagega.werewolf;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HousingDataManager {

    private final WerewolfPlugin plugin;
    private final File file;

    private FileConfiguration data;
    public ArrayList<House> houses = new ArrayList<>();

    public HousingDataManager(WerewolfPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "housing.yml");

        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                throw new RuntimeException(
                    "Could not create plugin data folder"
                );
            }
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException(
                        "File was not created"
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException(
                    "Could not create housing.yml",
                    e
                );
            }
        }

        data = YamlConfiguration.loadConfiguration(file);

        loadHouses();
    }

    private void loadHouses() {
        houses.clear();

        List<?> savedHouses = data.getList("houses");

        if (savedHouses == null) {
            return;
        }

        for (Object value : savedHouses) {
            if (value instanceof House house) {
                houses.add(house);
            } else {
                plugin.getLogger().warning(
                    "Skipped invalid house entry in housing.yml"
                );
            }
        }
    }

    public void save() {
        data.set("houses", houses);

        try {
            data.save(file);
        } catch (IOException e) {
            throw new RuntimeException(
                "Could not save housing.yml",
                e
            );
        }
    }

    public void addHouse(House house) {
        houses.add(house);
        save();
    }

    public boolean removeHouse(House house) {
        boolean removed = houses.remove(house);

        if (removed) {
            save();
        }

        return removed;
    }

    public ArrayList<House> getHouses() {
        return houses;
    }

    public FileConfiguration getData() {
        return data;
    }
}
