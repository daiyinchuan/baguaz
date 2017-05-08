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

package com.baguaz.module.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.interceptor.CatPrivInterceptor;
import com.baguaz.module.category.Category;
import com.baguaz.module.category.Category.CatTreeNode;
import com.baguaz.module.copyfrom.Copyfrom;
import com.baguaz.module.hits.Hits;
import com.baguaz.module.position.Position;
import com.baguaz.module.position.PositionData;
import com.baguaz.module.site.Site;
import com.baguaz.module.urlrule.Urlrule;
import com.baguaz.module.user.UserPrincipal;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/content", viewPath = "admin/content")
@Before(CatPrivInterceptor.class)
public class ContentAdminController extends BaseController{
	private static final Logger log=LoggerFactory
			.getLogger(ContentAdminController.class);
	
	@RequiresPermissions("menu:content")
	@Clear({CatPrivInterceptor.class})
	public void init(){
		CacheFunc.getIdCategorys();
		this.setAttr("catmenuReInit", CacheFunc.catmenuReInit.get());
		this.render("content_manage.html");
	}
	
	@RequiresPermissions("menu:content")
	@Clear({CatPrivInterceptor.class})
	public void list(){
		this.setAttr("catid",this.getParaToInt("catid"));
		this.render("content_manage.html");
	}
	
	@RequiresPermissions("menu:content")
	public void jsonlist(){
		com.jfinal.plugin.activerecord.Page<News> page=News.dao.paginate(1,100000,
				"select *","from "+News.dao.tn()+" where catid=? order by listorder desc,updatetime desc",this.getParaToInt("catid"));
		Ret ret=Ret.create();
		ret.put("page",page);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	public void gotoRelation(){
		this.setAttr("e",this.getPara("id",""));
		this.render("content_relation.html");
	}
	
	/**
	 * 添加相关文章的json列表
	 */
	public void jsonrelatlist(){
		String selectStr="select id,title,catid,(select catname from "+Category.dao.tn()+" c where c.catid=n.catid) catname,username,inputtime,updatetime";
		String fromStr=" from "+News.dao.tn()+" n ";
		String whereStr="";
		if(this.isParaExists("e") && !this.isParaBlank("e")){
			whereStr=" where id <>"+this.getParaToInt("e");
		}
		String orderStr=" order by updatetime desc,catid,username";
		com.jfinal.plugin.activerecord.Page<News> page=News.dao.paginate(1,100000,selectStr,fromStr+whereStr+orderStr);
		Ret ret=Ret.create();
		ret.put("page",page);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	/**
	 * 获取已有的相关文章json列表
	 */
	public void jsonrelatedlist(){
		String relation=this.getPara("r");
		String rnsp=Joiner.on(',').join(Splitter.on('|').split(relation));
		List<Record> list=News.dao.selectR("id,title","id in("+rnsp+")","","","");
		Ret ret=Ret.create();
		ret.put("relatedList",list);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("content:add")
	@Before(Tx.class)
	public void add(){
		if(this.isParaExists("dosubmit")){
			int type=this.getParaToInt("type");
			if(type==Category.TYPE_CAT){
				addCatContent();
			}else if(type==Category.TYPE_PAGE){
				Page page=new Page();
				page.set("catid",this.getParaToInt("catid"));
				page.set("title",this.getPara("title"));
				page.set("keywords",this.getPara("keywords"));
				page.set("content",this.getPara("content"));
				page.set("updatetime",BgzKit.genUnixTstamp());
				page.save();
				Ret ret=Ret.create();
				ret.setCodeAndMsg(C.scs_dataadd);
				this.renderJson(ret);
			}
		}else{
			int catid=this.getParaToInt("catid");
			Record r=Category.dao.getOneR("catname,type,url,setting","catid=?","","",catid);
			this.setAttr("catid",catid);
			if(r.getInt("type")==Category.TYPE_CAT){
				this.setAttr("catid",catid);
				this.setAttr("catname",r.getStr("catname"));
				
				List<Record> copyfroms=Copyfrom.dao.selectR("id,name","","listorder,id","","");
				this.setAttr("copyfromList",copyfroms);
				
				List<Record> positions=Position.dao.selectR("id,name","","listorder,id","","");
				this.setAttr("posList",positions);
				
				Map<String,Object> settings=BgzKit.json2obj(r.getStr("setting"));
				String style=(String)settings.get("template_list");
				this.setAttr("showtpllist",GlobalFunc.showtplList(style));
				
				this.setAttr("inputtime",DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
				
				this.render("content_add.html");
			}else if(r.getInt("type")==Category.TYPE_PAGE){
				this.setAttr("url",r.getStr("url"));
				Page page=Page.dao.getOneM("*","catid=?","","",catid);
				this.setAttr("page",page);
				this.render("content_page.html");
			}
		}
	}
	private void addCatContent(){
		/*
		 * news
		 */
		News news=new News();
		String title=this.getPara("title");
		news.set("title",title);
		int catid=this.getParaToInt("catid");
		news.set("catid",catid);
		news.set("thumb",this.getPara("thumb"));
		news.set("keywords",this.getPara("keywords"));
		String description=this.getPara("description");
		news.set("description",description);
		String[] posids=this.getParaValues("posids");
		if(posids!=null && posids.length>=1){
			news.set("posids",News.POSIDS_Y);
		}else{
			news.set("posids",News.POSIDS_N);
		}
		long inputtime=BgzKit.conv2UnixTstamp(this.getPara("inputtime"));
		news.set("inputtime",inputtime);
		news.set("updatetime",inputtime);
		news.set("username",BgzKit.getUsername());
		news.set("sysadd",News.SYSADD_Y);
		news.set("listorder",0);
		
		news.save();
		int newsid=news.getInt("id");
		
		/*
		 * 处理url
		 */
		Category cat=Category.dao.findById(catid);
		Map<String,Object> settings=BgzKit.json2obj(cat.getStr("setting"));
		int showRuleid=(int)settings.get("show_ruleid");
		Urlrule ur=Urlrule.dao.findById(showRuleid);
		int ishtml=ur.getInt("ishtml");
		String urStr=ur.getStr("urlrule");
		String catdir=cat.getStr("catdir");
		String[] arrStrInputDate=BgzKit.getYMDArr(inputtime);
		String year=arrStrInputDate[0];
		String month=arrStrInputDate[1];
		String day=arrStrInputDate[2];
		if(ishtml==Urlrule.ISHTML_Y){//生成静态
			Object url="";
			urStr=urStr.split("\\|")[0];
			urStr=GroovyKit.j2gStr(urStr);//
			Map<String,Object> map=Maps.newHashMap();
			map.put("categorydir", Category.dao.getCategorydir(cat));
			map.put("catdir",(Object)catdir);
			map.put("id",(Object)newsid);
			map.put("year",(Object)year);
			map.put("month",(Object)month);
			map.put("day",(Object)day);
			map.put("topcatdir",Category.dao.getTopcatdir(cat));
			map.put("uuid", BgzKit.md5strWithidRet24(newsid));
			url=GroovyKit.runScriptFromStr(urStr,map);
			news.set("url","/"+url.toString());
		}else{//不生成静态
			Object url="";
			String domain=Site.dao.getDomain();
			if(urStr.indexOf("|")>=0){//动态
				urStr=urStr.split("\\|")[0];// /content/index/lists?&catid=${catid}
				urStr=GroovyKit.j2gStr(urStr);
				url=GroovyKit.runScriptFromStr(urStr,ImmutableMap.of("catid",(Object)catid,
				                                                     "id",(Object)newsid));
				url=domain+url.toString();
			}else{//伪静态
				urStr=GroovyKit.j2gStr(urStr);
				url=GroovyKit.runScriptFromStr(urStr,ImmutableMap.of("catid",(Object)catid,
				                                                     "id",(Object)newsid,
				                                                     "page",(Object)1));
				url=domain+url.toString();
			}
			news.set("url",url);
		}
		news.update();
		
		/*
		 * news_data
		 */
		NewsData newsData=new NewsData();
		newsData.set("id",newsid);
		newsData.set("content",this.getPara("content"));
		newsData.set("template",this.getPara("template"));
		newsData.set("relation",this.getPara("relation"));
		newsData.set("copyfrom",this.getPara("copyfrom"));
		
		//处理组图片
		String[] gimageUrl=this.getParaValues("gimage_url");
		String[] gimageDesc=this.getParaValues("gimage_desc");
		newsData.set("gimage", gxxJson(gimageUrl,gimageDesc));
		//处理组文件
		String[] gfileUrl=this.getParaValues("gfile_url");
		String[] gfileDesc=this.getParaValues("gfile_desc");
		newsData.set("gfile", gxxJson(gfileUrl,gfileDesc));
		//处理组音频
		String[] gaudioUrl=this.getParaValues("gaudio_url");
		String[] gaudioDesc=this.getParaValues("gfile_desc");
		newsData.set("gaudio", gxxJson(gaudioUrl,gaudioDesc));
		//处理组视频
		String[] gvideoUrl=this.getParaValues("gvideo_url");
		String[] gvideoDesc=this.getParaValues("gfile_desc");
		newsData.set("gvideo", gxxJson(gvideoUrl,gvideoDesc));
		
		newsData.save();
		
		/*
		 * position_data
		 * String[] posids=this.getParaValues("posids");
		 */
		if(news.getInt("posids")==News.POSIDS_Y){
			for(String posid:posids){
				PositionData posData=new PositionData();
				posData.set("id",newsid);
				posData.set("catid",catid);
				posData.set("posid",posid);
				posData.set("modelid",cat.getInt("modelid"));
				posData.set("thumb",StringUtils.isNotEmpty(this.getPara("thumb"))?1:0);
				
				Map<String,Object> datas=Maps.newLinkedHashMap();
				datas.put("title",title);
				datas.put("description",description);
				datas.put("inputtime",inputtime);
				posData.set("data",BgzKit.obj2json(datas));
				
				posData.set("listorder",newsid);
				posData.set("synedit",0);// TODO 0还是1有待决定
				
				posData.save();
			}
		}
		
		/*
		 * 添加点击量统计记录
		 */
		new Hits()
			.set("hitsid", "c-"+cat.getInt("modelid")+"-"+newsid)
			.set("catid",catid)
			.set("updatetime",inputtime)
			.save();
		
		/*
		 * 更新栏目数据量统计
		 */
		Db.update("update "+Category.dao.tn()+" set items=items+1 where catid=?",catid);
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.scs_dataadd);
		this.renderJson(ret);
	}
	
	private String gxxJson(String[] url,String[] desc){
		if(url==null){
			return BgzKit.obj2json(new LinkedHashMap<>());
		}
		desc=Optional.ofNullable(desc).orElseGet(()->new String[url.length]);
		Map<Integer,Map<String,String>> gMap=new LinkedHashMap<>();
		for(int i=0;i<url.length;i++){
			Map<String,String> map=new LinkedHashMap<>();
			map.put("url",url[i]);
			map.put("desc",Optional.ofNullable(desc[i]).orElseGet(()->""));
			gMap.put(i, map);
		}
		return BgzKit.obj2json(gMap);
	}
	
	@RequiresPermissions("content:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")){
			int type=this.getParaToInt("type");
			if(type==Category.TYPE_CAT){
				editCatContent();
			}else if(type==Category.TYPE_PAGE){
				Page page=Page.dao.findById(this.getParaToInt("catid"));
				page.set("title",this.getPara("title"));
				page.set("keywords",this.getPara("keywords"));
				page.set("content",this.getPara("content"));
				page.set("updatetime",BgzKit.genUnixTstamp());
				page.update();
				Ret ret=Ret.create();
				ret.setCodeAndMsg(C.success);
				this.renderJson(ret);
			}
		}else{
			int catid=this.getParaToInt("catid");
			Record r=Category.dao.getOneR("catname,type,url,setting","catid=?","","",catid);
			if(r.getInt("type")==Category.TYPE_CAT){
				this.setAttr("catid",catid);
				this.setAttr("catname",r.getStr("catname"));
				
				List<Record> copyfromList=Copyfrom.dao.selectR("id,name","","listorder,id","","");
				this.setAttr("copyfromList",copyfromList);
				
				List<Record> posList=Position.dao.selectR("id,name","","listorder,id","","");
				this.setAttr("posList",posList);
				
				Map<String,Object> setting=BgzKit.json2obj(r.getStr("setting"));
				String style=(String)setting.get("template_list");
				this.setAttr("showtpllist",GlobalFunc.showtplList(style));
				
				int id=this.getParaToInt("id");
				News news=News.dao.findById(id);
				this.setAttr("news",news);
				NewsData newsData=NewsData.dao.findById(id);
				this.setAttr("newsData",newsData);
				if(news.getInt("posids")==News.POSIDS_Y){
					List<Integer> posids=PositionData.dao.select("posid","id=?","","","",id);
					this.setAttr("posids",posids);
				}
				this.render("content_edit.html");
			}
		}
	}
	
	private void editCatContent(){
		int newsid=this.getParaToInt("id");
		/*
		 * news
		 */
		News news=News.dao.findById(newsid);
		String title=this.getPara("title");
		news.set("title",title);
		int catid=this.getParaToInt("catid");
		news.set("thumb",this.getPara("thumb"));
		news.set("keywords",this.getPara("keywords"));
		String description=this.getPara("description");
		news.set("description",description);
		String[] posids=this.getParaValues("posids");
		if(posids!=null && posids.length>=1){
			news.set("posids",News.POSIDS_Y);
		}else{
			news.set("posids",News.POSIDS_N);
		}
		long inputtime=BgzKit.conv2UnixTstamp(this.getPara("inputtime"));
		news.set("inputtime",inputtime);
		news.set("updatetime",BgzKit.genUnixTstamp());
		news.set("username",BgzKit.getUsername());
		news.set("sysadd",News.SYSADD_Y);
		news.update();
		
		/*
		 * news_data
		 */
		NewsData newsData=NewsData.dao.findById(newsid);
		newsData.set("content",this.getPara("content"));
		newsData.set("template",this.getPara("template"));
		newsData.set("relation",this.getPara("relation"));
		newsData.set("copyfrom",this.getPara("copyfrom"));
		
		//处理组图片
		String[] gimageUrl=this.getParaValues("gimage_url");
		String[] gimageDesc=this.getParaValues("gimage_desc");
		newsData.set("gimage", gxxJson(gimageUrl,gimageDesc));
		//处理组文件
		String[] gfileUrl=this.getParaValues("gfile_url");
		String[] gfileDesc=this.getParaValues("gfile_desc");
		newsData.set("gfile", gxxJson(gfileUrl,gfileDesc));
		//处理组音频
		String[] gaudioUrl=this.getParaValues("gaudio_url");
		String[] gaudioDesc=this.getParaValues("gaudio_desc");
		newsData.set("gaudio", gxxJson(gaudioUrl,gaudioDesc));
		//处理组视频
		String[] gvideoUrl=this.getParaValues("gvideo_url");
		String[] gvideoDesc=this.getParaValues("gvideo_desc");
		newsData.set("gvideo", gxxJson(gvideoUrl,gvideoDesc));
		
		newsData.update();
		
		/*
		 * position_data
		 * String[] posids=this.getParaValues("posids");
		 */
		List<Integer> _posids=PositionData.dao.select("posid", "id=?", "", "", "", newsid);
		boolean isEqual=Arrays.equals(posids, _posids.stream().map(p->p.toString()).toArray(String[]::new));
		if(!isEqual){
			Db.update("delete from "+PositionData.dao.tn()+" where id=?",newsid);
			if(news.getInt("posids")==News.POSIDS_Y){
				int modelid=Category.dao.getOne("modelid","catid=?","","",catid);
				for(String posid:posids){
					PositionData posData=new PositionData();
					posData.set("id",newsid);
					posData.set("catid",catid);
					posData.set("posid",posid);
					posData.set("modelid",modelid);
					posData.set("thumb",StringUtils.isNotEmpty(this.getPara("thumb"))?1:0);
					
					Map<String,Object> datas=Maps.newLinkedHashMap();
					datas.put("title",title);
					datas.put("description",description);
					datas.put("inputtime",inputtime);
					posData.set("data",BgzKit.obj2json(datas));
					
					posData.set("listorder",newsid);
					posData.set("synedit",0);// TODO 0还是1有待决定
					
					posData.save();
				}
			}
		}
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("content:del")
	@Before(Tx.class)
	public void del(){
		String[] dcs=this.getParaValues("delcheck");
		for(String dc:dcs){
			int id=Integer.parseInt(dc);
			News news=News.dao.findById(id);
			int catid=news.getInt("catid");
			/*
			 * 删除推荐位数据
			 */
			if(news.getInt("posids")==News.POSIDS_Y){
				Db.update("delete from "+PositionData.dao.tn()+" where id=?",id);
			}
			int modelid=Category.dao.getOne("modelid", "catid=?", "", "",catid);
			/*
			 * 删除点击量统计数据记录
			 */
			//Db.update("delete "+Hits.dao.tn()+" where hitsid=?","c-"+modelid+"-"+id);
			Hits hits=Hits.dao.findById("c-"+modelid+"-"+id);
			if(hits!=null){
				hits.delete();
			}
			/*
			 * 更新栏目数据量统计
			 */
			Db.update("update "+Category.dao.tn()+" set items=items-1 where catid=? and items>0",catid);
			/*
			 * 删除内容模型扩展数据
			 */
			Db.update("delete from "+NewsData.dao.tn()+" where id=?",id);
			/*
			 * 删除内容模型数据
			 */
			news.delete();
		}
		
		Ret ret=Ret.create().setCodeAndMsg(C.success)
				   .setRdurl("/admin/content/list?catid="+this.getPara("catid"));
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
	
	@RequiresPermissions("content:listorder")
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
					int id=Integer.parseInt(name.substring(11,name.length()-1));
					sqls.add("update "+News.dao.tn()+" set listorder="+intListorder+" where id="+id);
				}
			}
			log.debug("listorder="+sqls);
			if(sqls.size()>=1){
				Db.batch(sqls,5);
			}
			
			Ret ret=Ret.create().setCodeAndMsg(C.success)
					   .setRdurl("/admin/content/list?catid="+this.getPara("catid"));
			this.setAttr("ret", ret).render("/admin/retmsg.html");
		}
	}
	@Clear({CatPrivInterceptor.class})
	public void catmenu(){
		int parentid=0;
		
		/*
		 * 栏目权限控制
		 */
		String arrchildid="";
		UserPrincipal principal=UserPrincipal.get();
		if(principal.isAdmin()){
			arrchildid=Category.dao.getArrchildid(parentid);
		}else{
			arrchildid=principal.getCatpriv().stream().map(c->c.toString()).collect(Collectors.joining(","));
		}
		
		if(!Strings.isNullOrEmpty(arrchildid)){
			String wherein=parentid+","+arrchildid;
			List<Record> cats=Category.dao.selectR("catid,catname,parentid,type,child,listorder",
					"type in(0,1) and catid in("+wherein+")","parentid,listorder","","");
			if(cats==null || cats.size()==0){
				this.setAttr("catmenu","[]");
			}else{
				Map<String, Object> mapCatTree=CatTreeNode.buildTree(cats,parentid).toCatTree();
				String catmenu=BgzKit.obj2json(mapCatTree.get("child"));
				if(log.isDebugEnabled()){
					log.debug("catmenu2="+catmenu);
				}
				this.setAttr("catmenu",catmenu);
			}
		}else{//空数据
			this.setAttr("catmenu","[]");
		}
		CacheFunc.catmenuReInit.set(false);
		this.render("content_catmenu.html");
	}
	
	/**
	 * 添加和编辑页面的预览功能，所有内容数据都是从页面获取
	 */
	public void preview(){
		int catid=this.getParaToInt("catid");
		
		int id=this.getParaToInt("id",0);
		
		Map<Integer, Category> categorys=CacheFunc.getIdCategorys();
		Category cat=categorys.get(catid);
		this.setAttr("CAT",cat);
		this.setAttr("CATEGORYS",categorys);
		
		Record news=new Record();
		news.set("id", id)
			.set("title",this.getPara("title"))
			.set("catid",catid)
			.set("thumb",this.getPara("thumb"))
			.set("keywords",this.getPara("keywords"))
			.set("description",this.getPara("description"));
		
		String[] posids=this.getParaValues("posids");
		if(posids!=null && posids.length>=1){
			news.set("posids",News.POSIDS_Y);
		}else{
			news.set("posids",News.POSIDS_N);
		}
		
		long inputtime=BgzKit.conv2UnixTstamp(this.getPara("inputtime"));
		news.set("inputtime",inputtime);
		news.set("updatetime",inputtime);
		
		NewsData newsData=new NewsData();
		newsData.set("content",this.getPara("content"));
		newsData.set("template",this.getPara("template"));
		newsData.set("relation",this.getPara("relation"));
		newsData.set("copyfrom",this.getPara("copyfrom"));
		
		//处理组图片
		String[] gimageUrl=this.getParaValues("gimage_url");
		String[] gimageDesc=this.getParaValues("gimage_desc");
		newsData.set("gimage", gxxJson(gimageUrl,gimageDesc));
		//处理组文件
		String[] gfileUrl=this.getParaValues("gfile_url");
		String[] gfileDesc=this.getParaValues("gfile_desc");
		newsData.set("gfile", gxxJson(gfileUrl,gfileDesc));
		//处理组音频
		String[] gaudioUrl=this.getParaValues("gaudio_url");
		String[] gaudioDesc=this.getParaValues("gfile_desc");
		newsData.set("gaudio", gxxJson(gaudioUrl,gaudioDesc));
		//处理组视频
		String[] gvideoUrl=this.getParaValues("gvideo_url");
		String[] gvideoDesc=this.getParaValues("gfile_desc");
		newsData.set("gvideo", gxxJson(gvideoUrl,gvideoDesc));
		
		
		String copyfrom=newsData.getStr("copyfrom");
		String[] copyfromArr=StringUtils.split(copyfrom,"|");
		if(copyfromArr.length==1){
			if(copyfromArr[0].equals("0")){
				copyfrom="本站";
			}else{
				copyfrom=CacheFunc.getIdCopyfroms().get(Integer.parseInt(copyfromArr[0])).getStr("name");
			}
		}else{
			copyfrom=copyfromArr[0];
		}
		newsData.set("copyfrom",copyfrom);
		
		news.setColumns(newsData);
		
		this.setAttrs(news.getColumns());
		
		String[] arrparentidArr=StringUtils.split(cat.getStr("arrparentid"),",");
		int top_parentid=arrparentidArr.length>=2?Integer.parseInt(arrparentidArr[1]):catid;
		
		this.setAttr("top_parentid",top_parentid);
		
		Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
		String style=(String)setting.get("template_list");
		String template=StringUtils.isNotEmpty(news.getStr("template"))?news.getStr("template"):(String)setting.get("show_template");
		template=StringUtils.isNotEmpty(template)?template:"show.html";

		/*
		 * SEO
		 */
		Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("catid",""+catid,
		                                          "title",news.getStr("title"),
		                                          "description",news.getStr("description"),
		                                          "keywords",news.getStr("keywords")));
		this.setAttr("SEO",seo);
		
		this.render(this.template(style, "content", template));
	}
}