package com.learningstorm.ml;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

import com.github.pmerienne.trident.ml.core.Instance;

/**
 * This class converts a string into an Instance required by Trident-ML.
 */
public class FeaturesToValues extends BaseFunction {

	@SuppressWarnings("rawtypes")
	public void execute(TridentTuple tuple, TridentCollector collector) {
		// get the input string
		String line = tuple.getString(0);

		double[] features = new double[60];

		// split the input string and iterate over them and covert to double
		String[] featureList = line.split("\\s+");
		for(int i = 0; i < features.length; i++){
			features[i] = Double.parseDouble(featureList[i]);
		}

		// emit the Instance object with the features from given input string
		collector.emit(new Values(new Instance(features)));
	}
}
