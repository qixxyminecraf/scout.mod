package scout.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import scout.State;

/** Låter klienten behandla spelaren som spectator (noclip) medan Noclip Fly är på. */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true, require = 0)
    private void scout$spectator(CallbackInfoReturnable<Boolean> cir) {
        if (State.noclip) cir.setReturnValue(true);
    }
}
