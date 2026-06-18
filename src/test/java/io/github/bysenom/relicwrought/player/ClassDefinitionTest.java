package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ClassDefinitionTest {
    static final DefinitionKey WARRIOR = DefinitionKey.parse("warrior", "relicwrought");

    @Test
    void createsValidClass() {
        ClassDefinition def = new ClassDefinition(WARRIOR, "class.relicwrought.warrior",
                "class.relicwrought.warrior.description", "relicwrought:warrior_starter", 0, true, 1);
        assertEquals(WARRIOR, def.id());
        assertEquals("class.relicwrought.warrior", def.displayNameKey());
        assertEquals("relicwrought:warrior_starter", def.starterKitId());
        assertTrue(def.enabled());
    }

    @Test
    void rejectsBlankDisplayNameKey() {
        assertThrows(IllegalArgumentException.class, () -> new ClassDefinition(
                WARRIOR, "  ", "desc", "relicwrought:warrior_starter", 0, true, 1));
    }

    @Test
    void rejectsNullDisplayNameKey() {
        assertThrows(IllegalArgumentException.class, () -> new ClassDefinition(
                WARRIOR, null, "desc", "relicwrought:warrior_starter", 0, true, 1));
    }

    @Test
    void rejectsBlankStarterKitId() {
        assertThrows(IllegalArgumentException.class, () -> new ClassDefinition(
                WARRIOR, "class.relicwrought.warrior", "desc", "  ", 0, true, 1));
    }

    @Test
    void rejectsNullStarterKitId() {
        assertThrows(IllegalArgumentException.class, () -> new ClassDefinition(
                WARRIOR, "class.relicwrought.warrior", "desc", null, 0, true, 1));
    }

    @Test
    void rejectsNonPositiveDataVersion() {
        assertThrows(IllegalArgumentException.class, () -> new ClassDefinition(
                WARRIOR, "class.relicwrought.warrior", "desc", "relicwrought:warrior_starter", 0, true, 0));
        assertThrows(IllegalArgumentException.class, () -> new ClassDefinition(
                WARRIOR, "class.relicwrought.warrior", "desc", "relicwrought:warrior_starter", 0, true, -1));
    }

    @Test
    void disabledClassIsNotEnabled() {
        ClassDefinition def = new ClassDefinition(WARRIOR, "class.relicwrought.warrior",
                "desc", "relicwrought:warrior_starter", 0, false, 1);
        assertFalse(def.enabled());
    }

    @Test
    void sortOrderIsStable() {
        ClassDefinition first = new ClassDefinition(WARRIOR, "name", "desc", "relicwrought:kit", 0, true, 1);
        ClassDefinition second = new ClassDefinition(DefinitionKey.parse("ranger", "relicwrought"),
                "name", "desc", "relicwrought:kit", 1, true, 1);
        assertTrue(first.sortOrder() < second.sortOrder());
    }

    @Test
    void descriptionKeyCanBeEmpty() {
        ClassDefinition def = new ClassDefinition(WARRIOR, "name", "", "relicwrought:kit", 0, true, 1);
        assertEquals("", def.descriptionKey());
    }
}
