/**
 * Copyright (c) 2015-2016, Yinchuan Dai 戴银川 (daiyinchuan@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.ext.plugin.artemis;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.plugin.IPlugin;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class ArtemisPlugin implements IPlugin {
	private static final Logger log = LoggerFactory.getLogger(ArtemisPlugin.class);

	private EmbeddedJMS jmsServer;
	private ConcurrentLinkedQueue<Session> consumerSessions=new ConcurrentLinkedQueue<>();
	
	private int emailConsumerSessionNum=2;
	private int scoreTaskConsumerSessionNum=1;
	
	private MessageListener emailMsgListener;
	private MessageListener scoreTaskMsgListener;

	public void setEmailConsumerSessionNum(int emailConsumerSessionNum) {
		this.emailConsumerSessionNum = emailConsumerSessionNum;
	}

	public void setScoreTaskConsumerSessionNum(int scoreTaskConsumerSessionNum) {
		this.scoreTaskConsumerSessionNum = scoreTaskConsumerSessionNum;
	}

	public void setEmailMsgListener(MessageListener emailMsgListener) {
		this.emailMsgListener = emailMsgListener;
	}

	public void setScoreTaskMsgListener(MessageListener scoreTaskMsgListener) {
		this.scoreTaskMsgListener = scoreTaskMsgListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jfinal.plugin.IPlugin#start()
	 */
	@Override
	public boolean start() {
		// Step 1. Create ActiveMQ Artemis core configuration, and set the
		// properties accordingly
		Configuration configuration = new ConfigurationImpl().setPersistenceEnabled(false)
				.setJournalDirectory("target/data/journal").setSecurityEnabled(false)
				.addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName()))
				.addConnectorConfiguration("connector",
						new TransportConfiguration(NettyConnectorFactory.class.getName()));

		// Step 2. Create the JMS configuration
		JMSConfiguration jmsConfig = new JMSConfigurationImpl();

		// Step 3. Configure the JMS ConnectionFactory
		ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl().setName("cf")
				.setConnectorNames(Arrays.asList("connector")).setBindings("cf");
		jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

		// Step 4. Configure the JMS Queue
		JMSQueueConfiguration queueEmailConfig = new JMSQueueConfigurationImpl().setName(ArtemisKit.QUEUE_EMAIL).setDurable(false)
				.setBindings("queue/email");
		jmsConfig.getQueueConfigurations().add(queueEmailConfig);
		
		JMSQueueConfiguration queueScoreTaskConfig = new JMSQueueConfigurationImpl().setName(ArtemisKit.QUEUE_SCORETASK).setDurable(false)
				.setBindings("queue/scoreTask");
		jmsConfig.getQueueConfigurations().add(queueScoreTaskConfig);
		

		// Step 5. Start the JMS Server using the ActiveMQ Artemis core server
		// and the JMS configuration

		try {
			jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig).start();
			log.debug("Started Embedded JMS Server");
		} catch (Exception e) {
			log.error("", e);
		}

		// Step 6. Lookup JMS resources defined in the configuration
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup("cf");
		Queue queueEmail = (Queue) jmsServer.lookup("queue/email");
		ArtemisKit.addQueue(ArtemisKit.QUEUE_EMAIL,queueEmail);
		Queue queueScoreTask = (Queue) jmsServer.lookup("queue/scoreTask");
		ArtemisKit.addQueue(ArtemisKit.QUEUE_SCORETASK,queueScoreTask);

		try {
			Connection connection = cf.createConnection();
			ArtemisKit.setConnection(connection);
			connection.start();
			
			for(int i=0;i<emailConsumerSessionNum;i++){
				Session session=ArtemisKit.getSession();
				consumerSessions.add(session);
				MessageConsumer consumer = session.createConsumer(queueEmail);
				consumer.setMessageListener(emailMsgListener);
			}
			
			for(int i=0;i<scoreTaskConsumerSessionNum;i++){
				Session session=ArtemisKit.getSession();
				consumerSessions.add(session);
				MessageConsumer consumer = session.createConsumer(queueScoreTask);
				consumer.setMessageListener(scoreTaskMsgListener);
			}
		} catch (JMSException e) {
			log.error("", e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jfinal.plugin.IPlugin#stop()
	 */
	@Override
	public boolean stop() {
		consumerSessions.stream().forEach(cs->{
			try {
				cs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		Connection connection = ArtemisKit.getConnection();
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		// Step 11. Stop the JMS server
		try {
			jmsServer.stop();
		} catch (Exception e) {
			log.error("", e);
		}
		log.debug("Stopped the JMS Server");

		return true;
	}

}
