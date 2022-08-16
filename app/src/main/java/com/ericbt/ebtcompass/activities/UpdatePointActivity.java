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

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;

import com.ericbt.ebtcompass.Color;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;

public class UpdatePointActivity extends BaseSaveUpdatePointActivity {
    @Override
    protected void onSaveButtonClicked() {
        Points.delete(this, originalName);

        super.onSaveButtonClicked();
    }

    private String originalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        originalName = getIntent().getStringExtra(StringLiterals.ORIGINAL_NAME);
        final EditText name = findViewById(R.id.name);
        name.setText(originalName);

        final Point[] allPoints = Points.getAll(this, originalName);

        final String lineToName = getIntent().getStringExtra(StringLiterals.LINE_TO_NAME);

        final int ordinalPosition = Points.getOrdinalPosition(allPoints, lineToName);

        if (ordinalPosition >= 0) {
            final Spinner lineToSpinner = findViewById(R.id.line_to);

            // Select ordinalPosition + 1 to get past the first element which is "(None)".
            lineToSpinner.setSelection(ordinalPosition + 1);
        }

        setColorSpinner();
    }

    private void setColorSpinner() {
        final int originalColor = getIntent().getIntExtra(StringLiterals.COLOR, 0);
        final int ordinalPosition = colors.indexOf(new Color(originalColor));

        if (ordinalPosition >= 0) {
            colorSpinner.setSelection(ordinalPosition);
        }
    }
}
