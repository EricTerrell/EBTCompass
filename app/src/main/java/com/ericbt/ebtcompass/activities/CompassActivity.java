package com.ericbt.ebtcompass.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.services.CompassService;
import com.ericbt.ebtcompass.services.GPSService;
import com.ericbt.ebtcompass.ui.CompassRose;
import com.ericbt.ebtcompass.utils.GoogleMapsUtils;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.MathUtils;
import com.ericbt.ebtcompass.utils.SensorUtils;

public abstract class CompassActivity extends CustomActivity {
    private final static int REQUEST_PERMISSIONS_CODE = 1000;

    protected Float bearingToDestination;

    protected CompassService compassService;

    protected GPSService gpsService;

    protected float[] accelerometerReading = null;
    protected float[] magnetometerReading = null;

    protected Float goToHeading;

    protected double latitude, longitude, lastLatitude, lastLongitude, altitude;

    protected long time;

    protected int magnetometerAccuracy = -1, accelerometerAccuracy = -1;

    public Float restoreGoToHeading;

    protected MenuItem share;

    protected BroadcastReceiver broadcastReceiver;

    private String accelerometerAccuracyText, magnetometerAccuracyText;

    private void updateUI() {
        updateAccuracyLink();

        updateOrientationAngles(accelerometerReading, magnetometerReading);
    }

    private void updateAccuracyLink() {
        final String currentAccelerometerAccuracyText = SensorUtils.getAccuracyText(accelerometerAccuracy);
        final String currentMagnetometerAccuracyText = SensorUtils.getAccuracyText(magnetometerAccuracy);

        if (!
                (
                        currentAccelerometerAccuracyText.equals(accelerometerAccuracyText) &&
                        currentMagnetometerAccuracyText.equals(magnetometerAccuracyText)
                )
           ) {
            final String htmlString = String.format(LocaleUtils.getDefaultLocale(),
                    "<a href='%s'>Accuracy: %s/%s</a>",
                    getString(R.string.accuracy_url),
                    SensorUtils.getAccuracyText(accelerometerAccuracy),
                    SensorUtils.getAccuracyText(magnetometerAccuracy));

            final TextView accuracyTV = findViewById(R.id.accuracy);

            accuracyTV.setText(Html.fromHtml(htmlString));
            accuracyTV.setMovementMethod(LinkMovementMethod.getInstance());

            accelerometerAccuracyText = currentAccelerometerAccuracyText;
            magnetometerAccuracyText = currentMagnetometerAccuracyText;
        }
    }

    protected abstract void clearQuantities();

    protected abstract void updateUI(double latitude, double longitude, double bearing,
                                     double speed, Float goToHeading);

    protected boolean havePermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestPermissions() {
        Log.i(StringLiterals.LOG_TAG, "requestPermissions");

        if (!havePermissions()) {
            final String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            // Checking whether user granted the permission or not.
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                displayPermissionsDeniedMessage();
            } else {
                startUpdates();
            }
        }
    }

    private void displayPermissionsDeniedMessage() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.permissions));
        alertDialogBuilder.setMessage(getString(R.string.permissions_not_granted));

        alertDialogBuilder.setPositiveButton(StringLiterals.OK, (dialog, which) -> {
        });

        final AlertDialog promptDialog = alertDialogBuilder.create();
        promptDialog.setCancelable(false);
        promptDialog.show();
    }

    protected void startCompassService() {
        if (compassService == null) {
            final Intent intent = new Intent(getApplicationContext(), CompassService.class);

            final Bundle bundle = new Bundle();
            bundle.putString(StringLiterals.ACTIVITY_NAME, this.getClass().getName());

            intent.putExtras(bundle);

            // Create service if it's not already alive.
            bindService(intent, compassServiceConnection, Context.BIND_AUTO_CREATE);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                startService(intent);
            } else {
                startForegroundService(intent);
            }
        }
    }

    protected void stopCompassService() {
        Log.i(StringLiterals.LOG_TAG, "stopCompassService");

        if (compassService != null) {
            compassService.stopService(this, compassServiceConnection);
            compassService = null;
        }
    }

    protected void startGPSService() {
        Log.i(StringLiterals.LOG_TAG, "startGPSService");

        if (gpsService == null) {
            final Intent intent = new Intent(getApplicationContext(), GPSService.class);

            // Create service if it's not already alive.
            bindService(intent, gpsServiceConnection, Context.BIND_AUTO_CREATE);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                startService(intent);
            } else {
                startForegroundService(intent);
            }
        }
    }

    protected void stopGPSService() {
        Log.i(StringLiterals.LOG_TAG, "stopGPSService");

        if (share != null) {
            share.setVisible(false);
        }

        latitude = longitude = 0.0f;

        if (gpsService != null) {
            gpsService.stopService(this, gpsServiceConnection);
            gpsService = null;
        }
    }

    protected void startUpdates() {
        Log.i(StringLiterals.LOG_TAG, "startUpdates");

        if (havePermissions()) {
            startCompassService();
            startGPSService();
        }
    }

    protected void stopUpdates() {
        Log.i(StringLiterals.LOG_TAG, "stopUpdates");

        if (havePermissions()) {
            stopCompassService();
            stopGPSService();
        }
    }

    protected final ServiceConnection compassServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(StringLiterals.LOG_TAG, "CompassService onServiceDisconnected");

            compassService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(StringLiterals.LOG_TAG, "CompassService onServiceConnected");

            final CompassService.CompassServiceBinder compassServiceBinder = (CompassService.CompassServiceBinder) service;
            compassService = compassServiceBinder.getService();

            compassService.startForeground();
        }
    };

    protected final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(StringLiterals.LOG_TAG, "GPSService onServiceDisconnected");

            gpsService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(StringLiterals.LOG_TAG, "GPSService onServiceConnected");

            final GPSService.GPSServiceBinder gpsServiceBinder = (GPSService.GPSServiceBinder) service;
            gpsService = gpsServiceBinder.getService();

            gpsService.startForeground();

            gpsService.setGoToHeading(restoreGoToHeading);
            restoreGoToHeading = null;
        }
    };

    @Override
    public void onDestroy() {
        Log.i(StringLiterals.LOG_TAG, "CompassActivity.onDestroy");

        super.onDestroy();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        try {
            unbindService(compassServiceConnection);
            unbindService(gpsServiceConnection);
        } catch (Exception ex) {
            Log.e(StringLiterals.LOG_TAG, ex.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        share = menu.findItem(R.id.share);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean result = false;

        final int itemId = item.getItemId();

        if (itemId == R.id.help) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url))));
            result = true;
        } else if (itemId == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
            result = true;
        } else if (itemId == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            result = true;
        } else if (itemId == R.id.share) {
            shareLocation();

            return true;
        }

        return result;
    }

    private void shareLocation() {
        final String uri = GoogleMapsUtils.getMapUri(latitude, longitude);

        Log.i(StringLiterals.LOG_TAG, String.format("shareLocation: %s", uri));

        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.my_location));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, uri);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }

    protected void onSharedPreferenceChanged(String key) {
        Log.i(StringLiterals.LOG_TAG,
                String.format(
                        LocaleUtils.getDefaultLocale(),
                        "onSharedPreferenceChanged: key: %s",
                        key));

        if (key.equals(StringLiterals.PREFERENCE_KEY_DISTANCE_UNITS)) {
            clearQuantities();
        }

        if (havePermissions()) {
            switch (key) {
                case CompassService.SENSOR_UPDATE_FREQUENCY: {
                    if (compassService != null) {
                        Log.i(StringLiterals.LOG_TAG, "restart CompassService");

                        stopCompassService();
                        startCompassService();
                    }
                }
                break;

                case GPSService.NOTIFICATION_FREQUENCY:
                case GPSService.GPS_UPDATE_FREQUENCY: {
                    if (gpsService != null) {
                        Log.i(StringLiterals.LOG_TAG, "restart GPSService");

                        restoreGoToHeading = gpsService.getGoToHeading();

                        stopGPSService();
                        startGPSService();
                    }
                }
                break;
            }
        }
    }

    protected BroadcastReceiver createBroadcastReceiver() {
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isInitialStickyBroadcast()) {
                    final int default_int = 0;
                    final long default_long = 0;
                    final float default_float = 0f;

                    switch (intent.getAction()) {
                        case CompassService.ACCELEROMETER_MESSAGE: {
                            accelerometerAccuracy = intent.getIntExtra(CompassService.ACCURACY, default_int);
                            accelerometerReading = intent.getFloatArrayExtra(CompassService.READING);

                            updateUI();
                        }
                        break;

                        case CompassService.MAGNETOMETER_MESSAGE: {
                            magnetometerAccuracy = intent.getIntExtra(CompassService.ACCURACY, default_int);
                            magnetometerReading = intent.getFloatArrayExtra(CompassService.READING);

                            updateUI();
                        }
                        break;

                        case GPSService.MESSAGE: {
                            latitude = lastLatitude = intent.getDoubleExtra(GPSService.LATITUDE, default_float);
                            longitude = lastLongitude = intent.getDoubleExtra(GPSService.LONGITUDE, default_float);
                            altitude = intent.getDoubleExtra(GPSService.ALTITUDE, default_float);
                            final double bearing = intent.getFloatExtra(GPSService.BEARING, default_float);
                            final double speed = intent.getFloatExtra(GPSService.SPEED, default_float);
                            time = intent.getLongExtra(GPSService.TIME, default_long);

                            if (intent.hasExtra(GPSService.GO_TO_HEADING)) {
                                goToHeading = intent.getFloatExtra(GPSService.GO_TO_HEADING, 0);
                            } else {
                                goToHeading = null;
                            }

                            updateUI(latitude, longitude, bearing, speed, goToHeading);
                        }
                        break;
                    }
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CompassService.ACCELEROMETER_MESSAGE);
        intentFilter.addAction(CompassService.MAGNETOMETER_MESSAGE);
        intentFilter.addAction(GPSService.MESSAGE);

        registerReceiver(broadcastReceiver, intentFilter);

        return broadcastReceiver;
    }

    protected double updateOrientationAngles(float[] accelerometerReading, float[] magnetometerReading) {
        float correctedAzimuth = 0.0f;

        if (accelerometerReading != null) {
            this.accelerometerReading = accelerometerReading;
        }

        if (magnetometerReading != null) {
            this.magnetometerReading = magnetometerReading;
        }

        if (this.accelerometerReading != null && this.magnetometerReading != null) {
            final float[] rotationMatrix = new float[9];

            SensorManager.getRotationMatrix(rotationMatrix, null,
                    this.accelerometerReading, this.magnetometerReading);

            final float[] orientationAngles = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            final float azimuth = (float) Math.toDegrees(orientationAngles[0]);

            final ImageView compassRoseCustom = findViewById(R.id.compass_rose_custom);

            final GeomagneticField geomagneticField = new GeomagneticField(
                    (float) latitude,
                    (float) longitude,
                    (float) altitude,
                    time);

            final float declination = geomagneticField.getDeclination();

            /*
            One adds the declination to the heading get the true value. Rationale: consider SW
            Colorado. Declination is 9 degrees E of N, so magnetic north is also 9 degrees E of N.

            Since declination is E of N, it's positive. When the sensor registers N, true north is
            9 degrees E, so one adds the declination to get the true heading.

            When declination is W of N, it's negative, so adding such a declination subtracts.
             */
            correctedAzimuth = MathUtils.normalizeAngle(azimuth + declination);

            final CompassRose compassRoseDrawable =
                    new CompassRose(
                            orientationAngles[1],
                            orientationAngles[2],
                            correctedAzimuth,
                            bearingToDestination);

            compassRoseCustom.setRotation(-correctedAzimuth);
            compassRoseCustom.setImageDrawable(compassRoseDrawable);
        }

        return correctedAzimuth;
    }
}
