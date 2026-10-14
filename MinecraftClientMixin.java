package scout.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import scout.Overlay;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void scout$tick(CallbackInfo ci) {
        Overlay.tick((MinecraftClient) (Object) this);
    }
}
