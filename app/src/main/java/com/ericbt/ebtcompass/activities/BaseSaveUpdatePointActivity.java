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

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.UnitUtils;

import java.util.ArrayList;
import java.util.List;

public class BaseSaveUpdatePointActivity extends BasePointActivity {
    private EditText name;

    public BaseSaveUpdatePointActivity() {
        super(R.layout.activity_base_save_update_point);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = findViewById(R.id.name);

        final Button saveButton = findViewById(R.id.save);

        actionButton = saveButton;

        saveButton.setOnClickListener(view -> {
            onSaveButtonClicked();
        });

        final boolean userPrefersMetric = UnitUtils.userPrefersMetric(this);

        final TextView altitudeUnit = findViewById(R.id.altitude_unit);

        altitudeUnit.setText(userPrefersMetric ? R.string.meters : R.string.feet);

        final EditText altitudeEditText = findViewById(R.id.altitude);

        int altitudeValue = userPrefersMetric ? (int) altitude : (int) UnitUtils.toFeet(altitude);

        altitudeEditText.setText(String.format(LocaleUtils.getDefaultLocale(), "%d", altitudeValue));
    }

    protected void onSaveButtonClicked() {
        final Spinner colorSpinner = findViewById(R.id.color);

        final String[] values = getResources().getStringArray(R.array.color_values);

        final int color = Integer.parseInt(values[colorSpinner.getSelectedItemPosition()]);

        savePoint(color);
    }

    private void savePoint(int color) {
        final String nameText = name.getText().toString().trim();

        if (!Points.exists(this, nameText)) {
            savePoint(nameText, color);
            finish();
        } else {
            overWrite(nameText, color);
        }
    }

    private void savePoint(String nameText, int color) {
        final double[] latLong = getLatLong();
        final double altitude = getAltitude();

        Points.upsert(this, new Point(nameText, latLong[0], latLong[1], altitude, color));
    }

    private double getAltitude() {
        final EditText altitude = findViewById(R.id.altitude);

        final int altitudeValue = Integer.parseInt(altitude.getText().toString());

        return UnitUtils.userPrefersMetric(this) ? altitudeValue : UnitUtils.toMeters(altitudeValue);
    }

    private void overWrite(String name, int color) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getText(R.string.overwrite_point));

        final String message = String.format(LocaleUtils.getDefaultLocale(),
                getString(R.string.point_exists_question), name);

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(StringLiterals.OK, (arg0, arg1) -> {
            savePoint(name, color);
            finish();
        });

        alertDialogBuilder.setNegativeButton(StringLiterals.CANCEL, (arg0, arg1) -> {
        });

        alertDialogBuilder.create().show();
    }

    @Override
    protected List<Integer> getEditTextIDs() {
        final List<Integer> ids = super.getEditTextIDs();

        final ArrayList<Integer> completeList = new ArrayList<>();
        completeList.add(R.id.name);
        completeList.add(R.id.altitude);

        completeList.addAll(ids);

        return completeList;
    }
}