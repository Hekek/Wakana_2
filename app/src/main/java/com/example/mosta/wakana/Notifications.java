package com.example.mosta.wakana;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;


public class Notifications extends Service {
    public Notifications() {
    }

    public int id = 0;
    public String label;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Vibrator v = (Vibrator) getBaseContext().getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        label = intent.getStringExtra("LABEL");
        v.vibrate(300);
        Intent i = new Intent("my-event");
        // add data
        i.putExtra("SOUND", label);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        return super.onStartCommand(intent , flags , startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void Notify(String label){
        if (label==null)
            onDestroy();
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("Yuri")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Yuri")
                .setContentText(label)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationManager.notify(0, notification);

    }
}
