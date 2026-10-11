package io.github.gegagegagega.werewolf.game;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

import com.destroystokyo.paper.profile.PlayerProfile;

import io.github.gegagegagega.werewolf.core.GamePhase;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.scene.SceneKeys;
import io.github.gegagegagega.werewolf.scene.SceneLocations;

/** The execution animation shown during {@link GamePhase#SENTENCE}. */
public final class Guillotine {

    private static final String TAG = "ww_guillotine";
    /** The blade starts falling when this many ticks are left in the sentence. */
    private static final int DROP_AT_TICKS_LEFT = (10 - 6) * 20;
    private static final float GRAVITY = -0.1f;

    private final JavaPlugin plugin;
    private final SceneLocations scene;
    private float bladeVelocity = 0;
    private BlockDisplay blade;
    private BlockDisplay chain;
    private ItemDisplay head;

    public Guillotine(JavaPlugin plugin, SceneLocations scene) {
        this.plugin = plugin;
        this.scene = scene;
    }

    private Location top() {
        return scene.getLocation(SceneKeys.GUILLOTINE_TOP);
    }

    private Location bottom() {
        return scene.getLocation(SceneKeys.GUILLOTINE_BOTTOM);
    }

    /** Raises the blade and removes any head. */
    public void reset() {
        Location top = top();
        if (top == null || bottom() == null) {
            return;
        }
        WorldEffects.removeEntitiesWithTag(TAG);
        bladeVelocity = 0;
        removeHead();

        blade = top.getWorld().spawn(top.getBlock().getLocation(), BlockDisplay.class, display -> {
            display.setBlock(Material.IRON_BLOCK.createBlockData());
            configure(display);
            display.setTransformationMatrix(new Matrix4f()
                    .translate(-0.25f, 0.1f, 0.75f)
                    .scale(1.5f, 1f, 0.0625f));
        });
        chain = top.getWorld().spawn(top.getBlock().getLocation(), BlockDisplay.class, display -> {
            display.setBlock(Material.IRON_CHAIN.createBlockData());
            configure(display);
            display.setTransformationMatrix(new Matrix4f()
                    .translate(0f, 1f, 0.25f)
                    .scale(1f, 1f, 1f));
        });
    }

    /** Puts the accused's head in the stocks. */
    public void placeHead(WWPlayer accused) {
        Location bottom = bottom();
        if (bottom == null || accused == null) {
            return;
        }
        head = bottom.getWorld().spawn(bottom.getBlock().getLocation(), ItemDisplay.class, display -> {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            if (!(item.getItemMeta() instanceof SkullMeta skullMeta)) {
                throw new IllegalStateException("PLAYER_HEAD did not provide SkullMeta");
            }
            PlayerProfile profile = Bukkit.createProfile(accused.getName());
            skullMeta.setPlayerProfile(profile);
            item.setItemMeta(skullMeta);
            display.setItemStack(item);
            configure(display);
            display.setTransformationMatrix(new Matrix4f().translate(0.5f, 0.8f, 0.6f));
        });
    }

    public void removeHead() {
        if (head != null) {
            head.remove();
            head = null;
        }
    }

    /**
     * Advances the animation. When the blade lands, {@code execute} is called
     * with the accused (once).
     */
    public void tick(int ticksLeft, WWPlayer accused, Consumer<WWPlayer> execute) {
        Location top = top();
        Location bottom = bottom();
        if (top == null || bottom == null || blade == null || chain == null) {
            return;
        }
        if (ticksLeft == DROP_AT_TICKS_LEFT) {
            top.getWorld().playSound(top, Sound.BLOCK_CHEST_LOCKED, 1.0f, 0.1f);
        }
        if (ticksLeft >= DROP_AT_TICKS_LEFT) {
            return;
        }

        bladeVelocity += GRAVITY;
        if (blade.getLocation().add(0, bladeVelocity, 0).getY() < bottom.getY()) {
            if (accused != null && !accused.isGhost()) {
                bottom.getWorld().playSound(bottom, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.1f);
                bottom.getWorld().playSound(bottom, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 2f);
                ItemDisplay fallingHead = head;
                if (fallingHead != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        fallingHead.setInterpolationDelay(0);
                        fallingHead.setInterpolationDuration(5);
                        fallingHead.setTransformationMatrix(new Matrix4f()
                                .translate(0.5f, -0.3f, 0.2f)
                                .rotateX((float) -Math.toRadians(90)));
                    });
                }
                execute.accept(accused);
            }
            blade.teleport(bottom.getBlock().getLocation());
            chain.teleport(bottom.getBlock().getLocation());
        } else {
            blade.teleport(blade.getLocation().add(0, bladeVelocity, 0));
            chain.teleport(chain.getLocation().add(0, bladeVelocity, 0));
        }

        chain.setTransformationMatrix(new Matrix4f()
                .translate(0f, 1f, 0.25f)
                .scale(1f, (float) (1 + top.getBlock().getLocation().getY() - chain.getLocation().getY()), 1f));
    }

    private static void configure(org.bukkit.entity.Display display) {
        display.setViewRange(256f);
        display.setShadowRadius(0f);
        display.setShadowStrength(0f);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(2);
        display.setPersistent(false);
        display.setVisibleByDefault(true);
        display.addScoreboardTag(TAG);
    }
}
