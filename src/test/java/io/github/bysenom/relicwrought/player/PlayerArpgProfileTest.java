package io.github.bysenom.relicwrought.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PlayerArpgProfileTest {
    @Test
    void emptyProfileHasNoClassAndNoKit() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty();
        assertFalse(profile.classSelected());
        assertTrue(profile.classId().isEmpty());
        assertFalse(profile.starterKitGranted());
        assertTrue(profile.starterKitId().isEmpty());
        assertEquals(0, profile.starterKitVersion());
        assertEquals(0, profile.selectionTimestamp());
    }

    @Test
    void emptyProfileHasCurrentVersion() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty();
        assertEquals(PlayerArpgProfile.CURRENT_VERSION, profile.dataVersion());
    }

    @Test
    void withClassSelectedSetsClassId() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 1000L);
        assertTrue(profile.classSelected());
        assertEquals("relicwrought:warrior", profile.classId());
        assertEquals(1000L, profile.selectionTimestamp());
        assertFalse(profile.starterKitGranted());
    }

    @Test
    void withKitGrantedSetsKitId() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 1000L)
                .withKitGranted("relicwrought:warrior_starter", 1);
        assertTrue(profile.starterKitGranted());
        assertEquals("relicwrought:warrior_starter", profile.starterKitId());
        assertEquals(1, profile.starterKitVersion());
        assertTrue(profile.classSelected());
    }

    @Test
    void emptyProfileImmutableWithClassSelected() {
        PlayerArpgProfile empty = PlayerArpgProfile.empty();
        PlayerArpgProfile modified = empty.withClassSelected("relicwrought:ranger", 2000L);
        assertFalse(empty.classSelected());
        assertTrue(modified.classSelected());
        assertNotEquals(empty, modified);
    }

    @Test
    void classProfileImmutableWithKitGranted() {
        PlayerArpgProfile withClass = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:arcanist", 3000L);
        PlayerArpgProfile withKit = withClass.withKitGranted("relicwrought:arcanist_starter", 1);
        assertFalse(withClass.starterKitGranted());
        assertTrue(withKit.starterKitGranted());
        assertNotEquals(withClass, withKit);
    }

    @Test
    void secondClassSelectionReplacesFirst() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 1000L)
                .withClassSelected("relicwrought:ranger", 2000L);
        assertEquals("relicwrought:ranger", profile.classId());
        assertEquals(2000L, profile.selectionTimestamp());
    }

    @Test
    void kitVersionIsPreserved() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 1000L)
                .withKitGranted("relicwrought:warrior_starter", 2);
        assertEquals(2, profile.starterKitVersion());
    }

    @Test
    void kitGrantedWithoutClassSelected() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withKitGranted("relicwrought:test_kit", 1);
        assertFalse(profile.classSelected());
        assertTrue(profile.starterKitGranted());
        assertEquals("relicwrought:test_kit", profile.starterKitId());
    }

    @Test
    void preservesDataVersion() {
        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 1000L);
        assertEquals(PlayerArpgProfile.CURRENT_VERSION, profile.dataVersion());
    }
}
