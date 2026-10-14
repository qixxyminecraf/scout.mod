package scout;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Alla inställningar. Sparas i config/scout.properties. */
public final class State {
    public static boolean spawners = true;
    public static boolean chests = true;
    public static boolean containers = false;
    public static boolean labels = true;
    public static boolean noclip = false;
    public static float flySpeed = 3.0f; // 1..10

    private static boolean loaded;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("scout.properties");
    }

    public static void load() {
        if (loaded) return;
        loaded = true;
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(f)) { p.load(in); }
            spawners = Boolean.parseBoolean(p.getProperty("spawners", "true"));
            chests = Boolean.parseBoolean(p.getProperty("chests", "true"));
            containers = Boolean.parseBoolean(p.getProperty("containers", "false"));
            labels = Boolean.parseBoolean(p.getProperty("labels", "true"));
            float s = Float.parseFloat(p.getProperty("flySpeed", "3"));
            flySpeed = Math.max(1f, Math.min(10f, s));
            // noclip startar medvetet alltid av
        } catch (Exception ignored) { }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("spawners", String.valueOf(spawners));
            p.setProperty("chests", String.valueOf(chests));
            p.setProperty("containers", String.valueOf(containers));
            p.setProperty("labels", String.valueOf(labels));
            p.setProperty("flySpeed", String.valueOf(flySpeed));
            try (OutputStream out = Files.newOutputStream(file())) { p.store(out, "Scout"); }
        } catch (Exception ignored) { }
    }

    private State() { }
}
