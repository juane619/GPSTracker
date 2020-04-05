package com.juane.arduino.gpstracker.ui.home;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.juane.arduino.gpstracker.MainActivity;
import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.gps.GPSDirection;
import com.juane.arduino.gpstracker.service.MessageType;
import com.juane.arduino.gpstracker.service.RequestGpsDates;
import com.juane.arduino.gpstracker.service.RequestService;
import com.juane.arduino.gpstracker.service.telegram.TelegramBot;
import com.juane.arduino.gpstracker.ui.map.MapFragment;
import com.juane.arduino.gpstracker.ui.settings.SettingsFragment;
import com.juane.arduino.gpstracker.utils.Errors;
import com.juane.arduino.gpstracker.utils.ToastUtils;
import com.juane.arduino.gpstracker.utils.URLConstants;
import com.juane.arduino.gpstracker.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private MainActivity mainActivity;
    private String dateSelected;

    private MapFragment mapFragment;
    private Switch enableSwitch;
    private Switch soundNotificationsSwitch;
    private Switch telegramNotificationsSwitch;
    private Button showLocationButton;
    private Button reloadButton;
    MaterialDatePicker.Builder<Long> builder;
    private TextView updatedText;

    // Request service attributes
    private boolean mIsBound;
    private Intent intentRequestService;
    private Messenger mService = null;
    private Ringtone ringtoneNotification;
    // callbacks when bind with request gps service
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //Toast.makeText(getActivity(), "Attached..", Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, MessageType.REGISTER_CLIENT);
                msg.replyTo = mMessenger;

                // From switch, pass today parsed date
                dateSelected = Utils.formatReadDate(getContext(), LocalDateTime.now());

                msg.obj = dateSelected;

                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            ToastUtils.ToastShort(mainActivity, R.string.info_service_disconnected);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        mapFragment = mainActivity.getMapFragment();

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        enableSwitch = root.findViewById(R.id.alarmSwitch);
        soundNotificationsSwitch = root.findViewById(R.id.soundNotifications);
        telegramNotificationsSwitch = root.findViewById(R.id.telegramNotifications);
        showLocationButton = root.findViewById(R.id.showLocationButton);
        reloadButton = root.findViewById(R.id.reload_button);
        updatedText = root.findViewById(R.id.updatedCalendarText);

        setEnableSwitch();
        setSoundNotificationsSwitch();
        setTelegramNotificationsSwitch();
        setShowLocationButton();
        setReloadButton();
        setDatePicker();

        intentRequestService = new Intent(getActivity(), RequestService.class);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Fragment resumed..");

        if (!SettingsFragment.isSettingsValidated()) {
            enableSwitch.setEnabled(false);
            soundNotificationsSwitch.setEnabled(false);
            showLocationButton.setEnabled(false);

            Utils.showInvalidParameterDialog(getActivity(), "some_invalid");
        } else {
            enableSwitch.setEnabled(true);
            //soundNotificationsSwitch.setEnabled(true);
            //showLocationButton.setEnabled(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }

    private void setEnableSwitch() {
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (enableSwitch.isChecked()) {
                    Log.i(TAG, "Switch enable ON");

                    showLocationButton.setEnabled(false);

                    if (soundNotificationsSwitch.isChecked()) {
                        //sound notification
                        String ringtonePreference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_soundNotification), "LOUD_SOUND");

                        switch (ringtonePreference) {
                            case ("LOUD_SOUND"):
                                ringtoneNotification = RingtoneManager.getRingtone(getContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                                // play alarm even in silent mode
                                ringtoneNotification.setAudioAttributes(
                                        new AudioAttributes
                                                .Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                .build());
                                break;
                            default:
                                ringtoneNotification = RingtoneManager.getRingtone(getContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        }
                    }

                    if (!RequestService.isRunning()) {
                        if (doBindService()) { //bind service to fragment
//                            SettingsFragment s = (SettingsFragment) getFragmentManager().findFragmentById(R.id.)
//                            EditTextPreference serverNamePref = (EditTextPreference) PreferenceManager.getDefaultSharedPreferences(getContext())..getString(R.string.key_url));

                            if (mapFragment != null)
                                mapFragment.clearMarkers();
                        }
                    }
                } else {
                    Log.i(TAG, "Switch alarm OFF");

                    showLocationButton.setEnabled(true);

                    if (RequestService.isRunning()) {
                        doUnbindService();
                    }

                    if (ringtoneNotification != null && ringtoneNotification.isPlaying())
                        ringtoneNotification.stop();
                }
            }
        });
    }

    //// Setting behaviour of root view components
    private void setSoundNotificationsSwitch() {
        soundNotificationsSwitch.setChecked(true);

        soundNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (soundNotificationsSwitch.isChecked()) {
                    Log.i(TAG, "Real Time ON");
                } else {
                    Log.i(TAG, "Real Time OFF");
                }
            }
        });
    }

    private void setTelegramNotificationsSwitch() {
        telegramNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (telegramNotificationsSwitch.isChecked()) {
                    Log.i(TAG, "Telegram ON");
                } else {
                    Log.i(TAG, "Telegram OFF");
                }
            }
        });
    }

    private void setReloadButton(){
        reloadButton.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                //updateValidDates();
                updateCalendar();
            }
        });
    }

    private void setShowLocationButton() {
        showLocationButton.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                MaterialDatePicker<Long> mat = builder.setSelection(LocalDateTime.now().atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli()).build();
                mat.show(getFragmentManager(), mat.toString());
            }
        });

       /* showLocationButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                mapFragment.clearMarkers();

                if (dateSelected != null) {
                    String server = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_url), "agrocarvajal.com");
                    String urlStr = server + File.separator + URLConstants.URL_GPS_DIRECTORY + File.separator + URLConstants.URL_READ_GPS_ENDPOINT;
                    urlStr = urlStr + "?" + URLConstants.DATE_PARAMETER + "=" + dateSelected;

                    String user = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_user), "juane619");
                    String passwd = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_password), "");

                    new RequestGps(mainActivity, mapFragment).execute(urlStr, String.valueOf(true), user, passwd);
                    mapFragment.setSelectedDayTextView(dateSelected);

                }
            }
        });*/
    }

    private void setUpdatedText(String dateFormatted) {
        updatedText.setText(getResources().getString(R.string.UI_updated_text) + " " + dateFormatted);
    }

    private void setDatePicker() {
        LocalDateTime localDateNow = LocalDateTime.now();
        String dateFormatted = Utils.formatWriteDate(getContext(), localDateNow);
        setUpdatedText(dateFormatted);
        dateSelected = Utils.formatReadDate(getContext(), localDateNow);

        builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Days with GPS data");
    }

    /*private void setCalendarView() {
        dateSelected = Utils.formatDate(getContext(), date);

        mat.set

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                dateSelected = Utils.formatDate(view.getContext(), date);
            }
        });
    }*/

    //// END Setting behaviour of root view components

    private void updateCalendar() {
        LocalDateTime localDateNow = LocalDateTime.now();
        String dateFormatted = Utils.formatWriteDate(getContext(), localDateNow);
        setUpdatedText(dateFormatted);

        updateValidDates();
    }

    private void updateValidDates() {
        String server = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_url), "agrocarvajal.com");
        String urlStr = server + File.separator + URLConstants.URL_GPS_DIRECTORY + File.separator + URLConstants.URL_READ_GPS_DATES_ENDPOINT;

        String user = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_user), "juane619");
        String passwd = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_password), "");

        new RequestGpsDates(mainActivity, this).execute(urlStr, String.valueOf(true), user, passwd);
    }

    public void updateUICalendar(JSONArray dates){
        for (int i = 0; i < dates.length(); i++) {

        }
    }

    private boolean doBindService() {
        if (!mIsBound) {
            mIsBound = Objects.requireNonNull(getActivity()).bindService(intentRequestService, mConnection, Context.BIND_AUTO_CREATE); //previous ServiceConnection to detect the service activity
            Log.i(TAG, "Binding with request gps service..");
        }

        return mIsBound;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MessageType.UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;

                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }

            // Detach our existing connection.
            Objects.requireNonNull(getActivity()).unbindService(mConnection);
            mIsBound = false;
            Log.i(TAG, "UnBinding from request gps service..");
        }
    }

    /* Class to handle the messages received from gps request Service*/
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.i(TAG, "RECIBIENDO MENSAJE DEL SERVICE..");
            super.handleMessage(msg);

            switch (msg.what) {
                case MessageType.PROBLEM_STOP:
                    Log.i(TAG, "REQUEST TO STOP..");
                    enableSwitch.setChecked(false);
                case MessageType.SHOW_TOAST:
                    String error = (msg.obj instanceof String) ? (String) msg.obj : null;

                    if (error.equals(Errors.AUTHENTICATION_FAILURE.getCode())) {
                        ToastUtils.ToastShort(mainActivity, R.string.error_authentication_failure);
                    } else if (error.equals(Errors.CONNECTION_PROBLEM.getCode())) {
                        ToastUtils.ToastShort(mainActivity, R.string.error_generic);
                    } else if (error.equals(Errors.GPS_DATA_NOT_FOUND.getCode())) {
                        ToastUtils.ToastShort(mainActivity, R.string.error_gps_data_not_found);
                    } else if (error.equals(Errors.BAD_URL.getCode())) {
                        ToastUtils.ToastShort(mainActivity, R.string.error_malformed_url);
                    } else if (error.equals(Errors.SEARCHING_GPS.getCode())) {
                        ToastUtils.ToastShort(mainActivity, R.string.info_searching_gps);
                    }
                    break;
                case MessageType.START_REQUEST:
                    Log.i(TAG, "REQUEST TO START..");

                    if (mService != null) {
                        try {
                            Message msgAux = Message.obtain(null, MessageType.START_REQUEST, "Start request to gps server");
                            msgAux.replyTo = mMessenger;
                            mService.send(msgAux);
                        } catch (RemoteException e) {
                            // There is nothing special we need to do if the service has crashed.
                        }
                    }
                    break;
                case MessageType.SENDING_LOCATION:
                    Log.i(TAG, "RECEIVING LOCATION..");
                    JSONArray addressesJSON;
                    GPSDirection gpsRead = null;

                    if (msg.arg1 == MessageType.FIRST_TIME_SWITCH) {
                        // FIRST time switch: receive complete JSON locations to print all previous locations
                        Log.i(TAG, "FIRST TIME SWITCH..");

                        addressesJSON = msg.obj instanceof JSONArray ? (JSONArray) msg.obj : null;

                        if (addressesJSON != null) {
                            try {
                                mapFragment.clearMarkers();
                                mapFragment.addMarkers(addressesJSON);
                                mapFragment.setSelectedDayTextView(dateSelected);
                                mainActivity.changeTab(R.id.mapTabId);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing JSON addresses");
                            }
                        }
                    } else {
                        // SECOND OR MORE time switch: receive only last device location
                        Log.i(TAG, "SECOND OF MORE SWITCH..");

                        gpsRead = msg.obj instanceof GPSDirection ? (GPSDirection) msg.obj : null;
                        Log.i(TAG, "New location: " + gpsRead.toString());

                        //update map
                        if (mapFragment != null) {
                            mapFragment.addMarker(gpsRead);
                            mainActivity.changeTab(R.id.mapTabId);
                        }
                    }

                    // send telegram msg
                    if (telegramNotificationsSwitch.isChecked()) {
                        String chatId = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_chatid), "chat_id");
                        String message = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(getResources().getString(R.string.key_message), "message_text");

                        new TelegramBot(getActivity().getString(R.string.telegram_bot_key)).execute(chatId, message + ":\n" + gpsRead.toString());
                    }

                    // add sound notification when location arrives
                    try {
                        if (soundNotificationsSwitch.isChecked())
                            ringtoneNotification.play();
                    } catch (Exception e) {
                        Log.e(TAG, "Problem playing sound notification");
                    }

                    break;

                default:
                    Log.w(TAG, "UNHANDLED MESSAGE RECEIVED..");
            }
        }
    }
}