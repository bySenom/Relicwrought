package io.github.bysenom.relicwrought.item.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bysenom.relicwrought.item.model.DefinitionKey;
import io.github.bysenom.relicwrought.item.model.ItemLevel;
import io.github.bysenom.relicwrought.item.scaling.CurvePoint;
import io.github.bysenom.relicwrought.item.scaling.LinearScalingCurve;
import io.github.bysenom.relicwrought.item.scaling.PiecewiseScalingCurve;
import io.github.bysenom.relicwrought.item.scaling.PowerScalingCurve;
import io.github.bysenom.relicwrought.item.scaling.RoundingStrategy;
import io.github.bysenom.relicwrought.item.scaling.ScalingCurve;
import io.github.bysenom.relicwrought.item.scaling.ScalingProfile;
import io.github.bysenom.relicwrought.item.scaling.ScalingStat;
import io.github.bysenom.relicwrought.item.scaling.ThresholdScalingCurve;
import io.github.bysenom.relicwrought.item.scaling.ThresholdValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScalingProfileJsonReader implements DefinitionJsonReader<ScalingProfile> {
    @Override
    public ScalingProfile read(JsonObject json, String defaultNamespace) {
        DefinitionKey id = DefinitionKey.parse(JsonReaderSupport.requiredString(json, "id"), defaultNamespace);
        ScalingStat stat = ScalingStat.valueOf(JsonReaderSupport.requiredString(json, "stat").toUpperCase(Locale.ROOT));
        ScalingCurve curve = readCurve(JsonReaderSupport.requiredObject(json, "curve"));
        RoundingStrategy rounding = RoundingStrategy.parse(JsonReaderSupport.optionalString(json, "rounding", "none"));

        return new ScalingProfile(
                id,
                stat,
                curve,
                rounding,
                JsonReaderSupport.optionalDouble(json, "minimum_clamp", 0.0D),
                JsonReaderSupport.optionalDouble(json, "maximum_clamp", 1_000_000_000_000.0D),
                JsonReaderSupport.requiredInt(json, "data_version")
        );
    }

    private static ScalingCurve readCurve(JsonObject curve) {
        String type = JsonReaderSupport.requiredString(curve, "type").toLowerCase(Locale.ROOT);
        return switch (type) {
            case "linear" -> new LinearScalingCurve(
                    JsonReaderSupport.optionalDouble(curve, "minimum", 0.0D),
                    JsonReaderSupport.optionalDouble(curve, "maximum", 0.0D)
            );
            case "power" -> new PowerScalingCurve(
                    JsonReaderSupport.optionalDouble(curve, "minimum", 0.0D),
                    JsonReaderSupport.optionalDouble(curve, "maximum", 0.0D),
                    JsonReaderSupport.optionalDouble(curve, "exponent", 1.0D)
            );
            case "piecewise" -> readPiecewise(curve);
            case "threshold" -> readThresholds(curve);
            default -> throw new IllegalArgumentException("Unknown scaling curve type: " + type);
        };
    }

    private static PiecewiseScalingCurve readPiecewise(JsonObject curve) {
        List<CurvePoint> points = new ArrayList<>();
        for (JsonElement element : JsonReaderSupport.optionalArray(curve, "points")) {
            JsonObject point = element.getAsJsonObject();
            points.add(new CurvePoint(
                    ItemLevel.of(JsonReaderSupport.requiredInt(point, "level")),
                    JsonReaderSupport.optionalDouble(point, "value", 0.0D)
            ));
        }
        return new PiecewiseScalingCurve(points);
    }

    private static ThresholdScalingCurve readThresholds(JsonObject curve) {
        List<ThresholdValue> thresholds = new ArrayList<>();
        for (JsonElement element : JsonReaderSupport.optionalArray(curve, "thresholds")) {
            JsonObject threshold = element.getAsJsonObject();
            thresholds.add(new ThresholdValue(
                    ItemLevel.of(JsonReaderSupport.requiredInt(threshold, "level")),
                    JsonReaderSupport.requiredInt(threshold, "value")
            ));
        }
        return new ThresholdScalingCurve(thresholds);
    }
}
