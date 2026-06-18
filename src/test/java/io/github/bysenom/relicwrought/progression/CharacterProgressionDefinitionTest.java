package io.github.bysenom.relicwrought.progression;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CharacterProgressionDefinitionTest {
    static final DefinitionKey DEFAULT = DefinitionKey.parse("default_character_progression", "relicwrought");

    @Test
    void createsValidProgressionDefinition() {
        var def = new CharacterProgressionDefinition(DEFAULT, 1, 100, 100, 1.65, 1);
        assertEquals(DEFAULT, def.id());
        assertEquals(100, def.baseXp());
        assertEquals(1.65, def.exponent());
    }

    @Test
    void createCurveMatchesParameters() {
        var def = new CharacterProgressionDefinition(DEFAULT, 1, 100, 100, 1.65, 1);
        ExperienceCurve curve = def.createCurve();
        assertEquals(100, curve.baseXp());
        assertEquals(1.65, curve.exponent());
    }

    @Test
    void rejectsInvalidMinimumLevel() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 0, 100, 100, 1.65, 1));
    }

    @Test
    void rejectsInvalidMaximumLevel() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 1, 101, 100, 1.65, 1));
    }

    @Test
    void rejectsMaxLessThanMin() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 50, 10, 100, 1.65, 1));
    }

    @Test
    void rejectsNonPositiveBaseXp() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 1, 100, 0, 1.65, 1));
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 1, 100, -1, 1.65, 1));
    }

    @Test
    void rejectsNonPositiveExponent() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 1, 100, 100, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new CharacterProgressionDefinition(DEFAULT, 1, 100, 100, -1, 1));
    }
}
