package com.juane.arduino.gpstracker.gps;

import androidx.annotation.NonNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GPSDirection {
    private boolean isValid = false;
    private double longitude;
    private double latitude;
    private LocalDateTime date;

    public GPSDirection(String rawCoordinates) {
        if (rawCoordinates != null && !rawCoordinates.isEmpty()) {
            parseRAWCoordinates(rawCoordinates);
        }
    }

    public double distanciaCoord(GPSDirection other) {
        //double hearthRadius = 3958.75; //in miles
        double hearthRadius = 6371; //radius of earth in Km
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLng = Math.toRadians(other.longitude - this.longitude);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));

        return hearthRadius * va2;
    }

    public boolean isEqual(GPSDirection other) {
        return this.date.isEqual(other.getDate()) &&
                Double.compare(this.latitude, other.latitude) == 0 &&
                Double.compare(this.longitude, other.longitude) == 0;
    }

    public boolean isValid() {
        return isValid;
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
        return "\nLongitude: " + longitude + "\nLatitude: " + latitude + "\nDate: " + date.toString();
    }

    private void parseRAWCoordinates(String RAWCoordinates) {
        String[] parsedArrayRAW = RAWCoordinates.split(",");

        if (parsedArrayRAW.length > 2) {
            this.latitude = Double.parseDouble(parsedArrayRAW[1]);
            this.longitude = -Double.parseDouble(parsedArrayRAW[0]);

            String dateRaw = parsedArrayRAW[2];
            this.date = parseDateRAW(dateRaw);

            this.isValid = true;
        }
    }

    private LocalDateTime parseDateRAW(String dateRaw) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateRaw, formatter);
    }


}
