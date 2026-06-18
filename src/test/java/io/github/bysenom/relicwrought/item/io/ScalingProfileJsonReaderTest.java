package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonParser;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.scaling.ScalingStat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ScalingProfileJsonReaderTest {
    @Test
    void readsValidPiecewiseProfile() {
        var profile = new ScalingProfileJsonReader().read(JsonParser.parseString("""
                {
                  "id": "weapon_damage_default",
                  "stat": "weapon_damage",
                  "curve": {
                    "type": "piecewise",
                    "points": [
                      {"level": 1, "value": 5.0},
                      {"level": 950, "value": 250000.0}
                    ]
                  },
                  "rounding": "nearest",
                  "data_version": 1
                }
                """).getAsJsonObject(), "arpgmod");

        assertEquals(ScalingStat.WEAPON_DAMAGE, profile.stat());
        assertEquals(5.0D, profile.valueAt(ItemLevel.of(1)));
        assertEquals(250000.0D, profile.valueAt(ItemLevel.of(950)));
    }

    @Test
    void rejectsUnknownCurveType() {
        assertThrows(IllegalArgumentException.class, () -> new ScalingProfileJsonReader().read(JsonParser.parseString("""
                {
                  "id": "bad_profile",
                  "stat": "weapon_damage",
                  "curve": {"type": "unknown"},
                  "rounding": "nearest",
                  "data_version": 1
                }
                """).getAsJsonObject(), "arpgmod"));
    }

    @Test
    void rejectsInvalidPointLevels() {
        assertThrows(IllegalArgumentException.class, () -> new ScalingProfileJsonReader().read(JsonParser.parseString("""
                {
                  "id": "bad_profile",
                  "stat": "weapon_damage",
                  "curve": {
                    "type": "piecewise",
                    "points": [
                      {"level": 0, "value": 5.0},
                      {"level": 950, "value": 250000.0}
                    ]
                  },
                  "rounding": "nearest",
                  "data_version": 1
                }
                """).getAsJsonObject(), "arpgmod"));
    }
}
