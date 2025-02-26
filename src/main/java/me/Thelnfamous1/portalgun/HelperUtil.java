package me.Thelnfamous1.portalgun;

import net.minecraft.world.phys.AABB;

/**
 * @author iPortalTeam
 */
public class HelperUtil {

    public static boolean boxContains(AABB outer, AABB inner) {
        return outer.contains(inner.minX, inner.minY, inner.minZ) &&
                outer.contains(inner.maxX, inner.maxY, inner.maxZ);
    }
}