package com.skylink;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class BookingManager {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();
    private final Queue<BookingRequest> bookingQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public void addFlight(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
    }

    public Flight searchFlight(String flightNumber) {
        return flights.get(flightNumber);
    }

    public List<Flight> searchFlights(String origin, String destination) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights.values()) {
            boolean originMatch = origin == null || origin.isEmpty() || flight.getOrigin().equalsIgnoreCase(origin);
            boolean destMatch = destination == null || destination.isEmpty() || flight.getDestination().equalsIgnoreCase(destination);
            if (originMatch && destMatch) {
                result.add(flight);
            }
        }
        return result.stream()
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());
    }

    public List<Flight> getAllFlights() {
        return flights.values().stream()
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());
    }

    public BookingResult requestBooking(String flightNumber, String seatNumber, String passengerName) {
        BookingRequest request = new BookingRequest(flightNumber, seatNumber, passengerName);
        bookingQueue.add(request);
        return processBooking(request);
    }

    private BookingResult processBooking(BookingRequest request) {
        Flight flight = flights.get(request.flightNumber());
        if (flight == null) {
            return new BookingResult(false, "Flight not found", null);
        }
        Seat seat = flight.getSeat(request.seatNumber());
        if (seat == null) {
            return new BookingResult(false, "Seat not found", null);
        }
        synchronized (seat) {
            if (seat.book(request.passengerName())) {
                Booking booking = new Booking(flight.getFlightNumber(), seat.getSeatNumber(), request.passengerName());
                bookings.put(booking.getBookingId(), booking);
                // persist booking
                Database.insertBooking(booking);
                return new BookingResult(true, "Booking successful", booking);
            } else {
                return new BookingResult(false, "Seat already booked", null);
            }
        }
    }

    public boolean cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.isCancelled()) {
            return false;
        }
        Flight flight = flights.get(booking.getFlightNumber());
        if (flight == null) {
            return false;
        }
        Seat seat = flight.getSeat(booking.getSeatNumber());
        if (seat == null) {
            return false;
        }
        synchronized (seat) {
            if (seat.cancel()) {
                bookings.remove(bookingId);
                // mark cancelled in DB
                Database.markCancelled(bookingId);
                return true;
            }
        }
        return false;
    }

    public void loadFromDatabase() {
        try {
            for (Booking b : Database.loadBookings()) {
                bookings.put(b.getBookingId(), b);
                Flight f = flights.get(b.getFlightNumber());
                if (f != null) {
                    Seat s = f.getSeat(b.getSeatNumber());
                    if (s != null) {
                        s.book(b.getPassengerName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    public List<Booking> getBookingsByPassenger(String passengerName) {
        return bookings.values().stream()
                .filter(b -> b.getPassengerName().equalsIgnoreCase(passengerName))
                .sorted(Comparator.comparing(Booking::getBookingDate).reversed())
                .collect(Collectors.toList());
    }

    public int getBookingQueueSize() {
        return bookingQueue.size();
    }

    public record BookingResult(boolean success, String message, Booking booking) {}
    private record BookingRequest(String flightNumber, String seatNumber, String passengerName) {}
}
