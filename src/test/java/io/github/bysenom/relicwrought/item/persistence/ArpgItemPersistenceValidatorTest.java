package io.github.bysenom.relicwrought.item.persistence;

import io.github.bysenom.relicwrought.Relicwrought;
import io.github.bysenom.relicwrought.item.ItemDataVersions;
import io.github.bysenom.relicwrought.item.model.ArpgItemData;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.model.Rarity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArpgItemPersistenceValidatorTest {
    private ArpgItemData validItem() {
        return new ArpgItemData(
                ItemDataVersions.CURRENT, UUID.randomUUID(),
                DefinitionKey.parse("starter_training_sword", Relicwrought.MOD_ID),
                new ItemLevel(300), 0, Rarity.RARE, 10, 42L,
                false, List.of(), List.of(), List.of()
        );
    }

    @Test
    void validItemPassesValidation() {
        assertTrue(ArpgItemPersistenceValidator.validate(validItem()).isEmpty());
    }

    @Test
    void nullItemDataFails() {
        assertFalse(ArpgItemPersistenceValidator.validate(null).isEmpty());
    }
}
