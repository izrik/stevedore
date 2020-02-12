package com.izrik.stevedore;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TransferService extends IntentService {

    public TransferService() {
        super("TransferService");
    }

    private NotificationManager nm;
    private int NOTIFICATION = R.string.transfer_service_started;

    OkHttpClient client;

    @Override
    public void onCreate() {
        this.nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        super.onCreate();
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
    public void onDestroy() {
        nm.cancel(NOTIFICATION);
        Toast.makeText(this, R.string.transfer_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        showNotification();

        String url = intent.getStringExtra("pref_url");
        if (!url.endsWith("/")){
            url = url + "/";
        }
        URI uri = null;
        try {
            uri = new URL(url).toURI().resolve("files/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        String source = intent.getStringExtra("source");
        File sourcef = new File(source);
        URI sourceu = sourcef.toURI();

        List<File> filesToUpload = getFilesToUpload(sourcef);

        for (File file : filesToUpload) {

            String relpath;
            if (sourcef.isFile()) {
                relpath = sourceu.relativize(file.toURI()).getPath();
            } else {
                relpath = file.getName();
            }

            uploadFile(uri, file, relpath);
        }
    }

    private List<File> getFilesToUpload(File source) {
        List<File> files = new ArrayList<File>();
        getFilesToUpload(source, files);
        return files;
    }
    private void getFilesToUpload(File source, List<File> files) {
        if (!source.exists()) return;
        if (source.isFile()) {
            files.add(source);
            return;
        }
        if (source.isDirectory()) {
            File[] files2 = source.listFiles();
            if (files2 == null) {
                int i = 0;
                Log.d("tag","null");
            }
            for (File f2 : files2) {
                getFilesToUpload(f2, files);
            }
        }
    }

    private void uploadFile(URI uri, File file, String relpath) {
        URI dest = uri.resolve(relpath);
        URL url;
        try {
            url = new URL(dest.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(file, MediaType.get("application/octet-stream"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.i("Upload", "Uploaded file \"" + file.toString());
    }

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
