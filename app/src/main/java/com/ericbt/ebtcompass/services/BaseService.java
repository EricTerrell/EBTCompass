/*
  EBT Compass
  (C) Copyright 2021, Eric Bergman-Terrell

  This file is part of EBT Compass.

    EBT Compass is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBT Compass is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBT Compass.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.ebtcompass.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.activities.FindPointActivity;
import com.ericbt.ebtcompass.activities.MainActivity;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.R;

import static android.app.Notification.FLAG_FOREGROUND_SERVICE;
import static android.app.Notification.FLAG_ONGOING_EVENT;
import static android.app.Notification.PRIORITY_LOW;

public abstract class BaseService extends Service {
    protected IBinder binder = null;

    public static final int NOTIFICATION_ID = 1000;

    public static final String NOTIFICATION_CHANNEL_ID = "ebt_compass_channel_id";

    public static final String NOTIFICATION_CHANNEL_NAME = "EBT Compass Notifications";

    protected final static int FOREGROUND_FLAGS = FLAG_ONGOING_EVENT | FLAG_FOREGROUND_SERVICE;

    private final String className;

    private NotificationChannel notificationChannel;

    private String activityName;

    public BaseService() {
        className = this.getClass().getName();
    }

    @Override
    public void onCreate() {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getDefaultLocale(),
                        "%s.onCreate begin",
                        className));

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getDefaultLocale(),
                        "%s.onDestroy begin",
                        className));

        super.onDestroy();

        notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel != null) {
            final NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);

            notificationChannel = null;
        }

        Log.i(StringLiterals.LOG_TAG,
                String.format(
                        LocaleUtils.getDefaultLocale(),
                        "%s.onDestroy end",
                        className));
    }

    @Override
    public void onLowMemory() {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getDefaultLocale(), "%s.onLowMemory", className));

        super.onLowMemory();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getDefaultLocale(), "%s.onBind", className));

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getDefaultLocale(), "%s.onUnbind", className));

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(StringLiterals.LOG_TAG,
                String.format(LocaleUtils.getDefaultLocale(), "%s.onRebind", className));
    }

    // Cache the notification so that it can be shared by multiple services (otherwise it will
    // appear twice in the notification area).
    static Notification notification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityName = intent.getStringExtra(StringLiterals.ACTIVITY_NAME);

        return super.onStartCommand(intent, flags, startId);
    }

    protected Notification createDefaultNotification() {
        if (notification == null) {
            createNotificationChannel("Compass Service");

            final Class activityClass;

            if (activityName != null && activityName.endsWith(".MainActivity")) {
                activityClass = MainActivity.class;
            } else {
                activityClass = FindPointActivity.class;
            }

            final Intent notificationIntent = new Intent(this, activityClass);

            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, 0);

            notification = new NotificationCompat
                    .Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(PRIORITY_LOW)
                    .setSmallIcon(R.mipmap.ic_launcher_notification)
                    .setContentText("Press \"Off\" when not in use to conserve battery")
                    .setContentTitle(StringLiterals.APP_NAME)
                    .setContentIntent(pendingIntent)
                    .build();

            notification.flags |= FOREGROUND_FLAGS;
        }

        return notification;
    }

    protected void createNotificationChannel(String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel == null) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(channelDescription);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
