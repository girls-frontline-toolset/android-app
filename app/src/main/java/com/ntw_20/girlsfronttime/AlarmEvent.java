package com.ntw_20.girlsfronttime;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlarmManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

public class AlarmEvent extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocalService", "onCreate() executed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendNotification(String NOTIFICATION_CHANNEL_ID ){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_NONE);
//
//            // Configure the notification channel.
//            notificationChannel.setDescription("Channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.enableVibration(false);
//            mNotificationManager.createNotificationChannel(notificationChannel);
//        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
        notificationBuilder
                        .setContentTitle("每日提示")
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentText("fefefe!")
                        .setAutoCancel(false)
                        .setVibrate(new long[]{0})
                        .addAction(R.drawable.ic_check, "BUTTON 1", pendingIntent) // #0
                        .addAction(R.drawable.ic_close, "BUTTON 2", pendingIntent)
                        .setSmallIcon(R.drawable.ic_logo) ;

        //Notification notification = mBuilder.build();
        //notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1,notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        this.sendNotification("my_channel_id_01");
        this.sendNotification("my_channel_id_02");
//        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.add(Calendar.SECOND, 10);
//
//        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
//        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

        return super.onStartCommand(intent, flags, startId);

//
//        return Service.START_STICK;
    }
}
