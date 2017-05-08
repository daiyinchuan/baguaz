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

package com.baguaz.module.community.section;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.GlobalFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.module.community.section.Section.SecTreeNode;
import com.baguaz.module.site.Site;
import com.baguaz.module.urlrule.Urlrule;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/community/section", viewPath = "admin/community/section")
public class SectionAdminController extends BaseController {
	private static final Logger log=LoggerFactory
			.getLogger(SectionAdminController.class);
	@RequiresPermissions("menu:section")
	public void init(){
		this.render("section_manage.html");
	}
	
	@RequiresPermissions("menu:section")
	public void list(){
		int parentid=0;
		String arrchildid=Section.dao.getArrchildid(parentid);
		Ret ret=Ret.create();
		if(!Strings.isNullOrEmpty(arrchildid)){
			String wherein=parentid+","+arrchildid;
			List<Record> secs=Section.dao.selectR("*","id in("+wherein+")","parentid,listorder","","");
			List<Map<String,Object>> secTreeNodes=SecTreeNode.buildTree(secs,parentid).toSecList();
			ret.put("data",secTreeNodes);
		}else{//空数据
			ret.put("data",ImmutableList.of());
		}
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("section:add")
	@Before(Tx.class)
	public void add(){
		if(this.isParaExists("dosubmit")) {
			Section sec=new Section();
			int parentid=this.getParaToInt("parentid");
			sec.set("parentid",parentid);
			sec.set("name",this.getPara("name"));
			sec.set("tab",this.getPara("tab"));
			sec.set("isdisplay",this.getParaToInt("isdisplay"));
			
			if(parentid==0){
				sec.set("arrparentid",parentid);//0
			}else{
				Section psec=Section.dao.findById(parentid);
				sec.set("arrparentid",psec.getStr("arrparentid")+","+parentid);
			}
			
			Map<String,Object> settings=Maps.newLinkedHashMap();
			settings.put("section_ruleid",this.getParaToInt("setting[section_ruleid]"));
			settings.put("thread_ruleid",this.getParaToInt("setting[thread_ruleid]"));
			settings.put("template_list",this.getPara("setting[template_list]"));
			settings.put("section_template",this.getPara("setting[section_template]"));
			settings.put("thread_template",this.getPara("setting[thread_template]"));
			settings.put("meta_title",this.getPara("setting[meta_title]"));
			settings.put("meta_keywords",this.getPara("setting[meta_keywords]"));
			settings.put("meta_description",this.getPara("setting[meta_description]"));
			
			sec.set("setting",BgzKit.obj2json(settings));
			
			if(!sec.save()){
				Ret ret=Ret.create();
				ret.setCodeAndMsg(C.fail);
				this.renderJson(ret);
				return;
			}
			
			int secid=sec.getInt("id");
			sec.set("arrchildid",secid);
			sec.set("listorder",secid);
			
			buildUrl(sec);
			
			sec.update();
			
			updateParent(Section.dao.findById(parentid));
			
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.scs_dataadd);
			this.renderJson(ret);
		}else{
			int parentid=0;
			String arrchildid=Section.dao.getArrchildid(parentid);
			if(!Strings.isNullOrEmpty(arrchildid)){
				String wherein=parentid+","+arrchildid;
				List<Record> secs=Section.dao.selectR("*","id in("+wherein+")","parentid,listorder","","");
				if(secs==null || secs.size()==0){
					this.setAttr("seclist",ImmutableList.of());
				}else{
					this.setAttr("seclist",SecTreeNode.buildTree(secs,parentid).toSecList());
				}
			}else{//空数据
				this.setAttr("seclist",ImmutableList.of());
			}
			
			this.setAttr("secSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_SEC,Urlrule.ISHTML_N));
			this.setAttr("thrSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_THR,Urlrule.ISHTML_N));
			
			this.setAttr("templatelist",GlobalFunc.getSiteTemplateStyles());
			
			String defaultStyle=Site.dao.getOne("default_style","siteid=?","","",Site.SITEID);
			this.setAttr("default_style",defaultStyle);
			
			this.render("section_add.html");
		}
	}
	
	/*
	 * 递归更新父栏目的child和arrchildid
	 */
	private void updateParent(Section parent){
		if(parent==null){
			return;
		}
		List<String> childs=Section.dao.select("arrchildid","parentid=?","","","",parent.getInt("id"));
		String arrchildid=""+parent.getInt("id");
		for(Iterator<String> it=childs.iterator();it.hasNext();){
			arrchildid+=(","+it.next());
		}
		parent.set("arrchildid",arrchildid);
		if(arrchildid.indexOf(",")>0){
			parent.set("child",1);
		}else{
			parent.set("child",0);
		}
		parent.update();
		updateParent(Section.dao.findById(parent.getInt("parentid")));
	}
	
	private void buildUrl(Section self){
		int secrid=this.getParaToInt("setting[section_ruleid]");
		int secid=self.getInt("id");
		String domain=Site.dao.getDomain();
		
		String ur=Urlrule.dao.findById(secrid).getStr("urlrule");
		Object url="";
		if(ur.indexOf("|")>=0){//动态
			ur=ur.split("\\|")[0];
			ur=GroovyKit.j2gStr(ur);
			url=GroovyKit.runScriptFromStr(ur,ImmutableMap.of(
					"secid",(Object)secid,
					"tab",(Object)self.getStr("tab")
					));
			url=domain+url.toString();
		}else{//伪静态
			ur=GroovyKit.j2gStr(ur);
			url=GroovyKit.runScriptFromStr(ur,ImmutableMap.of(
					"secid",(Object)secid,
					"page",(Object)1,
					"tab",(Object)self.getStr("tab"),
					"label",(Object)"latest"
					));
			url=domain+url.toString();
		}
		self.set("url",url);
	}
	
	public void tplFileList(){
		String style=this.getPara("style");
		
		Ret ret=Ret.create();
		ret.put("sectpllist",GlobalFunc.sectplList(style));
		ret.put("thrtpllist",GlobalFunc.thrtplList(style));
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("menu:section")
	public void checktab(){
		String tab=this.getPara("tab");
		int id=this.getParaToInt("id",0);
		
		Ret ret=Ret.create();
		
		List<Integer> secids=Section.dao.select("id","tab=? and id not in(?)","","","",tab,id);
		
		if(secids.size()>=1){
			ret.put("error","标签已存在");
		}else{
			ret.put("ok","");
		}
		this.renderJson(ret.getMap());
	}
	
	@RequiresPermissions("section:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")) {
			int id=this.getParaToInt("id");
			Section sec=Section.dao.findById(id);
			
			int parentid=this.getParaToInt("parentid");
			int oparentid=sec.getInt("parentid");
			
			if(parentid==0){
				sec.set("arrparentid",parentid);//0
			}else{
				Section psec=Section.dao.findById(parentid);
				sec.set("arrparentid",psec.getStr("arrparentid")+","+parentid);
			}
			sec.set("parentid",parentid);
			
			sec.set("name",this.getPara("name"));
			sec.set("tab",this.getPara("tab"));
			sec.set("isdisplay",this.getParaToInt("isdisplay"));
			
			Map<String,Object> settings=Maps.newLinkedHashMap();
			settings.put("section_ruleid",this.getParaToInt("setting[section_ruleid]"));
			settings.put("thread_ruleid",this.getParaToInt("setting[thread_ruleid]"));
			settings.put("template_list",this.getPara("setting[template_list]"));
			settings.put("section_template",this.getPara("setting[section_template]"));
			settings.put("thread_template",this.getPara("setting[thread_template]"));
			settings.put("meta_title",this.getPara("setting[meta_title]"));
			settings.put("meta_keywords",this.getPara("setting[meta_keywords]"));
			settings.put("meta_description",this.getPara("setting[meta_description]"));
			
			sec.set("setting",BgzKit.obj2json(settings));
			
			this.buildUrl(sec);
			
			if(!sec.update()){
				Ret ret=Ret.create();
				ret.setCodeAndMsg(C.fail);
				this.renderJson(ret);
				return;
			}
			
			//更新新的父板块
			updateParent(Section.dao.findById(parentid));
			//更新子板块
			updateChild(sec);
			//更新老的父板块
			updateParent(Section.dao.findById(oparentid));
			
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			int parentid=0;
			String arrchildid=Section.dao.getArrchildid(parentid);
			if(!Strings.isNullOrEmpty(arrchildid)){
				String wherein=parentid+","+arrchildid;
				List<Record> secs=Section.dao.selectR("*","id in("+wherein+")","parentid,listorder","","");
				if(secs==null || secs.size()==0){
					this.setAttr("seclist",ImmutableList.of());
				}else{
					this.setAttr("seclist",SecTreeNode.buildTree(secs,parentid).toSecList());
				}
			}else{//空数据
				this.setAttr("seclist",ImmutableList.of());
			}
			
			Section sec=Section.dao.findById(this.getParaToInt("id"));
			this.setAttrs(sec.toRecord().getColumns());
			
			Map<String,Object> settings=BgzKit.json2obj(sec.getStr("setting"));
			
			this.setAttr("secSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_SEC,Urlrule.ISHTML_N));
			this.setAttr("thrSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_THR,Urlrule.ISHTML_N));
			
			this.setAttr("templatelist",GlobalFunc.getSiteTemplateStyles());
			
			String style=(String)settings.get("template_list");
			this.setAttr("sectpllist",GlobalFunc.sectplList(style));
			this.setAttr("thrtpllist",GlobalFunc.thrtplList(style));
			
			this.render("section_edit.html");
		}
	}
	
	/*
	 * 循环更新子板块的arrparentid
	 */
	private void updateChild(Section self){
		String arrchildid=self.getStr("arrchildid");
		if(arrchildid.indexOf(",")<0){
			return;
		}
		String[] childids=arrchildid.split(",");
		for(int i=1;i<childids.length;i++){
			Section child=Section.dao.findById(childids[i]);
			int parentid=child.getInt("parentid");
			if(parentid==0){
				child.set("arrparentid",parentid);//0
			}else{
				Section parent=Section.dao.findById(parentid);
				child.set("arrparentid",parent.getStr("arrparentid")+","+parentid);
			}
			child.update();
		}
	}
	
	@RequiresPermissions("section:del")
	@Before(Tx.class)
	public void del(){
		Section self=Section.dao.findById(this.getParaToInt("id"));
		
		/*
		 * 处理子栏目
		 */
		String arrchildid=self.getStr("arrchildid");
		
		if(arrchildid.indexOf(",")>=0){
			String[] childids=arrchildid.split(",");
			for(int i=1;i<childids.length;i++){
				Section child=Section.dao.findById(childids[i]);
				
				/*
				 * 删除所属的话题和回复
				 */
				
				child.delete();
			}
		}else{
			/*
			 * 删除所属的话题和回复
			 */
		}
		
		self.delete();
		
		updateParent(Section.dao.findById(self.getInt("parentid")));
		
		Ret ret=Ret.create().setCodeAndMsg(C.success).setRdurl("/admin/section/init");
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
	
	@RequiresPermissions("section:listorder")
	@Before(Tx.class)
	public void listorder(){
		if(this.isParaExists("dosubmit")) {
			Enumeration<String> paras=this.getParaNames();
			ArrayList<String> sqls=Lists.newArrayList();
			while(paras.hasMoreElements()){
				String para=paras.nextElement();
				if(para.startsWith("listorders[")){
					Integer listorder=Ints.tryParse(this.getPara(para));
					int intListorder=(listorder==null?0:listorder);
					int id=Integer.parseInt(para.substring(11,para.length()-1));
					sqls.add("update "+Section.dao.tn()+" set listorder="+intListorder+" where id="+id);
				}
			}
			log.debug("listorder="+sqls);
			if(sqls.size()>=1){
				Db.batch(sqls,5);
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success).setRdurl("/admin/section/init");
			this.setAttr("ret", ret).render("/admin/retmsg.html");
		}
	}
}
