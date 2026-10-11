package io.github.gegagegagega.werewolf.commands.housing;

import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.gegagegagega.werewolf.housing.HousingManager;
import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.mojang.brigadier.Command;



public class InteriorCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("interior")
        .then(Commands.literal("open")
        .executes(ctx -> {
               
                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housing().follow=player;
                    plugin.housing().open=player.getLocation().getBlock().getLocation();
                    
             plugin.housing().interiorSetDisplay=plugin.housing().open.getWorld().spawn(plugin.housing().open, BlockDisplay.class, display -> {
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
                    plugin.housing().follow=null;
                    plugin.housing().open=null;
                    
             plugin.housing().interiorSetDisplay.remove();
             
             plugin.housing().interiorSetDisplay=null;
                        player.sendMessage(Component.text("Interior setting cancelled", NamedTextColor.LIGHT_PURPLE));
                }
                return Command.SINGLE_SUCCESS;
            }))
        .then(Commands.literal("close")
        .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : plugin.housing().getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    if(player==plugin.housing().follow){
                        plugin.housing().follow=null;
                        plugin.housing().addInterior(name);
                        if(HousingManager.debug)
                        plugin.housing().debugBlockDisplays.add(plugin.housing().interiorSetDisplay);
                        else{
                            plugin.housing().interiorSetDisplay.remove();
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
                            for (String name : plugin.housing().getHouseNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
            .executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");

                if (ctx.getSource().getSender() instanceof Player player) {
                    plugin.housing().deleteInterior(player.getLocation(), name);
                    
                }
                return Command.SINGLE_SUCCESS;
            })));
    }
}
