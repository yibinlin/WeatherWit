package ai.wit.eval.wit_eval.utils;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for {@link android.location.Location}.
 */
public class GeoUtils {
    private static final String TAG = "GeoUtils";

    /** @return zip code of a {@link android.location.Location} */
    public static String getZipCode(Geocoder geocoder, Location location) {
        Log.d(TAG, String.format("Geocoder object: %s, Location object: %s.", geocoder, location));

        Preconditions.checkArgument(location != null, "location object cannot be null, " +
                "maybe Google client has not been connected yet?");

        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        Preconditions.checkState(!addresses.isEmpty());
        return addresses.iterator().next().getPostalCode();
    }
}
