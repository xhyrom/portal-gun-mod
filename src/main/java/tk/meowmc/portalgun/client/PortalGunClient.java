package tk.meowmc.portalgun.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.client.renderer.CustomPortalEntityRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import qouteall.q_misc_util.api.McRemoteProcedureCall;

import static tk.meowmc.portalgun.PortalGunMod.id;

public class PortalGunClient {
    public static final ModelLayerLocation OVERLAY_MODEL_LAYER = new ModelLayerLocation(id("portal_overlay"), "main");

    public void onInitializeClient() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        KeyMapping clearPortals = new KeyMapping("key.portalgun.clearportals", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun");
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.register(clearPortals);
        });

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if(event.phase == TickEvent.Phase.END){
                while (clearPortals.consumeClick()) {
                    McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.onClientClearPortalGun");
                }
            }
        });

        modEventBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(OVERLAY_MODEL_LAYER, PortalOverlayModel::getTexturedModelData);
        });

        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(PortalGunMod.CUSTOM_PORTAL.get(), CustomPortalEntityRenderer::new);
        });

        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (PlayerInteractEvent.LeftClickBlock event) -> {
            ItemStack stack = event.getEntity().getItemInHand(event.getHand());
            if (stack.getItem() == PortalGunMod.PORTAL_GUN.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        });

        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (InputEvent.MouseButton.Pre event) -> {
            if (event.getButton() == 0 && event.getAction() == 1) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) return;

                ItemStack stack = mc.player.getMainHandItem();
                if (stack.getItem() == PortalGunMod.PORTAL_GUN.get()) {
                    ItemCooldowns cooldowns = mc.player.getCooldowns();
                    float cooldownPercent = cooldowns.getCooldownPercent(PortalGunMod.PORTAL_GUN.get(), 0);

                    if (cooldownPercent < 0.001) {
                        McRemoteProcedureCall.tellServerToInvoke(
                                "tk.meowmc.portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
                        );
                    }

                    event.setCanceled(true);
                }
            }
        });
    }
}
