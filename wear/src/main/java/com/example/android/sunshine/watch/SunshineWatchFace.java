/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import com.example.android.sunshine.R;
import com.example.android.sunshine.services.SunshineWearableListener;
import com.example.android.sunshine.utils.WatchDrawUtil;
import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * -------------------------------------------------------------------------------------------------
 * [SunshineWatchFace] CLASS
 * DEVELOPER: Michael Yoon Huh (HUHX0015)
 * DESCRIPTION: Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On
 * devices with low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 * -------------------------------------------------------------------------------------------------
 */

public class SunshineWatchFace extends CanvasWatchFaceService {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // BROADCAST VARIABLES
    private LocalBroadcastManager mBroadcastManager;

    // FONT VARIABLES
    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    // LOGGING VARIABLES
    private static final String LOG_TAG = SunshineWatchFace.class.getSimpleName();

    // TIMER VARIABLES
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1); // Update rate in milliseconds for interactive mode. We update once a second since seconds are displayed in interactive mode.
    private static final int MSG_UPDATE_TIME = 0; // Handler message id for updating the time periodically in interactive mode.

    // WEATHER VARIABLES
    private int mWeatherResourceId = R.drawable.art_clear;
    private String mTempMax = "";
    private String mTempMin = "";

    /** WATCH FACE SERVICE METHODS _____________________________________________________________ **/

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    /** SUBCLASSES _____________________________________________________________________________ **/

    private static class EngineHandler extends Handler {

        /** SUBCLASS VARIABLES _________________________________________________________________ **/

        private final WeakReference<SunshineWatchFace.Engine> mWeakReference;

        public EngineHandler(SunshineWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        /** SUBCLASS VARIABLES _________________________________________________________________ **/

        boolean mAmbient;
        boolean mRegisteredTimeZoneReceiver = false;
        Time mTime;

        // PAINT VARIABLES:
        Paint mBackgroundPaint;
        Paint mDateTextPaint;
        Paint mMaxTempTextPaint;
        Paint mMinTempTextPaint;
        Paint mTextPaint;
        Paint mTimeTextPaint;

        // HANDLER VARIABLES:
        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        final BroadcastReceiver mSunshineReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String sunshineData = intent.getStringExtra(SunshineWearableListener.SUNSHINE_WEATHER_KEY);

                Log.d(LOG_TAG, "mSunshineReceiver: Received Sunshine data: " + sunshineData);

                // If the received data is null or empty, the method ends.
                if (sunshineData == null || sunshineData.length() == 0) {
                    return;
                }

                String[] values = sunshineData.split(",");
                if (values.length < 3) {
                    return;
                }

                // Retrieves the weather ID code from the data passed from the mobile app.
                int weatherId = Integer.valueOf(values[0]);
                mWeatherResourceId = WatchDrawUtil.getWeatherConditionResource(weatherId);

                // Retrieves the max and min weather temperature vvalues.
                mTempMax = values[1];
                mTempMin = values[2];

                invalidate();
            }
        };

        float mXOffset;
        float mYOffset;
        int mTapCount;

        // Whether the display supports fewer bits for each color in ambient mode. When true, we
        // disable anti-aliasing in ambient mode.
        boolean mLowBitAmbient;

        /** ENGINE LIFECYCLE METHODS ___________________________________________________________ **/

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            Resources resources = SunshineWatchFace.this.getResources();
            mBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            mTime = new Time();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            initPaint(resources); // Initializes all the Paint objects in this class.
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /** ENGINE OVERRIDE METHODS ____________________________________________________________ **/

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float dateTextSize = resources.getDimension(isRound
                    ? R.dimen.date_text_size_round : R.dimen.date_text_size);
            float tempTextSize = resources.getDimension(isRound
                    ? R.dimen.temp_text_size_round : R.dimen.temp_text_size);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float timeTextSize = resources.getDimension(isRound
                    ? R.dimen.time_text_size_round : R.dimen.time_text_size);

            // Sets the text size for all the Paint text objects.
            mDateTextPaint.setTextSize(dateTextSize);
            mMaxTempTextPaint.setTextSize(tempTextSize);
            mMinTempTextPaint.setTextSize(tempTextSize);
            mTimeTextPaint.setTextSize(timeTextSize);
            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                    mTimeTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        // onTapCommand(): Captures tap event (and tap type) and toggles the background color if the
        // user finishes a tap.
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = SunshineWatchFace.this.getResources();
            switch (tapType) {

                // The user has started touching the screen.
                case TAP_TYPE_TOUCH:
                    break;

                // The user has started a different gesture or otherwise cancelled the tap.
                case TAP_TYPE_TOUCH_CANCEL:
                    break;

                // The user has completed the tap gesture.
                case TAP_TYPE_TAP:
                    mTapCount++;
                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
                            R.color.watchface_background : R.color.watchface_tap_background));
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            // Draws the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            WatchDrawUtil.drawDate(canvas, bounds, mDateTextPaint);
            WatchDrawUtil.drawTime(canvas, bounds, mTimeTextPaint);
            WatchDrawUtil.drawDivider(canvas, bounds, mDateTextPaint);
            WatchDrawUtil.drawMinMaxTemp(canvas, bounds, mTempMax, mTempMin, mMaxTempTextPaint, mMinTempTextPaint);
            WatchDrawUtil.drawWeather(canvas, bounds, mWeatherResourceId, mBackgroundPaint, isInAmbientMode(), getBaseContext());
        }

        // updateTimer(): Starts the {@link #mUpdateTimeHandler} timer if it should be running and
        // isn't currently or stops it if it shouldn't be running but currently is.
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        // shouldTimerBeRunning(): Returns whether the {@link #mUpdateTimeHandler} timer should be
        // running. The timer should only run when we're visible and in interactive mode.
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        // handleUpdateTimeMessage(): Handle updating the time periodically in interactive mode.
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /** RECEIVER METHODS ___________________________________________________________________ **/

        // registerReceiver(): Registers the broadcast receivers.
        private void registerReceiver() {

            if (mRegisteredTimeZoneReceiver) {
                return;
            }

            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);

            // Registers an IntentFilter for the SunshineWearableListener service class.
            IntentFilter sunshineFilter = new IntentFilter(SunshineWearableListener.SUNSHINE_WEATHER_INTENT);
            mBroadcastManager.registerReceiver(mSunshineReceiver, sunshineFilter);

            Log.d(LOG_TAG, "registerReceiver(): Sunshine broadcast receiver registered.");
        }

        // unregisterReceiver(): Unregisters the broadcast receivers.
        private void unregisterReceiver() {

            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;

            // Unregisters the broadcast receivers.
            SunshineWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
            mBroadcastManager.unregisterReceiver(mSunshineReceiver);

            Log.d(LOG_TAG, "registerReceiver(): Sunshine broadcast receiver unregistered.");
        }

        /** WATCHFACE CANVAS METHODS ___________________________________________________________ **/

        // initPaint(): Initializes all of the Paint objects in this class.
        private void initPaint(Resources resources) {

            // BACKGROUND:
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.watchface_background));

            // DIGITAL TEXT:
            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            // DATE TEXT:
            mDateTextPaint = new Paint();
            mDateTextPaint = createTextPaint(resources.getColor(R.color.min_temp_text));

            // TIME TEXT:
            mTimeTextPaint = new Paint();
            mTimeTextPaint = createTextPaint(resources.getColor(R.color.max_temp_text));

            // MAX TEMP TEXT:
            mMaxTempTextPaint = new Paint();
            mMaxTempTextPaint = createTextPaint(resources.getColor(R.color.max_temp_text));

            // MIN TEMP TEXT:
            mMinTempTextPaint = new Paint();
            mMinTempTextPaint = createTextPaint(resources.getColor(R.color.min_temp_text));
        }
    }
}