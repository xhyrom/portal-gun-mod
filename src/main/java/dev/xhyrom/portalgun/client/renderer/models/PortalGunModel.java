package dev.xhyrom.portalgun.client.renderer.models;

import dev.xhyrom.portalgun.items.ClawItem;
import dev.xhyrom.portalgun.items.PortalGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static dev.xhyrom.portalgun.PortalGunMod.id;

public class PortalGunModel extends AnimatedGeoModel<PortalGunItem> {
    @Override
    public ResourceLocation getModelResource(PortalGunItem object) {
        /*if (config.enabled.enableOldPortalGunModel)
            return id("geo/portalgun_og.geo.json");
        else*/
        return id("geo/item/portalgun.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PortalGunItem object) {
        /*if (config.enabled.enableOldPortalGunModel)
            return id("textures/item/portal_gun_og.png");
        else*/
        return id("textures/item/portal_gun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PortalGunItem animatable) {
        /*if (config.enabled.enableOldPortalGunModel)
            return id("animations/portalgun_og.animation.json");
        else*/
        return id("animations/item/portalgun.animation.json");
    }
}
