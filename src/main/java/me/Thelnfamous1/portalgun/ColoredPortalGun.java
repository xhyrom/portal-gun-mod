package me.Thelnfamous1.portalgun;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.entities.CustomPortal;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public interface ColoredPortalGun {

    String CUSTOM_PORTAL_COLORS_TAG = "CustomPortalColors";
    String CUSTOM_PORTAL_COLOR_KEY = Util.makeDescriptionId("item", PortalGunMod.id("portal_gun/custom_portal_color"));

    static void colorPortal(CustomPortal portal, ItemStack portalGun, PortalGunRecord.PortalGunSide side){
        if(portalGun.getItem() instanceof ColoredPortalGun cpg && cpg.hasCustomPortalColorForSide(portalGun, side)){
            portal.setCustomPortalColor(cpg.getPortalColorForSide(portalGun, side));
        } else{
            portal.clearCustomPortalColor();
        }
    }

    static Component getSideDisplayName(PortalGunRecord.PortalGunSide side){
        MutableComponent sideDisplayName = Component.translatable(String.format("%s.%s", "side.portalgun", side.name()));
        sideDisplayName.setStyle(sideDisplayName.getStyle().withColor(side.getColorInt()));
        return sideDisplayName;
    }

    static Component getColorName(int color) {
        MutableComponent colorName = Component.literal(String.format(Locale.ROOT, "#%06X", color));
        colorName.setStyle(colorName.getStyle().withColor(color));
        return colorName;
    }

    default boolean hasCustomPortalColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CompoundTag customPortalColors = portalGun.getTagElement(CUSTOM_PORTAL_COLORS_TAG);
        return customPortalColors != null && customPortalColors.contains(side.name(), Tag.TAG_ANY_NUMERIC);
    }

    default int getPortalColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CompoundTag customPortalColors = portalGun.getTagElement(CUSTOM_PORTAL_COLORS_TAG);
        return customPortalColors != null && customPortalColors.contains(side.name(), Tag.TAG_ANY_NUMERIC) ? customPortalColors.getInt(side.name()) : side.getColorInt();
    }

    default void setCustomPortalColorForSide(ItemStack portalGun, int pColor, PortalGunRecord.PortalGunSide side) {
        portalGun.getOrCreateTagElement(CUSTOM_PORTAL_COLORS_TAG).putInt(side.name(), pColor);
    }

    default void clearCustomPortalColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CompoundTag customPortalColors = portalGun.getTagElement(CUSTOM_PORTAL_COLORS_TAG);
        if (customPortalColors != null && customPortalColors.contains(side.name())) {
            customPortalColors.remove(side.name());
        }
    }

    default void addCustomPortalColorsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context){
        if (stack.hasTag() && stack.getTag().contains(CUSTOM_PORTAL_COLORS_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag customPortalColors = stack.getTag().getCompound(CUSTOM_PORTAL_COLORS_TAG);
            for(PortalGunRecord.PortalGunSide side : PortalGunRecord.PortalGunSide.values()){
                if (customPortalColors.contains(side.name(), Tag.TAG_ANY_NUMERIC)) {
                    tooltip.add(Component.translatable(
                            CUSTOM_PORTAL_COLOR_KEY,
                            getSideDisplayName(side),
                            getColorName(customPortalColors.getInt(side.name()))));
                }
            }
        }
    }
}