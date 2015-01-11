package ai.wit.eval.wit_eval.intents;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.wit.eval.wit_eval.background.YahooWeatherQuery;

/**
 * Intent to query weather.
 *
 * This class will support weather forecast later, but currently it queries current
 * outdoor weather.
 */
public class WeatherIntent implements SupportedIntent {
    /** Name of location entity that may be found in a weather wit intent. */
    public static final String ENTITY_LOC = "location";
    /** Logging tag. */
    private static final String TAG = "WeatherIntent";

    private static final String NAME = "weather";
    // Supported entities
    private static final List<String> SUPPORTED_ENTITIES = ImmutableList.of(ENTITY_LOC);

    private final Geocoder geocoder;
    private final ExecutorService executorService;

    /** constructor with Context. */
    public WeatherIntent(Context ctxt) {
        this.geocoder = new Geocoder(ctxt, Locale.getDefault());
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public Collection<String> getSupportedEntityTypes() {
        return SUPPORTED_ENTITIES;
    }

    /** @return weather with a given location, if no location is given, return current  */
    @Override
    public String getResultString(Map<String, JsonElement> entities, Location location) {
        Log.d(TAG, "Starting to query weather, resolving query entities.");
        Log.d(TAG, String.format("Get entities: %s", entities));
        Map<String, String> queryArgs = new HashMap<>();
        for (String entity : SUPPORTED_ENTITIES) {
            if (entities.containsKey(entity)) {
                JsonElement jsonValue = entities.get(entity);
                if (jsonValue.isJsonArray()) {
                    String stringValue = jsonValue.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsJsonPrimitive().getAsString();
                    queryArgs.put(entity, stringValue);
                }
            }
        }

        try {
            return executorService.submit(new YahooWeatherQuery(queryArgs, location, geocoder)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String msg = "Interrupted Exception encountered while query weather. ";
            Log.e(TAG, msg);
            throw new IllegalStateException(msg, e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Execution Exception encountered while querying Yahoo weather API. ", e);
        }
        return "network error";
    }

    /** @return intent name, case-insensitive, should be the same as defined in wit.ai intent list. */
    @Override public String getName() {
        return NAME;
    }

}
