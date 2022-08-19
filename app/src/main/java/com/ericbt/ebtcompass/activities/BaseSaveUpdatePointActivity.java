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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ericbt.ebtcompass.Color;
import com.ericbt.ebtcompass.Colors;
import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.array_adapters.ColorArrayAdapter;
import com.ericbt.ebtcompass.array_adapters.PointArrayAdapter;
import com.ericbt.ebtcompass.utils.I18NUtils;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.UnitUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseSaveUpdatePointActivity extends BasePointActivity {
    private List<Point> existingPointNames, lineToPointNames;

    protected final List<Color> colors = Colors.getColors();

    private EditText name;

    private Spinner lineToSpinner;

    private Point[] allPoints;

    protected Spinner colorSpinner;

    public BaseSaveUpdatePointActivity() {
        super(R.layout.activity_base_save_update_point);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TextView) findViewById(R.id.decimal_point)).setText(I18NUtils.getDecimalPoint());
        ((TextView) findViewById(R.id.decimal_point_2)).setText(I18NUtils.getDecimalPoint());

        allPoints = Points.getAll(this);

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

        altitudeEditText.setText(String.format(LocaleUtils.getLocale(), "%d", altitudeValue));

        colorSpinner = findViewById(R.id.color);

        setupExistingNameSpinner();
        setupLineToSpinner();
        setupColorSpinner();
    }

    private void setupLineToSpinner() {
        lineToSpinner = findViewById(R.id.line_to);

        PointArrayAdapter lineToArrayAdapter = new PointArrayAdapter(this, R.layout.point_list, R.id.point_text_view);

        lineToSpinner.setAdapter(lineToArrayAdapter);

        final String originalName = getIntent().getStringExtra(StringLiterals.ORIGINAL_NAME);

        final Point[] filteredPoints = Points.getAll(this, originalName);

        lineToPointNames = new ArrayList<>(filteredPoints.length + 1);
        lineToPointNames.add(new Point(getString(R.string.select_line_to_point_name)));
        lineToPointNames.addAll(Arrays.asList(filteredPoints));

        lineToArrayAdapter.clear();
        lineToArrayAdapter.addAll(lineToPointNames);
        lineToArrayAdapter.notifyDataSetChanged();
    }

    private void setupColorSpinner() {
        final ColorArrayAdapter colorArrayAdapter = new ColorArrayAdapter(this, R.layout.color_list, R.id.color_text_view);

        colorSpinner.setAdapter(colorArrayAdapter);

        colorArrayAdapter.clear();
        colorArrayAdapter.addAll(colors);
        colorArrayAdapter.notifyDataSetChanged();
    }

    private void setupExistingNameSpinner() {
        final Spinner existingNameSpinner = findViewById(R.id.existing_name);

        final PointArrayAdapter pointArrayAdapter = new PointArrayAdapter(this, R.layout.point_list, R.id.point_text_view);

        existingNameSpinner.setAdapter(pointArrayAdapter);

        existingPointNames = new ArrayList<>(allPoints.length + 1);
        existingPointNames.add(new Point(getString(R.string.select_existing_point_name)));
        existingPointNames.addAll(Arrays.asList(allPoints));

        pointArrayAdapter.clear();
        pointArrayAdapter.addAll(existingPointNames);
        pointArrayAdapter.notifyDataSetChanged();

        existingNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    name.setText(existingPointNames.get(position).getName());
                    existingNameSpinner.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // empty
            }
        });
    }

    protected void onSaveButtonClicked() {
        final Spinner colorSpinner = findViewById(R.id.color);

        final int color = colors.get(colorSpinner.getSelectedItemPosition()).getHue();

        savePoint(color);
    }

    private void savePoint(int color) {
        final String nameText = name.getText().toString().trim();
        final String lineToNameText = lineToSpinner.getSelectedItemPosition() == 0 ?
                null : lineToPointNames.get(lineToSpinner.getSelectedItemPosition()).getName();

        if (!Points.exists(this, nameText)) {
            savePoint(nameText, lineToNameText, color);
            finish();
        } else {
            overWrite(nameText, lineToNameText, color);
        }
    }

    private void savePoint(String nameText, String lineToName, int color) {
        final double[] latLong = getLatLong();
        final double altitude = getAltitude();

        Points.upsert(this, new Point(nameText, lineToName, latLong[0], latLong[1], altitude, color));
    }

    private double getAltitude() {
        final EditText altitude = findViewById(R.id.altitude);

        final int altitudeValue = Integer.parseInt(altitude.getText().toString());

        return UnitUtils.userPrefersMetric(this) ? altitudeValue : UnitUtils.toMeters(altitudeValue);
    }

    private void overWrite(String name, String lineToName, int color) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getText(R.string.overwrite_point));

        final String message = String.format(LocaleUtils.getLocale(),
                getString(R.string.point_exists_question), name);

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(getString(R.string.ok_button_text), (arg0, arg1) -> {
            savePoint(name, lineToName, color);
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