package io.github.bysenom.relicwrought.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ClassStartingAttributesTest {
    @Test
    void warriorHasCorrectBaseAttributes() {
        ClassStartingAttributes attrs = ClassStartingAttributes.forClass("warrior");
        assertEquals(10, attrs.getAttribute(CharacterAttribute.STRENGTH));
        assertEquals(3, attrs.getAttribute(CharacterAttribute.DEXTERITY));
        assertEquals(0, attrs.getAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(8, attrs.getAttribute(CharacterAttribute.VITALITY));
    }

    @Test
    void rangerHasCorrectBaseAttributes() {
        ClassStartingAttributes attrs = ClassStartingAttributes.forClass("ranger");
        assertEquals(3, attrs.getAttribute(CharacterAttribute.STRENGTH));
        assertEquals(10, attrs.getAttribute(CharacterAttribute.DEXTERITY));
        assertEquals(2, attrs.getAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(5, attrs.getAttribute(CharacterAttribute.VITALITY));
    }

    @Test
    void arcanistHasCorrectBaseAttributes() {
        ClassStartingAttributes attrs = ClassStartingAttributes.forClass("arcanist");
        assertEquals(0, attrs.getAttribute(CharacterAttribute.STRENGTH));
        assertEquals(3, attrs.getAttribute(CharacterAttribute.DEXTERITY));
        assertEquals(10, attrs.getAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(4, attrs.getAttribute(CharacterAttribute.VITALITY));
    }

    @Test
    void rogueHasCorrectBaseAttributes() {
        ClassStartingAttributes attrs = ClassStartingAttributes.forClass("rogue");
        assertEquals(3, attrs.getAttribute(CharacterAttribute.STRENGTH));
        assertEquals(9, attrs.getAttribute(CharacterAttribute.DEXTERITY));
        assertEquals(3, attrs.getAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(5, attrs.getAttribute(CharacterAttribute.VITALITY));
    }

    @Test
    void unknownClassGetsDefaultAttributes() {
        ClassStartingAttributes attrs = ClassStartingAttributes.forClass("unknown");
        assertEquals(5, attrs.getAttribute(CharacterAttribute.STRENGTH));
        assertEquals(5, attrs.getAttribute(CharacterAttribute.DEXTERITY));
        assertEquals(5, attrs.getAttribute(CharacterAttribute.INTELLIGENCE));
        assertEquals(5, attrs.getAttribute(CharacterAttribute.VITALITY));
    }

    @Test
    void differentClassesHaveDifferentStrength() {
        assertNotEquals(
                ClassStartingAttributes.forClass("warrior").getAttribute(CharacterAttribute.STRENGTH),
                ClassStartingAttributes.forClass("arcanist").getAttribute(CharacterAttribute.STRENGTH)
        );
    }
}
