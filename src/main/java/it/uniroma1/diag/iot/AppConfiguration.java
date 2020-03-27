package main.java.it.uniroma1.diag.iot;

/**
 * Central point for setting the configuration parameters
 *
 * @author ichatz@diag.uniroma1.it
 */
public interface AppConfiguration {

    public final String brokerHost = "tcp://localhost:1883";

    public final int brokerPort = 1883;

    public final String topic = "station";

    public final String outExchange = "edgent";
    
}
