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
import android.os.Bundle;
import android.widget.Button;

import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.R;

public class GoToPointActivity extends BasePointActivity {
    private Button goButton;

    public GoToPointActivity() {
        super(R.layout.activity_go_to_point);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        goButton = findViewById(R.id.go);
        actionButton = goButton;

        goButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, FindPointActivity.class);

            final Bundle bundle = new Bundle();

            final double[] latLong = getLatLong();
            bundle.putDouble(StringLiterals.LATITUDE, latLong[0]);
            bundle.putDouble(StringLiterals.LONGITUDE, latLong[1]);

            intent.putExtras(bundle);

            startActivity(intent);
        });
    }

}