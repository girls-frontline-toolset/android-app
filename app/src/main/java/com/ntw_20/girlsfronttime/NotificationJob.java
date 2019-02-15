package com.ntw_20.girlsfronttime;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NotificationJob extends JobService {
    private final String NOTIFICATION_CHANNEL_ID = "GirlsFrontLine_channel_id";
    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder groupNotificationCompat = null ;
    private int notificationId = 1;
    private int notificationCount = 0;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("ntw-20.com", "onStartJob");

        SharedPreferences pref = getSharedPreferences("notification", MODE_PRIVATE);

        String lastRunDate = pref.getString("date", "1999-01-01");

        if (!lastRunDate.equals(GetDate())) {
        //if (true) {
            if (mNotificationManager == null) {
                mNotificationManager = getNotificationManager();
            }

            if (groupNotificationCompat == null) {
                groupNotificationCompat = createNotification("", 0, true);
                mNotificationManager.notify(0, groupNotificationCompat.build());
            }

            NotificationJob.GetNotification task = new NotificationJob.GetNotification(this);
            task.setDataSource(this);
            task.execute("https://www.ntw-20.com/api/inquiry/notification/getNotification?date=" + GetDate());

            pref.edit().putString("date", GetDate()).apply();
        }
        jobFinished(params,true);
        return false;
    }

    public String GetDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return  df.format(c);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.i("ntw-20.com", "onStartCommand");

        if (intent == null) return flags;

        String intentAction = intent.getAction();

        Log.i("ntw-20.com", intentAction);

        if (intentAction == null )return  flags;
            switch (intentAction) {
                case "cancel":
                    SharedPreferences pref = getSharedPreferences("notification", MODE_PRIVATE);

                    notificationCount = pref.getInt("count", 0);

                    pref.edit().putInt("count", --notificationCount).apply();

                    Log.i("pendingIntent", String.valueOf(notificationCount));
                    int notificationId = intent.getIntExtra("notificationId", 0);

                    if (mNotificationManager == null) {
                        mNotificationManager = getNotificationManager();
                    }

                    mNotificationManager.cancel(notificationId);

                    Log.i("ntw-20.com", String.valueOf(notificationCount));

                    if (notificationCount <= 1) {
                        mNotificationManager.cancel(0);
                    }

                    break;
                case "success":
                    break;
            }
        return flags;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i("ntw-20.com", "onStopJob");

        Notification notif = new Notification(this);
        notif.SetNotification();
        return false;
    }

    public NotificationCompat.Builder createNotification(String contentText, int notificationId){
        return  createNotification(contentText,notificationId, false);
    }

    public NotificationCompat.Builder createNotification(String contentText, int notificationId, Boolean groupSummary){

        SharedPreferences pref = getSharedPreferences("notification", MODE_PRIVATE);
        pref.edit().putInt("count", ++notificationCount).apply();

        Intent cancelIntent = new Intent(this, NotificationJob.class);
        cancelIntent.setAction("cancel");
        cancelIntent.putExtra("notificationId",notificationId);
        PendingIntent cancelPendingIntent = PendingIntent.getService(this,notificationId,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent successIntent = new Intent(this, NotificationJob.class);
        successIntent.setAction("success");
        PendingIntent successPendingIntent = PendingIntent.getService(this,notificationId,successIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        String KEY_NOTIFICATION_GROUP = "GirlsFrontLine";
        notificationBuilder
                .setContentTitle("每日提示")
                .setColor(this.getResources().getColor(R.color.colorPrimary))
                .setContentText(contentText)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setVibrate(null)
                .setOngoing(true)
                //.setDefaults(Notification.DEFAULT_ALL)
                //.setLargeIcon(R.drawable.ic_logo)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //.addAction(R.drawable.ic_check, "完成", successPendingIntent)
                .addAction(R.drawable.ic_check, "完成", cancelPendingIntent)
                .addAction(R.drawable.ic_close, "跳過", cancelPendingIntent)
                .setSmallIcon(R.drawable.ic_logo)
                .setGroup(KEY_NOTIFICATION_GROUP);

        if (groupSummary){
            notificationBuilder.setGroupSummary(true);
        }

        return notificationBuilder;
    }

    public void sendNotification(String content){
        if (mNotificationManager == null){
            mNotificationManager = getNotificationManager();
        }

        if (groupNotificationCompat == null){
            groupNotificationCompat = createNotification("Group Notification",0,true);
            mNotificationManager.notify(0,groupNotificationCompat.build());
        }

        mNotificationManager.notify(++notificationId, createNotification(content,notificationId).build());
    }

    private NotificationManager getNotificationManager(){
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "每日提示",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }
        return mNotificationManager;
    }

    public void cancelAllNotification(){
        if (mNotificationManager == null){
            mNotificationManager = getNotificationManager();
        }
        mNotificationManager.cancelAll();
        groupNotificationCompat = null;
        SharedPreferences pref = getSharedPreferences("notification", MODE_PRIVATE);
        notificationCount = 0;
        pref.edit().putInt("count",notificationCount).apply();
    }


    // get JSON
    private class GetNotification extends AsyncTask<String, Void, String> {
        private Context context;
        private NotificationJob notification;
        GetNotification(Context context) { this.context = context; }

        @Override
        protected String doInBackground(String... params) {
            try {

                //set http connection
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                // https to save the data
                String line = "";
                StringBuilder reply = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null)
                    reply.append(line);
                reader.close();

                return reply.toString();

            } catch (Exception e) {
                return e.getMessage();
            }


        }

        protected void onPostExecute(String result) {
            try {
                //new json object
                Log.i("jsonResult",result);
                JSONObject json = new JSONObject(result);

                JSONArray notificationList = json.getJSONArray("data");
                notification.cancelAllNotification();

                SharedPreferences pref = getSharedPreferences("setting", MODE_PRIVATE);

                for (int i = 0; i < notificationList.length(); i++) {
                    String type = notificationList.getJSONObject(i).getString("type");
                    if(pref.getBoolean(type,true)){
            notification.sendNotification(notificationList.getJSONObject(i).getString("text"));
        }
    }
} catch (Exception e) {
        Log.i("jsonResult",e.getMessage());
        }
        }

        void setDataSource(NotificationJob notification) {
            this.notification = notification;
        }
    }





}
