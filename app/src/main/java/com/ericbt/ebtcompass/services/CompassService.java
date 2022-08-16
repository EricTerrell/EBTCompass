/*
  EBT Compass
  (C) Copyright 2022, Eric Bergman-Terrell

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

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.ericbt.ebtcompass.StringLiterals;

import java.util.HashMap;
import java.util.Map;

public class CompassService extends BaseService implements SensorEventListener {
    private SensorManager sensorManager;

    public static final String ACCELEROMETER_MESSAGE = "ACCELEROMETER_MESSAGE";
    public static final String MAGNETOMETER_MESSAGE = "MAGNETOMETER_MESSAGE";

    public static final String READING = "READING";
    public static final String ACCURACY = "ACCURACY";

    public static final String SENSOR_UPDATE_FREQUENCY = "sensor_update_frequency";

    public CompassService() {
        binder = new CompassServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        startUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopUpdates();
    }

    private void startUpdates() {
        final Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final int delay = getDelay();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, delay, delay);
        }

        final Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, delay, delay);
        }
    }

    private int getDelay() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        final String updateFrequency = preferences.getString(SENSOR_UPDATE_FREQUENCY, "0");

        final Map<String, Integer> map = new HashMap<>();
        map.put("0", SensorManager.SENSOR_DELAY_NORMAL);
        map.put("1", SensorManager.SENSOR_DELAY_UI);
        map.put("2", SensorManager.SENSOR_DELAY_GAME);
        map.put("3", SensorManager.SENSOR_DELAY_FASTEST);

        return map.get(updateFrequency);
    }

    private void stopUpdates() {
        sensorManager.unregisterListener(this);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] reading = new float[3];
        System.arraycopy(sensorEvent.values, 0, reading, 0, reading.length);

        int accuracy = sensorEvent.accuracy;

        String message = StringLiterals.EMPTY_STRING;

        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {
                message = ACCELEROMETER_MESSAGE;
            }
            break;

            case Sensor.TYPE_MAGNETIC_FIELD: {
                message = MAGNETOMETER_MESSAGE;
            }
            break;
        }

        final Intent intent = new Intent(message);

        intent.putExtra(READING, reading);
        intent.putExtra(ACCURACY, accuracy);

        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public class CompassServiceBinder extends Binder {
        public CompassService getService() {
            return CompassService.this;
        }
    }
}
