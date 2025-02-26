package tk.meowmc.portalgun.client.renderer;

import tk.meowmc.portalgun.client.renderer.models.PortalGunModel;
import tk.meowmc.portalgun.items.PortalGunItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PortalGunItemRenderer extends GeoItemRenderer<PortalGunItem> {
    public PortalGunItemRenderer() {
        super(new PortalGunModel());
    }
}
