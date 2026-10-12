package org.example;

import java.io.Serializable;
import java.util.ArrayList; // Добавлен импорт
import java.util.List;
import java.util.stream.Collectors;

public class RentalOffice implements Serializable { // Добавлен Serializable
    private static final long serialVersionUID = 1L;

    private String name;
    private String address;
    private String phoneNum;
    private List<Car> carList;

    public RentalOffice() {
        this.carList = new ArrayList<>(); // Инициализация для пустого конструктора
    }

    public RentalOffice(String name, String address, String phoneNum) {
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.carList = new ArrayList<>(); // ИСПРАВЛЕНИЕ: Инициализация списка!
    }

    public void addCar(Car newCar) {
        carList.add(newCar);
        System.out.println("Автомобиль " + newCar.getMark() + " добавлен в " + name);
    }

    public void removeCar(Car car)
    {
        carList.remove(car);
    }

    public List<Car> getAvailableCars() {
        return carList.stream()
                .filter(Car::isAvailable)
                .collect(Collectors.toList());
    }

    public Car findCarByVin(String vin) {
        if (vin == null) return null;
        for (Car c : carList) {
            if (c.getVin() != null && c.getVin().equalsIgnoreCase(vin)) return c;
        }
        return null;
    }

    public boolean removeCarByVin(String vin) {
        Car c = findCarByVin(vin);
        if (c == null) return false;
        carList.remove(c);
        return true;
    }

    public List<Car> getCarList() { return carList; }
    public String getName() { return name; }
}