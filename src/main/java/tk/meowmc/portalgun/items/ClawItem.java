package tk.meowmc.portalgun.items;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import tk.meowmc.portalgun.client.renderer.ClawItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class ClawItem extends Item implements GeoAnimatable {
    public AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    public ClawItem(Properties settings) {
        super(settings);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final ClawItemRenderer renderer = new ClawItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    private <P extends Item & GeoAnimatable> PlayState predicate(AnimationState<P> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        AnimationController<ClawItem> controller = new AnimationController<>(
                this,
                "clawController",
                1,
                this::predicate
        );

        animationData.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }
}