package jenny.werewolf.papermc.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class RotatingGhostBeam {

    private final ExamplePlugin plugin;
    private final List<BlockDisplay> parts = new ArrayList<>();

    private BukkitTask task;
    private float angle = 0.0f;
    private final int height;
    private Set<Player> viewers = new HashSet<>();
    private Material beamMat=Material.LIGHT_BLUE_STAINED_GLASS;

    public RotatingGhostBeam(ExamplePlugin plugin, Location base, Material m) {
        this.plugin = plugin;
        beamMat=m;
        World world = Objects.requireNonNull(base.getWorld());

        int x = base.getBlockX();
        int y = base.getBlockY();
        int z = base.getBlockZ();

        this.height = Math.max(1, world.getMaxHeight() - y);

        Location beamLocation = new Location(world, x, y, z);

        parts.add(spawnBeamPart(beamLocation));
        parts.add(spawnBeamPart(beamLocation));

        updateTransformations();
        startRotating();
    }

    private BlockDisplay spawnBeamPart(Location location) {
        return location.getWorld().spawn(location, BlockDisplay.class, display -> {
            display.setBlock(beamMat.createBlockData());

            display.setBrightness(new Display.Brightness(15, 15));
            display.setViewRange(256f);

            display.setShadowRadius(0f);
            display.setShadowStrength(0f);

            display.setInterpolationDelay(0);
            display.setInterpolationDuration(2);

            display.setPersistent(false);
            display.setVisibleByDefault(false);
        });
    }

    public void startRotating() {
        if (task != null) {
            return;
        }

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            angle += Math.toRadians(4);

            if (angle > Math.PI * 2) {
                angle = 0.0f;
            }

            updateTransformations();
        }, 0L, 1L);
    }

    private void updateTransformations() {
        setPanelTransformation(parts.get(0), angle);
        setPanelTransformation(parts.get(1), angle + (float) Math.toRadians(90));
    }

    private void setPanelTransformation(BlockDisplay display, float yawRadians) {
        float width = 0.06f;
        float depth = 0.85f;

        Matrix4f matrix = new Matrix4f()
            .translate(0.5f, 0.0f, 0.5f)
            .rotateY(yawRadians)
            .scale(width, height, depth)
            .translate(-0.5f, 0.0f, -0.5f);

        display.setTransformationMatrix(matrix);
    }

    public void setMat(Material m){
        beamMat=m;
        for(BlockDisplay part : parts){
            part.setBlock(beamMat.createBlockData());
        }
    }

    public void stopRotating() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void remove() {
        stopRotating();

        for (BlockDisplay display : parts) {
            display.remove();
        }

        parts.clear();
    }

    public void addViewer(Player viewer) {
        viewers.add(viewer);
        refreshVisibility();
    }
    public void removeViewer(Player viewer) {
        viewers.remove(viewer);
        refreshVisibility();
    }
    public void refreshVisibility(){
        
        Set<UUID> viewerIds = new HashSet<>();

        for (Player viewer : viewers) {
            viewerIds.add(viewer.getUniqueId());
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            for(BlockDisplay entity : parts) {
                if (viewerIds.contains(online.getUniqueId())) {
                    online.showEntity(plugin, entity);
                }else{
                    online.hideEntity(plugin, entity);
                }
            }
        }
    }
    
}