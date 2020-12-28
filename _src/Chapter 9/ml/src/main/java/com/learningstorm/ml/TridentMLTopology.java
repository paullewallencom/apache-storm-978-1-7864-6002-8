package com.learningstorm.ml;


import java.io.IOException;

import storm.kafka.BrokerHosts;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.testing.MemoryMapState;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;

import com.github.pmerienne.trident.ml.clustering.ClusterQuery;
import com.github.pmerienne.trident.ml.clustering.ClusterUpdater;
import com.github.pmerienne.trident.ml.clustering.KMeans;

public class TridentMLTopology {

	public static void main(String[] args) throws InterruptedException, IOException {
		// Specify the zk hosts for Kafka, change as needed
		BrokerHosts brokerHosts = new ZkHosts("localhost:2181");

		// Specify the topic name for training and the client id
		// here topic name is 'training' and client id is 'storm'
		TridentKafkaConfig kafkaConfig = new TridentKafkaConfig(brokerHosts, "training", "storm");

		// We will always consume from start so that we can run the topology multiple times while debugging. In production, this should be false.
		kafkaConfig.forceFromStart = true;

		// We have string data in the kafka, so specify string scheme here
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

        // Define the spout for reading from kafka
        TransactionalTridentKafkaSpout kafkaSpout = new TransactionalTridentKafkaSpout(kafkaConfig);


        // now we will define the topology that will build the clustering model
		TridentTopology topology = new TridentTopology();

		// Training stream:
		// 1. Read a from string from kafka
		// 2. Convert trident tuple to instance
		// 3. Update the state of clusterer
		// We are using KMeans(6) as we want to cluster into 6 categories
		TridentState kmeansState = topology.newStream("samples", kafkaSpout)
				.each(new Fields("str"), new FeaturesToValues(), new Fields("instance"))
				.partitionPersist(new MemoryMapState.Factory(), new Fields("instance"),
						new ClusterUpdater("kmeans", new KMeans(6)));

		// Now we will build LocalDRPC that will be used to predict the cluster of a tuple
		LocalDRPC localDRPC = new LocalDRPC();

		// Clustering stream
		// 1. Define a new clustering stream with name = 'predict'
		// 2. Convert DRPC args to instance
		// 3. Query cluster to classify the instance
		topology.newDRPCStream("predict", localDRPC)
			.each(new Fields("args"), new FeaturesToValues(), new Fields("instance"))
			.stateQuery(kmeansState, new Fields("instance"), new ClusterQuery("kmeans"), new Fields("prediction"));

		// Create a new local cluster for testing
		LocalCluster cluster = new LocalCluster();

		// submit the topology for execution
		cluster.submitTopology("kmeans", new Config(), topology.build());


		// give the topology enough time to create the clustering model
		Thread.sleep(10000);

		// Create the prediction consumer, please change the path for input and output
		// file as needed
		PredictionConsumer predictionConsumer =
				new PredictionConsumer(localDRPC, "/home/anand/Desktop/prediction.data", "/home/anand/Desktop/predicted.data");

		// Predict and write the output
		predictionConsumer.predict();

		// shutdown cluster and drpc
		cluster.shutdown();
		localDRPC.shutdown();
	}
}