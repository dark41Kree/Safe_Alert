package com.lab.safe_alert_esanu_cristian;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class ScheduledMessage {
    private android.os.Handler handler;
    private Runnable sendMessageRunnable;

    private Context context;

    public ScheduledMessage() {
        this.handler = new android.os.Handler();
    }

    public void sendScheduledMessage(Context context, int minutes) {
        String message = "Am programat trimiterea acestui mesaj cu " + minutes + " minute in urma, daca peste 20 de minute nu raspund sunt in pericol!";
        for (Contact contact : FavouriteContacts.contactList) {
            String phoneNumber = contact.getPhoneNumber();
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", message);
            context.startActivity(smsIntent);
        }
    }

    public void scheduledMessage(Context context, int minutes) {

        long delay = (long) minutes * 60 * 1000; // milisecunde


        sendMessageRunnable = new Runnable() {
            @Override
            public void run() {
                sendScheduledMessage(context, minutes);
                sendMessageRunnable = null;
                MainActivity.btnAnuleazaMesajProgramat.setVisibility(View.GONE);
            }
        };

        handler.postDelayed(sendMessageRunnable, delay);
    }

    public boolean cancelScheduledMessage() {
        if (sendMessageRunnable != null) {
            handler.removeCallbacks(sendMessageRunnable);
            sendMessageRunnable = null;
            return true;
        }
        return false;
    }
}