package com.juane.arduino.gpstracker.gps;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class GPSDirection {
    private static double CONSIDERATE_DISTANCE;

    private boolean isValid = false;
    private double longitude;
    private double latitude;
    private LocalDateTime date;

    public GPSDirection(String rawCoordinates, Context ctx) {
        if (rawCoordinates != null && !rawCoordinates.isEmpty()) {
            parseRAWCoordinates(rawCoordinates);
        }

        GPSDirection.CONSIDERATE_DISTANCE = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(ctx).getString("distance_value", "0.15"));
    }

    private double distanceCoord(GPSDirection other) {
        //double hearthRadius = 3958.75; //in miles
        double hearthRadius = 6371; //radius of earth in Km
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLng = Math.toRadians(other.longitude - this.longitude);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));

        return Math.abs(hearthRadius * va2);
    }

    public boolean isEqual(GPSDirection other) {
        return false;
//        double distance = this.distanceCoord(other);
//        boolean distanceEqual = Double.compare(distance, CONSIDERATE_DISTANCE) < 0;
//        Log.i("GPS DIRECTION", "Distance: " + distance);
//
//        return this.date.isEqual(other.getDate()) || distanceEqual;
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

    private LocalDateTime getDate() {
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
            this.latitude = Double.parseDouble(parsedArrayRAW[0]);
            this.longitude = Double.parseDouble(parsedArrayRAW[1]);

            String dateRaw = parsedArrayRAW[2];

            try {
                this.date = parseDateRAW(dateRaw);
                this.isValid = true;
            }catch(DateTimeParseException e){
                this.isValid = false;
            }
        }
    }

    private LocalDateTime parseDateRAW(String dateRaw) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        return LocalDateTime.parse(dateRaw, formatter);
    }


}
