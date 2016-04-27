package com.example.android.sunshine.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.android.sunshine.data.WatchData;

/**
 * -------------------------------------------------------------------------------------------------
 * [WatchDrawUtil] CLASS
 * DEVELOPER: Michael Yoon Huh (HUHX0015)
 * DESCRIPTION: Contains methods for drawing watchface-related elements to the watchface canvas.
 * -------------------------------------------------------------------------------------------------
 */
public class WatchDrawUtil {

    // drawDate(): Draws the date text on the watchface canvas.
    public static void drawDate(Canvas canvas, Rect bounds, Paint dateTimePaint) {
        WatchData watchData = new WatchData();
        int width = bounds.width();
        int height = bounds.height();

        float x = width * 0.1f;
        float y = height * 0.45f;
        canvas.drawText(watchData.date, x, y, dateTimePaint);
    }

    // drawTime(): Draws the time text on the watchface canvas.
    public static void drawTime(Canvas canvas, Rect bounds, Paint timeTextPaint) {
        WatchData watchData = new WatchData();
        int width = bounds.width();
        int height = bounds.height();

        float x = width * 0.2f;
        float y = height * 0.3f;
        canvas.drawText(watchData.time, x, y, timeTextPaint);
    }

    // drawDivider(): Draws the divider line on the watchface canvas.
    public static void drawDivider(Canvas canvas, Rect bounds, Paint dateTextPaint) {
        float width = bounds.width() * 0.25f;
        float startX = bounds.width() * 0.375f;
        float y = bounds.height() * 0.5f;
        canvas.drawLine(startX, y, startX + width, y, dateTextPaint);
    }

    // drawWeather(): Draws the weather resource on the watchfac canvas.
    public static void drawWeather(Canvas canvas, Rect bounds, int weatherResource,
                                   Paint backgroundPaint, boolean isAmbient, Context context) {
        float x = bounds.width() * 0.05f;
        float y = bounds.height() * 0.6f;

        // Checks to see if the watch is currently in ambient mode or not.
        if (isAmbient) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), weatherResource);
            canvas.drawBitmap(bitmap, x, y, backgroundPaint);
        }
    }

    // drawMinMaxTemp(): Draws the min and max temperature text on the watchface canvas.
    public static void drawMinMaxTemp(Canvas canvas, Rect bounds, String maxTemp, String minTemp,
                                  Paint maxTextPaint, Paint minTextPaint) {
        float y = bounds.height() * 0.75f;
        float xMax = bounds.width() * 0.4f;
        float xMin = bounds.width() * 0.7f;

        canvas.drawText(maxTemp, xMax, y, maxTextPaint);
        canvas.drawText(minTemp, xMin, y, minTextPaint);
    }
}