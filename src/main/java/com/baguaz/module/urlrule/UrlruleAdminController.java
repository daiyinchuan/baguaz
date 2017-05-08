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

package com.baguaz.module.urlrule;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.baguaz.C;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/urlrule", viewPath = "admin/urlrule")
public class UrlruleAdminController extends BaseController{
	@RequiresPermissions("menu:urlrule")
	public void init(){
		this.renderText("未实现");
	}
	
	public void getSelUrList(){
		String file=this.getPara("file");
		int ishtml=this.getParaToInt("ishtml");
		List<Record> selUrList=Urlrule.dao.getSelUrs(file,ishtml);
		Ret ret=Ret.create();
		ret.put("selUrList",selUrList);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
}
