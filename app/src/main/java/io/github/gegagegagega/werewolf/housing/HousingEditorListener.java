package io.github.gegagegagega.werewolf.housing;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.joml.Matrix4f;

/** Live preview while marking a house interior, and the claim beams that react to standing on them. */
public final class HousingEditorListener implements Listener {

    private final HousingManager housing;

    public HousingEditorListener(HousingManager housing) {
        this.housing = housing;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (housing.follow != null && housing.interiorSetDisplay != null && housing.open != null) {
            housing.close = housing.follow.getLocation().getBlock().getLocation();
            Location open = housing.open;
            Location close = housing.close;
            housing.interiorSetDisplay.setTransformationMatrix(new Matrix4f().scale(
                    (float) (1 + Math.abs(close.getX() - open.getX())),
                    (float) (1 + Math.abs(close.getY() - open.getY())),
                    (float) (1 + Math.abs(close.getZ() - open.getZ()))));
            housing.interiorSetDisplay.teleport(new Location(open.getWorld(),
                    Math.min(open.getX(), close.getX()),
                    Math.min(open.getY(), close.getY()),
                    Math.min(open.getZ(), close.getZ())));
        }
        housing.updateHover(event.getPlayer());
    }
}
