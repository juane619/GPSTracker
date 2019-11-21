package com.juane.arduino.gpstracker.gps;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GPSDirection {
    private double longitude;
    private double latitude;
    private LocalDateTime date;

    public GPSDirection(String rawCoordinates){
        parseRAWCoordinates(rawCoordinates);
    }

    public double distanciaCoord(GPSDirection other) {
        //double radioTierra = 3958.75;//en millas
        double radioTierra = 6371;//en kilómetros
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLng = Math.toRadians(other.longitude - this.longitude);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = radioTierra * va2;

        return distancia;
    }

    private void parseRAWCoordinates(String RAWCoordinates){
        String[] parsedArrayRAW = RAWCoordinates.split(",");

        if(parsedArrayRAW.length > 0) {
            this.latitude = Double.parseDouble(parsedArrayRAW[0]);
            this.longitude = Double.parseDouble(parsedArrayRAW[1]);

            String dateRaw = parsedArrayRAW[2];
            this.date = parseDateRAW(dateRaw);
        }
    }

    private LocalDateTime parseDateRAW(String dateRaw) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime dateTime = LocalDateTime.parse(dateRaw, formatter);

        return dateTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @NonNull
    @Override
    public String toString() {
        return "Longitude: " + longitude + "\nLatitude: " + latitude + "\nDate: " + date.toString();
    }
}
