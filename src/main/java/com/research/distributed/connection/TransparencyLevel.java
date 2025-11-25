package com.research.distributed.connection;

public enum TransparencyLevel {
    FRAGMENT_TRANSPARENCY(1, "Fragmentation Transparency"),
    LOCATION_TRANSPARENCY(2, "Location Transparency");

    private final int level;
    private final String description;

    TransparencyLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
