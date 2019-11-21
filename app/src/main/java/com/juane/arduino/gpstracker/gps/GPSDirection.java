package com.juane.arduino.gpstracker.gps;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GPSDirection {
    private boolean isValid = false;
    private double longitude;
    private double latitude;
    private LocalDateTime date;

    public GPSDirection(String rawCoordinates){
        if(rawCoordinates != null && !rawCoordinates.isEmpty()) {
            parseRAWCoordinates(rawCoordinates);
        }
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

    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    public boolean isEqual(GPSDirection other){
        return this.date.isEqual(other.getDate());
    }

    public boolean isValid(){
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

    private void parseRAWCoordinates(String RAWCoordinates){
        String[] parsedArrayRAW = RAWCoordinates.split(",");

        if(parsedArrayRAW.length > 2) {
            this.latitude = Double.parseDouble(parsedArrayRAW[1]);
            this.longitude = -Double.parseDouble(parsedArrayRAW[0]);

            String dateRaw = parsedArrayRAW[2];
            this.date = parseDateRAW(dateRaw);

            this.isValid = true;
        }
    }

    private LocalDateTime parseDateRAW(String dateRaw) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime dateTime = LocalDateTime.parse(dateRaw, formatter);

        return dateTime;
    }


}
