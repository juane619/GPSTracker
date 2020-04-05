package com.juane.arduino.gpstracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.fragment.app.FragmentActivity;

import com.juane.arduino.gpstracker.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static void showInvalidParameterDialog(FragmentActivity fragmentActivity, String parameter){
        String message;

        if(parameter == null){
            message = "This field can not be empty";
        }else if(parameter.equals("some_invalid")){
            message = "Some parameter is invalid. Go to settings tab.";
        }
        else{
            message = "Invalid entered " + parameter + " parameter";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);

        builder.setMessage(message)
                .setTitle(R.string.dialog_title_validate_parameter);
        builder.setPositiveButton(R.string.button_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public static void showServiceIsRunningDialog(FragmentActivity fragmentActivity, String parameter){
        String message = "Preferences are disabled while service is enabled";

        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);

        builder.setMessage(message)
                .setTitle(R.string.dialog_title_service_running);
        builder.setPositiveButton(R.string.button_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public static boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static String formatReadDate(Context context, LocalDateTime date){
        String pattern = context.getResources().getString(R.string.date_read_format);
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }

    public static String formatWriteDate(Context context, LocalDateTime date){
        String pattern = context.getResources().getString(R.string.date_write_format);
        return DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss a").format(date);
    }

}
