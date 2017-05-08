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

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class ArtemisKit {
	private static final Logger log = LoggerFactory.getLogger(ArtemisKit.class);
	
	private static Connection connection;
	private static ConcurrentHashMap<String,Queue> queues=new ConcurrentHashMap<>();
	
	private static ObjectPool<Session> sessionPool;
	
	public static String QUEUE_EMAIL="queue_email";
	public static String QUEUE_SCORETASK="queue_scoreTask";
	
	public static Connection getConnection() {
		return connection;
	}
	public static void setConnection(Connection connection) {
		ArtemisKit.connection = connection;
	}
	public static Queue getQueue(String name) {
		return queues.get(name);
	}
	public static void addQueue(String name,Queue queue) {
		queues.put(name, queue);
	}
	
	public static Session getSession(){
		try {
			return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Session getPooledSession(){
		try {
			return sessionPool().borrowObject();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void retPooledSession(Session session){
		try {
			sessionPool().returnObject(session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static ObjectPool<Session> sessionPool(){
		if(sessionPool==null){
			GenericObjectPoolConfig conf=new GenericObjectPoolConfig();
			conf.setMinIdle(0);
			conf.setMaxIdle(5);
			conf.setMaxTotal(10);
			conf.setMaxWaitMillis(60000);
			sessionPool=new GenericObjectPool<Session>(new SessionPooledObjectFactory(),conf);
		}
//		log.debug("{NumActive:"+sessionPool.getNumActive()+",NumIdl:"+sessionPool.getNumIdle()+"}");
		return sessionPool;
	}
	
	/**
	 * jms Session池对象工厂
	 * @author daiyc
	 */
	private static class SessionPooledObjectFactory
		extends BasePooledObjectFactory<Session>{
		@Override
		public Session create() throws Exception {
			return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		@Override
		public PooledObject<Session> wrap(Session session) {
			return new DefaultPooledObject<Session>(session);
		}
	    @Override
	    public void destroyObject(PooledObject<Session> pooledSession)
	        throws Exception  {
	    	pooledSession.getObject().close();
	    }
	}
}
