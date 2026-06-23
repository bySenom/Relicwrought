package io.github.bysenom.relicwrought.ability;

import io.github.bysenom.relicwrought.ability.AbilityExecutionService.AbilityActivationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbilityExecutionServiceTest {

    private AbilityRegistry emptyRegistry() {
        return new AbilityRegistry();
    }

    @Test
    void activate_nullPlayer_returnsFailure() {
        AbilityExecutionService service = new AbilityExecutionService(emptyRegistry());
        PlayerAbilityLoadout loadout = new PlayerAbilityLoadout();
        PlayerAbilityCooldowns cooldowns = new PlayerAbilityCooldowns();

        AbilityActivationResult result = service.activate(null, 0, loadout, cooldowns, null);

        assertFalse(result.success());
        assertNotNull(result.message());
        assertFalse(result.message().isBlank());
    }

    @Test
    void activate_nullPlayer_doesNotThrow() {
        AbilityExecutionService service = new AbilityExecutionService(emptyRegistry());
        PlayerAbilityLoadout loadout = new PlayerAbilityLoadout();
        PlayerAbilityCooldowns cooldowns = new PlayerAbilityCooldowns();

        assertDoesNotThrow(() -> service.activate(null, 0, loadout, cooldowns, null));
    }
}
