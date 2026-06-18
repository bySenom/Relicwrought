package io.github.bysenom.relicwrought.item.persistence;

import io.github.bysenom.relicwrought.item.model.ArpgItemData;

public record ArpgItemComponent(ArpgItemData data) {
    public ArpgItemComponent {
        if (data == null) {
            throw new IllegalArgumentException("ArpgItemComponent data must not be null");
        }
    }
}
