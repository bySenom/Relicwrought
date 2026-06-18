package io.github.bysenom.relicwrought.item.generation;

import java.util.SplittableRandom;

public final class GenerationSeedSplitter {
    private final SplittableRandom root;

    public GenerationSeedSplitter(long seed) {
        this.root = new SplittableRandom(seed);
    }

    public SplittableRandom itemBaseSeed() {
        return root.split();
    }

    public SplittableRandom raritySeed() {
        return root.split();
    }

    public SplittableRandom qualitySeed() {
        return root.split();
    }

    public SplittableRandom slotsSeed() {
        return root.split();
    }

    public SplittableRandom affixSeed() {
        return root.split();
    }

    public SplittableRandom uuidSeed() {
        return root.split();
    }
}
