package dev.xhyrom.portalgun.client.renderer;

import dev.xhyrom.portalgun.client.renderer.models.ClawModel;
import dev.xhyrom.portalgun.items.ClawItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class ClawItemRenderer extends GeoItemRenderer<ClawItem> {
    public ClawItemRenderer() {
        super(
                new ClawModel()
                //new DefaultedItemGeoModel<ClawItem>(new ResourceLocation("portalgun", "portalgun_claw"))
                //        .withAltTexture(new ResourceLocation("portalgun", "portalgun"))
        );
    }
}
