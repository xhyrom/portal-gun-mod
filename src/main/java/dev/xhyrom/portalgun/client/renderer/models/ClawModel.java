package dev.xhyrom.portalgun.client.renderer.models;

import dev.xhyrom.portalgun.items.ClawItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static dev.xhyrom.portalgun.PortalGunMod.id;

public class ClawModel extends AnimatedGeoModel<ClawItem> {
    @Override
    public ResourceLocation getModelResource(ClawItem object) {
        return id("geo/item/portalgun_claw.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClawItem object) {
        return id("textures/item/portalgun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ClawItem animatable) {
        return null;
    }
}
