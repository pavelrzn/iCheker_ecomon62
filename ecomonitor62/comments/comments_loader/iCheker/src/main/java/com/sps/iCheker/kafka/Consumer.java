package com.sps.iCheker.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Consumer {
    private final Properties props;
    private KafkaProducer<String, String> producer;

    public Consumer(Properties props) {
        this.props = props;
    }

    public void send(String message) {
        if (producer == null) startProducer();
        System.out.println("Sending:\t" + message);
        producer.send(new ProducerRecord<>(props.getProperty("kafka.topic"), message));
    }

    private void startProducer() {
        producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }
}
