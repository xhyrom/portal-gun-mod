package dev.xhyrom.portalgun;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.xhyrom.portalgun.client.PortalGunClient;
import dev.xhyrom.portalgun.client.renderer.models.PortalOverlayModel;
import dev.xhyrom.portalgun.entities.CustomPortal;
import dev.xhyrom.portalgun.items.ClawItem;
import dev.xhyrom.portalgun.items.PortalGunItem;
import dev.xhyrom.portalgun.misc.PortalManipulationPolyfill;
import dev.xhyrom.portalgun.misc.RemoteCallables;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import qouteall.q_misc_util.my_util.IntBox;
import software.bernie.geckolib3.GeckoLib;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PortalGunMod.MODID)
public class PortalGunMod {
    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";
    public static final String MOD_VERSION = "1.0.0";

    public static final double portalOffset = 0.001;
    public static final double portalOverlayOffset = 0.001;

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            id("main"),
            () -> MOD_VERSION,
            MOD_VERSION::equals,
            MOD_VERSION::equals
    );

    // Registries
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<PortalGunItem> PORTAL_GUN = ITEMS.register("portal_gun", () -> new PortalGunItem(
            new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC)
    ));
    public static final RegistryObject<Item> PORTAL_GUN_BODY = ITEMS.register("portal_gun_body", () -> new Item(
            new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.RARE)
    ));
    public static final RegistryObject<Item> PORTAL_GUN_CLAW = ITEMS.register("portal_gun_claw", () -> new ClawItem(
            new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.RARE)
    ));

    public static final RegistryObject<EntityType<CustomPortal>> CUSTOM_PORTAL = ENTITY_TYPES.register("custom_portal", () -> EntityType.Builder.of(
            CustomPortal::new, MobCategory.MISC
    ).build(id("custom_portal").toString()));


    public static final ResourceLocation PORTAL1_SHOOT = id("portal1_shoot");
    public static final ResourceLocation PORTAL2_SHOOT = id("portal2_shoot");
    public static final ResourceLocation PORTAL_OPEN = id("portal_open");
    public static final ResourceLocation PORTAL_CLOSE = id("portal_close");

    public static RegistryObject<SoundEvent> PORTAL1_SHOOT_EVENT = SOUNDS.register("portal1_shoot", () -> new SoundEvent(PORTAL1_SHOOT));
    public static RegistryObject<SoundEvent> PORTAL2_SHOOT_EVENT = SOUNDS.register("portal2_shoot", () -> new SoundEvent(PORTAL2_SHOOT));
    public static RegistryObject<SoundEvent> PORTAL_OPEN_EVENT = SOUNDS.register("portal_open", () -> new SoundEvent(PORTAL_OPEN));
    public static RegistryObject<SoundEvent> PORTAL_CLOSE_EVENT = SOUNDS.register("portal_close", () -> new SoundEvent(PORTAL_CLOSE));

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static boolean isBlockSolid(Level world, BlockPos p) {
//        return true;
//        return !world.getBlockState(p).isAir();
        return world.getBlockState(p).isSolidRender(world, p);
    }

    public static boolean isAreaClear(Level world, IntBox airBox1) {
        return airBox1.fastStream().allMatch(p -> world.getBlockState(p).isAir());
    }

    public static boolean isWallValid(Level world, IntBox wallBox1) {
        return wallBox1.fastStream().allMatch(p -> isBlockSolid(world, p));
    }

    public static record PortalAwareRaytraceResult(
            Level world,
            BlockHitResult hitResult,
            List<Portal> portalsPassingThrough
    ) {}

    // TODO move this into ImmPtl
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
            Entity entity, double maxDistance
    ) {
        return portalAwareRayTrace(
                entity.level,
                entity.getEyePosition(),
                entity.getViewVector(1),
                maxDistance,
                entity
        );
    }

    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
            Level world,
            Vec3 startingPoint,
            Vec3 direction,
            double maxDistance,
            Entity entity
    ) {
        return portalAwareRayTrace(world, startingPoint, direction, maxDistance, entity, List.of());
    }

    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
            Level world,
            Vec3 startingPoint,
            Vec3 direction,
            double maxDistance,
            Entity entity,
            @NotNull List<Portal> portalsPassingThrough
    ) {
        if (portalsPassingThrough.size() > 5) {
            return null;
        }

        Vec3 endingPoint = startingPoint.add(direction.scale(maxDistance));
        Optional<Pair<Portal, Vec3>> portalHit = PortalManipulationPolyfill.raytracePortals(
                world, startingPoint, endingPoint, true
        );

        ClipContext context = new ClipContext(
                startingPoint,
                endingPoint,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                entity
        );
        BlockHitResult blockHitResult = world.clip(context);

        boolean portalHitFound = portalHit.isPresent();
        boolean blockHitFound = blockHitResult.getType() == HitResult.Type.BLOCK;

        boolean shouldContinueRaytraceInsidePortal = false;
        if (portalHitFound && blockHitFound) {
            double portalDistance = portalHit.get().getSecond().distanceTo(startingPoint);
            double blockDistance = blockHitResult.getLocation().distanceTo(startingPoint);
            if (portalDistance < blockDistance) {
                // continue raytrace from within the portal
                shouldContinueRaytraceInsidePortal = true;
            }
            else {
                return new PortalAwareRaytraceResult(
                        world, blockHitResult, portalsPassingThrough
                );
            }
        }
        else if (!portalHitFound && blockHitFound) {
            return new PortalAwareRaytraceResult(
                    world, blockHitResult, portalsPassingThrough
            );
        }
        else if (portalHitFound && !blockHitFound) {
            // continue raytrace from within the portal
            shouldContinueRaytraceInsidePortal = true;
        }

        if (shouldContinueRaytraceInsidePortal) {
            double portalDistance = portalHit.get().getSecond().distanceTo(startingPoint);
            Portal portal = portalHit.get().getFirst();
            Vec3 newStartingPoint = portal.transformPoint(portalHit.get().getSecond())
                    .add(portal.getContentDirection().scale(0.001));
            Vec3 newDirection = portal.transformLocalVecNonScale(direction);
            double restDistance = maxDistance - portalDistance;
            if (restDistance < 0) {
                return null;
            }
            return portalAwareRayTrace(
                    portal.getDestinationWorld(),
                    newStartingPoint,
                    newDirection,
                    restDistance,
                    entity,
                    Stream.concat(
                            portalsPassingThrough.stream(), Stream.of(portal)
                    ).collect(Collectors.toList())
            );
        }
        else {
            return null;
        }
    }

    public PortalGunMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        SOUNDS.register(modEventBus);

        GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

/*    private void onClientTick(TickEvent.ClientTickEvent event) {


        Minecraft client = Minecraft.getInstance();
        if (client.options.keyAttack.isDown() && !client.player.getCooldowns().isOnCooldown(PortalGunMod.PORTAL_GUN.get())) {
            RemoteCallables.playAnim();
            //McRemoteProcedureCall.tellServerToInvoke("dev.xhyrom.portalgun.misc.RemoteCallables.portal1Place");
        }
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().getMainHandItem().getItem() == PORTAL_GUN.get()) {
            event.setCanceled(true);
        }
    }*/

    /*private void blockBreak(BlockEvent.BreakEvent event) {
        InteractionHand hand = event.getPlayer().getUsedItemHand();
        ItemStack stack = event.getPlayer().getItemInHand(hand);
        Direction direction = event.getPlayer().getDirection();
        if (stack.getItem() == PORTAL_GUN.get()) {
            PORTAL_GUN.get().onAttack(event.getPlayer(), event.getPlayer().getLevel(), hand, event.getPos(), direction);
        }
    }*/

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            new PortalGunClient().onInitializeClient();
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(PortalGunClient.OVERLAY_MODEL_LAYER, PortalOverlayModel::getTexturedModelData);
        }
    }
}
