package com.android.arijit.firebase.walker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private int counter;
    private boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter = intent.getIntExtra("counter", 0);
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_home_24)
                .setContentTitle("Foreground Service")
                .setContentText(String.valueOf(counter))
                .setContentIntent(pendingIntent);

        startForeground(1, mNotificationBuilder.build());
        isRunning = true;

        runCounter(mNotificationBuilder);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void runCounter(NotificationCompat.Builder mNotificationBuilder ){
        NotificationManager mNotificationManager = getSystemService(NotificationManager.class);

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                mNotificationBuilder.setContentText(String.valueOf(counter));
                mNotificationManager.notify(1, mNotificationBuilder.build());
                if(isRunning) {
                    counter++;
                    handler.postDelayed(this, 1000);
                }
                else{
                    mNotificationManager.cancel(1);
                }
            }
        });
    }
}
