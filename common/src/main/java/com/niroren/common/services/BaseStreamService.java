package com.niroren.common.services;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public abstract class BaseStreamService implements IStreamsService {
    private final String appId;
    private final String bootstrapServers;

    protected KafkaStreams streams;

    public BaseStreamService(String appId, String bootstrapServers) {
        this.appId = appId;
        this.bootstrapServers = bootstrapServers;
    }

    public void start() {
        this.streams = processStreams(getStreamsConfig(appId, bootstrapServers));
        StreamsSupport.startStream(streams);
    }

    protected Properties getStreamsConfig(String appId, String bootstrapServers) {
        final Properties properties = new Properties();
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);
        return properties;
    }

    protected abstract KafkaStreams processStreams(Properties streamsConfig);

}
