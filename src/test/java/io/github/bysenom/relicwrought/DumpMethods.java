package io.github.bysenom.relicwrought;

import org.junit.jupiter.api.Test;

public class DumpMethods {

    @Test
    public void dumpFabricHudApi() {
        String[] classNames = {
            "net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry",
            "net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements",
            "net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement",
            "net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents",
            "net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext",
            "net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents",
            "net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext",
            "net.minecraft.client.renderer.entity.EntityRenderer",
            "net.minecraft.client.renderer.entity.state.EntityRenderState",
            "net.minecraft.client.gui.GuiGraphicsExtractor",
            "net.minecraft.client.gui.Font",
            "net.minecraft.client.Camera",
            "net.minecraft.client.renderer.entity.EntityRenderDispatcher"
        };
        for (String cn : classNames) {
            try {
                Class<?> c = Class.forName(cn);
                System.out.println("=== " + cn + " ===");
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    System.out.println("  FIELD: " + f.getType().getSimpleName() + " " + f.getName());
                }
                for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                    System.out.println("  METHOD: " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + paramTypes(m) + ")");
                }
            } catch (Exception e) {
                System.out.println("NOT FOUND: " + cn);
            }
        }
    }
    
    private String paramTypes(java.lang.reflect.Method m) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> p : m.getParameterTypes()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.getSimpleName());
        }
        return sb.toString();
    }
}
