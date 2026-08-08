package jenny.werewolf.papermc.plugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;

import org.bukkit.entity.Player;

import java.util.List;

public class ItemHider {
    public void hideHeldItem(Player target, Player viewer) {
    WrapperPlayServerEntityEquipment packet =
            new WrapperPlayServerEntityEquipment(
                    target.getEntityId(),
                    List.of(
                            new Equipment(
                                    EquipmentSlot.MAIN_HAND,
                                    ItemStack.EMPTY
                            ),
                            new Equipment(
                                    EquipmentSlot.OFF_HAND,
                                    ItemStack.EMPTY
                            )
                    )
            );

    PacketEvents.getAPI()
            .getPlayerManager()
            .sendPacket(viewer, packet);
}
public void hideHeldItemFromEveryone(Player target) {
    for (Player viewer : target.getWorld().getPlayers()) {
        if (!viewer.equals(target)) {
            hideHeldItem(target, viewer);
        }
    }
}
public void showHeldItem(Player target, Player viewer) {
    WrapperPlayServerEntityEquipment packet =
            new WrapperPlayServerEntityEquipment(
                    target.getEntityId(),
                    List.of(
                            new Equipment(
                                    EquipmentSlot.MAIN_HAND,
                                    SpigotConversionUtil.fromBukkitItemStack(
                                            target.getInventory().getItemInMainHand()
                                    )
                            ),
                            new Equipment(
                                    EquipmentSlot.OFF_HAND,
                                    SpigotConversionUtil.fromBukkitItemStack(
                                            target.getInventory().getItemInOffHand()
                                    )
                            )
                    )
            );

    PacketEvents.getAPI()
            .getPlayerManager()
            .sendPacket(viewer, packet);
}
}
