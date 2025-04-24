package com.lab.safe_alert_esanu_cristian;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

public class BatteryAlertManager extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient; // punctul 6
    private static final int REQUEST_LOCATION_PERMISSION = 1;


    private Context context;
    private static final String FAVORITE_CONTACT = "+40742521837";

    public BatteryAlertManager(Context context) {
        this.context = context;
    }

    public boolean isBatteryLow(int batteryLevel) {
        return batteryLevel < 10;
    }

    public void sendSmsToFavoriteContact() {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + FAVORITE_CONTACT));
        smsIntent.putExtra("sms_body", "Telefonul meu este pe cale sa se descarce complet!");
        context.startActivity(smsIntent);
    }

    public void suggestPowerSavingMode() {
        Toast.makeText(context, "Sugestie: Activeaza modul de economisire a energiei!", Toast.LENGTH_LONG).show();
    }

    public void sendLastLocation() {
        if (ActivityCompat.checkSelfPermission(BatteryAlertManager.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BatteryAlertManager.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String locationUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
                    String message = "Telefonul meu e pe cale sa se descarce complet. Locatia mea: " + locationUrl;

                    for (Contact contact : FavouriteContacts.contactList) {
                        String phoneNumber = contact.getPhoneNumber();
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
                        smsIntent.putExtra("sms_body", message);
                        startActivity(smsIntent);
                    }
                } else {
                    Toast.makeText(BatteryAlertManager.this, "Locatie indisponibila!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}