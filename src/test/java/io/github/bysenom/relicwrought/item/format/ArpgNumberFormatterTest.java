package io.github.bysenom.relicwrought.item.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ArpgNumberFormatterTest {
    @Test
    void formatIntZero() {
        assertEquals("0", ArpgNumberFormatter.formatInt(0));
    }

    @Test
    void formatIntOne() {
        assertEquals("1", ArpgNumberFormatter.formatInt(1));
    }

    @Test
    void formatInt999() {
        assertEquals("999", ArpgNumberFormatter.formatInt(999));
    }

    @Test
    void formatInt1000() {
        assertEquals("1,000", ArpgNumberFormatter.formatInt(1000));
    }

    @Test
    void formatInt125000() {
        assertEquals("125,000", ArpgNumberFormatter.formatInt(125000));
    }

    @Test
    void formatIntNegative() {
        assertEquals("-1", ArpgNumberFormatter.formatInt(-1));
    }

    @Test
    void formatDoubleZero() {
        assertEquals("0", ArpgNumberFormatter.formatDouble(0.0, 0));
    }

    @Test
    void formatDoubleOneDecimal() {
        assertEquals("1.5", ArpgNumberFormatter.formatDouble(1.5, 1));
    }

    @Test
    void formatDoubleTwoDecimals() {
        assertEquals("1.23", ArpgNumberFormatter.formatDouble(1.234, 2));
    }

    @Test
    void formatDoubleFourDecimals() {
        assertEquals("1.2346", ArpgNumberFormatter.formatDouble(1.23456, 4));
    }

    @Test
    void formatDoubleNaN() {
        assertEquals("0", ArpgNumberFormatter.formatDouble(Double.NaN, 1));
    }

    @Test
    void formatDoublePositiveInfinity() {
        assertEquals("0", ArpgNumberFormatter.formatDouble(Double.POSITIVE_INFINITY, 1));
    }

    @Test
    void formatDoubleNegativeInfinity() {
        assertEquals("0", ArpgNumberFormatter.formatDouble(Double.NEGATIVE_INFINITY, 1));
    }

    @Test
    void formatPercentSimple() {
        assertEquals("10.5", ArpgNumberFormatter.formatPercent(10.5, 1));
    }

    @Test
    void formatPercentZero() {
        assertEquals("0", ArpgNumberFormatter.formatPercent(0.0, 0));
    }

    @Test
    void formatSignedPositive() {
        assertEquals("+5.0", ArpgNumberFormatter.formatSigned(5.0, 1));
    }

    @Test
    void formatSignedNegative() {
        assertEquals("-5.0", ArpgNumberFormatter.formatSigned(-5.0, 1));
    }

    @Test
    void formatSignedZero() {
        assertEquals("+0", ArpgNumberFormatter.formatSigned(0.0, 0));
    }

    @Test
    void formatSignedPercentPositive() {
        assertEquals("+10.5 %", ArpgNumberFormatter.formatSignedPercent(10.5, 1));
    }

    @Test
    void formatSignedPercentNegative() {
        assertEquals("-3.0 %", ArpgNumberFormatter.formatSignedPercent(-3.0, 1));
    }

    @Test
    void formatSignedPercentZero() {
        assertEquals("+0 %", ArpgNumberFormatter.formatSignedPercent(0.0, 0));
    }

    @Test
    void formatSignedPercentNaN() {
        assertEquals("0 %", ArpgNumberFormatter.formatSignedPercent(Double.NaN, 1));
    }

    @Test
    void formatSignedPercentInfinity() {
        assertEquals("0 %", ArpgNumberFormatter.formatSignedPercent(Double.POSITIVE_INFINITY, 1));
    }

    @Test
    void formatRangeTwoDecimals() {
        assertEquals("10\u201315", ArpgNumberFormatter.formatRange(10, 15, 0));
    }

    @Test
    void formatRangeDecimals() {
        assertEquals("1.5\u20132.5", ArpgNumberFormatter.formatRange(1.5, 2.5, 1));
    }

    @Test
    void formatCompactBelow1000() {
        assertEquals("999", ArpgNumberFormatter.formatCompact(999));
    }

    @Test
    void formatCompactThousand() {
        assertEquals("1K", ArpgNumberFormatter.formatCompact(1000));
    }

    @Test
    void formatCompactMillion() {
        assertEquals("1.5M", ArpgNumberFormatter.formatCompact(1500000));
    }

    @Test
    void formatCompactBillion() {
        assertEquals("2.5B", ArpgNumberFormatter.formatCompact(2500000000L));
    }

    @Test
    void formatCompactTrillion() {
        assertEquals("1T", ArpgNumberFormatter.formatCompact(1000000000000L));
    }

    @Test
    void formatCompactNaN() {
        assertEquals("0", ArpgNumberFormatter.formatCompact(Double.NaN));
    }

    @Test
    void formatCompactInfinity() {
        assertEquals("0", ArpgNumberFormatter.formatCompact(Double.POSITIVE_INFINITY));
    }

    @Test
    void formatTechnicalSimple() {
        assertEquals("1.5", ArpgNumberFormatter.formatTechnical(1.5));
    }

    @Test
    void formatTechnicalNaN() {
        assertEquals("NaN", ArpgNumberFormatter.formatTechnical(Double.NaN));
    }

    @Test
    void formatTechnicalPositiveInfinity() {
        assertEquals("Infinity", ArpgNumberFormatter.formatTechnical(Double.POSITIVE_INFINITY));
    }

    @Test
    void formatTechnicalNegativeInfinity() {
        assertEquals("-Infinity", ArpgNumberFormatter.formatTechnical(Double.NEGATIVE_INFINITY));
    }

    @Test
    void formatTechnicalLong() {
        assertEquals("123456789", ArpgNumberFormatter.formatTechnicalLong(123456789L));
    }

    @Test
    void formatTechnicalLongZero() {
        assertEquals("0", ArpgNumberFormatter.formatTechnicalLong(0L));
    }

    @Test
    void formatDurability() {
        assertEquals("1,000 / 1,500", ArpgNumberFormatter.formatDurability(1000, 1500));
    }

    @Test
    void formatDurabilityZero() {
        assertEquals("0 / 100", ArpgNumberFormatter.formatDurability(0, 100));
    }

    @Test
    void noScientificNotation() {
        String result = ArpgNumberFormatter.formatDouble(0.0001, 4);
        assertEquals("0.0001", result);
    }
}
