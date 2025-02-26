package tk.meowmc.portalgun.client.renderer;

import tk.meowmc.portalgun.client.renderer.models.ClawModel;
import tk.meowmc.portalgun.items.ClawItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ClawItemRenderer extends GeoItemRenderer<ClawItem> {
    public ClawItemRenderer() {
        super(
                new ClawModel()
        );
    }
}
