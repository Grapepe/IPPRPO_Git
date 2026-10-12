package org.example;

public enum CarCategory
{
    ECONOM(1.0),
    BUSINESS(2.5);

    private final double multiplier;

    CarCategory(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
