package ai.wit.eval.wit_eval.intents;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.wit.eval.wit_eval.utils.GeoUtils;

/**
 * Intent to query weather.
 *
 * This class will support weather forecast later, but currently it queries current
 * outdoor weather.
 */
public class WeatherIntent implements SupportedIntent {
    /** Logging tag. */
    private static final String TAG = "WeatherIntent";

    /** For http requests handling. */
    private static final HttpClient HTTP_CLIENT = new DefaultHttpClient();

    private static final String NAME = "weather";
    private static final String QUERY_HEAD = "https://query.yahooapis.com/v1/public/yql?q=";
    private static final MessageFormat QUERY_BODY_FORMAT = new MessageFormat("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"{0}\")");
    private static final String QUERY_END = "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
    private static final String ENTITY_LOC = "location";
    // Supported entities
    private static final List<String> SUPPORTED_ENTITIES = ImmutableList.of(ENTITY_LOC);
    private final Geocoder geocoder;

    /** constructor with Context. */
    public WeatherIntent(Context ctxt) {
        this.geocoder = new Geocoder(ctxt, Locale.getDefault());
    }

    @Override
    public Collection<String> getSupportedEntityTypes() {
        return SUPPORTED_ENTITIES;
    }

    /** @return weather with a given location, if no location is given, return current  */
    @Override
    public String getResultString(HashMap<String, JsonElement> entities, Location location) {
        Log.d(TAG, "Starting to query weather, resolving query entities.");
        Log.d(TAG, String.format("Get entities: %s", entities));
        Map<String, String> args = new HashMap<>();
        int cnt = 0;
        for (String entity : SUPPORTED_ENTITIES) {
            if (entities.containsKey(entity)) {
                JsonElement value = entities.get(entity);
                if (value.isJsonPrimitive()) {
                    args.put(entity, value.getAsJsonPrimitive().getAsString());
                }
            }
        }
        return getWeather(args, location);
    }

    /** @return intent name, case-insensitive, should be the same as defined in wit.ai intent list. */
    @Override public String getName() {
        return NAME;
    }

    /** @param args are map of {@link #SUPPORTED_ENTITIES} to be added to weather query. */
    private String getWeather(Map<String, String> args, Location location) {
        String loc = args.containsKey(ENTITY_LOC) ? args.get(ENTITY_LOC) : GeoUtils.getZipCode(geocoder, location);

        String query = QUERY_HEAD + Uri.encode(QUERY_BODY_FORMAT.format(loc)) + QUERY_END;
        Log.d(TAG, "Yahoo weather query String: " + query);
        try {
            HttpResponse response = HTTP_CLIENT.execute(new HttpGet(query));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                return out.toString();
            } else {
                response.getEntity().getContent().close();
                Log.e(TAG, statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            Log.e(TAG, "IO Exception happened when http query", e);
        }
        return "No weather data available";
    }

}
