package io.github.bysenom.relicwrought.loot;

import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.registry.DataRegistry;

public final class LootProfileResolver {
    private final DataRegistry<LootProfileDefinition> profiles;

    public LootProfileResolver(DataRegistry<LootProfileDefinition> profiles) {
        this.profiles = profiles;
    }

    public LootProfileDefinition resolve(DefinitionKey profileId) {
        return profiles.get(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown loot profile: " + profileId));
    }

    public LootProfileDefinition resolveForContext(LootContextData context) {
        for (LootProfileDefinition profile : profiles.values()) {
            if (profile.sourceType() != context.sourceType()) continue;
            if (!profile.dimensions().isEmpty() && !profile.dimensions().contains(context.dimension())) continue;
            if (!profile.entityOverrides().isEmpty()) {
                EntityLootOverride override = profile.entityOverrides().get(context.entityId());
                if (override != null && override.profileId() != null) {
                    return profiles.get(override.profileId())
                            .orElse(profile);
                }
            }
            return profile;
        }
        throw new IllegalArgumentException("No loot profile found for source type " + context.sourceType()
                + " in dimension " + context.dimension());
    }

    public EntityLootOverride findOverride(LootProfileDefinition profile, String entityId) {
        return profile.entityOverrides().get(entityId);
    }
}
