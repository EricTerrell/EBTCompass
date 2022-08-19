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

package com.ericbt.ebtcompass.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.Speaker;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.Vibrator;
import com.ericbt.ebtcompass.utils.AngleUtils;
import com.ericbt.ebtcompass.utils.AnimationUtils;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.MathUtils;
import com.ericbt.ebtcompass.utils.UnitUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class FindPointActivity extends CompassActivity {
    private static final String SUPPRESS_ARRIVAL_MESSAGE = "SUPPRESS_ARRIVAL_MESSAGE";

    private boolean suppressArrivalMessage;

    private static final double CLOSE_ENOUGH_IN_METERS = 10;

    private Button onOffButton;

    private double destinationLatitude, destinationLongitude;

    private SharedPreferences preferences;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private Location destinationLocation;

    private TextToSpeech textToSpeech;

    private boolean speechReady = false;

    private ImageView compassRose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_point);

        compassRose = findViewById(R.id.compass_rose_custom);

        if (savedInstanceState != null) {
            suppressArrivalMessage = savedInstanceState.getBoolean(SUPPRESS_ARRIVAL_MESSAGE, false);
        }

        destinationLatitude = getIntent().getDoubleExtra(StringLiterals.LATITUDE, 0.0f);
        destinationLongitude = getIntent().getDoubleExtra(StringLiterals.LONGITUDE, 0.0f);

        final String name = getIntent().getStringExtra(StringLiterals.NAME);
        final TextView pointName = findViewById(R.id.point_name);

        if (name != null) {
            pointName.setText(name);
        } else {
            pointName.setVisibility(View.GONE);
        }

        destinationLocation = new Location(StringLiterals.EMPTY_STRING);
        destinationLocation.setLatitude(destinationLatitude);
        destinationLocation.setLongitude(destinationLongitude);

        Log.i(StringLiterals.LOG_TAG,
                String.format(
                        LocaleUtils.getLocale(),
                        "destinationLatitude: %f destinationLongitude: %f",
                        destinationLatitude,
                        destinationLongitude));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        preferenceChangeListener =
                (sharedPreferences, key) -> {
                    onSharedPreferenceChanged(key);
                };

        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        broadcastReceiver = createBroadcastReceiver();

        onOffButton = findViewById(R.id.on_off);
        onOffButton.setText(getString(R.string.off_button_text));

        onOffButton.setOnClickListener(view -> {
            if (onOffButton.getText().equals(getString(R.string.on_button_text))) {
                startUpdates();
            } else {
                stopUpdates();
            }
        });

        startTTS();

        startUpdates();
    }

    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SUPPRESS_ARRIVAL_MESSAGE, suppressArrivalMessage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopTTS();
    }

    @Override
    protected void startUpdates() {
        Log.i(StringLiterals.LOG_TAG, "startUpdates");

        compassRose.startAnimation(AnimationUtils.getFadeInAnimation());

        super.startUpdates();

        if (haveAllPermissions()) {
            onOffButton.setText(getString(R.string.off_button_text));
        }
    }

    @Override
    protected void stopUpdates() {
        Log.i(StringLiterals.LOG_TAG, "stopUpdates");

        compassRose.startAnimation(AnimationUtils.getFadeOutAnimation());

        super.stopUpdates();

        if (haveAllPermissions()) {
            onOffButton.setText(getString(R.string.on_button_text));
        }
    }

    @Override
    protected double updateOrientationAngles(float[] accelerometerReading, float[] magnetometerReading) {
        final double correctedAzimuth = super.updateOrientationAngles(accelerometerReading, magnetometerReading);

        final TextView compass = findViewById(R.id.compass);

        if (correctedAzimuth >= 0.0f) {
            final String degreeSymbol = getString(R.string.degree_symbol);

            final String compassText =
                    String.format(LocaleUtils.getLocale(),
                    getString(R.string.update_orientation_angles_format_string),
                    (int) correctedAzimuth,
                    degreeSymbol,
                    AngleUtils.formatBearing(correctedAzimuth, this));

            compass.setText(compassText);
        } else {
            compass.setText(StringLiterals.EMPTY_STRING);
        }

        return correctedAzimuth;
    }

    @Override
    protected void updateUI(double latitude, double longitude, double bearing,
                            double speed, Float goToHeading) {

        updateUI(latitude, longitude);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopUpdates();
    }

    private void updateUI(double latitude, double longitude) {
        if (share != null && !share.isVisible() &&
                latitude != 0.0f && longitude != 0.0f) {
            share.setVisible(true);
        }

        final Location currentLocation = new Location(StringLiterals.EMPTY_STRING);
        currentLocation.setLatitude(latitude);
        currentLocation.setLongitude(longitude);

        bearingToDestination = MathUtils.normalizeAngle(currentLocation.bearingTo(destinationLocation));

        float distanceInMeters = currentLocation.distanceTo(destinationLocation);

        final String distanceText = getDistanceText(distanceInMeters);
        final String degreeSymbol = getString(R.string.degree_symbol);

        final String routeText = String.format(
                LocaleUtils.getLocale(),
                getString(R.string.find_point_activity_go_instructions_format_string),
                distanceText,
                (int) bearingToDestination.floatValue(),
                degreeSymbol,
                AngleUtils.formatBearing(bearingToDestination, this));

        final TextView route = findViewById(R.id.route);

        route.setText(routeText);

        final TextView currentLocationTV = findViewById(R.id.current_location);
        final String currentLocationText =
                String.format(
                        LocaleUtils.getLocale(),
                        getString(R.string.find_point_activity_current_location_format_string),
                        AngleUtils.toDMS(Math.abs(latitude), this),
                        AngleUtils.latitudeDirection(latitude, this),
                        AngleUtils.toDMS(Math.abs(longitude), this),
                        AngleUtils.longitudeDirection(longitude, this)
                );

        currentLocationTV.setText(currentLocationText);

        final TextView currentLocationUTM = findViewById(R.id.current_location_utm);
        currentLocationUTM.setText(AngleUtils.formatUTM(latitude, longitude, this));

        final TextView destinationTV = findViewById(R.id.destination);
        final String destinationText =
                String.format(
                        LocaleUtils.getLocale(),
                        getString(R.string.find_point_activity_destination_text_format_string),
                        AngleUtils.toDMS(Math.abs(destinationLatitude), this),
                        AngleUtils.latitudeDirection(destinationLatitude, this),
                        AngleUtils.toDMS(Math.abs(destinationLongitude), this),
                        AngleUtils.longitudeDirection(destinationLongitude, this)
                );

        destinationTV.setText(destinationText);

        final TextView destinationUTM = findViewById(R.id.destination_utm);
        destinationUTM.setText(AngleUtils.formatUTM(destinationLatitude, destinationLongitude, this));

        if (!suppressArrivalMessage && distanceInMeters <= CLOSE_ENOUGH_IN_METERS) {
            arrived(getDistanceText(distanceInMeters));
        }
    }

    private void arrived(String distance) {
        suppressArrivalMessage = true;

        stopUpdates();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getText(R.string.you_have_arrived));

        final String message = String.format(
                LocaleUtils.getLocale(),
                getString(R.string.you_are_within_x_of_your_destination_format_string),
                distance);

        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(getString(R.string.ok_button_text), (arg0, arg1) -> {
            // Go all the way back to the main activity.
            final Intent main = new Intent(this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(main);
        });

        alertDialogBuilder.setNegativeButton(getString(R.string.keep_going), (arg0, arg1) -> {
            startUpdates();
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        notifyArrival();
    }

    private void notifyArrival() {
        final String notificationMechanism =
                preferences.getString(StringLiterals.ARRIVAL_NOTIFICATION, StringLiterals.SPEECH);

        switch(notificationMechanism) {
            case StringLiterals.SPEECH: {
                if (speechReady) {
                    Speaker.speakForArrival(textToSpeech, this);
                }
            }
            break;

            case StringLiterals.VIBRATION: {
                Vibrator.vibrateForArrival(this);
            }
            break;
        }
    }

    private String getDistanceText(float distanceInMeters) {
        final String distance_units = preferences.getString(StringLiterals.PREFERENCE_KEY_DISTANCE_UNITS, StringLiterals.METRIC);

        final String distanceText;

        if (distance_units.equals(StringLiterals.METRIC)) {
            if (distanceInMeters < 1000.0f) {
                distanceText = String.format(
                        LocaleUtils.getLocale(),
                        getString(R.string.find_point_activity_distance_meters_format_string),
                        (int) distanceInMeters);
            } else {
                distanceText = String.format(LocaleUtils.getLocale(), getString(R.string.find_point_activity_distance_kilometers_format_string),
                        (distanceInMeters / 1000.0f));
            }
        } else {
            final int feet = (int) UnitUtils.toFeet(distanceInMeters);

            if (feet < 1000) {
                distanceText = String.format(LocaleUtils.getLocale(), getString(R.string.find_point_activity_distance_feet_format_string), feet);
            } else {
                distanceText = String.format(LocaleUtils.getLocale(), getString(R.string.find_point_activity_distance_miles_format_string),
                        UnitUtils.toMiles(feet));
            }
        }

        return distanceText;
    }

    @Override
    protected void clearQuantities() {
        final TextView route = findViewById(R.id.route);

        route.setText(StringLiterals.EMPTY_STRING);
    }

    private void startTTS() {
        Log.i(StringLiterals.LOG_TAG, "startTTS");

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            Log.i(StringLiterals.LOG_TAG,
                    String.format(
                            LocaleUtils.getLocale(),
                            "tts onInit status: %d",
                            status));

            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);

                speechReady = true;
            } else {
                Log.e(StringLiterals.LOG_TAG, "tts onInit ERROR");
            }
        });
    }

    private void stopTTS() {
        Log.i(StringLiterals.LOG_TAG, "stopTTS");

        if (textToSpeech != null) {
            textToSpeech.stop();

            textToSpeech.shutdown();

            textToSpeech = null;
        }
    }

}