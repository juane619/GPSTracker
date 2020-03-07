package com.juane.arduino.gpstracker.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class HttpUtils {
    private static final String TAG = "HttpUtils";

    public static StringBuffer doGet(URL url, boolean auth, final String user, final String password) throws IOException {
        StringBuffer response = null;

        // Request HTTP to URL
        if(auth){
            // basic auth on backend server
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password.toCharArray());
                }
            });
        }

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");//Set Request Method to "GET" since we are getting data

        httpConn.connect();//connect the URL Connection

        BufferedReader br = doConnection(httpConn);

        if (br != null) {
            response = readStream(br);
            //Log.i(TAG, response.toString());
        }

        if (httpConn != null)
            httpConn.disconnect();

        return response;
    }

    public static StringBuffer doPost(URL url, JSONObject postData, boolean auth, final String user, final String password) throws IOException{

        StringBuffer response = null;
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");//Set Request Method to "POST" since we are getting data
        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setDoOutput(true);

        OutputStream out = new BufferedOutputStream(httpConn.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                out, "UTF-8"));
        writer.write(postData.toString());
        writer.flush();

        BufferedReader br = doConnection(httpConn);

        if (br != null) {
            response = readStream(br);
            Log.i(TAG, response.toString());
        }

        if (httpConn != null)
            httpConn.disconnect();

        return response;
    }

    /**
     * From input stream (as Input Stream of HTTP Connection) transfer its data to FileOutputStream fo.
     *
     * @param br Input param pointing to resource (as HTTP Connection) to read from it.
     * @return response StringBuffer with response.
     */
    private static StringBuffer readStream(BufferedReader br) {
        StringBuffer response = new StringBuffer();
        String line;

        try {
            while ((line = br.readLine()) != null) {
                response.append(line);//.write(buffer, 0, len1);//Write new file
            }

            //Close all connection after doing task
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Read Error Exception " + e.getMessage());
        }

        return response;
    }

    private static BufferedReader doConnection(HttpURLConnection httpConn) throws IOException {
        BufferedReader br;

        int responseCode = httpConn.getResponseCode();
        if (responseCode != 201 && responseCode != 200) {
            throw new IOException("Invalid response from server: " + responseCode);
        }else{
            br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
        }

        return br;
    }
}
