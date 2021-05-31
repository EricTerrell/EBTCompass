package com.ericbt.ebtcompass.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericbt.ebtcompass.services.GPSService;
import com.ericbt.ebtcompass.utils.AngleUtils;
import com.ericbt.ebtcompass.services.CompassService;
import com.ericbt.ebtcompass.ui.CompassRose;
import com.ericbt.ebtcompass.utils.CompassUtils;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.SensorUtils;
import com.ericbt.ebtcompass.utils.MathUtils;
import com.ericbt.ebtcompass.utils.UnitUtils;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;

public class MainActivity extends AppCompatActivity {
    public Float restoreGoToHeading;

    private Float goToHeading;

    private CompassService compassService;

    private GPSService gpsService;

    private final static int REQUEST_PERMISSIONS_CODE = 1000;

    private float[] accelerometerReading = null;
    private float[] magnetometerReading = null;

    private int magnetometerAccuracy = -1, accelerometerAccuracy = -1;

    private float declination;

    private Button onOffButton, goLineButton, calibrate;

    private double latitude, longitude;

    private SharedPreferences preferences;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private TextView altitudeTV, speedTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Stay in portrait orientation - doesn't really work in landscape.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        preferenceChangeListener =
                (sharedPreferences, key) -> {
                    Log.i(StringLiterals.LOG_TAG,
                            String.format("onSharedPreferenceChanged: key: %s", key));

                    if (key.equals(StringLiterals.PREFERENCE_KEY_UNITS)) {
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

                            case GPSService.VIBRATION_FREQUENCY:
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
                };

        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        createBroadcastReceiver();

        onOffButton = findViewById(R.id.on_off);
        onOffButton.setText(StringLiterals.OFF);

        onOffButton.setOnClickListener(view -> {
            if (onOffButton.getText().equals(StringLiterals.ON)) {
                onOffButton.setText(StringLiterals.OFF);
                startUpdates();
            } else {
                onOffButton.setText(StringLiterals.ON);
                stopUpdates();
            }
        });

        final Button goPointButton = findViewById(R.id.go_point);

        goPointButton.setOnClickListener(view -> {
            GoToPointActivity.initialLatitude = latitude;
            GoToPointActivity.initialLongitude = longitude;

            startActivity(new Intent(this, GoToPointActivity.class));
        });

        final ActivityResultLauncher<Intent> goLineActivityResultLauncher =
                registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                final Intent data = result.getData();

                                if (data != null && data.hasExtra(StringLiterals.HEADING)) {
                                    final float goToHeading =
                                            data.getFloatExtra(StringLiterals.HEADING, 0.0f);

                                    if (gpsService != null) {
                                        gpsService.setGoToHeading(goToHeading);
                                    }
                                } else {
                                    if (gpsService != null) {
                                        gpsService.setGoToHeading(null);
                                    }
                                }
                            }
                        });

        goLineButton = findViewById(R.id.go_line);

        goLineButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, GoLineActivity.class);

            if (goToHeading != null) {
                intent.putExtra(GPSService.GO_TO_HEADING, goToHeading.floatValue());
            }

            goLineActivityResultLauncher.launch(intent);
        });

        calibrate = findViewById(R.id.calibrate);

        calibrate.setOnClickListener(view -> {
            displayCalibrateMessage();

            calibrate.setText(getString(R.string.calibrate));
        });

        requestPermissions();

        promptUserToAcceptLicenseTerms();

        startUpdates();
    }

    private void promptUserToAcceptLicenseTerms() {
        final boolean userAcceptedTerms = preferences.getBoolean(StringLiterals.USER_ACCEPTED_TERMS, false);

        // Prompt user to accept license terms if they have not been previously accepted.
        if (!userAcceptedTerms) {
            final ActivityResultLauncher<Intent> licenseTermsActivityResultLauncher =
                    registerForActivityResult(
                            new ActivityResultContracts.StartActivityForResult(),
                            result -> {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    final Intent data = result.getData();

                                    if (data != null && data.getBooleanExtra(StringLiterals.EXIT, false)) {
                                        MainActivity.this.stopUpdates();

                                        finish();

                                        System.exit(0);
                                    }
                                }
                            });

            final Intent licenseTermsIntent = new Intent(this, LicenseTermsActivity.class);
            licenseTermsIntent.putExtra(StringLiterals.ALLOW_CANCEL, false);
            licenseTermsActivityResultLauncher.launch(licenseTermsIntent);
        }
    }

    private void displayCalibrateMessage() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getText(R.string.title_dialog_calibrate));
        alertDialogBuilder.setMessage(getText(R.string.text_dialog_calibrate));

        alertDialogBuilder.setPositiveButton(StringLiterals.OK, (dialog, which) -> {
        });

        final AlertDialog promptDialog = alertDialogBuilder.create();
        promptDialog.setCancelable(false);
        promptDialog.show();
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

    private void startCompassService() {
        if (compassService == null) {
            final Intent intent = new Intent(getApplicationContext(), CompassService.class);

            // Create service if it's not already alive.
            bindService(intent, compassServiceConnection, Context.BIND_AUTO_CREATE);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                startService(intent);
            } else {
                startForegroundService(intent);
            }
        }
    }

    private void stopCompassService() {
        Log.i(StringLiterals.LOG_TAG, "stopCompassService");

        if (compassService != null) {
            compassService.stopService(this, compassServiceConnection);
            compassService = null;
        }
    }

    private void startGPSService() {
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

    private void stopGPSService() {
        Log.i(StringLiterals.LOG_TAG, "stopGPSService");

        if (gpsService != null) {
            gpsService.stopService(this, gpsServiceConnection);
            gpsService = null;
        }
    }

    private void startUpdates() {
        Log.i(StringLiterals.LOG_TAG, "startUpdates");

        if (havePermissions()) {
            goLineButton.setEnabled(true);

            startCompassService();
            startGPSService();
        }
    }

    private void stopUpdates() {
        Log.i(StringLiterals.LOG_TAG, "stopUpdates");

        if (havePermissions()) {
            goLineButton.setEnabled(false);

            stopCompassService();
            stopGPSService();
        }
    }

    private boolean havePermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
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

    private void updateOrientationAngles(float[] accelerometerReading, float[] magnetometerReading) {
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

            float[] orientationAngles = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            final float azimuth = (float) Math.toDegrees(orientationAngles[0]);

            final ImageView compassRoseCustom = findViewById(R.id.compass_rose_custom);

            /*
            One adds the declination to the heading get the true value. Rationale: consider SW
            Colorado. Declination is 9 degrees east of N. Since it's east of N, it's positive. When
            the sensor registers north, true north is 9 degrees east, so we add the declination to
            get the true heading.
             */
            final float correctedAzimuth = MathUtils.normalizeAngle(azimuth + declination);

            final CompassRose compassRoseDrawable = new CompassRose(orientationAngles[1], orientationAngles[2], correctedAzimuth);

            compassRoseCustom.setRotation(-correctedAzimuth);
            compassRoseCustom.setImageDrawable(compassRoseDrawable);

            final TextView heading = findViewById(R.id.heading);

            if (correctedAzimuth >= 0.0f) {
                heading.setText(String.format(LocaleUtils.getDefaultLocale(), "Compass: %d° %s",
                        (int) correctedAzimuth,
                        AngleUtils.formatBearing(correctedAzimuth)));
            } else {
                heading.setText(StringLiterals.EMPTY_STRING);
            }
        }
    }

    private final ServiceConnection compassServiceConnection = new ServiceConnection() {
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

    private final ServiceConnection gpsServiceConnection = new ServiceConnection() {
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

    private BroadcastReceiver createBroadcastReceiver() {
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
                            latitude = intent.getDoubleExtra(GPSService.LATITUDE, default_float);
                            longitude = intent.getDoubleExtra(GPSService.LONGITUDE, default_float);
                            final double altitude = intent.getDoubleExtra(GPSService.ALTITUDE, default_float);
                            final double bearing = intent.getFloatExtra(GPSService.BEARING, default_float);
                            final double speed = intent.getFloatExtra(GPSService.SPEED, default_float);
                            final long time = intent.getLongExtra(GPSService.TIME, default_long);

                            if (intent.hasExtra(GPSService.GO_TO_HEADING)) {
                                goToHeading = intent.getFloatExtra(GPSService.GO_TO_HEADING, 0);
                            } else {
                                goToHeading = null;
                            }

                            updateUI(latitude, longitude, altitude, bearing, speed, time, goToHeading);
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

    private void updateUI() {
        if (accelerometerAccuracy != -1 && magnetometerAccuracy != -1 &&
                (accelerometerAccuracy != SENSOR_STATUS_ACCURACY_HIGH ||
                magnetometerAccuracy != SENSOR_STATUS_ACCURACY_HIGH)) {
            calibrate.setText(getString(R.string.calibrate_exclamation_point));
        }

        final TextView accuracyTV = findViewById(R.id.accuracy);
        accuracyTV.setText(String.format(LocaleUtils.getDefaultLocale(),
                "Accuracy: %s/%s",
                SensorUtils.getAccuracyText(accelerometerAccuracy),
                SensorUtils.getAccuracyText(magnetometerAccuracy)));

        updateOrientationAngles(accelerometerReading, magnetometerReading);
    }

    private void updateUI(double latitude, double longitude, double altitude, double bearing,
                          double speed, long time, Float goToHeading) {
        final TextView latitudeTV = findViewById(R.id.latitude);
        latitudeTV.setText(String.format("%s %s",
                AngleUtils.toDMS(Math.abs((float) latitude)),
                AngleUtils.latitudeDirection(latitude)));

        final TextView longitudeTV = findViewById(R.id.longitude);
        longitudeTV.setText(String.format("%s %s",
                AngleUtils.toDMS(Math.abs(longitude)),
                AngleUtils.longitudeDirection(longitude)));

        final String units = preferences.getString(StringLiterals.PREFERENCE_KEY_UNITS, StringLiterals.METRIC);

        String altitudeText;

        if (units.equals(StringLiterals.METRIC)) {
            altitudeText = String.format(LocaleUtils.getDefaultLocale(), "%,d m",
                    (int) altitude);
        } else {
            altitudeText = String.format(LocaleUtils.getDefaultLocale(), "%,d ft",
                    (int) UnitUtils.toFeet(altitude));
        }

        altitudeTV = findViewById(R.id.altitude);
        altitudeTV.setText(altitudeText);

        String speedText;

        if (units.equals(StringLiterals.METRIC)) {
            speedText = String.format(LocaleUtils.getDefaultLocale(), "%,.1f km/h", UnitUtils.toKilometersPerHour(speed));
        } else {
            speedText = String.format(LocaleUtils.getDefaultLocale(), "%,.1f mi/h", UnitUtils.toMilesPerHour(speed));
        }

        speedTV = findViewById(R.id.speed);
        speedTV.setText(speedText);

        final GeomagneticField geomagneticField = new GeomagneticField(
                (float) latitude,
                (float) longitude,
                (float) altitude,
                time);

        declination = geomagneticField.getDeclination();

        final TextView declinationTV = findViewById(R.id.declination);
        declinationTV.setText(String.format(LocaleUtils.getDefaultLocale(),
                "Mag Decl: %.1f° %s",
                declination,
                CompassUtils.getDeclinationDirection(declination)));

        final TextView gpsHeading = findViewById(R.id.gpsHeading);
        gpsHeading.setText(String.format(LocaleUtils.getDefaultLocale(), "GPS: %d° (%s)",
                (int) bearing,
                AngleUtils.formatBearing(bearing)));

        final TextView lineHeading = findViewById(R.id.line_heading);

        if (goToHeading != null) {
            lineHeading.setText(String.format(LocaleUtils.getDefaultLocale(), "Line: %.1f° (%s)",
                    goToHeading.floatValue(),
                    AngleUtils.formatBearing(goToHeading.floatValue())));
        } else {
            lineHeading.setText(StringLiterals.EMPTY_STRING);
        }
    }

    private void clearQuantities() {
        altitudeTV.setText(StringLiterals.EMPTY_STRING);
        speedTV.setText(StringLiterals.EMPTY_STRING);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean result = false;

        final int itemId = item.getItemId();

        switch(itemId) {
            case R.id.help: {
                final String url = getString(R.string.help_url);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                result = true;
            }
            break;

            case R.id.about: {
                startActivity(new Intent(this, AboutActivity.class));
                result = true;
            }
            break;

            case R.id.settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                result = true;
            }
            break;
        }

        return result;
    }
}