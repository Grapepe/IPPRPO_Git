package org.example;

import java.io.Serializable; // Добавлен импорт
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Rent implements Serializable { // Добавлен Serializable
    private static final long serialVersionUID = 1L;

    private String rentId;
    private Client client;
    private Car car;
    private RentStatus status;
    private RentalOffice rentalOffice;
    private Payment payment;
    private double totalCost;
    private LocalDate startDate;
    private LocalDate endDate;
    private Insurance insurance;

    public Rent() {}

    public Rent(Client client, Car car, RentalOffice rentalOffice, Insurance insurance, LocalDate startDate, LocalDate endDate) {
        this.rentId = java.util.UUID.randomUUID().toString();
        this.client = client;
        this.car = car;
        this.rentalOffice = rentalOffice;
        this.insurance = insurance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = calculateTotalCost();
        this.status = RentStatus.RENTED;
    }

    public double calculateTotalCost() {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) days = 1;

        double carCost = car.calculatePrice((int) days);
        double insuranceCost = (insurance != null) ? insurance.calculateCost((int) days) : 0;
        double total = carCost + insuranceCost;

        if (days >= 7) {
            total *= 0.9;
        }
        return total;
    }



    public Car getCar() { return car; }
    public Client getClient() { return client; }
    public double getTotalCost() { return totalCost; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public RentStatus getStatus() { return status; }
    public RentalOffice getRentalOffice() {return  rentalOffice;}
    @Override
    public String toString() {
        return "Аренда " + rentId.substring(0, 8) +
                " | " + car.getMark() + " " + car.getModel() +
                " | " + startDate + " → " + endDate +
                " | Стоимость: " + totalCost + "$ | Статус: " + status;
    }

    public String getRentId() {
        return this.rentId;
    }

    public void completeRent() {
        this.status = RentStatus.COMPLETED;
        this.car.setStatus(RentStatus.AVAILABLE);
        if (client != null) {
            client.addRentToHistory(this);
        }
    }
}