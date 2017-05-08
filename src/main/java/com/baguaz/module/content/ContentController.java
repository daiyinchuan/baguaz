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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.baguaz.BgzKit;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.common.BaseController;
import com.baguaz.module.category.Category;
import com.baguaz.module.site.Site;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/content")
public class ContentController extends BaseController{
	/**
	 * 首页
	 */
	public void index(){
//		this.setAttr("siteid",siteid);
		Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("title",(String)CacheFunc.getSiteSettings().get("index_title")));
		this.setAttr("SEO",seo);
		Map<Integer, Category> idCategorys=CacheFunc.getIdCategorys();
		this.setAttr("CATEGORYS",idCategorys);
		String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
		
		this.render(template(style,"content","index.html"));
	}
	
	/**
	 * 内容页
	 */
	public void show(){
		int catid=this.getParaToInt("catid");
		int id=this.getParaToInt("id");
		
		Map<Integer, Category> categorys=CacheFunc.getIdCategorys();
		Category cat=categorys.get(catid);
		this.setAttr("CAT",cat);
		this.setAttr("CATEGORYS",categorys);
		
		Record content=News.dao.findById(id).toRecord();
		Record contentData=NewsData.dao.findById(id).toRecord();
		
		String copyfrom=contentData.getStr("copyfrom");
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
		contentData.set("copyfrom",copyfrom);
		
		content.setColumns(contentData);
		
		this.setAttrs(content.getColumns());
		
		String[] arrparentidArr=StringUtils.split(cat.getStr("arrparentid"),",");
		int top_parentid=arrparentidArr.length>=2?Integer.parseInt(arrparentidArr[1]):catid;
		
		this.setAttr("top_parentid",top_parentid);
		
		Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
		String style=(String)setting.get("template_list");
		String template=StringUtils.isNotEmpty(content.getStr("template"))?content.getStr("template"):(String)setting.get("show_template");
		template=StringUtils.isNotEmpty(template)?template:"show.html";

		/*
		 * SEO
		 */
		Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("catid",""+catid,
		                                          "title",content.getStr("title"),
		                                          "description",content.getStr("description"),
		                                          "keywords",content.getStr("keywords")));
		this.setAttr("SEO",seo);
		
		Record previousPage=News.dao.getOneR("*","catid=? and id<?","id desc","",catid,id);
		Record nextPage=News.dao.getOneR("*","catid=? and id>?","id","",catid,id);
		if(previousPage==null){
			previousPage=new Record();
			previousPage.set("title","第一篇")
						.set("thumb",BgzKit.getBgzProp("IMG_PATH")+"/nopic_small.gif")
						.set("url","javascript:void(0);");
		}
		if(nextPage==null){
			nextPage=new Record();
			nextPage.set("title","最后一篇")
					.set("thumb",BgzKit.getBgzProp("IMG_PATH")+"/nopic_small.gif")
					.set("url","javascript:void(0);");
		}
		this.setAttr("previous_page",previousPage);
		this.setAttr("next_page",nextPage);
		
		this.render(template(style,"content",template));
	}
	
	/**
	 * 列表页/单页
	 */
	public void lists(){
		int catid=this.getParaToInt("catid");
		String page=StringUtils.isNotEmpty(this.getPara("page"))?this.getPara("page"):"1";
		
		Map<Integer, Category> categorys=CacheFunc.getIdCategorys();
		Category cat=categorys.get(catid);
		this.setAttr("CAT",cat);
		this.setAttr("CATEGORYS",categorys);
		
		Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
		
		String style=(String)setting.get("template_list");
		
		String category_template=(String)setting.get("category_template");
		String template=StringUtils.isNotEmpty(category_template)?category_template:"category.html";
		
		String list_template=(String)setting.get("list_template");
		String template_list=StringUtils.isNotEmpty(list_template)?list_template:"list.html";
		
		this.setAttr("page",page);
		
		String[] arrparentidArr=StringUtils.split(cat.getStr("arrparentid"),",");
		int top_parentid=arrparentidArr.length>=2?Integer.parseInt(arrparentidArr[1]):catid;
		this.setAttr("top_parentid",top_parentid);
		
		if(cat.isTypeCat()){
			template = cat.getInt("child")==Category.CHILD_H ? template : template_list;
			
			/*
			 * URL规则
			 */
			String urlrules=CacheFunc.getIdUrlrules().get((int)setting.get("category_ruleid")).getStr("urlrule");
			urlrules=StringUtils.replace(urlrules,"|","~");
			this.setAttr("URLRULE",urlrules);
			List<String> urList=Splitter.on("~").trimResults().splitToList(urlrules);
			String tmpUrls=urList.size()==2?urList.get(1):urList.get(0);
			String[] _urls=BgzKit.regMatchAll("/\\${([a-z0-9_]+)}/i",tmpUrls);
			Map<String,Object> urlparas=Maps.newHashMap();
			for(String param:_urls){
				urlparas.put(param,this.getPara(param));
			}
//			String categorydir=(int)setting.get("create_to_html_root")==0?cat.getStr("parentdir"):"";
			urlparas.put("categorydir",Category.dao.getCategorydir(cat));
			urlparas.put("catdir",cat.getStr("catdir"));
			urlparas.put("catid",catid);
			urlparas.put("page",page);
			urlparas.put("topcatdir",Category.dao.getTopcatdir(cat));
			urlparas.put("uuid",BgzKit.md5strWithidRet24(catid));
			this.setAttr("URLPARA",urlparas);
			
			Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("catid",""+catid));
			this.setAttr("SEO",seo);
		}else{
			String page_template=(String)setting.get("page_template");
			template=StringUtils.isNotEmpty(page_template)?page_template:"page.html";
			
			Record r=Page.dao.getOneR("*","catid=?","","",catid);
			this.setAttrs(r.getColumns());
			
			String arrchild=cat.getStr("arrchildid");
			if(categorys.get(cat.getInt("parentid"))!=null){
				arrchild=categorys.get(cat.getInt("parentid")).getStr("arrchildid");
			}
			String[] arrchild_arr=StringUtils.split(arrchild,",");
			arrchild_arr=ArrayUtils.remove(arrchild_arr,0);
			this.setAttr("arrchild_arr",arrchild_arr);
			
			Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("catid",""+catid,
			                                          "keywords",r.getStr("keywords")));
			this.setAttr("SEO",seo);
		}
		this.render(template(style,"content",template));
	}
}
