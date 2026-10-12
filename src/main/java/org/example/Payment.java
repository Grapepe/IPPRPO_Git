package org.example;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String paymentId;
    private double amount;
    private String status;
    private LocalDateTime paymentDate;

    public Payment(double amount, String paymentMethod)
    {
        this.paymentId = java.util.UUID.randomUUID().toString();
        this.amount = amount;
        this.status = "PENDING";
        this.paymentDate = LocalDateTime.now();
    }

    public void processPayment()
    {
        this.status = "SUCCESS";
        System.out.println("Платеж на сумму " + amount + " успешно проведен!");
    }

    public double getAmount() { return amount; }
    public String getStatus() { return status; }
}