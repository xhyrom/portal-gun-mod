package dev.xhyrom.portalgun.items;

import dev.xhyrom.portalgun.PortalGunMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class PortalGunCreativeModeTabs {
    public static CreativeModeTab TAB = new CreativeModeTab("portalgun") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(PortalGunMod.PORTAL_GUN.get());
        }
    };
}
