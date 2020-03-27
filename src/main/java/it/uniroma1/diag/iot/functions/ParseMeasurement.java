package it.uniroma1.diag.iot.functions;

import com.google.gson.Gson;
import it.uniroma1.diag.iot.model.StationData;

import java.text.SimpleDateFormat;

public class ParseMeasurement {

    final static String pattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * Converts a value received from the RabbitMQ to a JSON object
     *
     * @param value the comma separated value received.
     * @return the JSON object.
     */
    public static StationData mapFunction(final String value) {
        Gson gson = new Gson();

        // from JSON to object
        StationData message = gson.fromJson(value, StationData.class);

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            message.setTime(simpleDateFormat.parse(message.getTimestamp()));
        } catch (Exception ex) {
            System.err.println("Failed to parse timestamp.");
        }

        return message;
    }

}
