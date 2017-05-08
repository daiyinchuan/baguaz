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

package com.baguaz.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig.G;
import com.baguaz.BgzKit;
import com.baguaz.module.member.Member;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class CommonInterceptor implements Interceptor {
	private static final Logger log=LoggerFactory
			.getLogger(CommonInterceptor.class);

	/* (non-Javadoc)
	 * @see com.jfinal.aop.Interceptor#intercept(com.jfinal.aop.Invocation)
	 */
	@Override
	public void intercept(Invocation inv) {
		Controller ctrl = inv.getController();
		
        // session cookie 互换
        String m_cookie = ctrl.getCookie(G.COOKIE_MEMBER);
        Member m_session = (Member) ctrl.getSession().getAttribute(G.SESSION_MEMBER);
        if (StringUtils.isBlank(m_cookie) && m_session != null) {
        	ctrl.setCookie(G.COOKIE_MEMBER, BgzKit.getEncryptToken(m_session.getStr("token")), 30 * 24 * 60 * 60);
        } else if (StringUtils.isNotBlank(m_cookie) && m_session == null) {
        	Member m = Member.dao.findByToken(BgzKit.getDecryptToken(m_cookie));
        	m.set("lastlogintime", BgzKit.genUnixTstamp());
			String realip=ctrl.getRequest().getHeader("X-Real-IP");
			if(StringUtils.isEmpty(realip)){
				realip=ctrl.getRequest().getRemoteAddr();
			}
			m.set("lastloginip", realip);
			m.update();
        	ctrl.setSessionAttr(G.SESSION_MEMBER, m);
        }
		
		log.debug("session id="+ctrl.getSession().getId());
		// 获取今天时间，放到session里
		ctrl.setSessionAttr("now", BgzKit.genUnixTstamp());
		
        inv.invoke();
	}

}
