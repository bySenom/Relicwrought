package io.github.bysenom.relicwrought;

import net.minecraft.client.gui.Hud;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HudInspectorTest {
    @Test
    public void printHudMethods() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MINECRAFT FIELDS ===\n");
        java.lang.reflect.Field[] fields = net.minecraft.client.Minecraft.class.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(java.lang.reflect.Field::getName));
        for (java.lang.reflect.Field f : fields) {
            if (f.getName().toLowerCase().contains("screen")) {
                sb.append(f.getType().getSimpleName()).append(" ").append(f.getName()).append("\n");
            }
        }
        sb.append("===================\n");
        Files.writeString(Paths.get("minecraft_fields.txt"), sb.toString());
    }
}
