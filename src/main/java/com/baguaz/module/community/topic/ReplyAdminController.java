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

package com.baguaz.module.community.topic;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.baguaz.common.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/reply")
public class ReplyAdminController extends BaseController {
	@RequiresPermissions("menu:reply")
	public void init(){
		this.render("reply_manage.html");
	}
	
	@RequiresPermissions("menu:reply")
	public void list(){
		
	}
	
	@RequiresPermissions("reply:del")
	@Before(Tx.class)
	public void del(){
		
	}
}
