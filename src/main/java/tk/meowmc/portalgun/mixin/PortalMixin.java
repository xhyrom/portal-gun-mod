package tk.meowmc.portalgun.mixin;

import me.Thelnfamous1.portalgun.CollisionExcluder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;

@Mixin(value = Portal.class, remap = false)
public abstract class PortalMixin implements CollisionExcluder {
    @Shadow
    public abstract Vec3 getNormal();

    @Shadow public abstract AABB getThinAreaBox();

    @Shadow public abstract Vec3 getPointInPlane(double xInPlane, double yInPlane);

    @Shadow public double width;
    @Shadow public double height;
    @Unique
    @Nullable
    private VoxelShape thisSideCollisionExclusion;

    @Inject(method = "updateCache", at = @At(value = "FIELD", target = "Lqouteall/imm_ptl/core/portal/Portal;thisTickPortalState:Lqouteall/imm_ptl/core/portal/PortalState;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), remap = false)
    private void fixCollisionUpdateCache(CallbackInfo ci){
        thisSideCollisionExclusion = null;
    }

    @Inject(method = "getThinAreaBox", at = @At("HEAD"), remap = false, cancellable = true)
    private void handleGetThinAreaBox(CallbackInfoReturnable<AABB> cir){
        double w = width;
        double h = height;
        cir.setReturnValue(new AABB(
                getPointInPlane(w / 2, h / 2),
                getPointInPlane(-w / 2, -h / 2)
        ).minmax(new AABB(
                getPointInPlane(-w / 2, h / 2),
                getPointInPlane(w / 2, -h / 2)
        )));
    }

    @Unique
    @Override
    public VoxelShape getThisSideCollisionExclusion() {
        if (thisSideCollisionExclusion == null) {
            AABB boundingBox = getThinAreaBox();
            Vec3 reaching = getNormal().scale(-10);
            AABB ignorance = boundingBox.minmax(boundingBox.move(reaching));
            thisSideCollisionExclusion = Shapes.create(ignorance);
        }

        return thisSideCollisionExclusion;
    }
}
