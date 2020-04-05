package com.juane.arduino.gpstracker.service;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.juane.arduino.gpstracker.MainActivity;
import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.ui.map.MapFragment;
import com.juane.arduino.gpstracker.utils.Errors;
import com.juane.arduino.gpstracker.utils.HttpUtils;
import com.juane.arduino.gpstracker.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


// first argument: empezar background (parameter in doBackground)
// second argument: durante background (parameter in publish progress)
// third background: finish background (return doBackground and parameter in onPostExecute)

public class RequestGps extends AsyncTask<String, Void, String> {
    private static final String TAG = "RequestGPS";
    StringBuffer response = new StringBuffer();
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
            String needAuth = params[1];

            if (needAuth.equals("true")) {
                String user = params[2];
                String passwd = params[3];

                // POR AQUI SEGUIMOS: nos falta tratar correctamente la PASS y el USER
                response = HttpUtils.doGet(url, true, user, passwd);
            } else {
                response = HttpUtils.doGet(url, false, null, null);
            }

        } catch (MalformedURLException e) {
            Log.w(TAG, "Connection problem (bad URL?): " + e.getMessage());
            response.append(Errors.BAD_URL.getCode());
        } catch (SecurityException e) {
            Log.w(TAG, "Security exception " + e.getMessage());
            response.append(Errors.AUTHENTICATION_FAILURE.getCode());
        }catch(Resources.NotFoundException e){
            Log.w(TAG, "Not found exception " + e.getMessage());
            response.append(Errors.GPS_DATA_NOT_FOUND.getCode());
        }
        catch (IOException e) {
            //Read exception if something went wrong
            //e.printStackTrace();
            Log.w(TAG, "Connection problem: " + e.getMessage());
            response.append(Errors.CONNECTION_PROBLEM.getCode());
        } finally {
            return response.toString();
        }
    }

    @Override
    protected void onPostExecute(String response) {
        Log.i(TAG, "Response: " + response);

        if (response != null && !response.isEmpty()) {
            if (response.equals(Errors.CONNECTION_PROBLEM.getCode())) {
                ToastUtils.ToastShort(mainActivity, R.string.error_connection_problem);
            } else if (response.equals(Errors.AUTHENTICATION_FAILURE.getCode())) {
                ToastUtils.ToastShort(mainActivity, R.string.error_authentication_failure);
            } else if (response.equals(Errors.GPS_DATA_NOT_FOUND.getCode())) {
                ToastUtils.ToastShort(mainActivity, R.string.error_gps_data_not_found);
            } else {
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
            }
        } else {
            ToastUtils.ToastShort(mainActivity, R.string.error_generic);
        }
    }
}