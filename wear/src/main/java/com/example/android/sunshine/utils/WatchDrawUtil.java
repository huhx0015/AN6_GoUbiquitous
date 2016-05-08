package com.example.android.sunshine.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.android.sunshine.R;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * -------------------------------------------------------------------------------------------------
 * [WatchDrawUtil] CLASS
 * DEVELOPER: Michael Yoon Huh (HUHX0015)
 * DESCRIPTION: Contains methods for drawing watchface-related elements to the watchface canvas.
 * -------------------------------------------------------------------------------------------------
 */
public class WatchDrawUtil {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // DATE FORMAT VARIABLES
    private static DateFormat DATE_FORMAT = DateFormat.getDateInstance();
    private static DateFormat DAY_OF_WEEK_FORMAT = new SimpleDateFormat("E", Locale.getDefault());
    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm");

    /** DRAW METHODS ___________________________________________________________________________ **/

    // drawDate(): Draws the date text on the watchface canvas.
    public static void drawDate(Canvas canvas, Rect bounds, Paint dateTimePaint) {
        Date date = new Date();
        int width = bounds.width();
        int height = bounds.height();

        float x = width * 0.1f;
        float y = height * 0.45f;
        canvas.drawText(DAY_OF_WEEK_FORMAT.format(date) + " " + DATE_FORMAT.format(date), x, y, dateTimePaint);
    }

    // drawTime(): Draws the time text on the watchface canvas.
    public static void drawTime(Canvas canvas, Rect bounds, Paint timeTextPaint) {
        Date date = new Date();
        int width = bounds.width();
        int height = bounds.height();

        float x = width * 0.2f;
        float y = height * 0.3f;
        canvas.drawText(TIME_FORMAT.format(date), x, y, timeTextPaint);
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
        if (!isAmbient) {
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

    // getWeatherConditionResource(): Returns a drawable resource ID based on the specified weather
    // ID code. Derived from the getArtResourceForWeatherCondition() method in the Utility class.
    // Weather ID codes are based on weather code data found at:
    // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
    public static int getWeatherConditionResource(int weatherId) {

        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }
}