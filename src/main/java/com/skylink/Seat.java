package com.skylink;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Seat {
    private final String seatNumber;
    private final AtomicBoolean booked;
    private final AtomicReference<String> passengerName;

    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
        this.booked = new AtomicBoolean(false);
        this.passengerName = new AtomicReference<>(null);
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public boolean isBooked() {
        return booked.get();
    }

    public String getPassengerName() {
        return passengerName.get();
    }

    public boolean book(String passenger) {
        if (booked.compareAndSet(false, true)) {
            passengerName.set(passenger);
            return true;
        }
        return false;
    }

    public boolean cancel() {
        if (booked.compareAndSet(true, false)) {
            passengerName.set(null);
            return true;
        }
        return false;
    }
}
