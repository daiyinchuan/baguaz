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

package com.baguaz.pagetpl;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class TagCache {
	private static final Logger log=LoggerFactory
			.getLogger(TagCache.class);
	
	public static TagCache me;
	private ConcurrentHashMap<String,Object> map=new ConcurrentHashMap<String,Object>();
	private ScheduledExecutorService ses;
	
	public static TagCache getInstance(){
		if(me==null){
			me=new TagCache();
		}
		return me;
	}
	
	private void newSes(){
		ses=Executors.newScheduledThreadPool(10);
	}
	
	private TagCache(){
		this.newSes();
	}
	
	public boolean contains(String key){
		return map.containsKey(key);
	}
	
	public void set(String key,int expireSecond,Callable<Object> callable){
		try {
			map.put(key, callable.call());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ses.schedule(new Runnable(){
			@Override
			public void run() {
				log.debug("cacheKey为["+key+"]缓存"+expireSecond+"秒时间到了");
				map.remove(key);
			}
		}, expireSecond, TimeUnit.SECONDS);
	}
	
	public Object get(String key){
		return map.get(key);
	}
	
	public void clear(){
		ses.shutdownNow();
		this.newSes();
		map.clear();
	}
}