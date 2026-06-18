package io.github.bysenom.relicwrought.progression;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;
import io.github.bysenom.relicwrought.player.PlayerArpgProfile;
import io.github.bysenom.relicwrought.player.PlayerProfileManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class ProgressionManager {
    private final DataRegistry<CharacterProgressionDefinition> progressionProfiles;
    private final PlayerProfileManager profileManager;
    private final ExperienceRewardService rewardService;
    private final ExperienceCurve curve;

    public ProgressionManager(
            DataRegistry<CharacterProgressionDefinition> progressionProfiles,
            PlayerProfileManager profileManager
    ) {
        this.progressionProfiles = progressionProfiles;
        this.profileManager = profileManager;
        this.curve = resolveCurve();
        this.rewardService = new ExperienceRewardService(curve);
    }

    private ExperienceCurve resolveCurve() {
        var defKey = DefinitionKey.parse("default_character_progression", Relicwrought.MOD_ID);
        var def = progressionProfiles.get(defKey);
        if (def.isPresent()) {
            Relicwrought.LOGGER.info("Loaded progression profile: {} (baseXp={}, exponent={})",
                    def.get().id(), def.get().baseXp(), def.get().exponent());
            return def.get().createCurve();
        }
        Relicwrought.LOGGER.warn("No progression profile found, using defaults (baseXp=100, exponent=1.65)");
        return new ExperienceCurve(100, 1.65);
    }

    public ExperienceGrantResult grantXp(ServerPlayer player, long rawXp) {
        UUID uuid = player.getUUID();
        PlayerArpgProfile profile = profileManager.getProfile(uuid);
        CharacterProgression progression = profile.toCharacterProgression();

        ExperienceGrantResult result = rewardService.grantXp(progression, rawXp);
        if (!result.success()) return result;

        CharacterProgression updated = rewardService.applyGrant(progression, result);
        PlayerArpgProfile updatedProfile = PlayerArpgProfile.fromCharacterProgression(profile, updated);
        profileManager.saveProfile(uuid, updatedProfile);

        return result;
    }

    public AttributeAllocationResult allocateAttribute(ServerPlayer player, CharacterAttribute attribute, int amount) {
        UUID uuid = player.getUUID();
        PlayerArpgProfile profile = profileManager.getProfile(uuid);
        CharacterProgression progression = profile.toCharacterProgression();

        AttributeAllocationResult result = rewardService.allocateAttribute(progression, attribute, amount);
        if (!result.success()) return result;

        Map<CharacterAttribute, Integer> newAllocated = new EnumMap<>(progression.allocatedAttributes());
        int current = newAllocated.getOrDefault(attribute, 0);
        newAllocated.put(attribute, current + amount);

        int newUnspent = progression.unspentAttributePoints() - amount;

        CharacterProgression updated = new CharacterProgression(
                progression.level(), progression.currentLevelXp(), progression.totalXp(),
                newUnspent, newAllocated
        );
        PlayerArpgProfile updatedProfile = PlayerArpgProfile.fromCharacterProgression(profile, updated);
        profileManager.saveProfile(uuid, updatedProfile);

        return result;
    }

    public CharacterProgression getProgression(ServerPlayer player) {
        return profileManager.getProfile(player.getUUID()).toCharacterProgression();
    }

    public PlayerArpgProfile getProfile(ServerPlayer player) {
        return profileManager.getProfile(player.getUUID());
    }

    public int getTotalAttribute(ServerPlayer player, CharacterAttribute attribute) {
        CharacterProgression prog = getProgression(player);
        ClassStartingAttributes base = ClassStartingAttributes.forClass(getProfile(player).classId());
        return base.getAttribute(attribute) + prog.allocatedAttributes().getOrDefault(attribute, 0);
    }

    public Map<CharacterAttribute, Integer> getTotalAttributes(ServerPlayer player) {
        PlayerArpgProfile profile = getProfile(player);
        CharacterProgression prog = profile.toCharacterProgression();
        ClassStartingAttributes base = ClassStartingAttributes.forClass(profile.classId());
        Map<CharacterAttribute, Integer> result = new EnumMap<>(CharacterAttribute.class);
        for (var attr : CharacterAttribute.values()) {
            result.put(attr, base.getAttribute(attr) + prog.allocatedAttributes().getOrDefault(attr, 0));
        }
        return result;
    }

    public long getXpForNextLevel(ServerPlayer player) {
        CharacterProgression prog = getProgression(player);
        return curve.xpToNextLevel(prog.level().value(), prog.currentLevelXp());
    }

    public void saveProfileDirect(net.minecraft.server.level.ServerPlayer player, io.github.bysenom.relicwrought.player.PlayerArpgProfile profile) {
        profileManager.saveProfile(player.getUUID(), profile);
    }

    public ExperienceRewardService rewardService() { return rewardService; }
    public ExperienceCurve curve() { return curve; }
}
