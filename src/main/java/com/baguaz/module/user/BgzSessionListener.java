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

package com.baguaz.module.user;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class BgzSessionListener implements SessionListener{
	private static final Logger log=LoggerFactory
			.getLogger(BgzSessionListener.class);
	/**
	 * 
	 */
	public BgzSessionListener(){
		// TODO Auto-generated constructor stub
	}
	private String buildLogStr(Session session){
		StringBuilder sb=new StringBuilder();
		sb.append("\n#################################################")
		  .append("\nid          :").append(session.getId())
		  .append("\nstart       :"+DateFormatUtils.format(session.getStartTimestamp(),"yyyy-MM-dd HH:mm:ss"))
		  .append("\nlast        :"+DateFormatUtils.format(session.getLastAccessTime(),"yyyy-MM-dd HH:mm:ss"))
		  .append("\ntimeout(min):"+session.getTimeout()/(1000*60))
		  .append("\nhost        :"+session.getHost())
		  .append("\nattr keys   :"+session.getAttributeKeys())
		  .append("\n#################################################");
		return sb.toString();
	}
	/* (non-Javadoc)
	 * @see org.apache.shiro.session.SessionListener#onStart(org.apache.shiro.session.Session)
	 */
	@Override
	public void onStart(Session session){
		log.debug("Session创建==="+buildLogStr(session));
	}
	/* (non-Javadoc)
	 * @see org.apache.shiro.session.SessionListener#onStop(org.apache.shiro.session.Session)
	 */
	@Override
	public void onStop(Session session){
		log.debug("Session停止==="+buildLogStr(session));
	}
	/* (non-Javadoc)
	 * @see org.apache.shiro.session.SessionListener#onExpiration(org.apache.shiro.session.Session)
	 */
	@Override
	public void onExpiration(Session session){
		log.debug("Session过期==="+buildLogStr(session));
	}
}
