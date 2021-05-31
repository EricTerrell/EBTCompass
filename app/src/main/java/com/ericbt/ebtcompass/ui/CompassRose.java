package com.ericbt.ebtcompass.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class CompassRose extends Drawable {
    private final float pitch, roll, azimuth;

    private final static float TEXT_SIZE = 100.0f;

    private final static float TEXT_SIZE_MULTIPLIER = 1.25f;

    public CompassRose(float pitch, float roll, float azimuth) {
        this.pitch = pitch;
        this.roll = roll;
        this.azimuth = azimuth;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Paint paint = getDefaultPaint();

        final int width = getBounds().width();
        final int height = getBounds().height();

        final float surroundingRadius = (Math.min(width, height) / 2.0f)
                - paint.getStrokeWidth();

        final float radius = (Math.min(width, height) / 2.0f)
                - paint.getStrokeWidth()
                - (TEXT_SIZE * TEXT_SIZE_MULTIPLIER);

        final Paint surroundingPaint = getDefaultPaint();
        surroundingPaint.setStyle(Paint.Style.FILL);
        surroundingPaint.setColor(Color.YELLOW);
        surroundingPaint.setStrokeWidth(15.0f);

        final float originX = width / 2.0f;
        final float originY = height / 2.0f;

        canvas.drawCircle(originX, originY, surroundingRadius, surroundingPaint);
        canvas.drawCircle(originX, originY, surroundingRadius, paint);

        final Paint interiorPaint = getDefaultPaint();
        interiorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        interiorPaint.setColor(Color.RED);
        interiorPaint.setStrokeWidth(15.0f);

        canvas.drawCircle(originX, originY, radius, interiorPaint);

        final String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

        for (int i = 0; i < directions.length; i++) {
            drawCompassLine(canvas, originX, originY, i * 45.0f, directions[i]);
        }

        final float largeRadius = getBounds().width() / 12.0f;

        drawLevel(canvas, 0.0f, 0.0f, 0.0f, Paint.Style.FILL, Color.WHITE, largeRadius);
        drawLevel(canvas, 0.0f, 0.0f, 0.0f, Paint.Style.STROKE, Color.BLACK, largeRadius);
        drawLevel(canvas, pitch, roll, azimuth, Paint.Style.FILL_AND_STROKE, Color.BLUE, getBounds().width() / 18.0f);
    }

    private Paint getDefaultPaint() {
        final float textSize = 100.0f;
        final float strokeWidth = 5.0f;
        final Typeface typeface = Typeface.MONOSPACE;

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setARGB(255, 0, 0, 0);

        return paint;
    }

    private void drawCompassLine(Canvas canvas, float originX, float originY, float angle, String text) {
        final Paint paint = getDefaultPaint();
        paint.setStrokeWidth(10.0f);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setColor(Color.BLACK);

        canvas.save();

        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        canvas.rotate(angle, originX, originY);

        final float margin_x = paint.getStrokeWidth() * 3.0f;
        final float margin_y = paint.getStrokeWidth() * 6.0f;

        canvas.drawLine(originX, originY, originX, TEXT_SIZE * TEXT_SIZE_MULTIPLIER + margin_y, paint);

        final float y = TEXT_SIZE * TEXT_SIZE_MULTIPLIER;

        final Path path = new Path();

        path.moveTo(originX, y);
        path.lineTo(originX - margin_x, y + margin_y);
        path.lineTo(originX + margin_x, y + margin_y);

        path.close();

        canvas.drawPath(path, paint);

        canvas.drawText(text, originX, bounds.height() + paint.getStrokeWidth() * 3.0f, paint);

        canvas.restore();
    }

    private void drawLevel(Canvas canvas, float pitch, float roll, float azimuth, Paint.Style style, int color, float radius) {
        final float pitchFraction = (float) (pitch / Math.PI); // -1.0 ... 1.0
        final float rollFraction = (float) (roll / Math.PI);   // -1.0 ... 1.0

        final float originX = getBounds().width() / 2.0f;
        final float originY = getBounds().height() / 2.0f;

        final float exaggerateError = 3.0f;

        final float x = originX - (originX * rollFraction * exaggerateError);
        final float y = originY + (originY * pitchFraction * exaggerateError);

        final Paint paint = getDefaultPaint();
        paint.setStyle(style);
        paint.setColor(color);
        paint.setStrokeWidth(15.0f);

        canvas.save();

        canvas.rotate(azimuth, originX, originY);

        canvas.drawCircle(x, y, radius, paint);

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
