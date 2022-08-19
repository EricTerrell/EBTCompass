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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.ericbt.ebtcompass.InputFilterMinMax;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.services.GPSService;
import com.ericbt.ebtcompass.utils.AngleUtils;
import com.ericbt.ebtcompass.utils.I18NUtils;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.R;

public class GoLineActivity extends CustomActivity {
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_line);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ((TextView) findViewById(R.id.decimal_point)).setText(I18NUtils.getDecimalPoint());

        final EditText headingDegrees = findViewById(R.id.heading_degrees);
        headingDegrees.setFilters(new InputFilter[] { new InputFilterMinMax("0", "359")});
        headingDegrees.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        final EditText headingFractionalDegrees = findViewById(R.id.heading_fractional_degrees);
        headingFractionalDegrees.setFilters(new InputFilter[] { new InputFilterMinMax("0", "9")});
        headingFractionalDegrees.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        final Spinner headingSpinner = findViewById(R.id.heading_direction);
        headingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    final float angle = 22.5f * (i - 1);
                    headingDegrees.setText(String.format(LocaleUtils.getLocale(), "%d", (int) angle));

                    final int fractionalDegrees = (int) ((angle - (int) angle) * 10.0f);
                    headingFractionalDegrees.setText(String.format(LocaleUtils.getLocale(), "%d", fractionalDegrees));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        goButton = findViewById(R.id.go);

        goButton.setOnClickListener(view -> {
            final int degrees = Integer.parseInt(headingDegrees.getText().toString());
            final int fractionalDegrees = Integer.parseInt(headingFractionalDegrees.getText().toString());

            final float heading = degrees + fractionalDegrees / 10.f;

            preferences.edit().putFloat(StringLiterals.HEADING, heading).apply();

            final Intent returnData = new Intent();
            returnData.putExtra(StringLiterals.HEADING, heading);

            setResult(RESULT_OK, returnData);
            finish();
        });

        final Button cancelButton = findViewById(R.id.cancel);

        cancelButton.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        final Button clearButton = findViewById(R.id.clear);

        clearButton.setOnClickListener(view -> {
            setResult(RESULT_OK);
            finish();
        });

        float goToHeading = getIntent().getFloatExtra(GPSService.GO_TO_HEADING, -1.0f);

        if (goToHeading < 0.0) {
            goToHeading = preferences.getFloat(StringLiterals.HEADING, -1.0f);
        }

        if (goToHeading >= 0.0f) {
            clearButton.setEnabled(true);

            headingDegrees.setText(String.format(LocaleUtils.getLocale(), "%d", (int) goToHeading));

            final int fractionalDegrees = Math.min(Math.round((goToHeading - (int) goToHeading) * 10.0f), 9);

            headingFractionalDegrees.setText(String.format(LocaleUtils.getLocale(), "%d", fractionalDegrees));
            headingSpinner.setSelection(0);
        }

        updateUI();
    }

    private void updateUI() {
        final EditText headingDegrees = findViewById(R.id.heading_degrees);
        final EditText headingFractionalDegrees = findViewById(R.id.heading_fractional_degrees);

        final TextView direction = findViewById(R.id.direction);

        if (headingDegrees.getText().toString().length() > 0) {
            final float degrees = Float.parseFloat(headingDegrees.getText().toString());

            direction.setText(
                    String.format(
                            getString(R.string.go_line_activity_direction_text_format_string),
                            AngleUtils.formatBearing(degrees, this)));
        } else {
            direction.setText(StringLiterals.EMPTY_STRING);
        }

        goButton.setEnabled(headingDegrees.getText().toString().length() != 0 &&
                headingFractionalDegrees.getText().toString().length() != 0);
    }
}