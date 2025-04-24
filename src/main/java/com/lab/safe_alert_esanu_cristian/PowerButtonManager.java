package com.lab.safe_alert_esanu_cristian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerButtonManager extends BroadcastReceiver {
    private static int count = 0;
    private static long lastPressTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPressTime < 1000) {
            count++;
        } else {
            count = 1;
        }
        lastPressTime = currentTime;

        if (count == 3) {
            Intent sosIntent = new Intent(context, MainActivity.class);
            sosIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sosIntent);
        }
    }
}
