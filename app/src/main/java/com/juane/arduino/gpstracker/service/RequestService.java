package com.juane.arduino.gpstracker.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.juane.arduino.gpstracker.gps.GPSDirection;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
    private static final String TAG = "Request Service";

    private final static int TIME_SLEEP_SECONDS = 15;
    private final static int TIME_SLEEP_MILISECONDS = TIME_SLEEP_SECONDS * 1000;

    // Thread
    private Looper serviceLooper; //loop over tasks
    private ServiceHandler serviceHandler; //communicate with main thread
    boolean isRunning = false;

    // Requests
    private final String urlText = "http:/agrocarvajal.com/gps.txt";
    URL url;
    FileOutputStream fo = null;
    private final String FILE_NAME = "readsgps.txt";
    private final String FILE_NAME_AUX = "readsgps_aux.txt";
    File fileOS = null;
    File fileOSAux = null;

    // manage GPS directions
    GPSDirection lastDirection = null;
    String lineAux = null;

    public RequestService() {
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            while (isRunning) {
                try {
                    if (fileOS.length() == 0 || lastDirection == null) { //first time read: read main file
                        Log.i(TAG, "Starting read..: ");

                        fo = new FileOutputStream(fileOS);

                        if (getRemoteGPSFile(fo)) {
                            Log.i(TAG, "main file already read. Length: " + fileOS.length());

                            ReversedLinesFileReader reverseFileOs = new ReversedLinesFileReader(fileOS, Charset.defaultCharset());

                            if ((lineAux = reverseFileOs.readLine()) != null) {
                                Log.i(TAG, "lineAux: " + lineAux);
                                lastDirection = new GPSDirection(lineAux);

                                Log.i(TAG, "Direction: " + lastDirection.toString());
                            }
                        }
                    } else { //next reads
                        FileOutputStream foAux = null;

                        if (foAux == null) {
                            foAux = new FileOutputStream(fileOSAux);
                        } else {
                            foAux.close();
                            foAux = new FileOutputStream(fileOSAux);
                        }

                        if (getRemoteGPSFile(foAux)) {
                            Log.i(TAG, "main file already read. Length: " + fileOS.length());
                            Log.i(TAG, "second file aux readed. Length: " + fileOSAux.length());
                        }

                        // two files already read, compare and work.
                        if (fileOS.length() != fileOSAux.length()) {
                            Log.i(TAG, "They aren't equal!");

                            ReversedLinesFileReader reverseFileOs = new ReversedLinesFileReader(fileOS, Charset.defaultCharset());

                            if ((lineAux = reverseFileOs.readLine()) != null) {
                                Log.i(TAG, "lineAux: " + lineAux);
                                lastDirection = new GPSDirection(lineAux);

                                Log.i(TAG, "Direction: " + lastDirection.toString());
                            }
                        } else {
                            Log.i(TAG, "They ARE equal!");
                        }

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
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Request Service created..");

        try {
            url = new URL(urlText);

            if (isExternalDirectoryPresent()) {
                fileOS = new File(getExternalFilesDir(null).getPath() + "/" + FILE_NAME);
                fileOSAux = new File(getExternalFilesDir(null).getPath() + "/" + FILE_NAME_AUX);

                if (fileOS.exists()) {
                    Log.i(TAG, "File exists! " + fileOS.length());
                } else {
                    Log.i(TAG, "File not exists! " + fileOS.getAbsolutePath());

                    if (fileOS.createNewFile()) {
                        Log.i(TAG, "File created!");
                    }
                }

                if (fileOSAux.exists()) {
                    Log.i(TAG, "File aux exists! " + fileOSAux.length());
                } else {
                    Log.i(TAG, "File aux not exists! " + fileOSAux.getAbsolutePath());

                    if (fileOSAux.createNewFile()) {
                        Log.i(TAG, "File aux created!");
                    }
                }

                HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();

                // Get the HandlerThread's Looper and use it for our Handler
                serviceLooper = thread.getLooper();
                serviceHandler = new ServiceHandler(serviceLooper);
            } else {
                Log.e(TAG, "Not external storage!");
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Request Service starting..", Toast.LENGTH_SHORT).show();

        isRunning = true;

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

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

    //Check If SD Card is present or not method
    private boolean isExternalDirectoryPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
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
     * Returns true when request is done and the gps file in remote server is readed and false
     * when HTTP request obtain a HTTP response indicating errors.
     *
     * @return boolean indicating the reading gps file process.
     */
    private boolean getRemoteGPSFile(FileOutputStream fo) {
        //create url and connect
        HttpURLConnection c = null;

        try {
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data

            c.connect();//connect the URL Connection

            //If Connection response is not OK then show Logs
            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage());

                return false;
            }

            InputStream is = new BufferedInputStream(c.getInputStream());//Get InputStream for connection
            readStream(is, fo);

            if (fo != null) {
                fo.close();
            }
            return true;
        } catch (IOException e) {
            //Read exception if something went wrong
            e.printStackTrace();
            Log.e(TAG, "Download Error Exception " + e.getMessage());
            return false;
        } finally {
            c.disconnect();
        }
    }

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
            Log.e(TAG, "Download Error Exception " + e.getMessage());
        }
    }
}
