package dev.xhyrom.portalgun.items;

import dev.xhyrom.portalgun.PortalGunMod;
import dev.xhyrom.portalgun.PortalGunRecord;
import dev.xhyrom.portalgun.client.renderer.PortalGunItemRenderer;
import dev.xhyrom.portalgun.entities.CustomPortal;
import dev.xhyrom.portalgun.misc.AARotation;
import dev.xhyrom.portalgun.misc.IntBoxPolyfill;
import dev.xhyrom.portalgun.misc.PortalManipulationPolyfill;
import dev.xhyrom.portalgun.misc.PortalPolyfill;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.chunk_loading.ChunkLoader;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.IntBox;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Consumer;

public class PortalGunItem extends Item implements IAnimatable {
    private static Logger LOGGER = LogManager.getLogger();
    public static final int COOLDOWN_TICKS = 4;

    public static String controllerName = "portalgunController";

    public final AnimationFactory animationFactory = new AnimationFactory(this);

    public PortalGunItem(Properties settings) {
        super(settings);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final PortalGunItemRenderer renderer = new PortalGunItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController controller = new AnimationController(this, controllerName, 1, this::predicate);
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return false;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide()) {
            return InteractionResultHolder.fail(itemStack);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        HitResult hit = player.pick(100.0D, 1.0F, false);

        if (!(hit instanceof BlockHitResult blockHit)) {
            return InteractionResultHolder.fail(itemStack);
        }

        boolean success = interact(
                (ServerPlayer) player, (ServerLevel) world, hand, blockHit,
                PortalGunRecord.PortalGunSide.orange
        );

        player.awardStat(Stats.ITEM_USED.get(this));

        return success ? InteractionResultHolder.success(itemStack) : InteractionResultHolder.pass(itemStack);
    }

    public InteractionResult onAttack(
            Player player, Level world, InteractionHand hand
    ) {
        if (world.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        HitResult hit = player.pick(100.0D, 1.0F, false);

        if (!(hit instanceof BlockHitResult blockHit)) {
            return InteractionResult.FAIL;
        }

        boolean success = interact(
                (ServerPlayer) player, (ServerLevel) world, hand, blockHit,
                PortalGunRecord.PortalGunSide.blue
        );

        player.awardStat(Stats.ITEM_USED.get(this));

        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    // return whether successful
    public boolean interact(
            ServerPlayer player,
            ServerLevel world,
            InteractionHand hand,
            BlockHitResult blockHit,
            PortalGunRecord.PortalGunSide side
    ) {
        BlockPos blockPos = blockHit.getBlockPos().relative(blockHit.getDirection());

        Direction wallFacing = blockHit.getDirection();

        Direction upDir = getUpDirection(player, wallFacing);

        Direction rightDir = AARotation.dirCrossProduct(upDir, wallFacing);

        BlockPos regionSize = new BlockPos(
                rightDir.getNormal()
                        .offset(upDir.getNormal().multiply(2))
                        .offset(wallFacing.getNormal())
        );

        IntBox areaForPlacing = IntBoxPolyfill.getBoxByPosAndSignedSize(BlockPos.ZERO, regionSize).stream().map(
                offset -> {
                    BlockPos testingBasePos = blockPos.subtract(offset);
                    IntBox testingArea = IntBoxPolyfill.getBoxByPosAndSignedSize(testingBasePos, regionSize);
                    boolean boxIsAllAir = testingArea.stream().allMatch(p -> world.getBlockState(p).isAir());
                    boolean wallIsSolid = testingArea.stream().map(p -> p.relative(wallFacing.getOpposite()))
                            .allMatch(p -> PortalGunMod.isBlockSolid(world, p));
                    if (boxIsAllAir && wallIsSolid) {
                        return testingArea;
                    }
                    else {
                        return null;
                    }
                }
        ).filter(Objects::nonNull).findFirst().orElse(null);

        if (areaForPlacing == null) {
            return false;
        }

        /*triggerAnim(
                player,
                GeoItem.getOrAssignId(player.getItemInHand(hand), ((ServerLevel) world)),
                "portalGunController", "shoot_anim"
        );*/

        PortalGunRecord record = PortalGunRecord.get();

        PortalGunRecord.PortalGunKind kind = PortalGunRecord.PortalGunKind._2x1;

        PortalGunRecord.PortalDescriptor descriptor =
                new PortalGunRecord.PortalDescriptor(player.getUUID(), kind, side);

        PortalGunRecord.PortalDescriptor otherSideDescriptor = descriptor.getTheOtherSide();

        PortalGunRecord.PortalInfo thisSideInfo = record.data.get(descriptor);
        PortalGunRecord.PortalInfo otherSideInfo = record.data.get(otherSideDescriptor);

        Vec3 wallFacingVec = Vec3.atLowerCornerOf(wallFacing.getNormal());
        Vec3 newPortalOrigin = Helper
                .getBoxSurface(areaForPlacing.toRealNumberBox(), wallFacing.getOpposite())
                .getCenter()
                .add(wallFacingVec.scale(PortalGunMod.portalOffset));

        CustomPortal portal = null;
        boolean isExistingPortal = false;

        if (thisSideInfo != null) {
            Entity entity = McHelper.getServerWorld(thisSideInfo.portalDim()).getEntity(thisSideInfo.portalId());
            if (entity instanceof CustomPortal customPortal) {
                portal = customPortal;
                isExistingPortal = true;
            }
        }

        if (portal == null) {
            portal = PortalGunMod.CUSTOM_PORTAL.get().create(world);
            Validate.notNull(portal);
        }

        portal.setOriginPos(newPortalOrigin);
        portal.setOrientationAndSize(
                Vec3.atLowerCornerOf(rightDir.getNormal()),
                Vec3.atLowerCornerOf(upDir.getNormal()),
                kind.getWidth(),
                kind.getHeight()
        );
        portal.descriptor = descriptor;
        portal.wallBox = areaForPlacing.getMoved(wallFacing.getOpposite().getNormal());
        portal.airBox = areaForPlacing;
        portal.thisSideUpdateCounter = thisSideInfo == null ? 0 : thisSideInfo.updateCounter();
        portal.otherSideUpdateCounter = otherSideInfo == null ? 0 : otherSideInfo.updateCounter();
        PortalManipulationPolyfill.makePortalRound(portal, 100); // MAKE POLYFILL
        portal.animation.defaultAnimation.durationTicks = 0; // disable default animation

        if (otherSideInfo == null) {
            // it's unpaired, invisible and not teleportable
            portal.setDestinationDimension(world.dimension());
            portal.setDestination(newPortalOrigin.add(0, 10, 0));
            portal.setIsVisible(false);
            portal.teleportable = false;
        }
        else {
            // it's linked portal
            portal.setDestinationDimension(otherSideInfo.portalDim());
            portal.setDestination(otherSideInfo.portalPos());
            PortalPolyfill.setOtherSideOrientation(portal, otherSideInfo.portalOrientation());             //portal.setOrientationRotation(otherSideInfo.portalOrientation());
            portal.setIsVisible(true);
            portal.teleportable = true;
        }

        portal.thisSideUpdateCounter += 1;
        thisSideInfo = new PortalGunRecord.PortalInfo(
                portal.getUUID(),
                world.dimension(),
                newPortalOrigin,
                portal.getOrientationRotation(),
                portal.thisSideUpdateCounter
        );
        record.data.put(descriptor, thisSideInfo);
        record.setDirty();

        if (!isExistingPortal) {
            McHelper.spawnServerEntity(portal);
        }
        else {
            portal.reloadAndSyncToClient();
        }

        return true;
    }

    private static Direction getUpDirection(Player user, Direction blockFacingDir) {
        return switch (blockFacingDir) {
            case DOWN, UP -> getHorizontalDirection(user);
            case NORTH, WEST, SOUTH, EAST -> Direction.UP;
        };
    }

    private static Direction getHorizontalDirection(Player user) {
        Vec3 viewVector = user.getViewVector(1);
        double x = viewVector.x;
        double z = viewVector.z;

        Direction[] horizontalDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        return Arrays.stream(horizontalDirections)
                .min(Comparator.comparingDouble(
                        dir -> Math.abs(dir.getStepX() * x + dir.getStepZ() * z)
                ))
                .orElse(Direction.NORTH);
    }
}