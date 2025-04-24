package com.lab.safe_alert_esanu_cristian;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherManager {

    private static final String EMERGENCY_MESSAGE = "Atentie! Situatie grava de urgenta (conditii meteo/calamitati naturale)!";

    private Context context;

    private static final String API_KEY = "3873897f0d16ef4958f9ff3151e08fcb";
    private static final String CITY = "Targu Mures";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&appid=" + API_KEY + "&units=metric";

    public WeatherManager(Context context) {
        this.context = context;
    }

    public void checkWeatherConditions() {
        boolean isSevereWeather = simulateWeatherCheck();

        if (isSevereWeather) {
            sendNotification("Atenție! Condiții meteo severe detectate!");
            sendEmergencySMS();
        } else {
            sendNotification("Condiițiile meteo sunt normale.");
        }
    }

    public static boolean simulateWeatherCheck() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray weatherArray = jsonResponse.getJSONArray("weather");

            for (int i = 0; i < weatherArray.length(); i++) {
                String condition = weatherArray.getJSONObject(i).getString("main").toLowerCase();
                if (condition.equals("thunderstorm") || condition.equals("tornado") || condition.equals("snow") ||
                        condition.equals("extreme") || condition.equals("hail") || condition.equals("hurricane")) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void sendNotification(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void sendEmergencySMS() {
        for (Contact contact : FavouriteContacts.contactList) {
            String phoneNumber = contact.getPhoneNumber();
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", EMERGENCY_MESSAGE);
            context.startActivity(smsIntent);
        }
    }
}