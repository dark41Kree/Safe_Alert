package com.lab.safe_alert_esanu_cristian;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Collections;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;


public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient; // punctul 1
    private static final int REQUEST_LOCATION_PERMISSION = 1; //punctul 1 si 4
    private VoiceRecognitionManager voiceRecognitionManager1 = new VoiceRecognitionManager(MainActivity.this);
    private Handler handler = new Handler(Looper.getMainLooper()); //punctul 2
    private Runnable callRunnable; //punctul 2
    private Button btnCancelCall;  // punctul 2
    private GeofencingClient geofencingClient;  //punctul 4
    private PendingIntent geofencePendingIntent; //punctul 4
    private WeatherManager weatherManager; //punctul 5
    private BatteryAlertManager batteryAlertManager1; //punctul 6
    private boolean isAlertTriggered = false;//punctul 6

    private ScheduledMessage mesajProgramat; //punctul 8

    public static Button btnAnuleazaMesajProgramat; //PUNCTUL 8 pentru a putea ascunde butonul dupa trimiterea mesajului

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FavouriteContacts.contactList.add(new Contact("Tata", "+40712345678"));
        FavouriteContacts.contactList.add(new Contact("Mama", "+40798765432"));
        FavouriteContacts.contactList.add(new Contact("Sotia", "+40755555555"));
        FavouriteContacts.contactList.add(new Contact("Fratele", "+40765656565"));
        FavouriteContacts.contactList.add(new Contact("Sora", "+40777777777"));

        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //puntul 1

        setContentView(R.layout.activity_main);

        voiceRecognitionManager1.startVoiceRecognition(); //punctul 1

        btnCancelCall = findViewById(R.id.btnCancelCall); //puntul 2
        btnCancelCall.setOnClickListener(v -> cancelEmergencyCall()); //punctul 2

        geofencingClient = LocationServices.getGeofencingClient(this); //punctul 4

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            addGeofence();
        }  //punctul 4

        weatherManager = new WeatherManager(this); //punctul 5

        batteryAlertManager1 = new BatteryAlertManager(this); //punct 6
        registerBatteryReceiver(); //punct 6

        mesajProgramat = new ScheduledMessage(); //punctul 8

        Button sosButton = findViewById(R.id.btnSos); //punctul 1
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSOS();
            }
        });

        Button checkWeatherButton = findViewById(R.id.btnCheckWeather);  //punctul 5
        checkWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherManager.checkWeatherConditions();
            }
        });

        Button btnMesajProgramat = findViewById(R.id.btnMesajProgramat);  //punctul 8
        btnAnuleazaMesajProgramat = findViewById(R.id.btnAnuleazaMesajProgramat);  //punctul 8
        btnAnuleazaMesajProgramat.setVisibility(View.GONE); // Ascundem butonul de anulare

        btnMesajProgramat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText minute = new EditText(MainActivity.this);
                layout.addView(minute);


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Peste cate minute sa se trimita mesajul?")
                        .setView(layout)
                        .setPositiveButton("Salveaza", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                int minutes = Integer.parseInt(minute.getText().toString());
                                mesajProgramat.scheduledMessage(MainActivity.this, minutes);
                                Toast.makeText(MainActivity.this, "Mesaj Programat!", Toast.LENGTH_LONG).show();
                                btnAnuleazaMesajProgramat.setVisibility(View.VISIBLE);


                            }
                        })
                        .setNegativeButton("Anuleaza", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.create().show();
            }
        });

        btnAnuleazaMesajProgramat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean anulareDisponibila = mesajProgramat.cancelScheduledMessage();
                if (anulareDisponibila){
                    btnAnuleazaMesajProgramat.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Mesaj Programat Anulat!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btnlistaContactefavorite = findViewById(R.id.btnAddContactFavorit);
        btnlistaContactefavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavouriteContacts.class);
                startActivity(intent);
            }
        });

    }

    public void sendSOS() {
        startEmergencyCall();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String locationUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
                    String message = "SOS! Am nevoie de ajutor. Locatia mea: " + locationUrl;

                    for (Contact contact : FavouriteContacts.contactList) {
                        String phoneNumber = contact.getPhoneNumber();
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
                        smsIntent.putExtra("sms_body", message);
                        startActivity(smsIntent);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Locatie indisponibila!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Cerem permisiune pentru locatie
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSOS();
            } else {
                Toast.makeText(this, "Permisiunea pentru locatie este necesara!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startEmergencyCall() {
        btnCancelCall.setVisibility(View.VISIBLE); // Afisam butonul de cancel

        callRunnable = () -> {
            String phoneNumber = "846";

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisiune pentru apel necesara!", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                return;
            }

            startActivity(callIntent);
            btnCancelCall.setVisibility(View.GONE);// Ascundem butonul de cancel
        };

        handler.postDelayed(callRunnable, 10000);
    }

    public void cancelEmergencyCall() {
        handler.removeCallbacks(callRunnable);
        btnCancelCall.setVisibility(View.GONE);
        Toast.makeText(this, "Apelul a fost anulat!", Toast.LENGTH_SHORT).show();
    }

    private void addGeofence() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, 1001);
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId("zona_sigura")
                .setCircularRegion(46.54664, 24.56356, 10000) // coordonatele pentru Targu Mures
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofences(Collections.singletonList(geofence))
                .build();

        geofencePendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(this, GeofenceManager.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(unused -> Toast.makeText(MainActivity.this, "Geofencing activat", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Eroare la adaugarea geofence", Toast.LENGTH_SHORT).show());
    }

    private void registerBatteryReceiver() {
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPercentage = (level * 100) / scale;

                if (batteryAlertManager1.isBatteryLow(batteryPercentage) && !isAlertTriggered) {
                    isAlertTriggered = true;
                    batteryAlertManager1.sendSmsToFavoriteContact();
                    batteryAlertManager1.suggestPowerSavingMode();
                    batteryAlertManager1.sendLastLocation();
                } else if (!batteryAlertManager1.isBatteryLow(batteryPercentage)) {
                    isAlertTriggered = false;
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
}
