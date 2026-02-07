package com.skylink;

import java.util.*;
import java.util.concurrent.*;

public class SkyLinkApp {
    public static void main(String[] args) throws InterruptedException {
        BookingManager manager = new BookingManager();
        Flight flight = new Flight("SKY123", "NYC", "LAX", new Date(), 100);
        manager.addFlight(flight);

        int parallelBookings = 120;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<BookingManager.BookingResult>> results = new ArrayList<>();

        for (int i = 1; i <= parallelBookings; i++) {
            final String seatNum = String.format("%03d", (i % 100) + 1);
            final String passenger = "Passenger-" + i;
            results.add(executor.submit(() -> manager.requestBooking("SKY123", seatNum, passenger)));
        }

        int success = 0, fail = 0;
        for (Future<BookingManager.BookingResult> result : results) {
            try {
                if (result.get().success()) success++;
                else fail++;
            } catch (Exception e) {
                fail++;
            }
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("Total successful bookings: " + success);
        System.out.println("Total failed bookings: " + fail);
        System.out.println("Available seats after booking: " + flight.getAvailableSeats().size());
    }
}
