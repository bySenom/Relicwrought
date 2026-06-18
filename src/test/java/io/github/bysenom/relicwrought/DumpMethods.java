package io.github.bysenom.relicwrought;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class DumpMethods {
    @Test
    public void dumpMethods() {
        System.out.println("Methods of Entity:");
        for (Method m : Entity.class.getDeclaredMethods()) {
            if (m.getParameterCount() == 2 && m.getParameterTypes()[0] == DamageSource.class && m.getParameterTypes()[1] == float.class) {
                System.out.println("  " + m.getReturnType().getName() + " " + m.getName() + "(DamageSource, float)");
            }
        }
        System.out.println("Methods of LivingEntity:");
        for (Method m : LivingEntity.class.getDeclaredMethods()) {
            if (m.getParameterCount() == 2 && m.getParameterTypes()[0] == DamageSource.class && m.getParameterTypes()[1] == float.class) {
                System.out.println("  " + m.getReturnType().getName() + " " + m.getName() + "(DamageSource, float)");
            }
        }
    }
}
