package com.learningstorm.kafka;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaProducer {
	
	
	public static void main(String[] args) {
		// Build the configuration required for connecting to Kafka
		Properties props = new Properties();

		// List of kafka borkers. Complete list of brokers is not required as
		// the producer will auto discover the rest of the brokers.
		props.put("metadata.broker.list", "localhost:9092");

		// Serializer used for sending data to kafka. Since we are sending
		// string,
		// we are using StringEncoder.
		props.put("serializer.class", "kafka.serializer.StringEncoder");

		// We want acks from Kafka that messages are properly recived.
		props.put("request.required.acks", "1");

		// Create the producer instance
		ProducerConfig config = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(config);

		try {
		FileInputStream fstream = new FileInputStream("/home/ankit/mywork/POCs/LexisNexisDemo/apache_test.log");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		/* read log line by line */
		while ((strLine = br.readLine()) != null) {
			KeyedMessage<String, String> data = new KeyedMessage<String,
					String>("apache_log", strLine);
			 producer.send(data);
		}
		br.close();
		fstream.close();
		}catch (Exception e) {
			throw new RuntimeException("Error occurred while persisting records : ");
		}

		// close the producer
		producer.close();
	}
	
}
