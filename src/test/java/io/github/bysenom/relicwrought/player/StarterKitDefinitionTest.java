package io.github.bysenom.relicwrought.player;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class StarterKitDefinitionTest {
    static final DefinitionKey KIT_ID = DefinitionKey.parse("warrior_starter", "relicwrought");

    @Test
    void createsValidKit() {
        StarterKitDefinition kit = new StarterKitDefinition(KIT_ID, List.of(
                new StarterKitEntry("relicwrought:starter_pickaxe", 1, 0, 1, false, ""),
                new StarterKitEntry("relicwrought:starter_axe", 1, 0, 1, false, ""),
                new StarterKitEntry("relicwrought:starter_shovel", 1, 0, 1, false, "")
        ), 1);
        assertEquals(KIT_ID, kit.id());
        assertEquals(3, kit.entries().size());
        assertEquals(1, kit.dataVersion());
    }

    @Test
    void rejectsEmptyEntriesList() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitDefinition(KIT_ID, List.of(), 1));
    }

    @Test
    void rejectsNonPositiveDataVersion() {
        assertThrows(IllegalArgumentException.class, () -> new StarterKitDefinition(KIT_ID, List.of(
                new StarterKitEntry("relicwrought:test", 1, 0, 1, false, "")
        ), 0));
        assertThrows(IllegalArgumentException.class, () -> new StarterKitDefinition(KIT_ID, List.of(
                new StarterKitEntry("relicwrought:test", 1, 0, 1, false, "")
        ), -1));
    }

    @Test
    void entriesListIsDefensivelyCopied() {
        List<StarterKitEntry> mutable = new java.util.ArrayList<>(List.of(
                new StarterKitEntry("relicwrought:test", 1, 0, 1, false, "")
        ));
        StarterKitDefinition kit = new StarterKitDefinition(KIT_ID, mutable, 1);
        mutable.add(new StarterKitEntry("relicwrought:test2", 1, 0, 1, false, ""));
        assertEquals(1, kit.entries().size());
    }

    @Test
    void entriesAreUnmodifiable() {
        StarterKitDefinition kit = new StarterKitDefinition(KIT_ID, List.of(
                new StarterKitEntry("relicwrought:test", 1, 0, 1, false, "")
        ), 1);
        assertThrows(UnsupportedOperationException.class, () -> kit.entries().add(
                new StarterKitEntry("relicwrought:test2", 1, 0, 1, false, "")));
    }
}
