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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ericbt.ebtcompass.Constants;
import com.ericbt.ebtcompass.InputFilterMinMax;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.R;

import java.util.Locale;

public class GoToPointActivity extends CustomActivity {
    public static double initialLatitude, initialLongitude;

    private Button goButton;

    private static final int[] editTextIDs = {
            // Latitude
            R.id.latitude_degrees,
            R.id.latitude_minutes,
            R.id.latitude_seconds,
            R.id.latitude_fractional_seconds,

            // Longitude
            R.id.longitude_degrees,
            R.id.longitude_minutes,
            R.id.longitude_seconds,
            R.id.longitude_fractional_seconds
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to_point);

        setupLatitudeLongitudeUI();

        goButton = findViewById(R.id.go);

        goButton.setOnClickListener(view -> {
            final String uriText = String.format(Locale.US,
                    "geo:%2.8f,%3.8f",
                    getLatitude(), getLongitude());

            final Uri gmmIntentUri = Uri.parse(uriText);

            final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            startActivity(mapIntent);
        });

        final Button cancelButton = findViewById(R.id.cancel);

        cancelButton.setOnClickListener(view -> finish());
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

        final int fractionalSeconds = (int) (decimalDegrees * Constants.MINUTES_PER_DEGREE * 1000.0d);

        final EditText degreesEditText = findViewById(degreesId);
        degreesEditText.setText(String.format(LocaleUtils.getDefaultLocale(), "%d", degrees));

        final EditText minutesEditText = findViewById(minutesId);
        minutesEditText.setText(String.format(LocaleUtils.getDefaultLocale(), "%d", minutes));

        final EditText secondsEditText = findViewById(secondsId);
        secondsEditText.setText(String.format(LocaleUtils.getDefaultLocale(), "%d", seconds));

        final EditText fractionalSecondsEditText = findViewById(fractionalSecondsId);
        String fractionSecondsText = String.format(LocaleUtils.getDefaultLocale(), "%d", fractionalSeconds);

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

    private void setupLatitudeLongitudeUI() {
        setupDefaultLatitude();
        setupDefaultLongitude();

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

        monitorTextChanges();
    }

    private void monitorTextChanges() {
        for (int editTextId : editTextIDs) {
            final EditText editText = findViewById(editTextId);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    enableDisableGoButton();
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

        return getValue(degreesId) + getValue(minutesId) / 60.0f + seconds / 3600.0f;
    }

    private double getLatitude() {
        final double angle = getAngle(R.id.latitude_degrees, R.id.latitude_minutes, R.id.latitude_seconds, R.id.latitude_fractional_seconds);

        final Spinner spinner = findViewById(R.id.latitude_direction);
        final int sign = spinner.getSelectedItemPosition() == 0 ? +1 : -1;

        return angle * sign;
    }

    private double getLongitude() {
        final double angle = getAngle(R.id.longitude_degrees, R.id.longitude_minutes, R.id.longitude_seconds, R.id.longitude_fractional_seconds);

        final Spinner spinner = findViewById(R.id.longitude_direction);
        final int sign = spinner.getSelectedItemPosition() == 1 ? +1 : -1;

        return angle * sign;
    }

    private void enableDisableGoButton() {
        boolean emptyField = false;

        for (int editTextId: editTextIDs) {
            final EditText editText = findViewById(editTextId);

            if (editText.getText().toString().length() == 0) {
                emptyField = true;
                break;
            }
        }

        goButton.setEnabled(!emptyField);
    }
}