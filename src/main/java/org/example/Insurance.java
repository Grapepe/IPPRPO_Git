package org.example;

import java.io.Serializable;

public class Insurance implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String type;
    private double coverageAmount;
    private double costPerDay;

    public Insurance(String type, double coverageAmount, double costPerDay) {
        this.type = type;
        this.coverageAmount = coverageAmount;
        this.costPerDay = costPerDay;
    }

    public double calculateCost(int days) {
        return costPerDay * days;
    }

    public String getType() { return type; }
}