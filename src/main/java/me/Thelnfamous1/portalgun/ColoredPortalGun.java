package me.Thelnfamous1.portalgun;

import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.entities.CustomPortal;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        CustomData data = portalGun.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(CUSTOM_PORTAL_COLORS_TAG)) {
            return false;
        }

        CompoundTag customPortalColors = data.copyTag().getCompound(CUSTOM_PORTAL_COLORS_TAG);
        return customPortalColors.contains(side.name(), Tag.TAG_ANY_NUMERIC);
    }

    default int getPortalColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CustomData data = portalGun.get(DataComponents.CUSTOM_DATA);
        System.out.println(data);
        if (data == null || !data.contains(CUSTOM_PORTAL_COLORS_TAG)) {
            return side.getColorInt();
        }

        CompoundTag customPortalColors = data.copyTag().getCompound(CUSTOM_PORTAL_COLORS_TAG);
        return customPortalColors.contains(side.name(), Tag.TAG_ANY_NUMERIC)
                ? customPortalColors.getInt(side.name())
                : side.getColorInt();
    }

    default void setCustomPortalColorForSide(ItemStack portalGun, int pColor, PortalGunRecord.PortalGunSide side) {
        CustomData data = portalGun.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();

        CompoundTag tt = tag.getCompound(CUSTOM_PORTAL_COLORS_TAG);
        tt.putInt(side.name(), pColor);

        tag.put(CUSTOM_PORTAL_COLORS_TAG, tt);

        CustomData.set(DataComponents.CUSTOM_DATA, portalGun, tag);
    }

    default void clearCustomPortalColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CustomData data = portalGun.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(CUSTOM_PORTAL_COLORS_TAG)) {
            return;
        }

        CompoundTag customPortalColors = data.copyTag().getCompound(CUSTOM_PORTAL_COLORS_TAG);
        if (customPortalColors.contains(side.name())) {
            customPortalColors.remove(side.name());
        }
    }

    default void addCustomPortalColorsTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag){
        if (!stack.has(DataComponents.CUSTOM_DATA) || !Objects.requireNonNull(stack.get(DataComponents.CUSTOM_DATA)).contains(CUSTOM_PORTAL_COLORS_TAG)) {
            return;
        }

        CompoundTag customPortalColors = stack.get(DataComponents.CUSTOM_DATA).copyTag().getCompound(CUSTOM_PORTAL_COLORS_TAG);
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