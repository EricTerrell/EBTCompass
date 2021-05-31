package com.ericbt.ebtcompass.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.ericbt.ebtcompass.Turn;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.Vibrator;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GPSService extends BaseService {
    private int signalInterval;

    private final List<Turn.Direction> directions = new ArrayList<>();

    public static final String MESSAGE = "GPS_MESSAGE";

    // Value keys:
    public static final String LATITUDE      = "LATITUDE";
    public static final String LONGITUDE     = "LONGITUDE";
    public static final String ACCURACY      = "ACCURACY";
    public static final String ALTITUDE      = "ALTITUDE";
    public static final String BEARING       = "BEARING";
    public static final String SPEED         = "SPEED";
    public static final String TIME          = "TIME";
    public static final String GO_TO_HEADING = "GO_TO_HEADING";

    public static final String GPS_UPDATE_FREQUENCY = "gps_update_frequency";
    public static final String VIBRATION_FREQUENCY = "vibration_frequency";

    private Float goToHeading = null;

    private long lastTime = 0;

    private long lastSignalTime = 0;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationCallback locationCallback;

    private SharedPreferences preferences;

    public GPSService() {
        binder = new GPSServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(StringLiterals.LOG_TAG, "GPSService.onCreate");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        signalInterval = getSignalInterval();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                onNewLocation(locationResult.getLastLocation());
            }
        };

        startUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(),
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void onNewLocation(Location location) {
        if (lastTime == 0 || lastSignalTime == 0) {
            lastTime = location.getTime();
            lastSignalTime = location.getTime();
        } else {
            lastTime = location.getTime();
        }

        if (goToHeading != null) {
            directions.add(Turn.getDirection(goToHeading.floatValue(), location.getBearing()));

            if (timeToSignal()) {
                signal();
            }
        }

        sendMessage(location);
    }

    private boolean timeToSignal() {
        final long elapsedTime = lastTime - lastSignalTime;

        return goToHeading != null && elapsedTime >= signalInterval;
    }

    private void signal() {
        Log.i(StringLiterals.LOG_TAG, "signal");

        final Turn.Direction direction = Turn.getMostCommonDirection(directions);

        directions.clear();
        lastSignalTime = lastTime;

        Vibrator.vibrateForTurn(direction, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopUpdates();
    }

    public void startForeground() {
        startForeground(NOTIFICATION_ID, createDefaultNotification());
    }

    public void stopService(Context context, ServiceConnection serviceConnection) {
        try {
            context.unbindService(serviceConnection);
        } catch (Throwable ex) {
            Log.i(StringLiterals.LOG_TAG, ex.toString());
        }

        this.stopForeground(true);
        this.stopSelf();
    }

    private LocationRequest createLocationRequest() {
        final int updateInterval = getUpdateInterval();

        LocationRequest locationRequest = LocationRequest.create();

        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(updateInterval / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    private int getUpdateInterval() {
        final String updateFrequency = preferences.getString(GPS_UPDATE_FREQUENCY, "1");

        return 1000 / Integer.parseInt(updateFrequency);
    }

    private int getSignalInterval() {
        final String signalInterval = preferences.getString(VIBRATION_FREQUENCY, "12");

        final int result = (60 / Integer.parseInt(signalInterval)) * 1000;
        Log.i(StringLiterals.LOG_TAG, String.format(LocaleUtils.getDefaultLocale(), "getSignalInterval interval: %d", result));

        return result;
    }

    private void sendMessage(Location location) {
        final Intent intent = new Intent(MESSAGE);

        intent.putExtra(LATITUDE, location.getLatitude());
        intent.putExtra(LONGITUDE, location.getLongitude());
        intent.putExtra(ACCURACY, location.getAccuracy());
        intent.putExtra(ALTITUDE, location.getAltitude());
        intent.putExtra(BEARING, location.getBearing());
        intent.putExtra(SPEED, location.getSpeed());
        intent.putExtra(TIME, location.getTime());

        if (goToHeading != null) {
            intent.putExtra(GO_TO_HEADING, goToHeading.floatValue());
        }

        sendBroadcast(intent);
    }

    public class GPSServiceBinder extends Binder {
        public GPSService getService() {
            return GPSService.this;
        }
    }

    public void setGoToHeading(Float goToHeading) {
        this.goToHeading = goToHeading;

        lastTime = lastSignalTime = new Date().getTime();
        directions.clear();
    }

    public Float getGoToHeading() {
        return goToHeading;
    }
}
