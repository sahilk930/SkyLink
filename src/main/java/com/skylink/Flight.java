package com.skylink;

import java.util.*;
import java.util.concurrent.*;

public class Flight {
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final Date departureTime;
    private final Map<String, Seat> seats;

    public Flight(String flightNumber, String origin, String destination, Date departureTime, int seatCount) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.seats = new ConcurrentHashMap<>();
        
        int businessRows = 3;
        int economyPlusRows = 6;
        int economyRows = (seatCount - (businessRows * 6) - (economyPlusRows * 9)) / 9;
        
        int rowNum = 1;
        
        char[] businessSeats = {'A', 'B', 'C', 'D', 'E', 'F'};
        for (int r = 0; r < businessRows; r++) {
            for (char letter : businessSeats) {
                String seatNum = String.format("%02d%c", rowNum, letter);
                seats.put(seatNum, new Seat(seatNum));
            }
            rowNum++;
        }
        
        char[] economySeats = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J'};
        for (int r = 0; r < economyPlusRows; r++) {
            for (char letter : economySeats) {
                String seatNum = String.format("%02d%c", rowNum, letter);
                seats.put(seatNum, new Seat(seatNum));
            }
            rowNum++;
        }
        
        for (int r = 0; r < economyRows; r++) {
            for (char letter : economySeats) {
                String seatNum = String.format("%02d%c", rowNum, letter);
                seats.put(seatNum, new Seat(seatNum));
            }
            rowNum++;
        }
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public Date getDepartureTime() { return departureTime; }
    public Map<String, Seat> getSeats() { return seats; }

    public Seat getSeat(String seatNumber) {
        return seats.get(seatNumber);
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (!seat.isBooked()) {
                available.add(seat);
            }
        }
        return available;
    }

    public int getAvailableSeatCount() {
        return getAvailableSeats().size();
    }

    public int getTotalSeatCount() {
        return seats.size();
    }

    public double getSeatPrice(String seatNumber) {
        Seat seat = seats.get(seatNumber);
        if (seat == null) return 0.0;
        
        int row = Integer.parseInt(seatNumber.substring(0, 2));
        char letter = seatNumber.charAt(2);
        
        double basePrice = 4500.0;
        
        if (row >= 1 && row <= 3) {
            basePrice = 22000.0;
            if (letter == 'A' || letter == 'F') {
                basePrice += 1500.0;
            }
        } else if (row >= 4 && row <= 9) {
            basePrice = 9500.0;
            if (letter == 'A' || letter == 'J') {
                basePrice += 800.0;
            }
        } else {
            basePrice = 4500.0;
            if (letter == 'A' || letter == 'J') {
                basePrice += 500.0;
            }
        }
        
        long daysUntilDeparture = (departureTime.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        if (daysUntilDeparture < 7) {
            basePrice *= 1.4;
        } else if (daysUntilDeparture < 14) {
            basePrice *= 1.25;
        } else if (daysUntilDeparture < 30) {
            basePrice *= 1.1;
        }
        
        return Math.round(basePrice * 100.0) / 100.0;
    }
}
