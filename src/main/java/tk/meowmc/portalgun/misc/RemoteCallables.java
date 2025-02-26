package tk.meowmc.portalgun.misc;

import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class RemoteCallables {
    public static void onClientLeftClickPortalGun(
            ServerPlayer player
    ) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() == PortalGunMod.PORTAL_GUN.get()) {
            PortalGunMod.PORTAL_GUN.get().onAttack(player, player.level(), InteractionHand.MAIN_HAND);
        }

        else {
            PortalGunMod.LOGGER.error("Invalid left click packet");
        }
    }

    public static void onClientClearPortalGun(
            ServerPlayer player
    ) {
        PortalGunRecord record = PortalGunRecord.get();
        PortalGunRecord.PortalDescriptor orangeDescriptor =
                new PortalGunRecord.PortalDescriptor(
                        player.getUUID(),
                        PortalGunRecord.PortalGunKind._2x1,
                        PortalGunRecord.PortalGunSide.orange
                );
        PortalGunRecord.PortalDescriptor blueDescriptor =
                new PortalGunRecord.PortalDescriptor(
                        player.getUUID(),
                        PortalGunRecord.PortalGunKind._2x1,
                        PortalGunRecord.PortalGunSide.blue
                );
        record.data.remove(orangeDescriptor);
        record.data.remove(blueDescriptor);
        record.setDirty();
    }
}