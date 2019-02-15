package com.ntw_20.girlsfronttime;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class Notification {
    private Context context;
    private final int MINUTE = 60000;

    public Notification(){}

    public Notification(Context context) {
        this.context = context;
    }

    public void SetNotification() {

        JobScheduler scheduler = (JobScheduler) this.context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName jobService = new ComponentName(this.context, NotificationJob.class);

        scheduler.cancel(10107982);
        JobInfo jobInfo = new JobInfo.Builder(10107982, jobService)
                .setPeriodic(30 * MINUTE)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .build();

        scheduler.schedule(jobInfo);
    }
}
