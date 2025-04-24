package com.lab.safe_alert_esanu_cristian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceManager extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e("GeofenceReceiver", "Eroare la geofencing");
            return;
        }

        if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Toast.makeText(context, "Atentie! Ai iesit din zona sigura!", Toast.LENGTH_LONG).show();

            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500); //beep p/u avertizare

            String message = "Alerta! Telefonul a iesit din zona sigura.";
            for (Contact contact : FavouriteContacts.contactList) {
                String phoneNumber = contact.getPhoneNumber();
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
                smsIntent.putExtra("sms_body", message);
                context.startActivity(smsIntent);
            }
        }
    }
}
