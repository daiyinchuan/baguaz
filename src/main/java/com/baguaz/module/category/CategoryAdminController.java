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

package com.baguaz.module.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig;
import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.module.category.Category.CatTreeNode;
import com.baguaz.module.content.News;
import com.baguaz.module.content.NewsData;
import com.baguaz.module.content.Page;
import com.baguaz.module.model.Model;
import com.baguaz.module.position.PositionData;
import com.baguaz.module.site.Site;
import com.baguaz.module.urlrule.Urlrule;
import com.baguaz.module.user.Role;
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
@ControllerBind(controllerKey = "/admin/category", viewPath = "admin/category")
public class CategoryAdminController extends BaseController{
	private static final Logger log=LoggerFactory
			.getLogger(CategoryAdminController.class);
	@RequiresPermissions("menu:category")
	public void init(){
		this.render("category_manage.html");
	}
	
	@RequiresPermissions("menu:category")
	public void catlist(){
		int parentid=0;
		String arrchildid=Category.dao.getArrchildid(parentid);
		Ret ret=Ret.create();
		if(!Strings.isNullOrEmpty(arrchildid)){
			String wherein=parentid+","+arrchildid;
			List<Record> catList=Category.dao.selectR("listorder,catid,catname,type,modelid,items,url,parentid,child,parentdir,catdir,setting,ismenu",
					"catid in("+wherein+")","parentid,listorder","","");
			List<Map<String,Object>> catlist=CatTreeNode.buildTree(catList,parentid).toCatList();
			ret.put("data",catlist);
		}else{//空数据
			ret.put("data",ImmutableList.of());
		}
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("category:add")
	@Before(Tx.class)
	public void add(){
		if(this.isParaExists("dosubmit")) {
			int type=this.getParaToInt("info[type]");
			if(type==Category.TYPE_CAT){
				addCat();
			}else if(type==Category.TYPE_PAGE){
				addPage();
			}else if(type==Category.TYPE_LINK){
				addLink();
			}
		}else{
			int parentid=0;
			
			String arrchildid=Category.dao.getArrchildid(parentid);
			if(!Strings.isNullOrEmpty(arrchildid)){
				String wherein=parentid+","+arrchildid;
				List<Record> cats=Category.dao.selectR("catid,catname,parentid,listorder,ismenu",
						"type=0 and catid in("+wherein+")","parentid,listorder","","");
				if(cats==null || cats.size()==0){
					this.setAttr("catlist",ImmutableList.of());
				}else{
					this.setAttr("catlist",CatTreeNode.buildTree(cats,parentid).toCatList());
				}
			}else{//空数据
				this.setAttr("catlist",ImmutableList.of());
			}
			
			int type=this.getParaToInt("type");
			
			if(type==Category.TYPE_CAT || type==Category.TYPE_PAGE){
				this.setAttr("catSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_CAT,Urlrule.ISHTML_N));
				
				this.setAttr("templatelist",GlobalFunc.getSiteTemplateStyles());
				
				String default_style=Site.dao.getOne("default_style","siteid=?","","",Site.SITEID);
				this.setAttr("default_style",default_style);
				
				this.setAttr("roles",Role.dao.selectM("*", "id !=1", "", "", ""));
			}
			
			if(type==Category.TYPE_CAT){
				List<Record> modellist=Model.dao.selectR("id,name","id=1","id","","");
				this.setAttr("modellist",modellist);
				
				this.setAttr("showSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_SHOW,Urlrule.ISHTML_N));
				
				this.render("category_add.html");
			}else if(type==Category.TYPE_PAGE){
				this.render("category_page_add.html");
			}else if(type==Category.TYPE_LINK){
				this.render("category_link.html");
			}
		}
	}
	
	private void addCat(){
		Category cat=new Category();
		int type=this.getParaToInt("info[type]");
		cat.set("type",type);
		cat.set("modelid",this.getParaToInt("info[modelid]"));
		int parentid=this.getParaToInt("info[parentid]");
		cat.set("parentid",parentid);
		cat.set("catname",this.getPara("info[catname]"));
		String catdir=this.getPara("info[catdir]");
		cat.set("catdir",catdir);
		cat.set("image",this.getPara("info[image]"));
		cat.set("description",this.getPara("info[description]"));
		cat.set("ismenu",this.getParaToInt("info[ismenu]"));
		
		if(parentid==0){
			cat.set("arrparentid",parentid);//0
			cat.set("parentdir","");//""
		}else{
			Category pcat=Category.dao.findById(parentid);
			cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
			String parentdir=pcat.getStr("parentdir")+pcat.getStr("catdir")+"/";
			cat.set("parentdir",parentdir);
		}
		
		Map<String,Object> settings=Maps.newLinkedHashMap();
		settings.put("workflowid",this.getParaToInt("setting[workflowid]"));
		int ishtml=this.getParaToInt("setting[ishtml]");
		settings.put("ishtml",ishtml);
		settings.put("content_ishtml",this.getParaToInt("setting[content_ishtml]"));
		int catrid=this.getParaToInt("setting[category_ruleid]");
		settings.put("category_ruleid",catrid);
		settings.put("show_ruleid",this.getParaToInt("setting[show_ruleid]"));
		int create_to_html_root=this.getParaToInt("setting[create_to_html_root]");
		settings.put("create_to_html_root",create_to_html_root);

		settings.put("template_list",this.getPara("setting[template_list]"));
		settings.put("category_template",this.getPara("setting[category_template]"));
		settings.put("list_template",this.getPara("setting[list_template]"));
		settings.put("show_template",this.getPara("setting[show_template]"));
		settings.put("meta_title",this.getPara("setting[meta_title]"));
		settings.put("meta_keywords",this.getPara("setting[meta_keywords]"));
		settings.put("meta_description",this.getPara("setting[meta_description]"));
		
		cat.set("setting",BgzKit.obj2json(settings));
		
		if(!cat.save()){
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.fail);
			this.renderJson(ret);
			return;
		}
		
		int catid=cat.getInt("catid");
		cat.set("arrchildid",catid);
		cat.set("listorder",catid);
		
		buildUrl(cat);
		
		cat.update();
		
		updateParent(Category.dao.findById(parentid));
		
		/*
		 * 添加栏目权限
		 */
		addCatPriv(catid);
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.scs_dataadd);
		this.renderJson(ret);
	}
	
	private void buildUrl(Category self){
		int ishtml=this.getParaToInt("setting[ishtml]");
		int catrid=this.getParaToInt("setting[category_ruleid]");
		int catid=self.getInt("catid");
		String domain=Site.dao.getDomain();
		if(ishtml==1){//生成静态
			if(this.isParaBlank("info[url]")){
				Urlrule ur=Urlrule.dao.findById(catrid);
				String urStr=ur.getStr("urlrule");
				// ${categorydir}${catdir}/index.html
				// ${topcatdir}/list/${uuid}.html
				urStr=urStr.split("\\|")[0];
				urStr=GroovyKit.j2gStr(urStr);
				Map<String,Object> urlparams=Maps.newHashMap();
				urlparams.put("categorydir",Category.dao.getCategorydir(self));
				urlparams.put("catdir",self.getStr("catdir"));
				urlparams.put("topcatdir",Category.dao.getTopcatdir(self));
				urlparams.put("uuid",BgzKit.md5strWithidRet24(catid));
				String url=GroovyKit.runScriptFromStr(urStr,urlparams).toString();
				if(url.endsWith("index.html")){
					url=StringUtils.substring(url,0,-10);
				}
				self.set("url","/"+url);
			}else{
				self.set("url",this.getPara("info[url]"));
			}
		}else{//不生成静态
			Urlrule ur=Urlrule.dao.findById(catrid);
			String urStr=ur.getStr("urlrule");
			Object url="";
			if(urStr.indexOf("|")>=0){//动态
				urStr=urStr.split("\\|")[0];// /content/index/lists?&catid=${catid}
				urStr=GroovyKit.j2gStr(urStr);
				url=GroovyKit.runScriptFromStr(urStr,ImmutableMap.of("catid",(Object)catid));
				url=domain+url.toString();
			}else{//伪静态
				urStr=GroovyKit.j2gStr(urStr);
				url=GroovyKit.runScriptFromStr(urStr,ImmutableMap.of("catid",(Object)catid,"page",(Object)1));
				url=domain+url.toString();
			}
			self.set("url",url);
		}
	}
	
	private void addPage(){
		Category cat=new Category();
		int type=this.getParaToInt("info[type]");
		cat.set("type",type);
		int parentid=this.getParaToInt("info[parentid]");
		cat.set("parentid",parentid);
		cat.set("catname",this.getPara("info[catname]"));
		String catdir=this.getPara("info[catdir]");
		cat.set("catdir",catdir);
		cat.set("image",this.getPara("info[image]"));
		cat.set("description",this.getPara("info[description]"));
		cat.set("ismenu",this.getParaToInt("info[ismenu]"));
		
		if(parentid==0){
			cat.set("arrparentid",parentid);//0
			cat.set("parentdir","");//""
		}else{
			Category pcat=Category.dao.findById(parentid);
			cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
			String parentdir=pcat.getStr("parentdir")+pcat.getStr("catdir")+"/";
			cat.set("parentdir",parentdir);
		}
		
		Map<String,Object> settings=Maps.newLinkedHashMap();
		int ishtml=this.getParaToInt("setting[ishtml]");
		settings.put("ishtml",ishtml);
		int catrid=this.getParaToInt("setting[category_ruleid]");
		settings.put("category_ruleid",catrid);

		settings.put("template_list",this.getPara("setting[template_list]"));
		settings.put("page_template",this.getPara("setting[page_template]"));

		settings.put("meta_title",this.getPara("setting[meta_title]"));
		settings.put("meta_keywords",this.getPara("setting[meta_keywords]"));
		settings.put("meta_description",this.getPara("setting[meta_description]"));
		
		cat.set("setting",BgzKit.obj2json(settings));
		if(!cat.save()){
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.fail);
			this.renderJson(ret);
			return;
		}
		
		int catid=cat.getInt("catid");
		cat.set("arrchildid",catid);
		cat.set("listorder",catid);
		
		this.buildUrl(cat);
		
		cat.update();
		
		updateParent(Category.dao.findById(parentid));
		
		Page page=new Page();
		page.set("catid",catid);
		page.set("title","");
		page.set("keywords","");
		page.set("content","");
		page.save();
		
		/*
		 * 添加栏目权限
		 */
		addCatPriv(catid);
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.scs_dataadd);
		this.renderJson(ret);
	}
	
	/**
	 * @param catid
	 */
	private void addCatPriv(int catid) {
		Integer[] roleids=this.getParaValuesToInt("roleids");
		if(roleids==null){
			roleids=new Integer[0];
		}
		List<String> sqls=new ArrayList<>();
		Stream.of(roleids).forEach(
			roleid->sqls.add("insert "+CategoryPriv.dao.tn()+" values("+catid+","+roleid+")")
		);
		if(sqls.size()>0){
			Db.batch(sqls,10);
		}
	}

	private void addLink(){
		Category cat=new Category();
		int type=this.getParaToInt("info[type]");
		cat.set("type",type);
		int parentid=this.getParaToInt("info[parentid]");
		cat.set("parentid",parentid);
		cat.set("catname",this.getPara("info[catname]"));
		cat.set("image",this.getPara("info[image]"));
		
		cat.set("ismenu",Category.ISMENU_S);
		
		if(parentid==0){
			cat.set("arrparentid",parentid);//0
		}else{
			Category pcat=Category.dao.findById(parentid);
			cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
		}
		
		if(!cat.save()){
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.fail);
			this.renderJson(ret);
			return;
		}
		
		int catid=cat.getInt("catid");
		cat.set("arrchildid",catid);
		cat.set("listorder",catid);
		
		cat.set("url",this.getPara("info[url]"));
		
		cat.update();
		
		updateParent(Category.dao.findById(parentid));
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.scs_dataadd);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("category:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")) {
			int type=this.getParaToInt("info[type]");
			if(type==Category.TYPE_CAT){
				editCat();
			}else if(type==Category.TYPE_PAGE){
				editPage();
			}else if(type==Category.TYPE_LINK){
				editLink();
			}
		}else{
			int parentid=0;
			
			String arrchildid=Category.dao.getArrchildid(parentid);
			if(!Strings.isNullOrEmpty(arrchildid)){
				String wherein=parentid+","+arrchildid;
				List<Record> catList=Category.dao.selectR("catid,catname,parentid,listorder,ismenu",
						"type=0 and catid in("+wherein+")","parentid,listorder","","");
				if(catList==null || catList.size()==0){
					this.setAttr("catlist",ImmutableList.of());
				}else{
					this.setAttr("catlist",CatTreeNode.buildTree(catList,parentid).toCatList());
				}
			}else{//空数据
				this.setAttr("catlist",ImmutableList.of());
			}
			
			Category cat=Category.dao.findById(this.getParaToInt("catid"));
			this.setAttr("cat",cat);
			
			Map<String,Object> settings=BgzKit.json2obj(cat.getStr("setting"));
			
			int type=this.getParaToInt("type");
			
			if(type==Category.TYPE_CAT || type==Category.TYPE_PAGE){
				this.setAttr("catSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_CAT,(int)settings.get("ishtml")));
				this.setAttr("templatelist",GlobalFunc.getSiteTemplateStyles());
				
				this.setAttr("roles",Role.dao.selectM("*", "id !=1", "", "", ""));
				List<String> catpriv=CategoryPriv.dao.select("roleid", "catid=?", "", "", "",cat.getInt("catid"));
				this.setAttr("catpriv",catpriv);
			}
			
			if(type==Category.TYPE_CAT){
				List<Record> modellist=Model.dao.selectR("id,name","id=1","id","","");
				this.setAttr("modellist",modellist);
				
				this.setAttr("showSelUrList",Urlrule.dao.getSelUrs(Urlrule.FILE_SHOW,(int)settings.get("content_ishtml")));
				
				String style=(String)settings.get("template_list");
				this.setAttr("cattpllist",GlobalFunc.cattplList(style));
				this.setAttr("listtpllist",GlobalFunc.listtplList(style));
				this.setAttr("showtpllist",GlobalFunc.showtplList(style));
				
				this.render("category_edit.html");
			}else if(type==Category.TYPE_PAGE){
				String style=(String)settings.get("template_list");
				this.setAttr("pagetpllist",GlobalFunc.pagetplList(style));
				
				this.render("category_page_edit.html");
			}else if(type==Category.TYPE_LINK){
				this.render("category_link.html");
			}
		}
	}
	
	private void editCat(){
		int catid=this.getParaToInt("info[catid]");
		Category cat=Category.dao.findById(catid);
		
		cat.set("modelid",this.getParaToInt("info[modelid]"));
		
		int parentid=this.getParaToInt("info[parentid]");
		int oparentid=cat.getInt("parentid");
		
		if(parentid==0){
			cat.set("arrparentid",parentid);//0
			cat.set("parentdir","");//""
		}else{
			Category pcat=Category.dao.findById(parentid);
			cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
			String parentdir=pcat.getStr("parentdir")+pcat.getStr("catdir")+"/";
			cat.set("parentdir",parentdir);
		}
		cat.set("parentid",parentid);

		cat.set("catname",this.getPara("info[catname]"));
		String catdir=this.getPara("info[catdir]");
		cat.set("catdir",catdir);
		cat.set("image",this.getPara("info[image]"));
		cat.set("description",this.getPara("info[description]"));
		cat.set("ismenu",this.getParaToInt("info[ismenu]"));
		
		Map<String,Object> settings=Maps.newLinkedHashMap();
		settings.put("workflowid",this.getParaToInt("setting[workflowid]"));
		int ishtml=this.getParaToInt("setting[ishtml]");
		settings.put("ishtml",ishtml);
		settings.put("content_ishtml",this.getParaToInt("setting[content_ishtml]"));
		int catrid=this.getParaToInt("setting[category_ruleid]");
		settings.put("category_ruleid",catrid);
		settings.put("show_ruleid",this.getParaToInt("setting[show_ruleid]"));
		int create_to_html_root=this.getParaToInt("setting[create_to_html_root]");
		settings.put("create_to_html_root",create_to_html_root);

		settings.put("template_list",this.getPara("setting[template_list]"));
		settings.put("category_template",this.getPara("setting[category_template]"));
		settings.put("list_template",this.getPara("setting[list_template]"));
		settings.put("show_template",this.getPara("setting[show_template]"));
		settings.put("meta_title",this.getPara("setting[meta_title]"));
		settings.put("meta_keywords",this.getPara("setting[meta_keywords]"));
		settings.put("meta_description",this.getPara("setting[meta_description]"));
		
		cat.set("setting",BgzKit.obj2json(settings));
		
		this.buildUrl(cat);
		
		cat.update();
		//更新新的父栏目
		updateParent(Category.dao.findById(parentid));
		//更新子栏目
		updateChild(cat);
		//更新老的父栏目
		updateParent(Category.dao.findById(oparentid));
		
		
		/*
		 * 更新栏目权限
		 */
		editCatPriv(catid);
		
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	/*
	 * 递归更新父栏目的child和arrchildid
	 */
	private void updateParent(Category parent){
		if(parent==null){
			return;
		}
		List<String> list=Category.dao.select("arrchildid","parentid=?","","","",parent.getInt("catid"));
		String arrchildid=""+parent.getInt("catid");
		for(Iterator<String> it=list.iterator();it.hasNext();){
			arrchildid+=(","+it.next());
		}
		parent.set("arrchildid",arrchildid);
		if(arrchildid.indexOf(",")>0){
			parent.set("child",1);
		}else{
			parent.set("child",0);
		}
		parent.update();
		updateParent(Category.dao.findById(parent.getInt("parentid")));
	}
	
	/*
	 * 循环更新子栏目的arrparentid、parentdir、url
	 */
	private void updateChild(Category self){
		String acidStr=self.getStr("arrchildid");
		if(acidStr.indexOf(",")<0){
			return;
		}
		String[] acids=acidStr.split(",");
		int template_child=this.getParaToInt("template_child");
		Map<String, Object> settings=BgzKit.json2obj(self.getStr("setting"));
		for(int i=1;i<acids.length;i++){
			Category cat=Category.dao.findById(acids[i]);
			int parentid=cat.getInt("parentid");
			String parentdir="";
			if(parentid==0){
				cat.set("arrparentid",parentid);//0
				cat.set("parentdir",parentdir);//""
			}else{
				Category pcat=Category.dao.findById(parentid);
				/*
				 * 更新arrparentid
				 */
				cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
				
				if(cat.isTypeLink()){
					continue;
				}
				
				/*
				 * 更新parentdir
				 */
				parentdir=pcat.getStr("parentdir")+pcat.getStr("catdir")+"/";
				cat.set("parentdir",parentdir);
				/*
				 * 更新url
				 */
				Map<String, Object> _settings=BgzKit.json2obj(cat.getStr("setting"));
				String url=cat.getStr("url");
				int ishtml=(int)_settings.get("ishtml");
				if(ishtml==1 && !url.startsWith("http")){
					int catid=cat.getInt("catid");
					int catrid=(int)_settings.get("category_ruleid");
					Urlrule ur=Urlrule.dao.findById(catrid);
					String urStr=ur.getStr("urlrule");
					// ${categorydir}${catdir}/index.html
					// ${topcatdir}/list/${uuid}.html
					urStr=urStr.split("\\|")[0];
					urStr=GroovyKit.j2gStr(urStr);
					Map<String,Object> urlparams=Maps.newHashMap();
					urlparams.put("categorydir",Category.dao.getCategorydir(cat));
					urlparams.put("catdir",cat.getStr("catdir"));
					urlparams.put("topcatdir",Category.dao.getTopcatdir(cat));
					urlparams.put("uuid",BgzKit.md5strWithidRet24(catid));
					url=GroovyKit.runScriptFromStr(urStr,urlparams).toString();
					if(url.endsWith("index.html")){
						url=StringUtils.substring(url,0,-10);
					}
					cat.set("url","/"+url);
				}
				/*
				 * 应用模板设置到子栏目
				 */
				if(template_child==1 && cat.isTypeCat()){
					_settings.put("template_list",settings.get("template_list"));
					_settings.put("category_template",settings.get("category_template"));
					_settings.put("list_template",settings.get("list_template"));
					_settings.put("show_template",settings.get("show_template"));
					cat.set("setting",BgzKit.obj2json(_settings));
				}
			}
			cat.update();
		}
	}
	private void editPage(){
		int catid=this.getParaToInt("info[catid]");
		Category cat=Category.dao.findById(catid);
		
		int parentid=this.getParaToInt("info[parentid]");
		int oparentid=cat.getInt("parentid");
		
		if(parentid==0){
			cat.set("arrparentid",parentid);//0
			cat.set("parentdir","");//""
		}else{
			Category pcat=Category.dao.findById(parentid);
			cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
			String parentdir=pcat.getStr("parentdir")+pcat.getStr("catdir")+"/";
			cat.set("parentdir",parentdir);
		}
		cat.set("parentid",parentid);

		cat.set("catname",this.getPara("info[catname]"));
		String catdir=this.getPara("info[catdir]");
		cat.set("catdir",catdir);
		cat.set("image",this.getPara("info[image]"));
		cat.set("description",this.getPara("info[description]"));
		cat.set("ismenu",this.getParaToInt("info[ismenu]"));
		
		Map<String,Object> settings=Maps.newLinkedHashMap();
		int ishtml=this.getParaToInt("setting[ishtml]");
		settings.put("ishtml",ishtml);
		int catrid=this.getParaToInt("setting[category_ruleid]");
		settings.put("category_ruleid",catrid);

		settings.put("template_list",this.getPara("setting[template_list]"));
		settings.put("page_template",this.getPara("setting[page_template]"));
		settings.put("meta_title",this.getPara("setting[meta_title]"));
		settings.put("meta_keywords",this.getPara("setting[meta_keywords]"));
		settings.put("meta_description",this.getPara("setting[meta_description]"));
		
		cat.set("setting",BgzKit.obj2json(settings));
		
		this.buildUrl(cat);
		
		cat.update();
		//更新新的父栏目
		updateParent(Category.dao.findById(parentid));
		//更新老的父栏目
		updateParent(Category.dao.findById(oparentid));
		
		editCatPriv(catid);
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	/**
	 * 
	 */
	private void editCatPriv(int catid) {
		Integer[] roleids=this.getParaValuesToInt("roleids");
		if(roleids==null){
			roleids=new Integer[0];
		}
		roleids=Stream.of(roleids).sorted().toArray(Integer[]::new);
		List<Integer> _roleids=CategoryPriv.dao.select("roleid", "catid=?", "roleid asc", "", "", catid);
		boolean isEqual=Arrays.equals(roleids, _roleids.stream().toArray(Integer[]::new));
		if(!isEqual){
			Db.update("delete from "+CategoryPriv.dao.tn()+" where catid=?",catid);
			List<String> sqls=new ArrayList<>();
			Stream.of(roleids).forEach(
				roleid->sqls.add("insert "+CategoryPriv.dao.tn()+" values("+catid+","+roleid+")")
			);
			if(sqls.size()>0){
				Db.batch(sqls,10);
			}
		}
	}

	private void editLink(){
		Category cat=Category.dao.findById(this.getParaToInt("info[catid]"));
		
		int parentid=this.getParaToInt("info[parentid]");
		int oparentid=cat.getInt("parentid");
		
		if(parentid==0){
			cat.set("arrparentid",parentid);//0
		}else{
			Category pcat=Category.dao.findById(parentid);
			cat.set("arrparentid",pcat.getStr("arrparentid")+","+parentid);
		}
		cat.set("parentid",parentid);

		cat.set("catname",this.getPara("info[catname]"));
		cat.set("image",this.getPara("info[image]"));
		
		cat.set("url",this.getPara("info[url]"));
		
		cat.update();
		//更新新的父栏目
		updateParent(Category.dao.findById(parentid));
		//更新老的父栏目
		updateParent(Category.dao.findById(oparentid));
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("category:del")
	@Before(Tx.class)
	public void del(){
		Category cat=Category.dao.findById(this.getParaToInt("catid"));
		
		/*
		 * 处理子栏目
		 */
		String acidStr=cat.getStr("arrchildid");
		if(cat.isTypeCat()){
			if(acidStr.indexOf(",")>=0){
				String[] acids=acidStr.split(",");
				for(int i=1;i<acids.length;i++){
					Category _cat=Category.dao.findById(acids[i]);
					
					if(_cat.isTypeCat()){
						List<News> newsList=News.dao.selectM("id,posids","catid=?","","","",_cat.getInt("catid"));
						for(News news:newsList){
							int id=news.getInt("id");
							if(news.getInt("posids")==News.POSIDS_Y){
								Db.update("delete from "+PositionData.dao.tn()+" where id=?",id);
							}
							Db.update("delete from "+NewsData.dao.tn()+" where id=?",id);
							news.delete();
						}
					}else if(_cat.isTypePage()){
						Page.dao.deleteById(_cat.getInt("catid"));
					}
					
					_cat.delete();
				}
			}else{
				Category _cat=Category.dao.findById(acidStr);
				
				List<News> newsList=News.dao.selectM("id,posids","catid=?","","","",_cat.getInt("catid"));
				for(News news:newsList){
					int id=news.getInt("id");
					if(news.getInt("posids")==News.POSIDS_Y){
						Db.update("delete from "+PositionData.dao.tn()+" where id=?",id);
					}
					Db.update("delete from "+NewsData.dao.tn()+" where id=?",id);
					news.delete();
				}
			}
		}else if(cat.isTypePage()){
			Page.dao.deleteById(this.getParaToInt("catid"));
		}
		
		/*
		 * 删除栏目权限
		 */
		Db.update("delete from "+CategoryPriv.dao.tn()+" where catid=?",cat.getInt("catid"));
		
		cat.delete();
		
		updateParent(Category.dao.findById(cat.getInt("parentid")));
		
		Ret ret=Ret.create().setCodeAndMsg(C.success).setRdurl("/admin/category/init");
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
	
	public void tplFileList(){
		String style=this.getPara("style");
		
		Ret ret=Ret.create();
		
		ret.put("cattpllist",GlobalFunc.cattplList(style));
		ret.put("listtpllist",GlobalFunc.listtplList(style));
		ret.put("showtpllist",GlobalFunc.showtplList(style));
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	public void pagetplFileList(){
		String style=this.getPara("style");
		
		Ret ret=Ret.create();
		
		ret.put("pagetpllist",GlobalFunc.pagetplList(style));
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	@RequiresPermissions("category:listorder")
	@Before(Tx.class)
	public void listorder(){
		if(this.isParaExists("dosubmit")) {
			Enumeration<String> paras=this.getParaNames();
			ArrayList<String> sqls=Lists.newArrayList();
			while(paras.hasMoreElements()){
				String name=paras.nextElement();
				if(name.startsWith("listorders[")){
					Integer listorder=Ints.tryParse(this.getPara(name));
					int intListorder=(listorder==null?0:listorder);
					int catid=Integer.parseInt(name.substring(11,name.length()-1));
					sqls.add("update "+Category.dao.tn()+" set listorder="+intListorder+" where catid="+catid);
				}
			}
			log.debug("listorder="+sqls);
			if(sqls.size()>=1){
				Db.batch(sqls,5);
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success).setRdurl("/admin/category/init");
			this.setAttr("ret", ret).render("/admin/retmsg.html");
		}
	}
	@RequiresPermissions("menu:category")
	public void checkcatdir(){
		String catdir=this.getPara("info[catdir]");
		int parentid=this.getParaToInt("parentid");
		int catid=this.getParaToInt("catid",0);
		
		Ret ret=Ret.create();
		if(parentid==0 && 
				Stream.of(AppConfig.AK).anyMatch(ak->ak.equalsIgnoreCase(catdir))){
			ret.put("error",String.format("一级目录名称不可为<span style=\"font-size:1.5rem\">%s</span>",
							Stream.of(AppConfig.AK).collect(Collectors.joining(",","[","]"))));
			this.renderJson(ret.getMap());
			return;
		}
		
		List<Integer> catids=Category.dao.select("catid","parentid=? and catdir=? and catid not in(?)","","","",parentid,catdir,catid);
		
		if(catids.size()>=1){
			ret.put("error","目录名称已存在");
		}else{
			ret.put("ok","");
		}
		this.renderJson(ret.getMap());
	}
	@RequiresPermissions("category:updateCache")
	public void updateCache(){
		CacheFunc.removeCommonsCategorys();
		CacheFunc.getIdCategorys();
		Ret ret=Ret.create().setCodeAndMsg(C.scs_updateCache).setRdurl("/admin/category/init");
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
}