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

package com.baguaz.module.site;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/site", viewPath = "admin/site")
public class SiteAdminController extends BaseController{
	private static final Logger log=LoggerFactory
			.getLogger(SiteAdminController.class);
	
	@RequiresPermissions("menu:site")
	public void init(){
		this.render("site_manage.html");
	}
	@RequiresPermissions("menu:site")
	public void list(){
		List<Record> siteList=Site.dao.selectR("siteid,name,domain","","siteid desc","","");
		Ret ret=Ret.create();
		ret.put("data",siteList);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	@RequiresPermissions("site:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")){
			log.debug("进入site修改提交请求");
			Site site=Site.dao.findById(this.getParaToInt("siteid"));
			site.set("name",this.getPara("name"))
				.set("domain",this.getPara("domain"))
				.set("site_title",this.getPara("site_title"))
				.set("keywords",this.getPara("keywords"))
				.set("description",this.getPara("description"))
				.set("default_style",this.getPara("default_style"));
			
			String[] templateArr=this.getParaValues("template");
			site.set("template",StringUtils.join(templateArr,"|"));
			
			Map<String,Object> setting=Maps.newLinkedHashMap();
			setting.put("index_ishtml",this.getParaToInt("setting[index_ishtml]"));
			setting.put("social_share_code", this.getPara("setting[social_share_code]"));
			setting.put("social_comment_code", this.getPara("setting[social_comment_code]"));
			setting.put("social_tuijian_code", this.getPara("setting[social_tuijian_code]"));
			setting.put("pvetc_stati_code", this.getPara("setting[pvetc_stati_code]"));
			setting.put("index_title", this.getPara("setting[index_title]"));
			
			site.set("setting",BgzKit.obj2json(setting));
			
			Ret ret=Ret.create();
			if(site.update()){
				CacheFunc.removeCommonsSites();
				CacheFunc.removeCommonsSiteSetting();
				ret.setCodeAndMsg(C.success);
				this.renderJson(ret);
			}else{
				ret.setCodeAndMsg(C.fail);
			}
		}else{
			log.debug("进入site修改页");
			Site site=Site.dao.findById(this.getParaToInt("siteid"));
			this.setAttrs(site.toRecord().getColumns());
			this.setAttr("templatelist",GlobalFunc.getFsTemplateStyles());
			this.render("site_edit.html");
		}
	}
}
