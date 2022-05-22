package com.example.jmsrestapi.models;

import java.util.UUID;

public class Reservation {
    private String reservationId;
    private String version;
    // And other necessary fields...

    public Reservation() {
    }

    public Reservation(String reservationId, String version) {
        this.reservationId = reservationId;
        this.version = version;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId='" + reservationId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
