package dev.xhyrom.portalgun.misc;

import dev.xhyrom.portalgun.PortalGunMod;
import dev.xhyrom.portalgun.PortalGunRecord;
import dev.xhyrom.portalgun.items.PortalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.util.GeckoLibUtil;

import static dev.xhyrom.portalgun.items.PortalGunItem.*;

public class RemoteCallables {
    public static void onClientLeftClickPortalGun(
            ServerPlayer player
    ) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() == PortalGunMod.PORTAL_GUN.get()) {
            PortalGunMod.PORTAL_GUN.get().onAttack(player, player.level, InteractionHand.MAIN_HAND);
        }
        else {
            PortalGunMod.LOGGER.error("Invalid left click packet");
        }
    }
}