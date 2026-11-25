package com.skyblockqol.mixin;

import com.skyblockqol.SkyblockQolClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes attack/use input to the holographic panels.
 *
 * <p>When the crosshair rests on a floating panel that is closer than the
 * vanilla target, the click belongs to the UI: attacks and block use are
 * suppressed and the component under the crosshair receives the click instead.
 * Holding the button on a slider keeps {@code continueAttack} suppressed until
 * release so no world interaction leaks through mid-drag.</p>
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void skyblockqol$holoStartAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft self = (Minecraft) (Object) this;
        if (SkyblockQolClient.HOLO.tryClick(self, true)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void skyblockqol$holoStartUse(CallbackInfo ci) {
        Minecraft self = (Minecraft) (Object) this;
        if (SkyblockQolClient.HOLO.tryClick(self, false)) {
            ci.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void skyblockqol$holoContinueAttack(CallbackInfo ci) {
        Minecraft self = (Minecraft) (Object) this;
        if (SkyblockQolClient.HOLO.isPanelTargeted(self)) {
            ci.cancel();
        }
    }
}
