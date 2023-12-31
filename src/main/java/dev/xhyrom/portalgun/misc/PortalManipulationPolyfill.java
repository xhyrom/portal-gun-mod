package dev.xhyrom.portalgun.misc;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.portal.GeometryPortalShape;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;
import qouteall.q_misc_util.my_util.DQuaternion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PortalManipulationPolyfill {
    public static void makePortalRound(Portal portal, int triangleNum) {
        GeometryPortalShape shape = new GeometryPortalShape();
        double twoPi = Math.PI * 2;
        shape.triangles = IntStream.range(0, triangleNum)
                .mapToObj(i -> new GeometryPortalShape.TriangleInPlane(
                        0, 0,
                        portal.width * 0.5 * Math.cos(twoPi * ((double) i) / triangleNum),
                        portal.height * 0.5 * Math.sin(twoPi * ((double) i) / triangleNum),
                        portal.width * 0.5 * Math.cos(twoPi * ((double) i + 1) / triangleNum),
                        portal.height * 0.5 * Math.sin(twoPi * ((double) i + 1) / triangleNum)
                )).collect(Collectors.toList());
        portal.specialShape = shape;
        portal.cullableXStart = 0;
        portal.cullableXEnd = 0;
        portal.cullableYStart = 0;
        portal.cullableYEnd = 0;
    }

    public static Optional<Pair<Portal, Vec3>> raytracePortals(
            Level world, Vec3 from, Vec3 to, boolean includeGlobalPortal
    ) {
        Stream<Portal> portalStream = McHelper.getEntitiesNearby(
                world,
                from,
                Portal.class,
                from.distanceTo(to)
        ).stream();
        if (includeGlobalPortal) {
            List<Portal> globalPortals = GlobalPortalStorage.getGlobalPortals(world);
            portalStream = Streams.concat(
                    portalStream,
                    globalPortals.stream()
            );
        }
        return portalStream.map(
                portal -> new Pair<Portal, Vec3>(
                        portal, portal.rayTrace(from, to)
                )
        ).filter(
                portalAndHitPos -> portalAndHitPos.getSecond() != null
        ).min(
                Comparator.comparingDouble(
                        portalAndHitPos -> portalAndHitPos.getSecond().distanceToSqr(from)
                )
        );
    }

    public static final DQuaternion flipAxisW = DQuaternion.rotationByDegrees(
            new Vec3(0, 1, 0), 180
    ).fixFloatingPointErrorAccumulation();
    public static DQuaternion computeDeltaTransformation(
            DQuaternion thisSideOrientation, DQuaternion otherSideOrientation
    ) {
        // otherSideOrientation * axis = rotation * thisSideOrientation * flipAxisW * axis
        // otherSideOrientation = rotation * thisSideOrientation * flipAxisW
        // rotation = otherSideOrientation * flipAxisW^-1 * thisSideOrientation^-1
        return otherSideOrientation
                .hamiltonProduct(flipAxisW)
                .hamiltonProduct(thisSideOrientation.getConjugated());
    }
}
