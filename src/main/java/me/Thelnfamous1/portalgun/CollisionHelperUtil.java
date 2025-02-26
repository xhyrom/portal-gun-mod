package me.Thelnfamous1.portalgun;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.teleportation.CollisionHelper;

import java.util.List;
import java.util.function.Function;

/**
 * @author iPortalTeam
 */
public class CollisionHelperUtil {
    public static Vec3 handleCollisionWithClipping(Entity entity, Vec3 attemptedMove, Portal collidingPortal) {
        Function<VoxelShape, VoxelShape> filter = shape -> processThisSideCollisionShape(shape, collidingPortal);
        AABB boundingBox = entity.getBoundingBox();
        List<VoxelShape> entityCollisions = entity.level().getEntityCollisions(entity, boundingBox.expandTowards(attemptedMove));
        Vec3 collidedMovement = attemptedMove.lengthSqr() == 0.0 ? attemptedMove : CollisionHelper.collideBoundingBox(entity, attemptedMove, boundingBox, entity.level(), entityCollisions, filter);
        boolean moveX = attemptedMove.x != collidedMovement.x;
        boolean moveY = attemptedMove.y != collidedMovement.y;
        boolean moveZ = attemptedMove.z != collidedMovement.z;
        boolean touchGround = entity.onGround() || moveY && attemptedMove.y < 0.0;
        if (entity.maxUpStep() > 0.0F && touchGround && (moveX || moveZ)) {
            Vec3 stepping = CollisionHelper.collideBoundingBox(entity, new Vec3(attemptedMove.x, (double)entity.maxUpStep(), attemptedMove.z), boundingBox, entity.level(), entityCollisions, filter);
            Vec3 verticalStep = CollisionHelper.collideBoundingBox(entity, new Vec3(0.0, (double)entity.maxUpStep(), 0.0), boundingBox.expandTowards(attemptedMove.x, 0.0, attemptedMove.z), entity.level(), entityCollisions, filter);
            Vec3 moveAfterStepping;
            if (verticalStep.y < (double)entity.maxUpStep()) {
                moveAfterStepping = CollisionHelper.collideBoundingBox(entity, new Vec3(attemptedMove.x, 0.0, attemptedMove.z), boundingBox.move(verticalStep), entity.level(), entityCollisions, filter).add(verticalStep);
                if (moveAfterStepping.horizontalDistanceSqr() > stepping.horizontalDistanceSqr()) {
                    stepping = moveAfterStepping;
                }
            }

            if (stepping.horizontalDistanceSqr() > collidedMovement.horizontalDistanceSqr()) {
                moveAfterStepping = CollisionHelper.collideBoundingBox(entity, new Vec3(0.0, -stepping.y + attemptedMove.y, 0.0), boundingBox.move(stepping), entity.level(), entityCollisions, filter);
                return stepping.add(moveAfterStepping);
            }
        }

        return collidedMovement;
    }

    @Nullable
    public static VoxelShape processThisSideCollisionShape(
            VoxelShape shape, Portal portal
    ) {
        VoxelShape exclusion = ((CollisionExcluder)portal).getThisSideCollisionExclusion();

        if (HelperUtil.boxContains(exclusion.bounds(), shape.bounds())) {
            return null;
        }

        VoxelShape result = Shapes.joinUnoptimized(
                shape,
                exclusion,
                BooleanOp.ONLY_FIRST
        );

        return result;
    }

}