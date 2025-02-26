package me.Thelnfamous1.portalgun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface ColoredPortal {

    String CUSTOM_PORTAL_COLOR_TAG = "CustomPortalColor";

    default void writeCustomPortalColor(CompoundTag compoundTag){
        if (this.hasCustomPortalColor()) {
            compoundTag.putInt(CUSTOM_PORTAL_COLOR_TAG, this.getPortalColor());
        }
    }

    default void readCustomPortalColor(CompoundTag compoundTag){
        if (compoundTag.contains(CUSTOM_PORTAL_COLOR_TAG, Tag.TAG_ANY_NUMERIC)) {
            this.setCustomPortalColor(compoundTag.getInt(CUSTOM_PORTAL_COLOR_TAG));
        } else{
            this.clearCustomPortalColor();
        }
    }

    boolean hasCustomPortalColor();

    int getPortalColor();

    void setCustomPortalColor(int portalColor);

    void clearCustomPortalColor();
}