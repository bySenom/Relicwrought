package io.github.bysenom.relicwrought.item.persistence;

import java.util.List;

public record ArpgItemWriteResult(boolean succeeded, List<String> messages) {
    public ArpgItemWriteResult {
        messages = List.copyOf(messages);
    }

    public static ArpgItemWriteResult success() {
        return new ArpgItemWriteResult(true, List.of());
    }

    public static ArpgItemWriteResult failure(List<String> messages) {
        return new ArpgItemWriteResult(false, messages);
    }
}
