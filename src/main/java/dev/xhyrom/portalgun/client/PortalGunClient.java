package dev.xhyrom.portalgun.client;

import dev.xhyrom.portalgun.PortalGunMod;
import dev.xhyrom.portalgun.client.renderer.CustomPortalEntityRenderer;
import dev.xhyrom.portalgun.mixin.EntityModelLayersAccessor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.network.NetworkDirection;

import static dev.xhyrom.portalgun.PortalGunMod.id;

public class PortalGunClient {
    public static final ModelLayerLocation OVERLAY_MODEL_LAYER = new ModelLayerLocation(id("portal_overlay"), "main");

    public void onInitializeClient() {
        EntityRenderers.register(PortalGunMod.CUSTOM_PORTAL.get(), CustomPortalEntityRenderer::new);

        EntityModelLayersAccessor.getLayers().add(OVERLAY_MODEL_LAYER);

        /*PortalGunMod.CHANNEL.messageBuilder(PortalPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PortalPacket::new)
                .encoder(PortalPacket::toBytes)
                .consumerMainThread(PortalPacket::handle)
                .add();*/
    }
}
