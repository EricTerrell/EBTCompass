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
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.array_adapters.PointArrayAdapter;
import com.ericbt.ebtcompass.utils.AngleUtils;
import com.ericbt.ebtcompass.utils.LocaleUtils;

public class PointsActivity extends CustomActivity {
    private PointArrayAdapter pointArrayAdapter;

    private MenuItem shareMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        pointArrayAdapter = new PointArrayAdapter(this, R.layout.point_list, R.id.point_text_view);

        ListView pointsListView = findViewById(R.id.points_list_view);
        pointsListView.setAdapter(pointArrayAdapter);

        registerForContextMenu(pointsListView);
        pointsListView.setLongClickable(false);

        pointsListView.setOnItemClickListener((adapterView, view, i, l) -> {
            this.openContextMenu(view);
        });

        pointsListView.setEmptyView(findViewById(R.id.no_points_saved));

        updateList();
    }

    private void updateList() {
        final Point[] allPoints = Points.getAll(this);

        pointArrayAdapter.clear();

        pointArrayAdapter.addAll(allPoints);

        pointArrayAdapter.notifyDataSetChanged();

        if (shareMenuItem != null) {
            shareMenuItem.setVisible(allPoints.length > 0);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view.getId()==R.id.points_list_view) {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.points_menu, menu);

            final int listItemPosition = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
            final Point point = pointArrayAdapter.getItem(listItemPosition);

            final String header = String.format(LocaleUtils.getDefaultLocale(), "Point \"%s\"", point.getName());
            menu.setHeaderTitle(header);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        boolean result = false;

        final int listItemPosition = ((AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo()).position;
        final Point point = pointArrayAdapter.getItem(listItemPosition);
        Log.i(StringLiterals.LOG_TAG, point.toString());

        switch(menuItem.getItemId()) {
            case R.id.find_with_compass: {
                finish();

                final Intent intent = new Intent(PointsActivity.this, FindPointActivity.class);

                final Bundle bundle = new Bundle();

                bundle.putString(StringLiterals.NAME, point.getName());
                bundle.putDouble(StringLiterals.LATITUDE, point.getLatitude());
                bundle.putDouble(StringLiterals.LONGITUDE, point.getLongitude());

                intent.putExtras(bundle);

                startActivity(intent);
            }
            break;

            case R.id.view_on_map: {
                finish();

                final Intent intent = new Intent(PointsActivity.this, MapsActivity.class);

                final Bundle bundle = new Bundle();

                bundle.putString(StringLiterals.NAME, point.getName());
                bundle.putDouble(StringLiterals.LATITUDE, point.getLatitude());
                bundle.putDouble(StringLiterals.LONGITUDE, point.getLongitude());

                intent.putExtras(bundle);

                startActivity(intent);
            }
            break;

            case R.id.delete: {
                delete(point);

                result = true;
            }
            break;

            case R.id.rename: {
                rename(point);

                result = true;
            }
            break;
        }

        return result;
    }

    private void delete(Point point) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getText(R.string.delete_point));

        final String message = String.format(LocaleUtils.getDefaultLocale(),
                getString(R.string.delete_point_question), point.getName());

        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(StringLiterals.OK, (arg0, arg1) -> {
            Points.delete(this, point.getName());
            updateList();
        });

        alertDialogBuilder.setNegativeButton(StringLiterals.CANCEL, (arg0, arg1) -> {
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void rename(Point point) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getText(R.string.rename_point));

        final String message = String.format(LocaleUtils.getDefaultLocale(),
                getString(R.string.rename_point_question), point.getName());

        alertDialogBuilder.setMessage(message);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setPositiveButton(StringLiterals.OK, (arg0, arg1) -> {
            Points.rename(PointsActivity.this, point.getName(), input.getText().toString().trim());
            updateList();
        });

        alertDialogBuilder.setNegativeButton(StringLiterals.CANCEL, (arg0, arg1) -> {
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        input.addTextChangedListener(new TextWatcher() {
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
                final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                if (button != null) {
                    final String newName = editable.toString().trim();

                    final boolean nameNotEmpty = newName.length() > 0;
                    final boolean nameIsNew = !Points.exists(PointsActivity.this, newName);

                    button.setEnabled(nameNotEmpty && nameIsNew);
                }
            }
        });

        alertDialog.show();

        input.setText(point.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.points_activity_menu, menu);

        shareMenuItem = menu.findItem(R.id.share);

        shareMenuItem.setVisible(Points.getAll(this).length > 0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean result = false;

        final int itemId = item.getItemId();

        if (itemId == R.id.share) {
            sharePoints();

            result = true;
        }

        return result;
    }

    private void sharePoints() {
        final StringBuilder allPointsText =
                new StringBuilder("\"Point\",\"Latitude\",\"Longitude\",\"Altitude (m)\",\"Zone\",\"Easting\",\"Northing\",\"Color\"\n");

        for (final Point point : Points.getAll(this)) {
            final String[] utmValues = AngleUtils.getUTMValues(point.getLatitude(), point.getLongitude());

            final String line = String.format(
                    LocaleUtils.getDefaultLocale(), "\"%s\",%.20f,%.20f,%.20f,\"%s\",%s,%s,%d\n",
                    point.getName().replace('\"', '\''),
                    point.getLatitude(),
                    point.getLongitude(),
                    point.getAltitude(),
                    utmValues[0],
                    utmValues[1],
                    utmValues[2],
                    point.getColor());

            allPointsText.append(line);
        }

        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.points));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, allPointsText.toString());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }
}