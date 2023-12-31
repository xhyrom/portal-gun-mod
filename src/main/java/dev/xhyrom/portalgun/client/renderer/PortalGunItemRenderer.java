package dev.xhyrom.portalgun.client.renderer;

import dev.xhyrom.portalgun.client.renderer.models.PortalGunModel;
import dev.xhyrom.portalgun.items.PortalGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class PortalGunItemRenderer extends GeoItemRenderer<PortalGunItem> {
    public PortalGunItemRenderer() {
        super(new PortalGunModel());
    }
}
