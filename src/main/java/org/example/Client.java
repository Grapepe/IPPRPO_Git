package org.example;

import java.util.ArrayList;
import java.util.List;

public class Client extends User
{
    private String driverLicenseNum;
    private String phoneNum;
    private List<Rent> rentalHistory;
    public Client()
    {
        super();
        this.rentalHistory = new ArrayList<>();
    }
    public Client(String login, String password, String email, String driverLicenseNum, String phoneNum) {
        super(login, password, email);
        this.driverLicenseNum = driverLicenseNum;
        this.phoneNum = phoneNum;
        this.rentalHistory = new ArrayList<>();
    }

    public void addRentToHistory(Rent rent) {
        this.rentalHistory.add(rent);
    }

    public String getDriverLicenseNum() { return driverLicenseNum; }
    public String getPhoneNum() { return phoneNum; }
    public List<Rent> getRentalHistory() { return rentalHistory; }
}
