package com.example.android.sunshine.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

/**
 * -------------------------------------------------------------------------------------------------
 * [SunshineSyncWear] CLASS
 * DEVELOPER: Michael Yoon Huh (HUHX0015)
 * DESCRIPTION: This class is responsible for synchronizing data to the paired Android Wear device.
 * -------------------------------------------------------------------------------------------------
 */

public class SunshineSyncWear implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    private static final String LOG_TAG = SunshineSyncWear.class.getSimpleName();

    // SYNC VARIABLES
    private static final String SUNSHINE_WEATHER_PATH = "/sunshine-weather";
    private GoogleApiClient mGoogleApiClient;

    // WEATHER VARIABLES
    private String mMaxTemp;
    private String mMinTemp;
    private String mWeatherId;

    /** CONSTRUCTOR METHODS ____________________________________________________________________ **/
    
    // SunshineSyncWear(): Constructor for the SunshineSyncWear class. Sets up the GoogleApiClient.
    public SunshineSyncWear(Context context) {

        // Sets up the GoogleApiClient connection.
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /** GOOGLE API CLIENT METHODS ______________________________________________________________ **/

    // onConnected(): Runs when the client is connected.
    @Override
    public void onConnected(Bundle bundle) {

        Log.d(LOG_TAG, "onConnected(): Google API client connection established.");

        // Updates the nodes in the background.
        SunshineNodeSyncTask syncTask = new SunshineNodeSyncTask();
        syncTask.execute();
    }

    // onConnectionFailed(): Runs when the client fails to connect.
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed(): Google API client connection failed: " + connectionResult.getErrorMessage());
    }

    // onConnectionSuspended(): Runs when the client connection is suspended.
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "onConnectionSuspended(): Google API client connection was suspended.");
    }

    /** SYNC METHODS ___________________________________________________________________________ **/

    // fetchNodes(): Retrieves the connected nodes.
    private Collection<String> fetchNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    // syncWearWeather(): Sets the weatherId, maxTemp, and minTemp values and initializes the
    // connection.
    public void syncWearWeather(String weatherId, String maxTemp, String minTemp) {

        this.mMaxTemp = maxTemp;
        this.mMinTemp = minTemp;
        this.mWeatherId = weatherId;

        Log.d(LOG_TAG, "syncWearWeather(): Connecting to Google API client...");

        mGoogleApiClient.connect();
    }

    // updateNote(): Updates the requested node.
    private void updateNode(String node) {

        String dataString = mWeatherId + "," + mMaxTemp + "," + mMinTemp;
        byte[] data = dataString.getBytes(Charset.forName("UTF-8"));

        Log.d(LOG_TAG, "updateNote(): Updating Android Wear with: " + dataString);

        PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node, SUNSHINE_WEATHER_PATH, data);
        messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                            @Override
                                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                                Log.d(LOG_TAG, "onResult(): Node update result: " + sendMessageResult.toString());
                                            }
                                        }
        );
    }

    /** SUBCLASSES _____________________________________________________________________________ **/

    /**
     * --------------------------------------------------------------------------------------------
     * [SunshineNodeSyncTask] CLASS
     * DESCRIPTION: This is an AsyncTask-based class that handles the updating of the Nodes in the
     * background.
     * --------------------------------------------------------------------------------------------
     */
    private class SunshineNodeSyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = fetchNodes();
            for (String node : nodes) {
                updateNode(node);
            }
            return null;
        }
    }
}