package com.juane.arduino.gpstracker.utils;

import android.content.res.Resources;
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
import java.util.concurrent.TimeoutException;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private int attempts = 0;

    public static StringBuffer doGet(URL url, boolean auth, final String user, final String password) throws SecurityException, IOException {
        StringBuffer response = null;

        // Request HTTP to URL
        if (auth) {
            MyAuthenticator authenticator = new MyAuthenticator(user, password, 0);
            // basic auth on backend server
            Authenticator.setDefault(authenticator);
        }

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");//Set Request Method to "GET" since we are getting data
        httpConn.setConnectTimeout(5000);

        BufferedReader br = doConnection(httpConn);

        if (br != null) {
            response = readStream(br);
            //Log.i(TAG, response.toString());
        }

        if (httpConn != null)
            httpConn.disconnect();

        return response;
    }

    public static StringBuffer doPost(URL url, JSONObject postData, boolean auth, final String user, final String password) throws SecurityException, TimeoutException, IOException {

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

    private static BufferedReader doConnection(HttpURLConnection httpConn) throws IOException, SecurityException {
        BufferedReader br;

        httpConn.connect();//connect the URL Connection
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new SecurityException("Authentication failed");
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new Resources.NotFoundException("Not GPS data found");
        } else if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
            br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
        }

        return br;
    }

    private static class MyAuthenticator extends Authenticator {
        private int attempts;
        private String user;
        private String password;

        public MyAuthenticator(String user, String password, int attempts) {
            this.attempts = attempts;
            this.user = user;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            if (attempts == 0) {
                attempts++;
                return new PasswordAuthentication(user, password.toCharArray());
            } else {
                attempts = 0;
                return null;
            }
        }

    }
}
