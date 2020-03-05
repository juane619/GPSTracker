package com.juane.arduino.gpstracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.juane.arduino.gpstracker.MainActivity;
import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.gps.GPSDirection;
import com.juane.arduino.gpstracker.utils.HttpUtils;
import com.juane.arduino.gpstracker.utils.URLConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Objects;

public class RequestService extends Service {

    private static final String TAG = "Request Service";
    private static boolean isRunning = false;
    private int TIME_SLEEP_SECONDS = 5;
    private int TIME_SLEEP_MILISECONDS = TIME_SLEEP_SECONDS * 1000;
    private int failCounter = 0;

    private ServiceHandler serviceHandler; //communicate with main thread

    // Requests
    //private FileOutputStream fo = null;
    //private File fileOS = null;
    //private File fileOSAux = null;
    // manage GPS directions
    private GPSDirection lastDirection = null;
    private String lineAux = null;

    private Messenger mMessenger = null; // Target we publish for clients to send messages to IncomingHandler.
    private Messenger mClient;

    public RequestService() {
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // Get the HandlerThread's Looper and use it for our Handler
        // Thread
        //loop over tasks
        Looper serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        mMessenger = new Messenger(serviceHandler); // Target we publish for clients to send messages to IncomingHandler.

        Log.i(TAG, "Request Service created..");
    }

    @Override
    public IBinder onBind(Intent intent) {
        setNotification();

        TIME_SLEEP_SECONDS = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("interval_time", "5"));
        TIME_SLEEP_MILISECONDS = TIME_SLEEP_SECONDS * 1000;
        Log.i(TAG, "SECONDS in service: " + TIME_SLEEP_SECONDS);

        //once URL is assigned, and files to store gps data are created  start service
//        String FILE_NAME = this.getResources().getString(R.string.path_main_filegps);
//        String FILE_NAME_AUX = this.getResources().getString((R.string.path_auxfilegps));
//
//        if (getExternalFilesDir(null) != null) {
//            fileOS = new File(Objects.requireNonNull(getExternalFilesDir(null)).getPath() + File.separator + FILE_NAME);
//            fileOSAux = new File(Objects.requireNonNull(getExternalFilesDir(null)).getPath() + File.separator + FILE_NAME_AUX);
//        }

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job

        //When previous resources are avalaible, service start
        Toast.makeText(this, "Request Service starting..", Toast.LENGTH_SHORT).show();

        isRunning = true;

        // TODO: Return the communication channel to the service.
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Request service stopped..", Toast.LENGTH_SHORT).show();
        isRunning = false;

//        try {
//            if (fo != null) {
//                fo.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        super.onDestroy();
    }

    private void setNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String NOTIFICATION_CHANNEL_ID = "com.juane.arduino.gpstracker";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.googleg_standard_color_18)
                .setContentTitle("MyGPSTracker is running")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            //Log.i(TAG, "RECIBIENDO MENSAJE DEL FRAGMENT..: " + msg.arg2);

            switch(msg.what){
                case MessageType.REGISTER_CLIENT:
                    Log.i(TAG, "RECEIVING REGISTER MSG..");
                    mClient = msg.replyTo;

                    Message msgAux = serviceHandler.obtainMessage();
                    msgAux.what = MessageType.START_REQUEST;
                    msgAux.obj = msg.obj; // date from client (homeFragment)

                    if (serviceHandler.sendMessage(msgAux)) {
                        Log.i(TAG, "SENDING request to obtain gps data..");
                    }
                    break;
                case MessageType.START_REQUEST:
                    Log.i(TAG, "RECEIVING STARTING MSG..");

                    String dateSelected = (String) msg.obj;
                    Log.i(TAG, "Date selected: " + dateSelected);

                    while (isRunning) {
                        try {
                            String urlStr = URLConstants.URL_GPS_DIRECTORY + File.separator + URLConstants.URL_READ_GPS_ENDPOINT;

                            if(dateSelected != null){
                                urlStr = urlStr + "?" + URLConstants.DATE_PARAMETER + "=" + dateSelected;
                            }

                            URL url = new URL(urlStr);

                            // LOGIC of request GPS service
                            if (lastDirection == null || !lastDirection.isValid()) { //first time read: read main file
                                Log.i(TAG, "FIRST TIME switch: ");

                                StringBuffer response = HttpUtils.doGet(url, true, "juane619", "Mygpstracker1!");

                                if (response != null) {
                                    Log.i(TAG, "Content from server: " + response.length());

                                    // DATA read -> parse it to JSON
                                    JSONObject lastGpsDataRead = new JSONObject(response.toString());
                                    JSONArray addressesJSON = lastGpsDataRead.getJSONArray("addresses");

                                    JSONObject lastDirectionRAW = addressesJSON.getJSONObject(addressesJSON.length() - 1);
                                    lastDirection = new GPSDirection(lastDirectionRAW, getApplicationContext());

                                    if (lastDirection.isValid()) {
                                        //Log.i(TAG, "Direction: " + lastDirection.toString());
                                        try {
                                            if (mClient != null) {
                                                // sending msg to client (homeFragment)
                                                mClient.send(Message.obtain(null, MessageType.SENDING_LOCATION, MessageType.FIRST_TIME_SWITCH, -1, addressesJSON));
                                            }
                                        } catch (RemoteException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Problem reading data from gps server..");

                                    failCounter++;

                                    if(failCounter > 5){
                                        failCounter = 0;

                                        if (mClient != null) {
                                            mClient.send(Message.obtain(null, MessageType.PROBLEM_STOP));
                                        }
                                    }
                                }
                            } else { //next reads
                                Log.i(TAG, "SECOND OR MORE times switch: ");
                                StringBuffer response = HttpUtils.doGet(url, true, "juane619", "Mygpstracker1!");

                                if (response != null) {
                                    Log.i(TAG, "Content from server: " + response.length());

                                    JSONObject lastGpsDataRead = new JSONObject(response.toString());
                                    JSONArray addressesJSON = lastGpsDataRead.getJSONArray("addresses");

                                    JSONObject lastDirectionRAW = addressesJSON.getJSONObject(addressesJSON.length() - 1);
                                    GPSDirection auxDirection = new GPSDirection(lastDirectionRAW, getApplicationContext());

                                    if (auxDirection.isValid()) {
                                        // two locations already read, compare and work.
                                        if (!lastDirection.isEqual(auxDirection)) {
                                            Log.i(TAG, "MOVING!!!!");

                                            try {
                                                if (mClient != null) {
                                                    mClient.send(Message.obtain(null, MessageType.SENDING_LOCATION, auxDirection));
                                                }
                                            } catch (RemoteException ex) {
                                                ex.printStackTrace();
                                            }

                                            lastDirection = auxDirection;
                                        } else {
                                            Log.i(TAG, "Directions equal..");
                                        }

                                    } else {
                                        Log.w(TAG, "Aux Direction not valid..");
                                    }
                                } else {
                                    Log.w(TAG, "Problem reading second one..");

                                    if (mClient != null) {
                                        mClient.send(Message.obtain(null, MessageType.PROBLEM_STOP));
                                    }
                                }
                            }

                            Thread.sleep(TIME_SLEEP_MILISECONDS);
                        } catch (InterruptedException e) {
                            // Restore interrupt status.
                            Thread.currentThread().interrupt();
                        } catch (IOException e) {
                            Log.e(TAG, "IO Exception server request?.." + Objects.requireNonNull(e.getLocalizedMessage()));
                            try {
                                mClient.send(Message.obtain(null, MessageType.PROBLEM_STOP));
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                            //e.printStackTrace();
                        } catch (JSONException e) {
                            Log.e(TAG, "ERROR parsing data to JSON format..");
                            //e.printStackTrace();
                        } catch (RemoteException e) {
                            Log.e(TAG, "ERROR sending msg to client..");
                            e.printStackTrace();
                        }
                    }
                    break;

                case MessageType.UNREGISTER_CLIENT:
                    Log.i(TAG, "Unregistering client..");
                    mClient = null;
                default:
                    Log.w(TAG, "UNHANDLED MESSAGE RECEIVED..");
            }
        }
    }
}
