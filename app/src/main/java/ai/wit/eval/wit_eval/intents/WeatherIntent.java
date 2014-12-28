package ai.wit.eval.wit_eval.intents;

import android.content.Context;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;

/**
 * Intent to query weather.
 *
 * This class will support weather forecast later, but currently it queries current
 * outdoor weather.
 */
public class WeatherIntent implements SupportedIntent {
    private static final String NAME = "weather";
    private static final MessageFormat QUERY_FORMAT = new MessageFormat("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"{0}\")");
    // Supported entities with default value.
    private static final ImmutableMap<String, String> SUPPORTED_ENTITIES = ImmutableMap.of("location", "");

    private final Context cntxt;

    /** constructor with Context. */
    public WeatherIntent(Context cntxt) {
        this.cntxt = cntxt;
    }


    @Override
    public Collection<String> getSupportedEntityTypes() {
        return SUPPORTED_ENTITIES.keySet();
    }

    @Override
    public String getResultString(HashMap<String, JsonElement> entities) {
        Object[] args = new Object[SUPPORTED_ENTITIES.size()];
        int cnt = 0;
        for (String entity : SUPPORTED_ENTITIES.keySet()) {
            if (entities.containsKey(entity)) {
                args[cnt++] = entity;
            }
        }
        return null;
    }

    /** @return intent name, case-insensitive */
    @Override public String getName() {
        return NAME;
    }

    private String getDefaultValue(String entity) {
        if (entity.equals("location")) {
        }

        return null;
    }
}
