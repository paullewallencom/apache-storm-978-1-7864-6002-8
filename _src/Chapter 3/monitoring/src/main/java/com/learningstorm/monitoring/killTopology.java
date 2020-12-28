package com.learningstorm.monitoring;

import backtype.storm.generated.Nimbus.Client;

public class killTopology {
	
	public void kill(String topologyId) {
		try {
		ThriftClient thriftClient = new ThriftClient();
		// Get the nimbus thrift client
		Client client = thriftClient.getClient();
		// kill the given topology
		client.killTopology(topologyId);
		
		}catch (Exception exception) {
			throw new RuntimeException("Error occurred while killing the topology : "+exception);
		}
	}
	
	public static void main(String[] args) {
		new killTopology().kill("topologyId");
	}
}
