package com.learningstorm.stormlogprocessing;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * This class use the KeywordGenerator class to generate the search keyword from
 * referrer URL.
 * 
 */
public class KeyWordIdentifierBolt extends BaseRichBolt {

	private static final long serialVersionUID = 1L;
	private KeywordGenerator keywordGenerator = null;
	public OutputCollector collector;

	public KeyWordIdentifierBolt() {

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("ip", "dateTime", "request", "response",
				"bytesSent", "referrer", "useragent", "country", "browser",
				"os", "keyword"));
	}

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		this.keywordGenerator = new KeywordGenerator();

	}

	public void execute(Tuple input) {

		String referrer = input.getStringByField("referrer").toString();
		// call the getKeyword(String referrer) method KeywordGenerator class to
		// generate the search keyword.
		Object keyword = keywordGenerator.getKeyword(referrer);
		// emits all the field emitted by previous bolt + keyword
		collector.emit(new Values(input.getString(0), input.getString(1), input
				.getString(2), input.getString(3), input.getString(4), input
				.getString(5), input.getString(6), input.getString(7), input
				.getString(8), input.getString(9), keyword));

	}
}