package tk.meowmc.portalgun.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;

@Mixin(value = BlockManipulationClient.class, remap = false)
public class MixinImmPtlBlockManipulationClient {
    @Shadow
    @Final
    private static Minecraft client;

    @Inject(
            method = "myAttackBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onMyAttackBlock(CallbackInfoReturnable<Boolean> cir) {
        InputEvent.InteractionKeyMappingTriggered event = ForgeHooksClient.onClickInput(0, client.options.keyAttack, InteractionHand.MAIN_HAND);
        if(event.isCanceled()) cir.setReturnValue(true);
    }
}