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

package com.baguaz.module.flink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.baguaz.C;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/flink", viewPath = "admin/flink")
public class FlinkAdminController extends BaseController {
	@RequiresPermissions("menu:flink")
	public void init(){
		this.render("flink_manage.html");
	}
	
	@RequiresPermissions("menu:flink")
	public void list(){
		List<Record> flinkList=Flink.dao.selectR("*","","listorder desc,id asc","","");
		Ret ret=Ret.create();
		ret.put("data",flinkList);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("flink:add")
	@Before(Tx.class)
	public void add(){
		if(this.isParaExists("dosubmit")){
			Flink flink=new Flink();
			flink.set("name", this.getPara("name"));
			flink.set("icon", this.getPara("icon"));
			flink.set("url", this.getPara("url"));
			flink.save();
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			this.render("flink_add.html");
		}
	}
	
	@RequiresPermissions("flink:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")){
			Flink flink=Flink.dao.findById(this.getPara("id"));
			flink.set("name", this.getPara("name"));
			flink.set("icon", this.getPara("icon"));
			flink.set("url", this.getPara("url"));
			flink.update();
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Record flink=Flink.dao.findById(this.getPara("id")).toRecord();
			this.setAttrs(flink.getColumns());
			this.render("flink_edit.html");
		}
	}
	
	@RequiresPermissions("flink:del")
	@Before(Tx.class)
	public void del(){
		String[] dcs=this.getParaValues("delcheck");
		String dcsStr=Arrays.stream(dcs).collect(Collectors.joining(","));
		Db.update("delete from "+Flink.dao.tn()+" where id in("+dcsStr+")");
		Ret ret=Ret.create().setCodeAndMsg(C.success)
				   .setRdurl("/admin/flink/init");
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
	
	@RequiresPermissions("flink:listorder")
	@Before(Tx.class)
	public void listorder(){
		if(this.isParaExists("dosubmit")) {
			Enumeration<String> paras=this.getParaNames();
			ArrayList<String> sqlList=Lists.newArrayList();
			while(paras.hasMoreElements()){
				String name=paras.nextElement();
				if(name.startsWith("listorders[")){
					Integer listorder=Ints.tryParse(this.getPara(name));
					int intListorder=(listorder==null?0:listorder);
					int id=Integer.parseInt(name.substring(11,name.length()-1));
					sqlList.add("update "+Flink.dao.tn()+" set listorder="+intListorder+" where id="+id);
				}
			}
			if(sqlList.size()>=1){
				Db.batch(sqlList,5);
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success)
					   .setRdurl("/admin/flink/init");
			this.setAttr("ret", ret).render("/admin/retmsg.html");
		}
	}
}