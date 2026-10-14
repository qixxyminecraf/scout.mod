package scout;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Menyn i övre vänstra hörnet. Klicka för att slå av/på. Esc stänger. */
public class MenuScreen extends Screen {
    private static final int X = 6, W = 150, H = 18, GAP = 3;
    private int nextY = 20;

    public MenuScreen() {
        super(Text.literal("Scout"));
    }

    @Override
    protected void init() {
        nextY = 20;
        toggle("Spawner ESP", () -> State.spawners, v -> State.spawners = v);
        toggle("Chest ESP", () -> State.chests, v -> State.chests = v);
        toggle("Barrels / Shulkers", () -> State.containers, v -> State.containers = v);
        toggle("Text + avstånd", () -> State.labels, v -> State.labels = v);
        toggle("Noclip Fly", () -> State.noclip, v -> State.noclip = v);
        addDrawableChild(new SpeedSlider(X, nextY, W, H));
    }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
            State.save();
        }).dimensions(X, nextY, W, H).build());
        nextY += H + GAP;
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "PÅ" : "AV"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawText(textRenderer, "Scout – Esc för att stänga", X, 8, 0xFFFFFFFF, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class SpeedSlider extends SliderWidget {
        SpeedSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Text.empty(), (State.flySpeed - 1.0) / 9.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Flyghastighet: " + String.format("%.1f", State.flySpeed) + "x"));
        }

        @Override
        protected void applyValue() {
            State.flySpeed = (float) (1.0 + this.value * 9.0);
            State.save();
        }
    }
}
