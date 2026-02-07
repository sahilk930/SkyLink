package com.skylink;

public class SeatDTO {
    private String seatNumber;
    private boolean booked;
    private String passengerName;
    private double price;

    public SeatDTO(Seat seat, double price) {
        this.seatNumber = seat.getSeatNumber();
        this.booked = seat.isBooked();
        this.passengerName = seat.getPassengerName();
        this.price = price;
    }

    public String getSeatNumber() { return seatNumber; }
    public boolean isBooked() { return booked; }
    public String getPassengerName() { return passengerName; }
    public double getPrice() { return price; }
}

