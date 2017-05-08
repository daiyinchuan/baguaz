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

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.GeneralVarTagBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.google.common.collect.Maps;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public abstract class BgzTag extends GeneralVarTagBinding {
	private static final Logger log=LoggerFactory
			.getLogger(BgzTag.class);
	
	protected Map<String,String> prepare(){
		@SuppressWarnings("unchecked")
		Map<String,String> paras = (Map<String,String>) this.args[1];
		
		/*
		 * 对非字符串类型传进来的参数统一转换成字符串
		 */
		paras.entrySet().forEach(p->{
			if(!((Object)p.getValue() instanceof String)){
				String strP=String.valueOf(p.getValue());
				paras.put(p.getKey(), strP);
			}
		});
		
		return paras;
	}
	
	protected Object[] handleWithCache(Map<String, String> paras){
		Object[] data=null;
		if(StringUtils.isNotEmpty(paras.get("cache")) && !BgzKit.devMode){//单位秒
			/*String cacheKey=tagName+"_"+paras.entrySet().stream()
				.sorted((a,b)->a.getKey().compareTo(b.getKey()))
				.map(e->e.getValue().replace(' ', '-')).collect(Collectors.joining("_"));*/
			Map<String,String> sortedParas=Maps.newTreeMap();
			sortedParas.put("_tag", (String)this.args[0]);
			sortedParas.putAll(paras);
			String cacheKey=BgzKit.obj2json(sortedParas);
			
			log.debug("cacheKey plain="+cacheKey);
			cacheKey=DigestUtils.md5Hex(cacheKey);
			log.debug("cacheKey md5="+cacheKey);
			
			TagCache tc=TagCache.getInstance();
			if(!tc.contains(cacheKey)){
				int cacheSecond=Integer.parseInt(paras.get("cache"));
				tc.set(cacheKey, cacheSecond, new Callable<Object>(){
					@Override
					public Object call() throws Exception {
						return doExpensive(paras);
					}
				});
			}
			data=(Object[])tc.get(cacheKey);
		}else{
			data=doExpensive(paras);
		}
		return data;
	}

	/**
	 * @param paras
	 * @return
	 */
	abstract Object[] doExpensive(Map<String, String> paras);
}
