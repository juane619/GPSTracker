package com.juane.arduino.gpstracker.utils;

import android.content.res.Resources;

import com.juane.arduino.gpstracker.R;

public enum Errors {
    GENERIC_ERROR(0, R.string.error_authentication_failure),
     AUTHENTICATION_FAILURE(1, R.string.error_authentication_failure),
    CONNECTION_PROBLEM(2, R.string.error_connection_problem),
    GPS_DATA_NOT_FOUND(3, R.string.error_gps_data_not_found),
    PERMISSION_REQUIRED(4, R.string.error_permission_required_toast),
    BAD_URL(5, R.string.error_malformed_url),
    SEARCHING_GPS(6, R.string.info_searching_gps);;

    private final int code;
    private final int description;

    private Errors(int code, int descriptionId) {
        this.code = code;
        //Resources r = Resources.getSystem();
        this.description = descriptionId;
    }

    public int getDescription() {
        return description;
    }

    public String getCode() {
        return String.valueOf(code);
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
