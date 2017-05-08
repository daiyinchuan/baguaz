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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.module.user.UserPrincipal;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.kit.StrKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class CatPrivInterceptor implements Interceptor {
	private static final Logger log = LoggerFactory.getLogger(CatPrivInterceptor.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jfinal.aop.Interceptor#intercept(com.jfinal.aop.Invocation)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void intercept(Invocation inv) {
		UserPrincipal principal = UserPrincipal.get();
		if (!principal.isAdmin()) {
			Set<Integer> catids = principal.getCatpriv();
			log.debug("catids=" + catids);
			Controller ctrl = inv.getController();
			int catid = ctrl.getParaToInt("catid");
			if (!catids.contains(catid)) {
				HttpServletRequest req = ctrl.getRequest();
				if (req.getHeader("x-requested-with") != null
						&& "XMLHttpRequest".equalsIgnoreCase(req.getHeader("x-requested-with"))) {
					ctrl.getResponse().setStatus(403, ShiroKit.getUnauthorizedUrl());
					ctrl.renderJson();
					return;
				} else {
					if (StrKit.notBlank(ShiroKit.getUnauthorizedUrl())) {
						ctrl.redirect(ShiroKit.getUnauthorizedUrl());
					} else {
						ctrl.renderError(403);
					}
					return;
				}
			}
		}
		inv.invoke();
	}
}
