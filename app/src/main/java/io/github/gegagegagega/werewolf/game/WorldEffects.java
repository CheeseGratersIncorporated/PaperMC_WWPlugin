package io.github.gegagegagega.werewolf.game;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.gegagegagega.werewolf.scene.SceneKeys;
import io.github.gegagegagega.werewolf.scene.SceneLocations;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;

/** World-level atmosphere: the town's fog biome and the time of day. */
public final class WorldEffects {

    private static final Key FOG_BIOME = Key.key("hehehe:foggy");

    private final JavaPlugin plugin;
    private final SceneLocations scene;
    private final Biome fogBiome;

    public WorldEffects(JavaPlugin plugin, SceneLocations scene) {
        this.plugin = plugin;
        this.scene = scene;
        Registry<Biome> biomes = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        this.fogBiome = biomes.get(FOG_BIOME);
        if (fogBiome == null) {
            plugin.getLogger().warning("Biome " + FOG_BIOME.asString() + " not found; night fog disabled. Is the datapack installed?");
        }
    }

    /** Covers the town in the night fog biome. */
    public void fog() {
        if (fogBiome != null) {
            setTownBiome(fogBiome);
        }
    }

    /** Back to the normal daytime biome. */
    public void clearFog() {
        setTownBiome(Biome.TAIGA);
    }

    private void setTownBiome(Biome biome) {
        Location corner1 = scene.getLocation(SceneKeys.TOWN_CORNER_1);
        Location corner2 = scene.getLocation(SceneKeys.TOWN_CORNER_2);
        if (corner1 == null || corner2 == null) {
            return;
        }
        setBiomeVolume(corner1, corner2, biome);
    }

    public void setTime(long time) {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(time);
        }
    }

    public void setBiomeVolume(Location corner1, Location corner2, Biome biome) {
        Objects.requireNonNull(corner1, "corner1");
        Objects.requireNonNull(corner2, "corner2");
        Objects.requireNonNull(biome, "biome");

        World world = corner1.getWorld();
        if (world == null || corner2.getWorld() == null) {
            throw new IllegalArgumentException("Locations must have worlds");
        }
        if (!world.equals(corner2.getWorld())) {
            throw new IllegalArgumentException("Both locations must be in the same world");
        }

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.max(world.getMinHeight(), Math.min(corner1.getBlockY(), corner2.getBlockY()));
        int maxY = Math.min(world.getMaxHeight() - 1, Math.max(corner1.getBlockY(), corner2.getBlockY()));
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        if (minY > maxY) {
            return;
        }

        // Align the coordinates to the containing 4x4x4 biome cells.
        int startX = Math.floorDiv(minX, 4) * 4;
        int startY = Math.floorDiv(minY, 4) * 4;
        int startZ = Math.floorDiv(minZ, 4) * 4;
        int endX = Math.floorDiv(maxX, 4) * 4;
        int endY = Math.floorDiv(maxY, 4) * 4;
        int endZ = Math.floorDiv(maxZ, 4) * 4;

        for (int x = startX; x <= endX; x += 4) {
            for (int y = startY; y <= endY; y += 4) {
                for (int z = startZ; z <= endZ; z += 4) {
                    world.setBiome(x, y, z, biome);
                }
            }
        }

        int minChunkX = startX >> 4;
        int maxChunkX = endX >> 4;
        int minChunkZ = startZ >> 4;
        int maxChunkZ = endZ >> 4;
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        world.refreshChunk(chunkX, chunkZ);
                    }
                }
            }
        });
    }

    public static int removeEntitiesWithTag(String tag) {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains(tag)) {
                    entity.remove();
                    removed++;
                }
            }
        }
        return removed;
    }
}
