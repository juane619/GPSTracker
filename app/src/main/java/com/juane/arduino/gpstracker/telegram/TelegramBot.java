package com.juane.arduino.gpstracker.telegram;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class TelegramBot extends AsyncTask<String, Void, Void> {
    private static final String TAG = "Telegram BOT";

    private final String BASEURL = "https://api.telegram.org/bot";
    private final String CHATID_FIELD = "chat_id";
    private final String MESSAGE_FIELD = "text";
    private final String SEND_MESSAGE = "sendMessage";
    private String botId;
    private String chatMessage;

    public TelegramBot(String botId) {
        this.botId = botId;
    }

    public void sendMessage(String chatId, String message) {
        if (botId != null) {
            String URLString = BASEURL + botId + "/" + SEND_MESSAGE;// + "?" + CHATID_FIELD + "=" + chatId + "&" + MESSAGE_FIELD + "=" + message;
            HttpURLConnection c = null;
            URL URLbot;

            try {
                URLbot = new URL(URLString);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Download Error Exception " + e.getMessage());
                return;
            }

            JSONObject postData = new JSONObject();
            try {
                postData.put(CHATID_FIELD, chatId);
                postData.put(MESSAGE_FIELD, message);
                postData.put("disable_notification", true);

                c = (HttpURLConnection) URLbot.openConnection();
                c.setRequestMethod("POST");//Set Request Method to "GET" since we are grtting data
                c.setRequestProperty("Content-Type", "application/json");
                c.setDoOutput(true);

//                c.connect();//connect the URL Connection
//
//                //If Connection response is not OK then show Logs
//                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
//                            + " " + c.getResponseMessage());
//                }

                OutputStream out = new BufferedOutputStream(c.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        out, "UTF-8"));
                writer.write(postData.toString());
                writer.flush();

                int code = c.getResponseCode();
                if (code != 201 && code != 200) {
                    throw new IOException("Invalid response from server: " + code);
                }

                StringBuilder content;

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(c.getInputStream()))) {
                    String line;
                    content = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                        content.append(System.lineSeparator());
                    }

                    System.out.println(content.toString());
                } catch (ProtocolException e) {
                    Log.e(TAG, "Error reading from buffer in telegram message: " + e.getMessage());
                    return;
                }
            } catch (IOException e) {
                //Read exception if something went wrong
                //e.printStackTrace();
                Log.e(TAG, "Telegram connection failed: " + e.getMessage());
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        sendMessage(params[0], params[1]);
        return null;
    }
}
