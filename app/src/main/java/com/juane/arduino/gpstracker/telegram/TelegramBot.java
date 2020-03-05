package com.juane.arduino.gpstracker.telegram;

import android.os.AsyncTask;
import android.util.Log;

import com.juane.arduino.gpstracker.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TelegramBot extends AsyncTask<String, Void, Void> {
    private static final String TAG = "Telegram BOT";

    private final String BASEURL = "https://api.telegram.org/bot";
    private final String CHATID_FIELD = "chat_id";
    private final String MESSAGE_FIELD = "text";
    private final String SEND_MESSAGE = "sendMessage";
    private String botId;

    public TelegramBot(String botId) {
        this.botId = botId;
    }

    public String sendMessage(String chatId, String message) {
        if (botId != null) {
            String URLString = BASEURL + botId + "/" + SEND_MESSAGE;// + "?" + CHATID_FIELD + "=" + chatId + "&" + MESSAGE_FIELD + "=" + message;

            URL URLbot;

            try {
                URLbot = new URL(URLString);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Download Error Exception setting URL" + e.getMessage());
                return null;
            }

            JSONObject postData = new JSONObject();
            try {
                postData.put(CHATID_FIELD, chatId);
                postData.put(MESSAGE_FIELD, message);

                StringBuffer response = HttpUtils.doPost(URLbot, postData, false, null, null);

                return response.toString();
                //if(response != null){
                //Log.i(TAG, "Response from TelegramBot: " + response.toString());
                //}
            } catch (IOException e) {
                //Read exception if something went wrong
                //e.printStackTrace();
                Log.e(TAG, "Telegram connection problem: " + e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected Void doInBackground(String... params) {
        sendMessage(params[0], params[1]);
        return null;
    }
}
