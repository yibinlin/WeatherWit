package ai.wit.eval.wit_eval;

import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ai.wit.eval.wit_eval.intents.SupportedIntent;
import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;


public class MainActivity extends ActionBarActivity implements IWitListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /** For debugging */
    private static final String TAG = "MainActivity";
    private Wit wit;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SupportedIntent.SupportedIntents supportedIntents;

    /**
     * Set up the following things:
     *
     * (1). Set up Google API client for location updates.
     * (2). set up Wit.ai client.
     * (3). Set up all {@link ai.wit.eval.wit_eval.intents.SupportedIntent}s.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up location updates.
        buildGoogleApiClient();

        setContentView(R.layout.activity_main);

        // Set up wit.ai
        String accessToken = "37B6Q65ER534PMK5YE6KL2NP7FGXA4C5";
        Log.i(TAG, "Wit Access token: " + accessToken);
        wit = new Wit(accessToken, this);
        wit.enableContextLocation(getApplicationContext());

        // Set up supported intents.
        supportedIntents = new SupportedIntent.SupportedIntents(getApplicationContext());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggle(View v) {
        try {
            wit.toggleListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String messageId, Error error) {

        TextView jsonView = (TextView) findViewById(R.id.jsonView);
        jsonView.setMovementMethod(new ScrollingMovementMethod());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (error != null) {
            jsonView.setText(error.getLocalizedMessage());
            return ;
        }

        // for now get the first intent.
        if (!witOutcomes.isEmpty()) {
            WitOutcome witResult = witOutcomes.iterator().next();
            jsonView.setText(dealWithIntent(witResult));
            ((TextView) findViewById(R.id.txtText)).setText("Done!");
        }
        // TODO remove this commented code.
        /**
        String jsonOutput = gson.toJson(witOutcomes);
        jsonView.setText(sb.toString() + "\n\n" + jsonOutput);
        ((TextView) findViewById(R.id.txtText)).setText("Done!");
        */
    }

    @Override
    public void witDidStartListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Witting...");
    }

    @Override
    public void witDidStopListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Processing...");
    }

    @Override
    public void witActivityDetectorStarted() {
        ((TextView) findViewById(R.id.txtText)).setText("Listening");
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private String dealWithIntent(WitOutcome witResult) {
        String intent = witResult.get_intent();
        Log.d(TAG, String.format("Dealing with intent: %s.", intent));

        for (SupportedIntent intentObj : supportedIntents.getSupportedIntents()) {
            if (intent.equalsIgnoreCase(intentObj.getName())) {
                HashMap<String, JsonElement> entities = witResult.get_entities();
                return intentObj.getResultString(entities, mLastLocation);
            }
        }

        return "Unknown Intent";
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Api Client connected.");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Api Client suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Api Client failed.");
    }

    protected LocationRequest createLocationRequest() {
        return new LocationRequest().setPriority(LocationRequest.PRIORITY_NO_POWER);
    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, createLocationRequest(), this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Application onPause() called.");
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.wit_button, container, false);
        }
    }

}
