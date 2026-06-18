package io.github.bysenom.relicwrought.player;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class PlayerProfileManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void newManagerReturnsEmptyProfiles() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();
        PlayerArpgProfile profile = manager.getProfile(uuid);
        assertFalse(profile.classSelected());
        assertFalse(profile.starterKitGranted());
    }

    @Test
    void saveAndLoadPersistsProfile() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();

        PlayerArpgProfile original = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 1000L)
                .withKitGranted("relicwrought:warrior_starter", 1);
        manager.saveProfile(uuid, original);

        PlayerProfileManager loaded = new PlayerProfileManager(tempDir);
        PlayerArpgProfile restored = loaded.getProfile(uuid);
        assertTrue(restored.classSelected());
        assertEquals("relicwrought:warrior", restored.classId());
        assertTrue(restored.starterKitGranted());
        assertEquals("relicwrought:warrior_starter", restored.starterKitId());
        assertEquals(1, restored.starterKitVersion());
        assertEquals(1000L, restored.selectionTimestamp());
    }

    @Test
    void saveAndLoadMultipleProfiles() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        manager.saveProfile(uuid1, PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 100L));
        manager.saveProfile(uuid2, PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:ranger", 200L));

        PlayerProfileManager loaded = new PlayerProfileManager(tempDir);
        assertEquals("relicwrought:warrior", loaded.getProfile(uuid1).classId());
        assertEquals("relicwrought:ranger", loaded.getProfile(uuid2).classId());
    }

    @Test
    void hasSelectedClassReturnsCorrectly() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();
        assertFalse(manager.hasSelectedClass(uuid));

        manager.saveProfile(uuid, PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 100L));
        assertTrue(manager.hasSelectedClass(uuid));
    }

    @Test
    void hasReceivedKitReturnsCorrectly() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();
        assertFalse(manager.hasReceivedKit(uuid));

        manager.saveProfile(uuid, PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 100L)
                .withKitGranted("relicwrought:warrior_starter", 1));
        assertTrue(manager.hasReceivedKit(uuid));
    }

    @Test
    void corruptedFileDoesNotCrash() throws IOException {
        Path profilesFile = tempDir.resolve("relicwrought_profiles.json");
        Files.writeString(profilesFile, "{invalid json...");

        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();
        PlayerArpgProfile profile = manager.getProfile(uuid);
        assertFalse(profile.classSelected());
    }

    @Test
    void emptyFileProducesEmptyProfiles() throws IOException {
        Path profilesFile = tempDir.resolve("relicwrought_profiles.json");
        Files.writeString(profilesFile, "");

        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();
        assertFalse(manager.hasSelectedClass(uuid));
    }

    @Test
    void profileSurvivesSaveAndLoadCycleWithKitVersion() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID uuid = UUID.randomUUID();

        PlayerArpgProfile profile = PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:rogue", 500L)
                .withKitGranted("relicwrought:rogue_starter", 3);
        manager.saveProfile(uuid, profile);

        PlayerProfileManager loaded = new PlayerProfileManager(tempDir);
        PlayerArpgProfile restored = loaded.getProfile(uuid);
        assertEquals(3, restored.starterKitVersion());
        assertEquals(500L, restored.selectionTimestamp());
        assertEquals("relicwrought:rogue_starter", restored.starterKitId());
    }

    @Test
    void unknownPlayerReturnsEmptyProfile() {
        PlayerProfileManager manager = new PlayerProfileManager(tempDir);
        UUID known = UUID.randomUUID();
        UUID unknown = UUID.randomUUID();

        manager.saveProfile(known, PlayerArpgProfile.empty()
                .withClassSelected("relicwrought:warrior", 100L));
        assertFalse(manager.getProfile(unknown).classSelected());
    }
}
