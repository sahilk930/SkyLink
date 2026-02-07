package com.skylink;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class SkyLinkServer {
    private static final BookingManager manager = new BookingManager();
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    public static void main(String[] args) {
        // Initialize database first (reads env vars: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS)
        try {
            Database.initFromEnv();
        } catch (Exception e) {
            System.err.println("Warning: could not initialize database: " + e.getMessage());
            e.printStackTrace();
        }

        initializeFlights();

        // load persisted bookings and apply to in-memory seats
        try {
            manager.loadFromDatabase();
        } catch (Exception e) {
            System.err.println("Warning: could not load bookings from database: " + e.getMessage());
            e.printStackTrace();
        }

        String port = System.getenv("PORT");
        if (port != null) {
            port(Integer.parseInt(port));
        } else {
            port(4567);
        }
        staticFiles.location("/public");

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        options("/*", (request, response) -> "OK");

        get("/api/flights", (req, res) -> {
            res.type("application/json");
            try {
                String origin = req.queryParams("origin");
                String destination = req.queryParams("destination");
                
                List<Flight> flights;
                if ((origin == null || origin.isEmpty()) && (destination == null || destination.isEmpty())) {
                    flights = manager.getAllFlights();
                } else {
                    flights = manager.searchFlights(origin, destination);
                }
                
                List<FlightDTO> flightDTOs = new ArrayList<>();
                for (Flight flight : flights) {
                    flightDTOs.add(new FlightDTO(flight));
                }
                return gson.toJson(flightDTOs);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to retrieve flights: " + e.getMessage()));
            }
        });

        get("/api/flight/:flightNumber", (req, res) -> {
            res.type("application/json");
            try {
                Flight f = manager.searchFlight(req.params("flightNumber"));
                if (f == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Flight not found"));
                }
                return gson.toJson(new FlightDTO(f));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to retrieve flight: " + e.getMessage()));
            }
        });

        post("/api/book", (req, res) -> {
            res.type("application/json");
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> data = gson.fromJson(req.body(), Map.class);
                String flightNumber = data.get("flightNumber");
                String seatNumber = data.get("seatNumber");
                String passengerName = data.get("passengerName");

                if (flightNumber == null || seatNumber == null || passengerName == null || 
                    passengerName.trim().isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of("success", false, "message", "Missing required fields"));
                }

                BookingManager.BookingResult result = manager.requestBooking(flightNumber, seatNumber, passengerName.trim());
                if (result.success()) {
                    return gson.toJson(Map.of(
                        "success", true,
                        "message", result.message(),
                        "bookingId", result.booking().getBookingId(),
                        "booking", result.booking()
                    ));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("success", false, "message", result.message()));
                }
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("success", false, "message", "Internal server error"));
            }
        });

        delete("/api/booking/:bookingId", (req, res) -> {
            res.type("application/json");
            String bookingId = req.params("bookingId");
            boolean cancelled = manager.cancelBooking(bookingId);
            if (cancelled) {
                return gson.toJson(Map.of("success", true, "message", "Booking cancelled successfully"));
            } else {
                res.status(404);
                return gson.toJson(Map.of("success", false, "message", "Booking not found or already cancelled"));
            }
        });

        get("/api/booking/:bookingId", (req, res) -> {
            res.type("application/json");
            Booking booking = manager.getBooking(req.params("bookingId"));
            if (booking == null) {
                res.status(404);
                return gson.toJson(Map.of("error", "Booking not found"));
            }
            return gson.toJson(booking);
        });

        get("/api/bookings", (req, res) -> {
            res.type("application/json");
            String passengerName = req.queryParams("passengerName");
            if (passengerName == null || passengerName.isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("error", "passengerName parameter required"));
            }
            return gson.toJson(manager.getBookingsByPassenger(passengerName));
        });

        get("/api/health", (req, res) -> {
            res.type("application/json");
            return gson.toJson(Map.of(
                "status", "healthy", 
                "timestamp", new Date(),
                "uptime", System.currentTimeMillis(),
                "activeFlights", manager.getAllFlights().size()
            ));
        });

        get("/api/keepalive", (req, res) -> {
            res.type("application/json");
            return gson.toJson(Map.of("status", "alive", "timestamp", new Date()));
        });
    }

    private static void initializeFlights() {
        String[] popularRoutes = {
            "DEL-MUM", "MUM-DEL", "DEL-BLR", "BLR-DEL",
            "DEL-HYD", "HYD-DEL", "MUM-BLR", "BLR-MUM",
            "DEL-CCU", "CCU-DEL", "MUM-HYD", "HYD-MUM",
            "DEL-JAI", "JAI-DEL", "BLR-CCU", "CCU-BLR"
        };
        Random rand = new Random();
        Calendar cal = Calendar.getInstance();
        int flightCounter = 100;
        
        for (int month = 0; month < 3; month++) {
            for (int week = 0; week < 4; week++) {
                for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                    int dayOffset = month * 30 + week * 7 + dayOfWeek;
                    
                    for (int hour = 6; hour < 22; hour += 3) {
                        String route = popularRoutes[rand.nextInt(popularRoutes.length)];
                        String[] parts = route.split("-");
                        String from = parts[0];
                        String to = parts[1];
                        
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.add(Calendar.DAY_OF_MONTH, dayOffset);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, rand.nextInt(4) * 15);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        
                        String flightNum = "SKY" + flightCounter++;
                        Flight f = new Flight(flightNum, from, to, cal.getTime(), 252);
                        manager.addFlight(f);
                    }
                }
            }
        }
        
        String skipDummy = System.getenv("SKIP_DUMMY");
        if (skipDummy == null || !skipDummy.equalsIgnoreCase("true")) {
            createDummyBookings(rand);
        } else {
            System.out.println("SKIP_DUMMY=true — skipping creation of large dummy bookings");
        }
    }
    
    private static void createDummyBookings(Random rand) {
        String[] dummyNames = {
            "Rajesh Kumar", "Priya Sharma", "Amit Patel", "Anjali Singh",
            "Rahul Verma", "Kavita Reddy", "Vikram Mehta", "Swati Desai",
            "Arjun Iyer", "Neha Gupta", "Suresh Nair", "Deepika Joshi",
            "Ravi Malhotra", "Sunita Rao", "Mohan Kapoor", "Pooja Agarwal",
            "Kiran Shah", "Nikhil Trivedi", "Divya Bhatia", "Ashish Dubey"
        };
        
        List<Flight> allFlights = manager.getAllFlights();
        int totalFlights = allFlights.size();
        
        for (int i = 0; i < totalFlights; i++) {
            Flight flight = allFlights.get(i);
            int bookingsToCreate;
            
            if (i < totalFlights * 0.1) {
                bookingsToCreate = 252;
            } else if (i < totalFlights * 0.3) {
                bookingsToCreate = 200 + rand.nextInt(52);
            } else if (i < totalFlights * 0.6) {
                bookingsToCreate = 100 + rand.nextInt(100);
            } else {
                bookingsToCreate = rand.nextInt(50);
            }
            
            List<String> availableSeats = new ArrayList<>(flight.getSeats().keySet());
            Collections.shuffle(availableSeats, rand);
            
            for (int j = 0; j < bookingsToCreate && j < availableSeats.size(); j++) {
                String seatNum = availableSeats.get(j);
                String passengerName = dummyNames[rand.nextInt(dummyNames.length)];
                manager.requestBooking(flight.getFlightNumber(), seatNum, passengerName);
            }
        }
    }
}
