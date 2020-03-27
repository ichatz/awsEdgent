package it.uniroma1.diag.iot;

import it.uniroma1.diag.iot.functions.ParseMeasurement;
import it.uniroma1.diag.iot.model.StationData;
import org.apache.edgent.connectors.mqtt.MqttStreams;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StationListener {

    /**
     * Generate a simple timestamp with the form {@code HH:mm:ss.SSS}
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

        // Process the received msgs - just print them out
        tach.sink(tuple -> System.out.println(
                String.format("[%s] received: %s", simpleTS(), tuple)));

        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(org.apache.edgent.console.server.HttpServer.class).getConsoleUrl());

        tp.submit(topology);
    }

}
