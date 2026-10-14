package scout;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;

/** Flygning (noclip hanteras i mixins). */
public final class Fly {
    private static boolean wasOn;

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null) { wasOn = false; return; }
        PlayerAbilities a = p.getAbilities();
        if (State.noclip) {
            a.allowFlying = true;
            a.flying = true;
            a.setFlySpeed(0.05f * State.flySpeed);
            wasOn = true;
        } else if (wasOn) {
            wasOn = false;
            a.setFlySpeed(0.05f);
            boolean canFly = p.isCreative() || p.isSpectator();
            a.allowFlying = canFly;
            if (!canFly) a.flying = false;
        }
    }

    private Fly() { }
}
