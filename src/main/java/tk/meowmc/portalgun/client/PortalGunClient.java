package tk.meowmc.portalgun.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.client.renderer.CustomPortalEntityRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import qouteall.q_misc_util.api.McRemoteProcedureCallClient;
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

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (PlayerInteractEvent.LeftClickBlock event) -> {
            ItemStack stack = event.getEntity().getItemInHand(event.getHand());
            if (stack.getItem() == PortalGunMod.PORTAL_GUN.get()) {
                event.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (InputEvent.MouseButton.Pre event) -> {
            if (event.getButton() == 0 && event.getAction() == 1) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) return;

                HitResult hit = mc.hitResult;
                if (hit == null || hit.getType() == HitResult.Type.MISS) return;

                ItemStack stack = mc.player.getMainHandItem();
                if (stack.getItem() == PortalGunMod.PORTAL_GUN.get()) {
                    ItemCooldowns cooldowns = mc.player.getCooldowns();
                    float cooldownPercent = cooldowns.getCooldownPercent(PortalGunMod.PORTAL_GUN.get(), 0);

                    if (cooldownPercent < 0.001) {
                        McRemoteProcedureCallClient.tellServerToInvoke(
                                "tk.meowmc.portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
                        );
                    }

                    event.setCanceled(true);
                }
            }
        });
    }
}
