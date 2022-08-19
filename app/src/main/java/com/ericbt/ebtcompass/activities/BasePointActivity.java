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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.preference.PreferenceManager;

import com.ericbt.ebtcompass.Constants;
import com.ericbt.ebtcompass.InputFilterMinMax;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ibm.util.CoordinateConversion;

import java.util.Arrays;
import java.util.List;

public class BasePointActivity extends CustomActivity {
    private final CoordinateConversion coordinateConversion = new CoordinateConversion();

    protected SharedPreferences preferences;

    private double initialLatitude, initialLongitude;

    protected double altitude;

    protected String angleUnits;

    protected List<Integer> getEditTextIDs() {
        final Integer[] editTextIDs = {
                // Latitude
                R.id.latitude_degrees,
                R.id.latitude_minutes,
                R.id.latitude_seconds,
                R.id.latitude_fractional_seconds,

                // Longitude
                R.id.longitude_degrees,
                R.id.longitude_minutes,
                R.id.longitude_seconds,
                R.id.longitude_fractional_seconds,

                // UTM
                R.id.easting,
                R.id.northing
        };

        return Arrays.asList(editTextIDs);
    }

    private final int layoutId;

    // Save or Go button
    protected Button actionButton;

    public BasePointActivity(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        angleUnits = preferences.getString(StringLiterals.PREFERENCE_KEY_ANGLE_UNITS, StringLiterals.LATLONG);

        initialLatitude = getIntent().getDoubleExtra(StringLiterals.LATITUDE, 0.0f);
        initialLongitude = getIntent().getDoubleExtra(StringLiterals.LONGITUDE, 0.0f);
        altitude = getIntent().getDoubleExtra(StringLiterals.ALTITUDE, 0.0f);

        setupPointUI();

        final Button cancelButton = findViewById(R.id.cancel);

        cancelButton.setOnClickListener(view -> finish());
    }

    protected double[] getLatLongForUTM() {
        final Spinner longitudeZone = findViewById(R.id.longitude_zone);
        final Spinner latitudeZone = findViewById(R.id.latitude_zone);
        final EditText easting = findViewById(R.id.easting);
        final EditText northing = findViewById(R.id.northing);

        final String utmCoordinate = String.format(LocaleUtils.getLocale(),
                "%s %s %s %s",
                longitudeZone.getSelectedItem(),
                latitudeZone.getSelectedItem(),
                easting.getText(),
                northing.getText());

        return coordinateConversion.utm2LatLon(utmCoordinate);
    }

    private void setupDefaultAngle(int degreesId, int minutesId, int secondsId,
                                   int fractionalSecondsId, double initialValue) {
        double decimalDegrees = Math.abs(initialValue);

        final int degrees = (int) decimalDegrees;

        decimalDegrees -= degrees;

        final int minutes = (int) (decimalDegrees * 60.0f);

        decimalDegrees -= minutes / 60.0f;

        final int seconds = (int) (decimalDegrees * Constants.SECONDS_PER_DEGREE);

        decimalDegrees -= seconds / Constants.SECONDS_PER_DEGREE;

        final int fractionalSeconds =
                (int) (decimalDegrees * Constants.SECONDS_PER_DEGREE * 1000.0d);

        final EditText degreesEditText = findViewById(degreesId);
        degreesEditText.setText(String.format(LocaleUtils.getLocale(), "%d", degrees));

        final EditText minutesEditText = findViewById(minutesId);
        minutesEditText.setText(String.format(LocaleUtils.getLocale(), "%d", minutes));

        final EditText secondsEditText = findViewById(secondsId);
        secondsEditText.setText(String.format(LocaleUtils.getLocale(), "%d", seconds));

        final EditText fractionalSecondsEditText = findViewById(fractionalSecondsId);
        String fractionSecondsText = String.format(LocaleUtils.getLocale(), "%d", fractionalSeconds);

        while (fractionSecondsText.length() < 3) {
            fractionSecondsText = "0" + fractionSecondsText;
        }

        fractionalSecondsEditText.setText(fractionSecondsText);
    }

    private void setupDefaultLatitude() {
        setupDefaultAngle(R.id.latitude_degrees, R.id.latitude_minutes, R.id.latitude_seconds, R.id.latitude_fractional_seconds, initialLatitude);

        if (initialLatitude < 0.0d) {
            Spinner spinner = findViewById(R.id.latitude_direction);
            spinner.setSelection(1);
        }
    }

    private void setupDefaultLongitude() {
        setupDefaultAngle(R.id.longitude_degrees, R.id.longitude_minutes, R.id.longitude_seconds, R.id.longitude_fractional_seconds, initialLongitude);

        if (initialLongitude > 0.0d) {
            Spinner spinner = findViewById(R.id.longitude_direction);
            spinner.setSelection(1);
        }
    }

    private void setupDefaultUTM() {
        final String[] utm_values = coordinateConversion.latLon2UTM(initialLatitude, initialLongitude).split(StringLiterals.REGEX_WORDS);

        final int longitudeZoneValue = Integer.parseInt(utm_values[0]);

        final Spinner longitudeZone = findViewById(R.id.longitude_zone);
        longitudeZone.setSelection(longitudeZoneValue - 1);

        final char latitudeZoneValue = utm_values[1].charAt(0);

        final int latitudeZoneOffset = latitudeZoneValue - 'A';

        final Spinner latitudeZone = findViewById(R.id.latitude_zone);
        latitudeZone.setSelection(latitudeZoneOffset);

        final EditText easting = findViewById(R.id.easting);
        easting.setText(utm_values[2]);

        final EditText northing = findViewById(R.id.northing);
        northing.setText(utm_values[3]);
    }

    private void setupPointUI() {
        final LinearLayout latLong = findViewById(R.id.latLong);
        final LinearLayout utm = findViewById(R.id.utm);

        latLong.setVisibility(angleUnits.equals(StringLiterals.LATLONG) ? View.VISIBLE : View.GONE);
        utm.setVisibility(angleUnits.equals(StringLiterals.LATLONG) ? View.GONE : View.VISIBLE);

        setupDefaultLatitude();
        setupDefaultLongitude();

        setupDefaultUTM();

        final InputFilter[] zeroTo59 = new InputFilter[] { new InputFilterMinMax("0", "59")};
        final InputFilter[] zeroTo99 = new InputFilter[] { new InputFilterMinMax("0", "999")};

        // Latitude
        final EditText latitude_degrees = findViewById(R.id.latitude_degrees);
        latitude_degrees.setFilters(new InputFilter[] { new InputFilterMinMax("0", "90")});

        final EditText latitude_minutes = findViewById(R.id.latitude_minutes);
        latitude_minutes.setFilters(zeroTo59);

        final EditText latitude_seconds = findViewById(R.id.latitude_seconds);
        latitude_seconds.setFilters(zeroTo59);

        final EditText latitude_fractional_seconds = findViewById(R.id.latitude_fractional_seconds);
        latitude_fractional_seconds.setFilters(zeroTo99);

        // Longitude
        final EditText longitude_degrees = findViewById(R.id.longitude_degrees);
        longitude_degrees.setFilters(new InputFilter[] { new InputFilterMinMax("0", "179")});

        final EditText longitude_minutes = findViewById(R.id.longitude_minutes);
        longitude_minutes.setFilters(zeroTo59);

        final EditText longitude_seconds = findViewById(R.id.longitude_seconds);
        longitude_seconds.setFilters(zeroTo59);

        final EditText longitude_fractional_seconds = findViewById(R.id.longitude_fractional_seconds);
        longitude_fractional_seconds.setFilters(zeroTo99);

        // UTM
        final InputFilter[] zeroTo99999999 = new InputFilter[] { new InputFilterMinMax("0", "99999999")};

        final EditText easting = findViewById(R.id.easting);
        easting.setFilters(zeroTo99999999);

        final EditText northing = findViewById(R.id.northing);
        northing.setFilters(zeroTo99999999);

        monitorTextChanges();
    }

    private void monitorTextChanges() {
        for (int editTextId : getEditTextIDs()) {
            final EditText editText = findViewById(editTextId);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // empty
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // empty
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    enableDisableActionButton();
                }
            });
        }
    }

    private double getValue(int editTextId) {
        final EditText editText = findViewById(editTextId);
        final String text = editText.getText().toString();

        return Double.parseDouble(text);
    }

    /**
     * Get the value entered into a specific EditText. If numberOfDigits is 3 and user entered
     * 22, the "22" is padded to "220".
     * @param editTextId EditText containing value
     * @param numberOfDigits add zeros to end of value until the value has numberOfDigits digits
     * @return numerical value of text
     */
    private double getValue(int editTextId, int numberOfDigits) {
        final EditText editText = findViewById(editTextId);
        String text = editText.getText().toString();

        if (text.length() < numberOfDigits) {
            text += "0";
        }

        return Double.parseDouble(text);
    }

    private double getAngle(int degreesId, int minutesId, int secondsId, int fractionalSecondsId) {
        final double seconds = getValue(secondsId) + getValue(fractionalSecondsId, 3) / 1000.0f;

        return getValue(degreesId) + (getValue(minutesId) / Constants.MINUTES_PER_DEGREE) + (seconds / Constants.SECONDS_PER_DEGREE);
    }

    protected double getLatitude() {
        final double angle = getAngle(R.id.latitude_degrees, R.id.latitude_minutes, R.id.latitude_seconds, R.id.latitude_fractional_seconds);

        final Spinner spinner = findViewById(R.id.latitude_direction);
        final int sign = spinner.getSelectedItemPosition() == 0 ? +1 : -1;

        return angle * sign;
    }

    protected double getLongitude() {
        final double angle = getAngle(R.id.longitude_degrees, R.id.longitude_minutes, R.id.longitude_seconds, R.id.longitude_fractional_seconds);

        final Spinner spinner = findViewById(R.id.longitude_direction);
        final int sign = spinner.getSelectedItemPosition() == 1 ? +1 : -1;

        return angle * sign;
    }

    private void enableDisableActionButton() {
        boolean emptyField = false;

        for (int editTextId: getEditTextIDs()) {
            final EditText editText = findViewById(editTextId);

            if (editText.getText().toString().length() == 0) {
                emptyField = true;
                break;
            }
        }

        actionButton.setEnabled(!emptyField);
    }

    protected double[] getLatLong() {
        final double latitude, longitude;

        if (angleUnits.equals(StringLiterals.LATLONG)) {
            latitude = getLatitude();
            longitude = getLongitude();
        } else {
            final double[] latLong = getLatLongForUTM();

            latitude = latLong[0];
            longitude = latLong[1];
        }

        return new double[] { latitude, longitude };
    }
}