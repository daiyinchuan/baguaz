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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig.G;
import com.baguaz.module.member.Member;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class MemberInterceptor implements Interceptor {
	private static final Logger log = LoggerFactory.getLogger(MemberInterceptor.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jfinal.aop.Interceptor#intercept(com.jfinal.aop.Invocation)
	 */
	@Override
	public void intercept(Invocation inv) {
		HttpServletRequest request = inv.getController().getRequest();
		HttpSession session = request.getSession();
		Member m = (Member) session.getAttribute(G.SESSION_MEMBER);
		if (m == null) {
			String uri = request.getRequestURI();
			String para = "";
			if (request.getQueryString() != null) {
				try {
					para = new String(request.getQueryString().getBytes("ISO8859-1"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if (!para.equals("")) {
				uri += "?" + para;
			}
			session.setAttribute(G.BEFORE_URL, uri);
			inv.getController().getResponse().setCharacterEncoding("UTF-8");
			try {
				inv.getController().getResponse().getWriter()
						.write("<script>alert('请先登录');location.href=\'/login\'</script>");
			} catch (IOException e) {
				log.error("", e);
			}
		} else {
			inv.invoke();
		}
	}
}
