package me.Thelnfamous1.portalgun;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PortalGunCommands {

    public static final String NOT_HOLDING_PORTAL_GUN_KEy = Util.makeDescriptionId("commands", PortalGunMod.id("customportalcolors/not_holding_portal_gun"));
    private static final SimpleCommandExceptionType NOT_HOLDING_PORTAL_GUN = new SimpleCommandExceptionType(Component.translatable(NOT_HOLDING_PORTAL_GUN_KEy));
    public static final String SET_CUSTOM_PORTAL_COLOR_KEY = Util.makeDescriptionId("commands", PortalGunMod.id("customportalcolors/set"));
    public static final String CLEAR_ALL_CUSTOM_PORTAL_COLORS_KEY = Util.makeDescriptionId("commands", PortalGunMod.id("customportalcolors/clear/all"));
    public static final String CLEAR_CUSTOM_PORTAL_COLOR_KEY = Util.makeDescriptionId("commands", PortalGunMod.id("customportalcolors/clear"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal(PortalGunMod.MODID)
                .then(Commands.literal("customportalcolors")
                        //.requires(commandSourceStack -> commandSourceStack.isPlayer() && commandSourceStack.getPlayer().isHolding(is -> is.getItem() instanceof ColoredPortalGun))
                        .then(Commands.literal("set")
                                .then(Commands.argument("side", EnumArgument.enumArgument(PortalGunRecord.PortalGunSide.class))
                                        .then(Commands.literal("dye")
                                                .then(Commands.argument("color", DyeColorArgument.color())
                                                        .then(Commands.literal("texture")
                                                                .executes(context -> setCustomPortalColor(context, DyeColorArgument::getColor, PortalGunCommands::getTextureColor)))
                                                        .then(Commands.literal("material")
                                                                .executes(context -> setCustomPortalColor(context, DyeColorArgument::getColor, dyeColor -> dyeColor.getMapColor().col))
                                                        )
                                                        .then(Commands.literal("firework")
                                                                .executes(context -> setCustomPortalColor(context, DyeColorArgument::getColor, DyeColor::getFireworkColor))
                                                        )
                                                        .then(Commands.literal("text")
                                                                .executes(context -> setCustomPortalColor(context, DyeColorArgument::getColor, DyeColor::getTextColor))
                                                        )))
                                        .then(Commands.literal("team")
                                                .then(Commands.argument("color", ColorArgument.color())
                                                        .executes(context -> setCustomPortalColor(context, ColorArgument::getColor, ChatFormatting::getColor))))
                                        .then(Commands.literal("integer")
                                                .then(Commands.argument("color", IntegerArgumentType.integer())
                                                        .executes(context -> setCustomPortalColor(context, IntegerArgumentType::getInteger, i -> i))))))
                        .then(Commands.literal("clear")
                                .then(Commands.literal("all")
                                        .executes(PortalGunCommands::clearAllCustomPortalColors))
                                .then(Commands.argument("side", EnumArgument.enumArgument(PortalGunRecord.PortalGunSide.class))
                                        .executes(PortalGunCommands::clearCustomPortalColor)))));
    }

    private static <T> int setCustomPortalColor(CommandContext<CommandSourceStack> context, BiFunction<CommandContext<CommandSourceStack>, String, T> argGetter, Function<T, Integer> colorGetter) throws CommandSyntaxException {
        T dyeColor = argGetter.apply(context, "color");
        int color = colorGetter.apply(dyeColor);
        return setCustomPortalColor(context, color);
    }

    private static int getTextureColor(DyeColor dyeColor) {
        float[] textureDiffuseColors = dyeColor.getTextureDiffuseColors();
        int r = (int) (textureDiffuseColors[0] * 255F);
        int g = (int) (textureDiffuseColors[1] * 255F);
        int b = (int) (textureDiffuseColors[2] * 255F);
        return (r << 16) | (g << 8) | b;
    }

    private static int setCustomPortalColor(CommandContext<CommandSourceStack> context, int color) throws CommandSyntaxException {
        PortalGunRecord.PortalGunSide side = context.getArgument("side", PortalGunRecord.PortalGunSide.class);
        ServerPlayer player = context.getSource().getPlayerOrException();
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(player, i -> i instanceof ColoredPortalGun);
        ItemStack portalGun = player.getItemInHand(hand);
        if(portalGun.getItem() instanceof ColoredPortalGun cpg){
            cpg.setCustomPortalColorForSide(portalGun, color, side);
            context.getSource().sendSuccess(() -> Component.translatable(SET_CUSTOM_PORTAL_COLOR_KEY,
                            ColoredPortalGun.getSideDisplayName(side),
                            ColoredPortalGun.getColorName(color),
                            portalGun.getDisplayName()),
                    false);
            return 1;
        } else{
            throw NOT_HOLDING_PORTAL_GUN.create();
        }
    }

    private static int clearAllCustomPortalColors(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(player, i -> i instanceof ColoredPortalGun);
        ItemStack portalGun = player.getItemInHand(hand);
        if(portalGun.getItem() instanceof ColoredPortalGun cpg){
            for(PortalGunRecord.PortalGunSide side : PortalGunRecord.PortalGunSide.values()){
                cpg.clearCustomPortalColorForSide(portalGun, side);
            }
            context.getSource().sendSuccess(() -> Component.translatable(CLEAR_ALL_CUSTOM_PORTAL_COLORS_KEY,
                            portalGun.getDisplayName()),
                    false);
            return 1;
        } else{
            throw NOT_HOLDING_PORTAL_GUN.create();
        }
    }

    private static int clearCustomPortalColor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PortalGunRecord.PortalGunSide side = context.getArgument("side", PortalGunRecord.PortalGunSide.class);
        ServerPlayer player = context.getSource().getPlayerOrException();
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(player, i -> i instanceof ColoredPortalGun);
        ItemStack portalGun = player.getItemInHand(hand);
        if(portalGun.getItem() instanceof ColoredPortalGun cpg){
            cpg.clearCustomPortalColorForSide(portalGun, side);
            context.getSource().sendSuccess(() -> Component.translatable(CLEAR_CUSTOM_PORTAL_COLOR_KEY,
                            ColoredPortalGun.getSideDisplayName(side),
                            portalGun.getDisplayName()),
                    false);
            return 1;
        } else{
            throw NOT_HOLDING_PORTAL_GUN.create();
        }
    }
}