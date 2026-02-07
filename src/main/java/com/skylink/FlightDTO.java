package com.skylink;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FlightDTO {
    private String flightNumber;
    private String origin;
    private String destination;
    private Date departureTime;
    private Map<String, SeatDTO> seats;
    private int availableSeatCount;
    private int totalSeatCount;

    public FlightDTO(Flight flight) {
        this.flightNumber = flight.getFlightNumber();
        this.origin = flight.getOrigin();
        this.destination = flight.getDestination();
        this.departureTime = flight.getDepartureTime();
        this.availableSeatCount = flight.getAvailableSeatCount();
        this.totalSeatCount = flight.getTotalSeatCount();
        
        this.seats = new HashMap<>();
        for (Map.Entry<String, Seat> entry : flight.getSeats().entrySet()) {
            double price = flight.getSeatPrice(entry.getKey());
            seats.put(entry.getKey(), new SeatDTO(entry.getValue(), price));
        }
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public Date getDepartureTime() { return departureTime; }
    public Map<String, SeatDTO> getSeats() { return seats; }
    public int getAvailableSeatCount() { return availableSeatCount; }
    public int getTotalSeatCount() { return totalSeatCount; }
}

