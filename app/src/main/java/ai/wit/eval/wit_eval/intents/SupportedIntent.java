package ai.wit.eval.wit_eval.intents;

import android.location.Location;
import android.support.annotation.Nullable;

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
}
