package org.example;

public class Manager extends User
{
    private long emplId;
    private RentalOffice rentalOffice;


    public Manager(String login, String password, String email, long emplId)
    {
        super(login, password, email);
        this.emplId = emplId;
    }
    public void setRentalOffice(RentalOffice office) {this.rentalOffice = office;}
    public long getEmplId() { return emplId; }
    public RentalOffice getRentalOffice() { return rentalOffice; }
}
