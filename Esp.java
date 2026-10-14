package scout;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * ESP utan egna render-pipelines: block-entities hittas i laddade chunks och
 * projiceras till skärmen som 2D-rutor som ritas i HUD:en (syns genom väggar).
 */
public final class Esp {
    private record Target(BlockPos pos, int color, String label) { }

    private static List<Target> targets = List.of();
    private static int cooldown;

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null || !(State.spawners || State.chests || State.containers)) {
            targets = List.of();
            return;
        }
        if (cooldown-- > 0) return;
        cooldown = 10;

        List<Target> out = new ArrayList<>();
        int radius = Math.min(mc.options.getViewDistance().getValue(), 16);
        int pcx = mc.player.getBlockPos().getX() >> 4;
        int pcz = mc.player.getBlockPos().getZ() >> 4;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk c = mc.world.getChunk(pcx + dx, pcz + dz, ChunkStatus.FULL, false);
                if (!(c instanceof WorldChunk wc)) continue;
                for (BlockEntity be : new ArrayList<>(wc.getBlockEntities().values())) {
                    BlockPos pos = be.getPos();
                    if (be instanceof MobSpawnerBlockEntity) {
                        if (State.spawners) out.add(new Target(pos, 0xFFFF3B3B, "Spawner"));
                    } else if (be instanceof EnderChestBlockEntity) {
                        if (State.chests) out.add(new Target(pos, 0xFFB44CFF, "Ender Chest"));
                    } else if (be instanceof TrappedChestBlockEntity) {
                        if (State.chests) out.add(new Target(pos, 0xFFFF6A00, "Trapped Chest"));
                    } else if (be instanceof ChestBlockEntity) {
                        if (State.chests) out.add(new Target(pos, 0xFFFFC21A, "Chest"));
                    } else if (be instanceof BarrelBlockEntity) {
                        if (State.containers) out.add(new Target(pos, 0xFFB5651D, "Barrel"));
                    } else if (be instanceof ShulkerBoxBlockEntity) {
                        if (State.containers) out.add(new Target(pos, 0xFFFF7FD0, "Shulker"));
                    }
                }
            }
        }
        targets = out;
    }

    public static void render(DrawContext ctx, MinecraftClient mc, float delta) {
        if (targets.isEmpty() || mc.player == null) return;
        ClientPlayerEntity p = mc.player;

        Vec3d cam = p.getCameraPosVec(delta);
        float yaw = p.getYaw(delta);
        float pitch = p.getPitch(delta);
        Vec3d fwd = Vec3d.fromPolar(pitch, yaw);
        Vec3d up = Vec3d.fromPolar(pitch - 90f, yaw);
        Vec3d right = fwd.crossProduct(up);

        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue() / 2.0));
        double aspect = (double) w / (double) h;
        TextRenderer tr = mc.textRenderer;

        for (Target t : targets) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            boolean any = false;

            for (int i = 0; i < 8; i++) {
                double cx = t.pos().getX() + (i & 1);
                double cy = t.pos().getY() + ((i >> 1) & 1);
                double cz = t.pos().getZ() + ((i >> 2) & 1);
                Vec3d d = new Vec3d(cx - cam.x, cy - cam.y, cz - cam.z);
                double f = d.dotProduct(fwd);
                if (f < 0.05) continue;
                double sx = w / 2.0 * (1.0 + d.dotProduct(right) / f / (tanHalf * aspect));
                double sy = h / 2.0 * (1.0 - d.dotProduct(up) / f / tanHalf);
                minX = Math.min(minX, sx); maxX = Math.max(maxX, sx);
                minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
                any = true;
            }
            if (!any) continue;
            if (maxX < 0 || minX > w || maxY < 0 || minY > h) continue;

            int x1 = clamp(minX), y1 = clamp(minY), x2 = clamp(maxX), y2 = clamp(maxY);
            if (x2 - x1 < 3) { x2 = x1 + 3; }
            if (y2 - y1 < 3) { y2 = y1 + 3; }

            int c = t.color();
            ctx.fill(x1, y1, x2, y2, (c & 0x00FFFFFF) | 0x30000000); // genomskinlig fyllning
            ctx.fill(x1, y1, x2, y1 + 1, c);                        // kant
            ctx.fill(x1, y2 - 1, x2, y2, c);
            ctx.fill(x1, y1, x1 + 1, y2, c);
            ctx.fill(x2 - 1, y1, x2, y2, c);

            if (State.labels) {
                double dist = Math.sqrt(t.pos().toCenterPos().squaredDistanceTo(cam));
                String text = t.label() + " " + (int) dist + "m";
                int tw = tr.getWidth(text);
                int tx = (x1 + x2) / 2 - tw / 2;
                int ty = y1 - 10;
                ctx.fill(tx - 2, ty - 1, tx + tw + 2, ty + 9, 0x90000000);
                ctx.drawText(tr, text, tx, ty, c, false);
            }
        }
    }

    private static int clamp(double v) {
        return (int) Math.max(-5000, Math.min(5000, v));
    }

    private Esp() { }
}
