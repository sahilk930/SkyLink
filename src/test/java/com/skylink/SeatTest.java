package com.skylink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class SeatTest {
    private Seat seat;

    @BeforeEach
    void setUp() {
        seat = new Seat("001");
    }

    @Test
    void testSeatInitialization() {
        assertFalse(seat.isBooked());
        assertNull(seat.getPassengerName());
        assertEquals("001", seat.getSeatNumber());
    }

    @Test
    void testBookSeat() {
        assertTrue(seat.book("John Doe"));
        assertTrue(seat.isBooked());
        assertEquals("John Doe", seat.getPassengerName());
    }

    @Test
    void testDoubleBooking() {
        assertTrue(seat.book("John Doe"));
        assertFalse(seat.book("Jane Doe"));
        assertEquals("John Doe", seat.getPassengerName());
    }

    @Test
    void testCancelBooking() {
        seat.book("John Doe");
        assertTrue(seat.cancel());
        assertFalse(seat.isBooked());
        assertNull(seat.getPassengerName());
    }

    @Test
    void testCancelUnbookedSeat() {
        assertFalse(seat.cancel());
    }
}

