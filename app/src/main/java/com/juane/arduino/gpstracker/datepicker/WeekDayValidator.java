package com.juane.arduino.gpstracker.datepicker;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

public class WeekDayValidator implements CalendarConstraints.DateValidator {
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    JSONArray dates = null;
    public Parcelable.Creator<WeekDayValidator> CREATOR = new Parcelable.Creator<WeekDayValidator>() {

        @Override
        public WeekDayValidator createFromParcel(Parcel source) {
            return new WeekDayValidator(dates);
        }

        @Override
        public WeekDayValidator[] newArray(int size) {
            return new WeekDayValidator[0];
        }
    };

    public WeekDayValidator(){
    }

    public WeekDayValidator(JSONArray dates){
        this.dates = dates;
    }

    @Override
    public boolean isValid(long date) {
        for (int i = 0; i < dates.length(); i++) {
            try {
                LocalDate localDate = LocalDate.parse(dates.getString(i), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                long valid = localDate.atStartOfDay(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli();
                if(valid == date)
                    return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
