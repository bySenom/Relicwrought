package io.github.bysenom.relicwrought;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;

public class DumpMethods {

    @Test
    public void dumpFabricHudApi() {
        File loomCache = new File(System.getProperty("user.home"), ".gradle/caches/fabric-loom");
        searchJars(loomCache);
    }

    private void searchJars(File dir) {
        if (!dir.exists()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                searchJars(f);
            } else if (f.getName().endsWith(".jar")) {
                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(f))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.equals("net/minecraft/client/gui/Gui.class")) {
                            System.out.println("Found Gui.class");
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        
        try {
            Class<?> guiClass = Class.forName("net.minecraft.client.gui.Gui");
            System.out.println("=== Gui Fields ===");
            for (java.lang.reflect.Field f : guiClass.getDeclaredFields()) {
                System.out.println(f.getType().getSimpleName() + " " + f.getName());
            }
            System.out.println("=== Gui Methods ===");
            for (java.lang.reflect.Method m : guiClass.getDeclaredMethods()) {
                if (m.getName().toLowerCase().contains("render") || m.getName().toLowerCase().contains("extract")) {
                    System.out.println(m);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            Class<?> hudClass = Class.forName("net.minecraft.client.gui.Hud");
            System.out.println("=== Hud Fields ===");
            for (java.lang.reflect.Field f : hudClass.getDeclaredFields()) {
                System.out.println(f.getType().getSimpleName() + " " + f.getName());
            }
            System.out.println("=== Hud Methods ===");
            for (java.lang.reflect.Method m : hudClass.getDeclaredMethods()) {
                System.out.println(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class<?> cb = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback");
            System.out.println("=== HudRenderCallback Methods ===");
            for (java.lang.reflect.Method m : cb.getDeclaredMethods()) {
                System.out.println(m);
            }
        } catch (Exception e) {
            System.out.println("HudRenderCallback not found.");
        }
    }
}
