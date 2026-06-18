package io.github.bysenom.relicwrought.item.generation;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class GenerationSeedSplitterTest {

    @Test
    void deterministicSeeds() {
        GenerationSeedSplitter s1 = new GenerationSeedSplitter(42);
        GenerationSeedSplitter s2 = new GenerationSeedSplitter(42);

        assertEquals(s1.itemBaseSeed().nextLong(), s2.itemBaseSeed().nextLong());
        assertEquals(s1.raritySeed().nextLong(), s2.raritySeed().nextLong());
        assertEquals(s1.qualitySeed().nextLong(), s2.qualitySeed().nextLong());
        assertEquals(s1.slotsSeed().nextLong(), s2.slotsSeed().nextLong());
        assertEquals(s1.affixSeed().nextLong(), s2.affixSeed().nextLong());
        assertEquals(s1.uuidSeed().nextLong(), s2.uuidSeed().nextLong());
    }

    @Test
    void differentRootSeedGivesDifferentStreams() {
        GenerationSeedSplitter s1 = new GenerationSeedSplitter(42);
        GenerationSeedSplitter s2 = new GenerationSeedSplitter(99);

        assertNotEquals(s1.itemBaseSeed().nextLong(), s2.itemBaseSeed().nextLong());
    }

    @Test
    void substreamsAreIndependent() {
        GenerationSeedSplitter splitter = new GenerationSeedSplitter(100);
        long base1 = splitter.itemBaseSeed().nextLong();
        long rarity1 = splitter.raritySeed().nextLong();
        long quality1 = splitter.qualitySeed().nextLong();

        GenerationSeedSplitter splitter2 = new GenerationSeedSplitter(100);
        long base2 = splitter2.itemBaseSeed().nextLong();
        long rarity2 = splitter2.raritySeed().nextLong();
        long quality2 = splitter2.qualitySeed().nextLong();

        assertEquals(base1, base2);
        assertEquals(rarity1, rarity2);
        assertEquals(quality1, quality2);
    }

    @Test
    void allStreamsProduceDifferentValues() {
        GenerationSeedSplitter splitter = new GenerationSeedSplitter(42);
        java.util.Set<Long> values = new java.util.HashSet<>();
        values.add(splitter.itemBaseSeed().nextLong());
        values.add(splitter.raritySeed().nextLong());
        values.add(splitter.qualitySeed().nextLong());
        values.add(splitter.slotsSeed().nextLong());
        values.add(splitter.affixSeed().nextLong());
        values.add(splitter.uuidSeed().nextLong());
        assertEquals(6, values.size(), "All substreams should produce different values");
    }
}
