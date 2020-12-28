package com.learningstorm.ml.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * This class reads the input data set and produces 90% of the data into kafka
 * for training the model and the remaining data in a file that will be used for
 * predictions later.
 */
public class KafkaProducer {

	public static void main(String[] args) throws IOException {

		// Build the configuration required for connecting to Kafka
		Properties props = new Properties();

		// List of kafka brokers. Complete list of brokers is not required as
		// the producer will auto discover the rest of the brokers.
		props.put("metadata.broker.list", "localhost:9092");

		// Serializer used for sending data to kafka. Since we are sending
		// strings, we are using StringEncoder.
		props.put("serializer.class", "kafka.serializer.StringEncoder");

		// We want acks from Kafka that messages are properly recived.
		props.put("request.required.acks", "1");

		// Create the producer instance
		ProducerConfig config = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(config);

		// This is the input file. This should be the path to the file downloaded
		// from UIC Machine Learning Repository at
		// http://archive.ics.uci.edu/ml/databases/synthetic_control/synthetic_control.data
		File file = new File("/home/anand/Desktop/synthetic_control.data");
		Scanner scanner = new Scanner(file);

		// This is the output file for prediction data. Change it to something
		// appropiate for your setup
		File predictioFile = new File("/home/anand/Desktop/prediction.data");
		BufferedWriter writer = new BufferedWriter(new FileWriter(predictioFile));

		int i = 0;
		while(scanner.hasNextLine()){
			String instance = scanner.nextLine();
			if(i++ % 5 == 0){
				// write to file
				writer.write(instance+"\n");
			} else {
				// produce to kafka
				KeyedMessage<String, String> data = new KeyedMessage<String, String>(
						"training", instance);
				producer.send(data);
			}
		}

		// close the files
		scanner.close();
		writer.close();

		// close the producer
		producer.close();

		System.out.println("Produced data");
	}
}
