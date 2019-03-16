package com.izrik.stevedore;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class TransferService extends Service {
    public TransferService() {
    }

    private NotificationManager nm;
    private int NOTIFICATION = R.string.transfer_service_started;

    public class LocalBinder extends Binder {
        TransferService getService() {
            return TransferService.this;
        }
    }

    @Override
    public void onCreate() {
        this.nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private static final String CHANNEL_ID = "Transfer_Notification_Channel";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            this.nm.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TransferService", "Received startId " + startId + ": " + intent);
        showNotification();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        nm.cancel(NOTIFICATION);
        Toast.makeText(this, R.string.transfer_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new LocalBinder();

    private void showNotification() {
        CharSequence text = getText(R.string.transfer_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, NotifiedActivity.class),
                0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sample)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.transfer_service_label))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build();

        nm.notify(12346, notification);
        this.startForeground(12346, notification);
    }
}
