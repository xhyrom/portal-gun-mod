package dev.xhyrom.portalgun.client.renderer;

import dev.xhyrom.portalgun.client.renderer.models.ClawModel;
import dev.xhyrom.portalgun.items.ClawItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ClawItemRenderer extends GeoItemRenderer<ClawItem> {
    public ClawItemRenderer() {
        super(
                new ClawModel()
                //new DefaultedItemGeoModel<ClawItem>(new ResourceLocation("portalgun", "portalgun_claw"))
                //        .withAltTexture(new ResourceLocation("portalgun", "portalgun"))
        );
    }
}
