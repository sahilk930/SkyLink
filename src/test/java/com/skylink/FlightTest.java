package com.skylink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FlightTest {
    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = new Flight("SKY123", "NYC", "LAX", new Date(), 252);
    }

    @Test
    void testFlightInitialization() {
        assertEquals("SKY123", flight.getFlightNumber());
        assertEquals("NYC", flight.getOrigin());
        assertEquals("LAX", flight.getDestination());
        assertEquals(252, flight.getTotalSeatCount());
        assertEquals(252, flight.getAvailableSeatCount());
    }

    @Test
    void testGetSeat() {
        Seat seat = flight.getSeat("01A");
        assertNotNull(seat);
        assertEquals("01A", seat.getSeatNumber());
    }

    @Test
    void testGetNonExistentSeat() {
        Seat seat = flight.getSeat("99Z");
        assertNull(seat);
    }

    @Test
    void testAvailableSeats() {
        assertEquals(252, flight.getAvailableSeats().size());
        Seat seat = flight.getSeat("01A");
        seat.book("Test Passenger");
        assertEquals(251, flight.getAvailableSeatCount());
    }
}

