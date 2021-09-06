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

package com.ericbt.ebtcompass.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.GeomagneticField;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericbt.ebtcompass.services.GPSService;
import com.ericbt.ebtcompass.utils.AngleUtils;
import com.ericbt.ebtcompass.utils.AnimationUtils;
import com.ericbt.ebtcompass.utils.CompassUtils;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.UnitUtils;

public class MainActivity extends CompassActivity {
    private Button onOffButton, goLineButton, savePointButton;

    private SharedPreferences preferences;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private ImageView compassRose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Don't want the back arrow in the title bar of this activity.
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        compassRose = findViewById(R.id.compass_rose_custom);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        preferenceChangeListener =
                (sharedPreferences, key) -> {
                    onSharedPreferenceChanged(key);
                };

        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        broadcastReceiver = createBroadcastReceiver();

        onOffButton = findViewById(R.id.on_off);
        onOffButton.setText(StringLiterals.OFF);

        onOffButton.setOnClickListener(view -> {
            if (onOffButton.getText().equals(StringLiterals.ON)) {
                startUpdates();
            } else {
                stopUpdates();
            }
        });

        final Button goPointButton = findViewById(R.id.go_point);

        goPointButton.setOnClickListener(view -> {
            stopUpdates();

            final Intent intent = new Intent(this, GoToPointActivity.class);

            final Bundle bundle = new Bundle();
            bundle.putDouble(StringLiterals.LATITUDE, lastLatitude);
            bundle.putDouble(StringLiterals.LONGITUDE, lastLongitude);

            intent.putExtras(bundle);

            startActivity(intent);
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
                                    } else {
                                        restoreGoToHeading = goToHeading;
                                        startUpdates();
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

        savePointButton = findViewById(R.id.save_point);

        savePointButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, SavePointActivity.class);

            final Bundle bundle = new Bundle();
            bundle.putDouble(StringLiterals.LATITUDE, lastLatitude);
            bundle.putDouble(StringLiterals.LONGITUDE, lastLongitude);
            bundle.putDouble(StringLiterals.ALTITUDE, altitude);

            intent.putExtras(bundle);

            startActivity(intent);
        });

        final Button pointsButton = findViewById(R.id.points);

        pointsButton.setOnClickListener(view -> {
            stopUpdates();

            startActivity(new Intent(this, PointsActivity.class));
        });

        requestPermissions();

        promptUserToAcceptLicenseTerms();

        startUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (goToHeading == null) {
            stopUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        goLineButton.setEnabled(false);
        savePointButton.setEnabled(false);
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

    @Override
    protected void startUpdates() {
        Log.i(StringLiterals.LOG_TAG, "startUpdates");

        if (goToHeading != null) {
            restoreGoToHeading = goToHeading;
        }

        compassRose.startAnimation(AnimationUtils.getFadeInAnimation());

        super.startUpdates();

        if (havePermissions()) {
            onOffButton.setText(StringLiterals.OFF);
        }
    }

    @Override
    protected void stopUpdates() {
        Log.i(StringLiterals.LOG_TAG, "stopUpdates");

        compassRose.startAnimation(AnimationUtils.getFadeOutAnimation());

        super.stopUpdates();

        if (havePermissions()) {
            goLineButton.setEnabled(false);
            savePointButton.setEnabled(false);
            onOffButton.setText(StringLiterals.ON);
        }
    }

    @Override
    protected double updateOrientationAngles(float[] accelerometerReading, float[] magnetometerReading) {
        final double correctedAzimuth = super.updateOrientationAngles(accelerometerReading, magnetometerReading);

        final TextView heading = findViewById(R.id.heading);

        if (correctedAzimuth >= 0.0f) {
            heading.setText(String.format(LocaleUtils.getDefaultLocale(), "Compass: %d° %s",
                    (int) correctedAzimuth,
                    AngleUtils.formatBearing(correctedAzimuth)));
        } else {
            heading.setText(StringLiterals.EMPTY_STRING);
        }

        return correctedAzimuth;
    }

    @Override
    protected void updateUI(double latitude, double longitude, double bearing,
                          double speed, Float goToHeading) {
        final TextView latitudeTV = findViewById(R.id.latitude);
        latitudeTV.setText(String.format("%s %s",
                AngleUtils.toDMS(Math.abs(latitude)),
                AngleUtils.latitudeDirection(latitude)));

        final TextView longitudeTV = findViewById(R.id.longitude);
        longitudeTV.setText(String.format("%s %s",
                AngleUtils.toDMS(Math.abs(longitude)),
                AngleUtils.longitudeDirection(longitude)));

        final boolean userPrefersMetric = UnitUtils.userPrefersMetric(this);

        String altitudeText;

        if (userPrefersMetric) {
            altitudeText = String.format(LocaleUtils.getDefaultLocale(), "%,d m",
                    (int) altitude);
        } else {
            altitudeText = String.format(LocaleUtils.getDefaultLocale(), "%,d ft",
                    (int) UnitUtils.toFeet(altitude));
        }

        final TextView altitudeTV = findViewById(R.id.altitude);
        altitudeTV.setText(altitudeText);

        String speedText;

        if (userPrefersMetric) {
            speedText = String.format(LocaleUtils.getDefaultLocale(), "%,.1f km/h", UnitUtils.toKilometersPerHour(speed));
        } else {
            speedText = String.format(LocaleUtils.getDefaultLocale(), "%,.1f mi/h", UnitUtils.toMilesPerHour(speed));
        }

        final TextView speedTV = findViewById(R.id.speed);
        speedTV.setText(speedText);

        final GeomagneticField geomagneticField = new GeomagneticField(
                (float) latitude,
                (float) longitude,
                (float) altitude,
                time);

        final float declination = geomagneticField.getDeclination();

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

        final int[] ids =
                new int[] { R.id.line_heading_2, R.id.line_heading_3, R.id.line_heading_4 };

        if (goToHeading != null) {
            lineHeading.setText(String.format(LocaleUtils.getDefaultLocale(), "Line: %.1f° (%s)",
                    goToHeading.floatValue(),
                    AngleUtils.formatBearing(goToHeading.floatValue())));

            bearingToDestination = goToHeading.floatValue();
        } else {
            lineHeading.setText(StringLiterals.EMPTY_STRING);

            bearingToDestination = null;
        }

        for (int id : ids) {
            findViewById(id).setVisibility(goToHeading == null ? View.GONE : View.VISIBLE);
        }

        if (share != null && !share.isVisible() &&
                latitude != 0.0f && longitude != 0.0f) {
            share.setVisible(true);
        }

        final TextView utm = findViewById(R.id.utm);
        utm.setText(AngleUtils.formatUTM(latitude, longitude));

        goLineButton.setEnabled(true);
        savePointButton.setEnabled(true);
    }

    @Override
    protected void clearQuantities() {
        final int[] ids = new int[] { R.id.altitude, R.id.speed };

        for (final int id: ids) {
            final TextView textView = findViewById(id);

            if (textView != null) {
                textView.setText(StringLiterals.EMPTY_STRING);
            }
        }
    }
}