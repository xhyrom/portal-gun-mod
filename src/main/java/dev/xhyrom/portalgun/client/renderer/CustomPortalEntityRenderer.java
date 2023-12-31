package dev.xhyrom.portalgun.client.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import dev.xhyrom.portalgun.PortalGunMod;
import dev.xhyrom.portalgun.client.PortalGunClient;
import dev.xhyrom.portalgun.client.renderer.models.PortalOverlayModel;
import dev.xhyrom.portalgun.entities.CustomPortal;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.CHelper;
import qouteall.q_misc_util.my_util.DQuaternion;

import static dev.xhyrom.portalgun.PortalGunMod.id;
import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

public class CustomPortalEntityRenderer extends EntityRenderer<CustomPortal> {
    private final PortalOverlayModel model;

    public CustomPortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new PortalOverlayModel(context.bakeLayer(PortalGunClient.OVERLAY_MODEL_LAYER));
    }

    @Override
    public void render(CustomPortal entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pushPose();
        matrices.mulPose(entity.getOrientationRotation().toMcQuaternion());

        int color = entity.descriptor.side().getColorInt();

        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;

        this.model.renderToBuffer(matrices, vertexConsumers.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity))), light, NO_OVERLAY, r, g, b, 1F);
        matrices.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(CustomPortal entity) {
        return entity.isVisible() ? OVERLAY_FRAME : OVERLAY_FILLED;
    }

    private static final ResourceLocation OVERLAY_FRAME = id("textures/entity/overlay_frame.png");
    private static final ResourceLocation OVERLAY_FILLED = id("textures/entity/overlay_filled.png");

}
