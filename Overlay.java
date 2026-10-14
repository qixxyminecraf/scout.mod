package scout;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;


/** Ritar ESP + den lilla statuspanelen i övre vänstra hörnet. */
public final class Overlay {
    private static boolean keyDown;

    public static void tick(MinecraftClient mc) {
        State.load();
        Esp.tick(mc);
        Fly.tick(mc);

        if (mc.player == null || mc.currentScreen != null) { keyDown = false; return; }
        boolean down = org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(),
                org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (down && !keyDown) mc.setScreen(new MenuScreen());
        keyDown = down;
    }

    public static void render(DrawContext ctx, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;

        Esp.render(ctx, mc, delta);

        if (mc.currentScreen != null) return; // menyn ritar sin egen panel
        int x = 4, y = 4;
        String[] names = { "Spawner ESP", "Chest ESP", "Containers", "Noclip Fly" };
        boolean[] on = { State.spawners, State.chests, State.containers, State.noclip };
        int w = 96, h = 12 + names.length * 10;
        ctx.fill(x, y, x + w, y + h, 0x90000000);
        ctx.drawText(mc.textRenderer, "Scout  [R-Shift]", x + 3, y + 2, 0xFFFFFFFF, false);
        for (int i = 0; i < names.length; i++) {
            ctx.drawText(mc.textRenderer, names[i], x + 3, y + 12 + i * 10, on[i] ? 0xFF55FF55 : 0xFF777777, false);
        }
    }

    private Overlay() { }
}
