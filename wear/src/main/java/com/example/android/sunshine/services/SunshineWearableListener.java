package com.example.android.sunshine.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.nio.charset.Charset;

/**
 * -------------------------------------------------------------------------------------------------
 * [SunshineWearableListener] CLASS
 * PROGRAMMER: Michael Yoon Huh (HUHX0015)
 * DESCRIPTION: A service class that runs in the background which monitors synchronization events
 * between the mobile Android device and any paired Android Wear devices.
 * -------------------------------------------------------------------------------------------------
 */
public class SunshineWearableListener extends WearableListenerService {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    private static final String LOG_TAG = SunshineWearableListener.class.getSimpleName();

    // SYNC VARIABLES
    public static final String SUNSHINE_WEATHER_PATH = "/sunshine-weather";
    public static final String SUNSHINE_WEATHER_INTENT = "sunshine_weather_intent";
    public static final String SUNSHINE_WEATHER_KEY = "sunshine_weather_key";

    private GoogleApiClient googleApiClient;
    private LocalBroadcastManager broadcastManager;

    /** OVERRIDDEN METHODS _____________________________________________________________________ **/

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "onCreate(): onCreate() invoked.");

        // Sets up a new Google API data connection to communicate with the wearable device.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    // onDataChanged(): Called when data item objects are created, changed, or deleted. An event on
    // one side of a connection triggers this callback on both sides.
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(LOG_TAG, "onDataChanged(): Data Event: " + dataEvents);
    }

    // onMessageReceived(): A message sent from one side of a connection triggers this callback on
    // the other side of the connection.
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.d(LOG_TAG, "onMessageReceived(): Message Event: " + messageEvent);

        super.onMessageReceived(messageEvent);

        // If messageEvent matches the path value of SUNSHINE_WEATHER_PATH...
        if (messageEvent.getPath().startsWith(SUNSHINE_WEATHER_PATH)) {
            byte[] data = messageEvent.getData();
            String messageContent = new String(data, Charset.forName("UTF-8"));

            Intent weatherIntent = new Intent(SUNSHINE_WEATHER_INTENT);
            weatherIntent.putExtra(SUNSHINE_WEATHER_KEY, messageContent);
            broadcastManager.sendBroadcast(weatherIntent);

            Log.d(LOG_TAG, "onMessageReceived(): Sunshine weather update received: " + messageContent);
        }
    }

    // onPeerConnected(): Called when connection with the handheld or wearable is connected. Changes
    // in connection state on one side of the connection triggers these callbacks on both sides of
    // the connection.
    @Override
    public void onPeerConnected(Node peer) {
        Log.d(LOG_TAG, "onPeerConnected(): Peer: " + peer);
        super.onPeerConnected(peer);
    }

    // onPeerDisconnected(): Called when connection with the handheld or wearable is disconnected.
    // Changes in connection state on one side of the connection triggers these callbacks on both
    // sides of the connection.
    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(LOG_TAG, "onPeerDisconnected(): Peer: " + peer);
        super.onPeerDisconnected(peer);
    }
}