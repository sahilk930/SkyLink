package com.skylink;

import java.util.Date;
import java.util.UUID;

public class Booking {
    private final String bookingId;
    private final String flightNumber;
    private final String seatNumber;
    private final String passengerName;
    private final Date bookingDate;
    private final boolean cancelled;

    public Booking(String flightNumber, String seatNumber, String passengerName) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.passengerName = passengerName;
        this.bookingDate = new Date();
        this.cancelled = false;
    }

    public Booking(String bookingId, String flightNumber, String seatNumber, String passengerName, Date bookingDate, boolean cancelled) {
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.passengerName = passengerName;
        this.bookingDate = bookingDate;
        this.cancelled = cancelled;
    }

    public String getBookingId() { return bookingId; }
    public String getFlightNumber() { return flightNumber; }
    public String getSeatNumber() { return seatNumber; }
    public String getPassengerName() { return passengerName; }
    public Date getBookingDate() { return bookingDate; }
    public boolean isCancelled() { return cancelled; }
}

