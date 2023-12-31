package dev.xhyrom.portalgun.misc;

import dev.xhyrom.portalgun.entities.CustomPortal;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.portal.GeometryPortalShape;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.DQuaternion;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortalPolyfill {
    public static void setRotation(Portal portal, @Nullable DQuaternion quaternion) {
        setRotationTransformationD(portal, quaternion);
    }

    public static void setRotationTransformationD(Portal portal, @Nullable DQuaternion quaternion) {
        if (quaternion == null) {
            portal.rotation = null;
        }
        else {
            portal.rotation = quaternion.fixFloatingPointErrorAccumulation().toMcQuaternion();
        }
        portal.updateCache();
    }

    public static void setOtherSideOrientation(Portal portal, DQuaternion otherSideOrientation) {
        setRotation(portal, PortalManipulationPolyfill.computeDeltaTransformation(
                portal.getOrientationRotation(), otherSideOrientation
        ));
    }
}
