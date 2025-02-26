package tk.meowmc.portalgun.mixin;

import me.Thelnfamous1.portalgun.CollisionHelperUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.teleportation.PortalCollisionHandler;


@Mixin(value = PortalCollisionHandler.class, remap = false)
public class CollisionHelperMixin {
    @Inject(method = "handleOtherSideMove", at = @At("RETURN"), remap = false, cancellable = true)
    private static void handleGetThisSideMove(Entity entity, Vec3 attemptedMove, Portal collidingPortal, AABB originalBoundingBox, int portalLayer, CallbackInfoReturnable<Vec3> cir){
        cir.setReturnValue(CollisionHelperUtil.handleCollisionWithClipping(entity, attemptedMove, collidingPortal));
    }
}
