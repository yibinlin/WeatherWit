package ai.wit.eval.wit_eval.intents;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intent to query weather.
 *
 * This class will support weather forecast later, but currently it queries current
 * outdoor weather.
 */
public class WeatherIntent implements SupportedIntent {
    /** Log */
    private static final String TAG = "WeatherIntent";

    private static final String NAME = "weather";
    private static final MessageFormat QUERY_FORMAT = new MessageFormat("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"{0}\")");
    private static final String ENTITY_LOC = "location";
    // Supported entities
    private static final List<String> SUPPORTED_ENTITIES = ImmutableList.of(ENTITY_LOC);

    private final Context cntxt;

    /** constructor with Context. */
    public WeatherIntent(Context cntxt) {
        this.cntxt = cntxt;
    }

    @Override
    public Collection<String> getSupportedEntityTypes() {
        return SUPPORTED_ENTITIES;
    }

    /** @return weather with a given location, if no location is given, return current  */
    @Override
    public String getResultString(HashMap<String, JsonElement> entities) {
        Map<String, String> args = new HashMap<>();
        int cnt = 0;
        for (String entity : SUPPORTED_ENTITIES) {
            if (entities.containsKey(entity)) {
                args.put(entity, entities.get(entity).getAsString());
                Log.d(TAG, entities.get(entity).getAsString());
            }
        }
        return getWeather(args);
    }

    /** @return intent name, case-insensitive */
    @Override public String getName() {
        return NAME;
    }

    /** @param args are map of {@link #SUPPORTED_ENTITIES} to be added to weather query. */
    private String getWeather(Map<String, String> args) {
        return null;
    }

}
