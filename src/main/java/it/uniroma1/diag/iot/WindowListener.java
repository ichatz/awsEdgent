package it.uniroma1.diag.iot;

import com.google.gson.JsonObject;
import it.uniroma1.diag.iot.functions.ParseMeasurement;
import it.uniroma1.diag.iot.model.StationData;
import org.apache.edgent.analytics.math3.Aggregations;
import org.apache.edgent.analytics.math3.MvResultMap;
import org.apache.edgent.analytics.math3.ResultMap;
import org.apache.edgent.analytics.math3.stat.Regression2;
import org.apache.edgent.analytics.math3.stat.Statistic2;
import org.apache.edgent.connectors.mqtt.MqttStreams;
import org.apache.edgent.function.Functions;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.TWindow;
import org.apache.edgent.topology.Topology;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WindowListener {

    /**
     * Generate a simple timestamp with the form {@code HH:mm:ss.SSS}
     *
     * @return the timestamp
     */
    public static String simpleTS() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    /**
     * Run a topology with a RabbitMQ connector printing readings to standard out.
     *
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {

        System.out.println("StreamListener: Simple output from MQTT");

        DirectProvider tp = new DevelopmentProvider();

        Topology topology = tp.newTopology("StreamListener");

        // MQTT Connector
        MqttStreams mqtt = new MqttStreams(topology, main.java.it.uniroma1.diag.iot.AppConfiguration.brokerHost, "edgent");

        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = mqtt.subscribe(main.java.it.uniroma1.diag.iot.AppConfiguration.topic, 0/*qos*/);

        TStream<StationData> tach = msgs.map(ja -> {
            return ParseMeasurement.mapFunction(ja);
        });

        // Create a window on the stream of the last 50 readings partitioned
        // by sensor name. In this case two independent windows are created (for a and b)
        TWindow<StationData, Integer> sensorWindow = tach.last(10, Functions.unpartitioned());

        TStream<MvResultMap> aggregations = sensorWindow.batch(
                (list, partition) -> {
                    ResultMap tempResults = Aggregations.aggregateN(list, t -> t.getTemperature(), Statistic2.MAX, Statistic2.MEAN, Statistic2.STDDEV);
                    ResultMap humidityResults = Aggregations.aggregateN(list, t -> t.getHumidity(), Statistic2.MEAN, Regression2.SLOPE);
                    ResultMap rainResults = Aggregations.aggregateN(list, t -> t.getRain_height(), Statistic2.SUM);

                    MvResultMap results = new MvResultMap();
                    results.put("temp", tempResults);
                    results.put("humidity", humidityResults);
                    results.put("rain", rainResults);

                    return results;
                });

        TStream<JsonObject> joResultMap = aggregations.map(MvResultMap.toJsonObject());

        // Process the received msgs - just print them out
        joResultMap.sink(tuple -> System.out.println(
                String.format("[%s] aggregated: %s", simpleTS(), tuple)));
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(org.apache.edgent.console.server.HttpServer.class).getConsoleUrl());

        tp.submit(topology);
    }

}
