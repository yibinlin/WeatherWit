package ai.wit.eval.wit_eval.background;

import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.common.collect.ImmutableMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import ai.wit.eval.wit_eval.intents.WeatherIntent;
import ai.wit.eval.wit_eval.utils.GeoUtils;

/**
 * Make HTTP query to Yahoo Weather API locally.
 *
 * Returns weather information (current weather or forecast).
 */
public class YahooWeatherQuery implements Callable<String> {
    private static final String TAG = "YahooWeatherQuery";

    /** For http requests handling. */
    private static final HttpClient HTTP_CLIENT = new DefaultHttpClient();

    private static final String QUERY_HEAD = "https://query.yahooapis.com/v1/public/yql?q=";
    private static final String QUERY_BODY_FORMAT = "select item.condition from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='c'";
    private static final String QUERY_END = "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

    /** Indentation for pretty String printing of Yahoo weather query results. */
    private static final int PRETTY_JSON_INDENT = 2;

    private final Map<String, String> arguments;
    private final Location location;
    private final Geocoder geocoder;

    public YahooWeatherQuery(Map<String, String> arguments, Location location, Geocoder geocoder) {
        this.arguments = ImmutableMap.copyOf(arguments);
        this.location = location;
        this.geocoder = geocoder;
    }

    /** Query Yahoo Weather API. */
    @Override public String call() {
        Log.d(TAG, String.format("Entities: %s", arguments));

        String locationString = arguments.containsKey(WeatherIntent.ENTITY_LOC)
                ? arguments.get(WeatherIntent.ENTITY_LOC)
                : GeoUtils.getZipCode(geocoder, location);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Showing current condition for %s: \n", locationString));

        String query = QUERY_HEAD + Uri.encode(String.format(QUERY_BODY_FORMAT, locationString)) + QUERY_END;
        Log.d(TAG, "Yahoo weather query String: " + query);
        try {
            HttpResponse response = HTTP_CLIENT.execute(new HttpGet(query));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();

                JSONObject jsonObject = new JSONObject(out.toString());
                return sb.append(extractInfo(jsonObject)).toString();
            } else {
                response.getEntity().getContent().close();
                Log.e(TAG, statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            Log.e(TAG, "IO Exception happened when http query", e);
        } catch (JSONException e) {
            Log.e(TAG, "Json object creation error for Yahoo API response.", e);
        }
        return sb.append("No weather data available").toString();
    }

    /**
     * Now it only supports "condition" query.
     *
     * @return extracted information to display from a responded Yahoo weather query, in the
     * form of {@link org.json.JSONObject}.
     */
    private String extractInfo(JSONObject jsonObject) throws JSONException {
        JSONObject condition = jsonObject
                .getJSONObject("query")
                .getJSONObject("results")
                .getJSONObject("channel")
                .getJSONObject("item")
                .getJSONObject("condition");

        String temp = condition.getString("temp");
        String text = condition.getString("text");

        return String.format("Condition: %s, Temperature: %s°C", text, temp);
    }
}
