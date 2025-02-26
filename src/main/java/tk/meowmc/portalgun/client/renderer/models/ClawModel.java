package tk.meowmc.portalgun.client.renderer.models;

import tk.meowmc.portalgun.items.ClawItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static tk.meowmc.portalgun.PortalGunMod.id;

public class ClawModel extends GeoModel<ClawItem> {
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
