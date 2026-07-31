package jenny.werewolf.papermc.plugin.commands.housing;

import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.ExamplePlugin;
import jenny.werewolf.papermc.plugin.HousingManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.mojang.brigadier.Command;



public class InteriorCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(ExamplePlugin plugin) {
        return Commands.literal("interior")
        .then(Commands.literal("open")
        .executes(ctx -> {
               
                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housingManager.follow=player;
                    plugin.housingManager.open=player.getLocation().getBlock().getLocation();
                    
             plugin.housingManager.interiorSetDisplay=plugin.housingManager.open.getWorld().spawn(plugin.housingManager.open, BlockDisplay.class, display -> {
                    display.setBlock(Material.PINK_STAINED_GLASS.createBlockData());
                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
            });
                        player.sendMessage(Component.text("Interior setting opened", NamedTextColor.LIGHT_PURPLE));
                }
                return Command.SINGLE_SUCCESS;
            }))
        .then(Commands.literal("cancel")
        .executes(ctx -> {
               
                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housingManager.follow=null;
                    plugin.housingManager.open=null;
                    
             plugin.housingManager.interiorSetDisplay.remove();
             
             plugin.housingManager.interiorSetDisplay=null;
                        player.sendMessage(Component.text("Interior setting cancelled", NamedTextColor.LIGHT_PURPLE));
                }
                return Command.SINGLE_SUCCESS;
            }))
        .then(Commands.literal("close")
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.housingManager.getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    if(player==plugin.housingManager.follow){
                        plugin.housingManager.follow=null;
                        plugin.housingManager.addInterior(name);
                        if(HousingManager.debug)
                        plugin.housingManager.debugBlockDisplays.add(plugin.housingManager.interiorSetDisplay);
                        else{
                            plugin.housingManager.interiorSetDisplay.remove();
                        }
                        player.sendMessage(Component.text("Interior added to "+name, NamedTextColor.LIGHT_PURPLE));
                    }else{

                    }
                    
                }
                return Command.SINGLE_SUCCESS;
            })))
            .then(Commands.literal("delete")
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.housingManager.getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housingManager.deleteInterior(player.getLocation(), name);
                    
                }
                return Command.SINGLE_SUCCESS;
            })));
    }
}
