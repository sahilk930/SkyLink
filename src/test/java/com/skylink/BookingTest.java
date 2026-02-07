package com.skylink;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class BookingTest {
    @Test
    void testBookingCreation() {
        Booking booking = new Booking("SKY123", "001", "John Doe");
        
        assertNotNull(booking.getBookingId());
        assertEquals(8, booking.getBookingId().length());
        assertEquals("SKY123", booking.getFlightNumber());
        assertEquals("001", booking.getSeatNumber());
        assertEquals("John Doe", booking.getPassengerName());
        assertFalse(booking.isCancelled());
        assertNotNull(booking.getBookingDate());
    }

    @Test
    void testBookingWithAllFields() {
        Date date = new Date();
        Booking booking = new Booking("BOOK123", "SKY456", "002", "Jane Doe", date, false);
        
        assertEquals("BOOK123", booking.getBookingId());
        assertEquals("SKY456", booking.getFlightNumber());
        assertEquals("002", booking.getSeatNumber());
        assertEquals("Jane Doe", booking.getPassengerName());
        assertEquals(date, booking.getBookingDate());
        assertFalse(booking.isCancelled());
    }
}

