package ai.wit.eval.wit_eval.background;

import com.google.common.util.concurrent.Callables;

import java.util.concurrent.Callable;

/**
 * Make HTTP query to Yahoo Weather API locally.
 *
 * Returns weather information (current weather or forecast).
 */
public class YahooWeatherQuery implements Callable<String> {
    @Override public String call() throws Exception {
        return null;
    }
}
