package com.juane.arduino.gpstracker.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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
import androidx.preference.PreferenceManager;

import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.gps.GPSDirection;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class RequestService extends Service {
    public static final int MSG_PROBLEM_STARTING = 1;
    private static final String TAG = "Request Service";

    private int TIME_SLEEP_SECONDS = 5;
    private int TIME_SLEEP_MILISECONDS = TIME_SLEEP_SECONDS * 1000;

    private ServiceHandler serviceHandler; //communicate with main thread

    private static boolean isRunning = false;

    // Requests
    private String SOURCE_URL = null;
    private URL url;
    private FileOutputStream fo = null;
    private File fileOS = null;
    private File fileOSAux = null;

    // manage GPS directions
    private GPSDirection lastDirection = null;
    private String lineAux = null;

    private NotificationManager nm;
    private Messenger mMessenger = null; // Target we publish for clients to send messages to IncomingHandler.
    private Messenger mClient;

    public RequestService() {
    }

    public static boolean isRunning() {
        return isRunning;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.arg2 != 999) {
                Log.i(TAG, "RECIBIENDO MENSAJE DEL INTENT PARA REGISTRAR..: " + msg.arg2);
                mClient = msg.replyTo;
            }else{
                double considerateDistance = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getFloat("distance_value", (float) 0.15);

                Log.i(TAG, "DISTANCE: " + considerateDistance);

                while (isRunning) {
                    try {
                        if (fileOS.length() == 0 || (lastDirection == null || !lastDirection.isValid())) { //first time read: read main file
                            Log.i(TAG, "Starting read..: ");

                            fo = new FileOutputStream(fileOS);

                            if (getRemoteGPSFile(fo)) {
                                Log.i(TAG, "main file read. Length: " + fileOS.length());

                                ReversedLinesFileReader reverseFileOs = new ReversedLinesFileReader(fileOS, Charset.defaultCharset());

                                while ((lineAux = reverseFileOs.readLine()) != null) {
                                    lastDirection = new GPSDirection(lineAux);

                                    if (lastDirection.isValid()) {
                                        //Log.i(TAG, "Direction: " + lastDirection.toString());
                                        break;
                                    }
                                }
                            } else {
                                Log.e(TAG, "Problem reading main file..");
                            }
                        } else { //next reads
                            FileOutputStream foAux = new FileOutputStream(fileOSAux);

                            if (getRemoteGPSFile(foAux)) {
                                //Log.i(TAG, "main file already read. Length: " + fileOS.length());
                                Log.i(TAG, "second file aux readed. Length: " + fileOSAux.length());

                                ReversedLinesFileReader reverseFileOsAux = new ReversedLinesFileReader(fileOSAux, Charset.defaultCharset());

                                while ((lineAux = reverseFileOsAux.readLine()) != null) {
                                    GPSDirection auxDirection = new GPSDirection(lineAux);

                                    if (auxDirection.isValid()) {
//                                    Log.i(TAG, "Direction: " + lastDirection.toString());
//                                    Log.i(TAG, "Aux Direction: " + auxDirection.toString());

                                        // two files already read, compare and work.
                                        if (!lastDirection.isEqual(auxDirection)) {
                                            if (lastDirection.distanciaCoord(auxDirection) > considerateDistance) {
                                                Log.i(TAG, "MOVING!!!!");
                                            }

                                            lastDirection = auxDirection;
                                        }

                                        break;
                                    } else {
                                        Log.i(TAG, "Aux Direction not valid..");
                                    }
                                }
                            } else {
                                Log.e(TAG, "Problem reading aux file..");
                            }

                            // Update last direction
                            if (fileOS != null && fileOSAux != null) {
                                if (fileOS.delete()) {
                                    Files.copy(fileOSAux.toPath(), fileOS.toPath());
                                }
                            }

                        }

                        Thread.sleep(TIME_SLEEP_MILISECONDS);

                    } catch (InterruptedException e) {
                        // Restore interrupt status.
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mMessenger.getBinder();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        TIME_SLEEP_SECONDS = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("interval_time", "5"));
        TIME_SLEEP_MILISECONDS = TIME_SLEEP_SECONDS * 1000;
        Log.i(TAG, "SECONDS: " + TIME_SLEEP_SECONDS);
        SOURCE_URL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("url_text", "http://prueba.com/gps.txt"); //"http:/agrocarvajal.com/gps.txt"

        try {
            url = new URL(SOURCE_URL);
            Log.i(TAG, "SOURCE_URL: " + SOURCE_URL);

            String FILE_NAME = this.getResources().getString(R.string.path_main_filegps);
            String FILE_NAME_AUX = this.getResources().getString((R.string.path_auxfilegps));

            if(getExternalFilesDir(null) != null) {
                fileOS = new File(getExternalFilesDir(null).getPath() + "/" + FILE_NAME);
                fileOSAux = new File(getExternalFilesDir(null).getPath() + "/" + FILE_NAME_AUX);
            }
        } catch (IOException e) {
            if(e instanceof MalformedURLException){
                Log.e(TAG, "URL malformed..");
                //Toast.makeText(this, "URL malformed..", Toast.LENGTH_SHORT).show();
            }

            if(serviceHandler != null){
                try {
                    mClient.send(Message.obtain(null, MSG_PROBLEM_STARTING, 97, 98));
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }

            Log.e(TAG, e.getLocalizedMessage());
            //e.printStackTrace();
            stopSelf();

            return START_STICKY;
        }
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        if(serviceHandler != null) {
            Toast.makeText(this, "Request Service starting..", Toast.LENGTH_SHORT).show();

            isRunning = true;

            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.arg2 = 999;
            serviceHandler.sendMessage(msg);
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Request service stopped..", Toast.LENGTH_SHORT).show();
        isRunning = false;

        try {
            if (fo != null) {
                fo.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }



    private StringBuilder readFile(File file) {
        StringBuilder text = new StringBuilder();
        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
                //Log.i(TAG, line + '\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }

    /**
     * Returns true when request is done and the gps file in remote server is read and false
     * when HTTP request obtain a HTTP response indicating errors.
     *
     * @param fo Output param to save in output file of Android OS (might be a database)
     *           the file read.
     *
     * @return boolean indicating the reading gps file process.
     */
    private boolean getRemoteGPSFile(FileOutputStream fo) {
        //create url and connect
        HttpURLConnection c = null;
        boolean exitReading = true;

        try {
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data

            c.connect();//connect the URL Connection

            //If Connection response is not OK then show Logs
            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage());

                exitReading = false;
            }

            InputStream is = new BufferedInputStream(c.getInputStream());//Get InputStream for connection

//            if(is.available() > 0) {
                readStream(is, fo);
//            }else{
//                exitReading = false;
//            }

            if (fo != null) {
                fo.close();
            }

            return exitReading;
        } catch (IOException e) {
            //Read exception if something went wrong
            e.printStackTrace();
            Log.e(TAG, "Download Error Exception " + e.getMessage());
            return false;
        } finally {
            if(c != null)
                c.disconnect();
        }
    }

    /**
     * From input stream (as Input Stream of HTTP Connection) transfer its data to FileOutputStream fo.
     *
     * @param is Input param pointing to resource (as HTTP Connection) to read from it.
     * @param fo Output param to save in output file of Android OS (might be a database)
     *          the file read.
     *
     */
    private void readStream(InputStream is, FileOutputStream fo) {
        byte[] buffer = new byte[1024];//Set buffer type
        int len1;//init length

        try {
            while ((len1 = is.read(buffer)) != -1) {
                fo.write(buffer, 0, len1);//Write new file
            }

            //Close all connection after doing task
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Read Error Exception " + e.getMessage());
        }
    }
}
