package ai.wit.eval.wit_eval.intents;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.HashMap;

import ai.wit.eval.wit_eval.utils.IHasName;

/**
 * interface for supported intent.
 */
public interface SupportedIntent extends IHasName {
    /** Get supported entities for this intent. */
    Collection<String> getSupportedEntityTypes();

    /** Get query/computation result as a String.  */
    String getResultString(HashMap<String, JsonElement> entities, @Nullable Location location);

    /** Registering all finished {@link ai.wit.eval.wit_eval.intents.SupportedIntent}s. */
    public static class SupportedIntents {
        private final ImmutableSet<SupportedIntent> REGISTERED_INTENTS;

        public SupportedIntents(Context context) {
            REGISTERED_INTENTS = ImmutableSet.<SupportedIntent>of(
                new WeatherIntent(context)
            );
        }

        public ImmutableSet<SupportedIntent> getSupportedIntents() {
            return REGISTERED_INTENTS;
        }
    }
}
