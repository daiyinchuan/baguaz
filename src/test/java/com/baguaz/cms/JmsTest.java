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

package com.baguaz.cms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig.G;
import com.baguaz.util.EmailSender;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.kit.PropKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class JmsTest {
	private static final Logger log=LoggerFactory
			.getLogger(JmsTest.class);
	
	private static EmbeddedJMS jmsServer;
	private static Connection connection;
	private static Queue queue;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		@SuppressWarnings("unchecked")
		Map<String,String> config=(Map<String,String>)GroovyKit.runScriptFromFile(G.baguaz_prop);
		PropKit.use(G.baguaz_prop,config);
		
		// Step 1. Create ActiveMQ Artemis core configuration, and set the properties accordingly
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
		JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl().setName("queue1").setDurable(false)
				.setBindings("queue/queue1");
		jmsConfig.getQueueConfigurations().add(queueConfig);

		// Step 5. Start the JMS Server using the ActiveMQ Artemis core server
		// and the JMS configuration
		jmsServer = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig).start();
		log.debug("Started Embedded JMS Server");

		// Step 6. Lookup JMS resources defined in the configuration
		ConnectionFactory cf = (ConnectionFactory) jmsServer.lookup("cf");
		queue = (Queue) jmsServer.lookup("queue/queue1");

		connection = cf.createConnection();

		connection.start();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
         }
         // Step 11. Stop the JMS server
         jmsServer.stop();
         log.debug("Stopped the JMS Server");
	}

	@Test
	public void testTxtMessage() throws JMSException{
	      // Step 7. Send and receive a message using JMS API
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         
         MessageProducer producer = session.createProducer(queue);
         TextMessage message = session.createTextMessage("Hello sent at " + new Date());
         log.debug("Sending message: " + message.getText());
         producer.send(message);
         
         MessageConsumer messageConsumer = session.createConsumer(queue);
         TextMessage messageReceived = (TextMessage) messageConsumer.receive(1000);
         log.debug("Received message:" + messageReceived.getText());
         
         session.close();
	}
	
	//@Test
	public void testMapMessage() throws JMSException{
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		MessageProducer producer=session.createProducer(queue);
		MapMessage msg=session.createMapMessage();
		msg.setString("title", "我是标题");
		msg.setString("mailTo", "daiyinchuan@163.com");
		msg.setString("content", "我是内容");
		producer.send(msg);
		
		MessageConsumer consumer = session.createConsumer(queue);
		MapMessage getMsg=(MapMessage)consumer.receive(1000);
		EmailSender.syncSendMail(getMsg.getString("title"), new String[]{getMsg.getString("mailTo")}, getMsg.getString("content"));
		
		session.close();
	}
	
	private static class Email implements Serializable{
		private static final long serialVersionUID = -2562018977562336303L;
		private String title;
		private String[] mailTo;
		private String content;
		public String getTitle() {
			return title;
		}
		public Email setTitle(String title) {
			this.title = title;
			return this;
		}
		public String[] getMailTo() {
			return mailTo;
		}
		public Email setMailTo(String[] mailTo) {
			this.mailTo = mailTo;
			return this;
		}
		public String getContent() {
			return content;
		}
		public Email setContent(String content) {
			this.content = content;
			return this;
		}
	}
	
	private static class EmailMessageListener implements MessageListener{
		@Override
		public void onMessage(Message message) {
			ObjectMessage om=(ObjectMessage)message;
			try {
				Email email = (Email)om.getObject();
				EmailSender.syncSendMail(email.getTitle(),email.getMailTo(), email.getContent());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testObjectMessage() throws JMSException{
		for(int i=0;i<10;i++){
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer=session.createProducer(queue);
			ObjectMessage msg=session.createObjectMessage(new Email()
					.setTitle("我是标题")
					.setMailTo(new String[]{"daiyinchuan@163.com"})
					.setContent("我是内容"));
			producer.send(msg);
			session.close();
		}
		
		for(int i=0;i<3;i++){
			Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = session1.createConsumer(queue);
			consumer.setMessageListener(new EmailMessageListener());
		}
		
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}