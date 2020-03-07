package com.juane.arduino.gpstracker.service;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.juane.arduino.gpstracker.MainActivity;
import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.ui.map.MapFragment;
import com.juane.arduino.gpstracker.utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Map;


// first argument: empezar background (parameter in doBackground)
// second argument: durante background (parameter in publish progress)
// third background: finish background (return doBackground and parameter in onPostExecute)

public class RequestGps extends AsyncTask<String, Void, String> {
    private static final String TAG = "RequestGPS";

    private MainActivity mainActivity;
    private MapFragment mapFragment;

    public RequestGps(MainActivity mainActivity, MapFragment mapFragment) {
        this.mainActivity = mainActivity;
        this.mapFragment = mapFragment;
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            // This is getting the url from the string we passed in

            URL url = new URL(params[0]);

            // POR AQUI SEGUIMOS: nos falta tratar correctamente la PASS y el USER
            StringBuffer response = HttpUtils.doGet(url, true, "juane619", "Mygpstracker1!");

            return response.toString();
        } catch (IOException e) {
            //Read exception if something went wrong
            //e.printStackTrace();
            Log.e(TAG, "Connection problem (bad URL?): " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response){
        Log.i(TAG, "Response: " + response);

        if(response != null) {
            // DATA read -> parse it to JSON
            JSONObject lastGpsDataRead = null;
            try {
                lastGpsDataRead = new JSONObject(response);
                JSONArray addressesJSON = lastGpsDataRead.getJSONArray("addresses");

                if (addressesJSON != null) {
                    mapFragment.addMarkers(addressesJSON);
                    mainActivity.changeTab(R.id.mapTabId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(mainActivity,
                    "No GPS data for selected day..",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
