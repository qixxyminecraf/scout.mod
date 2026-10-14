package scout.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import scout.State;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public boolean noClip;

    @Inject(method = "move", at = @At("HEAD"))
    private void scout$noclip(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (State.noclip && (Object) this == MinecraftClient.getInstance().player) {
            this.noClip = true;
        }
    }
}
