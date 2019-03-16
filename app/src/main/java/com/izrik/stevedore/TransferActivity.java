package com.izrik.stevedore;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TransferActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "Transfer_Notification_Channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        Button startServiceButton = (Button)findViewById(R.id.start_service);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(
                        new Intent(
                                TransferActivity.this,
                                TransferService.class));

            }
        });
        createNotificationChannel();
        Button createNotificationButton = (Button)findViewById(R.id.create_notification);
        createNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TransferActivity.this, NotifiedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(TransferActivity.this, 0, intent, 0);

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(TransferActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_sample)
                        .setContentTitle("This is the notification title")
                        .setContentText("This is the notification text")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat nmc = NotificationManagerCompat.from(TransferActivity.this);
                nmc.notify(1337, builder.build());
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = getSystemService(NotificationManager.class);
            assert nm != null;
            nm.createNotificationChannel(channel);
        }
    }

    private boolean shouldUnbind;
    private TransferService boundService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            boundService= ((TransferService.LocalBinder)iBinder).getService();
            Toast.makeText(TransferActivity.this, R.string.transfer_service_connected, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            boundService=null;
            Toast.makeText(TransferActivity.this, R.string.transfer_service_disconnected, Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        if (bindService(new Intent(TransferActivity.this, TransferService.class),
                connection, Context.BIND_AUTO_CREATE)) {
            shouldUnbind = true;
        } else {
            Log.e("MY_APP_TAG", "Error: the requested service doesn't exist, or this client isn't allowed to access it.");
        }
    }

    void doUnbindService() {
        if (shouldUnbind) {
            unbindService(connection);
            shouldUnbind = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
