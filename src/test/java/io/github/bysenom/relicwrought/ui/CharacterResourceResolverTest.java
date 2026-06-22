package io.github.bysenom.relicwrought.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterResourceResolverTest {
    @Test
    void classIdsResolveToExpectedResources() {
        assertEquals(CharacterResourceType.RAGE, CharacterResourceResolver.resolveType("relicwrought:warrior"));
        assertEquals(CharacterResourceType.ENERGY, CharacterResourceResolver.resolveType("ranger"));
        assertEquals(CharacterResourceType.MANA, CharacterResourceResolver.resolveType("relicwrought:arcanist"));
        assertEquals(CharacterResourceType.ENERGY, CharacterResourceResolver.resolveType("rogue"));
    }

    @Test
    void unknownOrBlankClassUsesNone() {
        assertEquals(CharacterResourceType.NONE, CharacterResourceResolver.resolveType(""));
        assertEquals(CharacterResourceType.NONE, CharacterResourceResolver.resolveType("relicwrought:unknown"));
        assertEquals(CharacterResourceType.NONE, CharacterResourceResolver.resolveType(null));
    }

    @Test
    void selectedClassGetsVisibleDefaultResourceState() {
        CharacterResourceState state = CharacterResourceResolver.resolveState("warrior", 0.0);

        assertEquals(CharacterResourceType.RAGE, state.type());
        assertEquals(100.0, state.currentValue(), 0.001);
        assertEquals(100.0, state.maximumValue(), 0.001);
    }
}
