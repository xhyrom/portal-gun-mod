package tk.meowmc.portalgun.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import qouteall.q_misc_util.api.McRemoteProcedureCallClient;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.client.renderer.CustomPortalEntityRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static tk.meowmc.portalgun.PortalGunMod.id;

@Mod(value = PortalGunMod.MODID, dist = Dist.CLIENT)
public class PortalGunClient {
    public static final ModelLayerLocation OVERLAY_MODEL_LAYER = new ModelLayerLocation(id("portal_overlay"), "main");

    public PortalGunClient(IEventBus modEventBus, ModContainer modContainer) {
        KeyMapping clearPortals = new KeyMapping("key.portalgun.clearportals", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun");
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.register(clearPortals);
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            while (clearPortals.consumeClick()) {
                McRemoteProcedureCallClient.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.onClientClearPortalGun");
            }
        });

        modEventBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(OVERLAY_MODEL_LAYER, PortalOverlayModel::getTexturedModelData);
        });

        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(PortalGunMod.CUSTOM_PORTAL.get(), CustomPortalEntityRenderer::new);
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (InputEvent.InteractionKeyMappingTriggered event) -> {
            if(event.isAttack() && event.getKeyMapping() == Minecraft.getInstance().options.keyAttack && event.getHand() == InteractionHand.MAIN_HAND){
                if (Minecraft.getInstance().hitResult == null || Minecraft.getInstance().player == null) {
                    return;
                }

                ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
                if (mainHandItem.getItem() == PortalGunMod.PORTAL_GUN.get()) {
                    McRemoteProcedureCallClient.tellServerToInvoke(
                            "tk.meowmc.portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
                    );
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        });
    }
}
