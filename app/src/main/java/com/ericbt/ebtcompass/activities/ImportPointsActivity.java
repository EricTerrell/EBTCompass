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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.array_adapters.PointArrayAdapter;
import com.ericbt.ebtcompass.import_points.ImportPointsResult;
import com.ericbt.ebtcompass.import_points.ImportPointsRunnable;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportPointsActivity extends CustomActivity {
    private TextView messageTV;

    private boolean enabled = true;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_points);

        messageTV = findViewById(R.id.message);

        // Get intent, action and MIME type
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                setEnabled(false);

                final ImportPointsRunnable importPointsRunnable =
                        new ImportPointsRunnable(
                                this,
                                intent.getStringExtra(Intent.EXTRA_TEXT),
                                createHandler());

                executorService.execute(importPointsRunnable);
            }
        }
    }

    private Handler createHandler() {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Log.i(StringLiterals.LOG_TAG, "handleMessage");

                executorService.shutdown();

                final ImportPointsResult importPointsResult = (ImportPointsResult) inputMessage.obj;

                if (importPointsResult.getException() != null) {
                    messageTV.setText(
                            String.format(
                                    LocaleUtils.getLocale(),
                                    getString(R.string.import_failed_format_string),
                                    importPointsResult.getException().getMessage())
                    );
                } else {
                    final float seconds = importPointsResult.getDuration() / 1000.0f;

                    messageTV.setText(
                            String.format(LocaleUtils.getLocale(),
                                    getString(R.string.import_successful_format_string),
                                    importPointsResult.getImportedPoints().length,
                                    seconds));

                    updateList(importPointsResult.getImportedPoints());
                }

                setEnabled(true);
            }
        };

        return handler;
    }

    private void updateList(Point[] importedPoints) {
        final PointArrayAdapter pointArrayAdapter =
                new PointArrayAdapter(this, R.layout.point_list, R.id.point_text_view);

        final ListView pointsListView = findViewById(R.id.points_list_view);
        pointsListView.setAdapter(pointArrayAdapter);

        pointArrayAdapter.addAll(importedPoints);

        pointArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (enabled) {
            finish();

            startActivity(new Intent(this, MainActivity.class));

            return true;
        } else {
            return false;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
