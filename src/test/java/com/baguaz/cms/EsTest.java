/**
 * 
 */
package com.baguaz.cms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.google.common.collect.ImmutableMap;

/**
 * @author daiyc
 *
 */
public class EsTest {
	private static final Logger log = LoggerFactory.getLogger(EsTest.class);

	private Client client;

	@Before
	public void setUp() throws UnknownHostException {
		client = TransportClient.builder().build()
				.addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("vm.gabsenv.com"), 9300));
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testIndex() {
		IndexResponse response = client
				.prepareIndex("twitter", "tweet", "1")
				.setSource(BgzKit.obj2json(
						ImmutableMap.of("user", "kimchy", "postDate", 
								new Date(), "message", "trying out Elasticsearch")))
				.get();
		log.info("response=" + BgzKit.obj2json(response, true));
	}
	@Test
	public void testGet(){
		GetResponse response=client
				.prepareGet("twitter","tweet","1")
				.get();
		log.info("response=" + BgzKit.obj2json(response, true));
	}

	@Test
	public void testDelete(){
		DeleteResponse response=client
				.prepareDelete("twitter","tweet","1")
				.get();
		log.info("response=" + BgzKit.obj2json(response, true));
	}
	
	@Test
	public void testUpdate(){
		UpdateResponse response=client
				.prepareUpdate("twitter","tweet","1")
				.setDoc(BgzKit.obj2json(
						ImmutableMap.of("message", "trying out Elasticsearch daiyc")))
				.get();
		log.info("response=" + BgzKit.obj2json(response, true));
	}
	
	@Test
	public void testBulk(){
		BulkResponse response=client
				.prepareBulk()
				.add(client.prepareIndex("twitter","tweet","2")
						.setSource(BgzKit.obj2json(
								ImmutableMap.of("message", "trying out Elasticsearch daiyc abc"))))
				.add(client.prepareDelete("twitter","tweet","1"))
				.get();
		if (response.hasFailures()) {
			log.error("response=" + BgzKit.obj2json(response, true));
		}else{
			log.info("response=" + BgzKit.obj2json(response, true));
		}
	}
	
	@Test
	public void testSearch(){
		SearchResponse response=client.prepareSearch("bank","megacorp")
			.setTypes("account","employee")
			.get();
		log.info("response=" + BgzKit.obj2json(response, true));
	}
	
	@Test
	public void testCount(){
		SearchResponse response=client.prepareSearch("bank","megacorp")
			.setTypes("account","employee")
			.setSize(0)
			.get();
		log.info("response=" + BgzKit.obj2json(response, true));
	}
	
	@Test
	public void testQuery(){
		SearchResponse response=client.prepareSearch("index")
			.setQuery(QueryBuilders.matchQuery("content", "移动平台登录"))
			.setFetchSource(false)
			.addHighlightedField("content")
			.get();
		log.info("response=" + BgzKit.obj2json(BgzKit.json2obj(response.toString()), true));
	}
}
