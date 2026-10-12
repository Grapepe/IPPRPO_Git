package org.example;

import java.io.Serializable;

public class Car implements Serializable {
    private static final long serialVersionUID = 1L;

    private String vinCode;
    private String mark;
    private String model;
    private CarCategory carCategory;
    private double baseDailyPrice;
    private RentStatus rentStatus;
    public void setMark(String mark) { this.mark = mark; }
    public void setModel(String model) { this.model = model; }
    public void setCarCategory(CarCategory c) { this.carCategory = c; }
    public void setBaseDailyPrice(double price) { this.baseDailyPrice = price; }

    public Car(String vinCode, String mark, String model, CarCategory carCategory, double baseDailyPrice) {
        this.vinCode = vinCode;
        this.mark = mark;
        this.model = model;
        this.carCategory = carCategory;
        this.baseDailyPrice = baseDailyPrice;
        this.rentStatus = RentStatus.AVAILABLE;
    }

    public String getMark() { return mark; }
    public String getModel() { return model; }
    public String getVin() { return vinCode; }
    public RentStatus getRentStatus() { return rentStatus; }
    public CarCategory getCarCategory() { return carCategory; }
    public double getBaseDailyPrice() { return baseDailyPrice; }
    @Override
    public String toString() {
        return vinCode + " " + mark + " " + model + " (" + carCategory + ") - " + baseDailyPrice + "$/day";
    }

    public boolean isAvailable() {
        return rentStatus == RentStatus.AVAILABLE;
    }

    public void setStatus(RentStatus rentStatus) {
        this.rentStatus = rentStatus;
    }

    public double calculatePrice(int days) {
        return this.baseDailyPrice * this.carCategory.getMultiplier() * days;
    }
}