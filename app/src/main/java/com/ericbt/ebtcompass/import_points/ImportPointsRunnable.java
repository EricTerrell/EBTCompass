package com.ericbt.ebtcompass.import_points;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ericbt.ebtcompass.Color;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.StringLiterals;
import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.ericbt.ebtcompass.utils.UnitUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportPointsRunnable implements Runnable {
    private final static int NUMBER_OF_FIELDS_PER_VALID_LINE = 10;

    private final String text;

    private final Context context;

    private final Handler handler;

    public ImportPointsRunnable(Context context, String text, Handler handler) {
        this.context = context;
        this.text = text;
        this.handler = handler;
    }

    @Override
    public void run() {
        final ImportPointsResult importPointsResult = new ImportPointsResult();

        try {
            final long start = new Date().getTime();

            final Point[] importedPoints = importPoints(text);

            final long stop = new Date().getTime();

            importPointsResult.setImportedPoints(importedPoints);

            importPointsResult.setDuration(stop - start);
        } catch (Exception exception) {
            importPointsResult.setException(exception);
        }

        Message message = Message.obtain();
        message.obj = importPointsResult;

        handler.sendMessage(message);
    }

    private Point[] importPoints(String text) {
        final List<Point> importedPoints = new ArrayList<>();

        if (text != null) {
            Log.i(StringLiterals.LOG_TAG,
                    String.format(
                            LocaleUtils.getDefaultLocale(),
                            "Shared Text: %s", text));

            final String[] lines = text.split("\n");

            for (final String line : lines) {
                // Convert ever comma not surrounded by double quotes to a null, to be used
                // as a delimiter.
                final StringBuilder originalLine = new StringBuilder(line);
                final StringBuilder delimitedLine = new StringBuilder(originalLine.length());

                boolean insideDoubleQuotes = false;

                for (int i = 0; i < originalLine.length(); i++) {
                    char currentCharacter = originalLine.charAt(i);

                    if (currentCharacter == '"') {
                        insideDoubleQuotes = !insideDoubleQuotes;
                    }

                    if (currentCharacter == ',') {
                        if (!insideDoubleQuotes) {
                            currentCharacter = '\0';
                        }
                    }

                    if (currentCharacter != '"') {
                        delimitedLine.append(currentCharacter);
                    }
                }

                final Point importedPoint = processLine(delimitedLine.toString());

                if (importedPoint != null) {
                    importedPoints.add(importedPoint);
                }
            }
        }

        return importedPoints.toArray(new Point[0]);
    }

    private Point processLine(String line) {
        final String[] fields = line.split("\0");

        if (fields.length == NUMBER_OF_FIELDS_PER_VALID_LINE) {
            Log.i(StringLiterals.LOG_TAG, String.format("Found importable line: %s", line));

            try {
                final String name = fields[0];
                final String lineTo = fields[1];
                final double latitude = Double.parseDouble(fields[2]);
                final double longitude = Double.parseDouble(fields[3]);

                double altitude = 0.0d;

                // Need not have a valid altitude value.
                try {
                    altitude = Double.parseDouble(fields[4]);
                } catch (Exception ex) {
                    // empty
                }

                // Need not have a valid color value.
                int color = Color.RED.getHue();

                try {
                    color = Integer.parseInt(fields[8]);
                } catch (Exception ex) {
                    // empty
                }

                final String units = fields[9];

                if (units.trim().equalsIgnoreCase(StringLiterals.ENGLISH)) {
                    altitude = UnitUtils.toMeters(altitude);
                } else if (!units.trim().equalsIgnoreCase(StringLiterals.METRIC)) {
                    throw new Exception(
                            String.format(
                                    LocaleUtils.getDefaultLocale(),
                                    "Units must be \"%s\" or \"%s\"",
                                    StringLiterals.ENGLISH,
                                    StringLiterals.METRIC));
                }

                final Point point = new Point(name, lineTo, latitude, longitude, altitude, color);
                Points.upsert(context, point);

                return point;
            } catch (Exception ex) {
                Log.e(StringLiterals.LOG_TAG, "cannot process line", ex);
            }
        }

        return null;
    }
}
