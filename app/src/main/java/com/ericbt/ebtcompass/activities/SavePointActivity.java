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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;

public class SavePointActivity extends BasePointActivity {
    private Button saveButton;
    private EditText name;

    public SavePointActivity() {
        super(R.layout.activity_save_point);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = findViewById(R.id.name);

        saveButton = findViewById(R.id.save);

        name.addTextChangedListener(new TextWatcher() {
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
                final String text = editable.toString().trim();

                saveButton.setEnabled(text.length() > 0);
            }
        });

        actionButton = saveButton;

        saveButton.setOnClickListener(view -> {
            final Spinner colorSpinner = findViewById(R.id.color);

            final String[] values = getResources().getStringArray(R.array.color_values);

            final int color = Integer.parseInt(values[colorSpinner.getSelectedItemPosition()]);

            savePoint(color);
        });
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

        Points.upsert(this, new Point(nameText, latLong[0], latLong[1], altitude, color));
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
}