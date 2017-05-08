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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.ext.jfinal.BeetlRenderFactory;
import org.beetl.ext.web.SessionWrapper;
import org.beetl.ext.web.WebVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.common.BaseController;
import com.baguaz.module.category.Category;
import com.baguaz.module.site.Site;
import com.baguaz.module.urlrule.Urlrule;
import com.baguaz.pagetpl.TagCache;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/createhtml", viewPath = "admin/content")
public class CreateHtmlController extends BaseController{
	private static final Logger log=LoggerFactory
			.getLogger(CreateHtmlController.class);
	
	/**
	 * 更新URL
	 */
	@RequiresPermissions("createhtml:updateUrls")
	@Before(Tx.class)
	public void updateUrls(){
		List<Category> catlist=Category.dao.selectM("*","","","","");
		String domain=Site.dao.getDomain();
		for(Category cat:catlist){
			if(cat.isTypeCat()){
				Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
				int catid=cat.getInt("catid");
				String catdir=cat.getStr("catdir");
//				String parentdir=cat.getStr("parentdir");
				int ishtml=(int)setting.get("ishtml");
				int catrid=(int)setting.get("category_ruleid");
//				int create_to_html_root=(int)setting.get("create_to_html_root");
				if(ishtml==1){//生成静态
					String url=cat.getStr("url");
					if(url.indexOf("://")<0){
						Urlrule ur=Urlrule.dao.findById(catrid);
						String urStr=ur.getStr("urlrule");
						// ${categorydir}${catdir}/index.html
						// ${topcatdir}/list/${uuid}.html
						urStr=urStr.split("\\|")[0];
						urStr=GroovyKit.j2gStr(urStr);
//						String categorydir=create_to_html_root==0?cat.getStr("parentdir"):"";
						Map<String,Object> urlparas=Maps.newHashMap();
						urlparas.put("categorydir",Category.dao.getCategorydir(cat));
						urlparas.put("catdir",cat.getStr("catdir"));
						urlparas.put("topcatdir",Category.dao.getTopcatdir(cat));
						urlparas.put("uuid",BgzKit.md5strWithidRet24(catid));
						url=GroovyKit.runScriptFromStr(urStr,urlparas).toString();
						if(url.endsWith("index.html")){
							url=StringUtils.substring(url,0,-10);
						}
						cat.set("url","/"+url);
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
					cat.set("url",url);
				}
				
				cat.update();
				
				/*
				 * 更新栏目下文章内容
				 */
				if(cat.getInt("child")==Category.CHILD_NH){
					List<News> lstDatas=News.dao.selectM("*","catid=?","","","",catid);
					for(News data:lstDatas){
						log.debug("setting="+setting);
						int nShowRuleid=(int)setting.get("show_ruleid");
						Urlrule ur=Urlrule.dao.findById(nShowRuleid);
						ishtml=ur.getInt("ishtml");
						String urStr=ur.getStr("urlrule");
						int nNewsid=data.getInt("id");
						String strInputDate=DateFormatUtils.format(BgzKit.s2ms(data.getLong("inputtime")),"yyyy-MM-dd");
						String[] arrStrInputDate=StringUtils.split(strInputDate,"-");
						String year=arrStrInputDate[0];
						String month=arrStrInputDate[1];
						String day=arrStrInputDate[2];
						if(ishtml==Urlrule.ISHTML_Y){//生成静态
							Object url="";
							urStr=urStr.split("\\|")[0];
							urStr=GroovyKit.j2gStr(urStr);//
							Map<String,Object> urlparas=Maps.newHashMap();
/*							if(create_to_html_root==0){
								map.put("categorydir",(Object)parentdir);
							}else{
								map.put("categorydir",(Object)"");
							}*/
							urlparas.put("categorydir", Category.dao.getCategorydir(cat));
							urlparas.put("catdir",(Object)catdir);
							urlparas.put("id",(Object)nNewsid);
							urlparas.put("year",(Object)year);
							urlparas.put("month",(Object)month);
							urlparas.put("day",(Object)day);
							urlparas.put("topcatdir",Category.dao.getTopcatdir(cat));
							urlparas.put("uuid", BgzKit.md5strWithidRet24(nNewsid));
							url=GroovyKit.runScriptFromStr(urStr,urlparas);
							data.set("url","/"+url.toString());
						}else{//不生成静态
							Object url="";
							if(urStr.indexOf("|")>=0){//动态
								urStr=urStr.split("\\|")[0];// /content/index/lists?&catid=${catid}
								urStr=GroovyKit.j2gStr(urStr);
								url=GroovyKit.runScriptFromStr(urStr,ImmutableMap.of("catid",(Object)catid,
								                                                     "id",(Object)nNewsid));
								url=domain+url.toString();
							}else{//伪静态
								urStr=GroovyKit.j2gStr(urStr);
								url=GroovyKit.runScriptFromStr(urStr,ImmutableMap.of("catid",(Object)catid,
								                                                     "id",(Object)nNewsid,
								                                                     "page",(Object)1));
								url=domain+url.toString();
							}
							data.set("url",url);
						}
						data.update();
					}
				}
				
			}else if(cat.isTypePage()){
				Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
				int catid=cat.getInt("catid");
				int ishtml=(int)setting.get("ishtml");
				int catrid=(int)setting.get("category_ruleid");
				if(ishtml==1){//生成静态
					String url="";
					Urlrule ur=Urlrule.dao.findById(catrid);
					String urStr=ur.getStr("urlrule");
					// ${categorydir}${catdir}/index.html
					// ${topcatdir}/list/${uuid}.html
					urStr=urStr.split("\\|")[0];
					urStr=GroovyKit.j2gStr(urStr);
//					String categorydir=cat.getStr("parentdir");
					Map<String,Object> urlparas=Maps.newHashMap();
					urlparas.put("categorydir",Category.dao.getCategorydir(cat));
					urlparas.put("catdir",cat.getStr("catdir"));
					urlparas.put("topcatdir",Category.dao.getTopcatdir(cat));
					urlparas.put("uuid",BgzKit.md5strWithidRet24(catid));
					url=GroovyKit.runScriptFromStr(urStr,urlparas).toString();
					if(url.endsWith("index.html")){
						url=StringUtils.substring(url,0,-10);
					}
					cat.set("url","/"+url);
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
					cat.set("url",url);
				}
				cat.update();
			}else{
				continue;
			}
		}
		this.renderText("更新成功！");
	}
	
	private void doGenHtml(String tplPath,String htmlPath,WebVariable wv){
		log.debug("tplPath="+tplPath);
		log.debug("htmlPath="+htmlPath);
		Template tpl=BeetlRenderFactory.groupTemplate.getTemplate(tplPath);
		HttpServletRequest request=wv.getRequest();
		Enumeration<String> attrs = request.getAttributeNames();
		while (attrs.hasMoreElements()){
			String attrName = attrs.nextElement();
			tpl.binding(attrName, request.getAttribute(attrName));
		}
		tpl.binding("session", new SessionWrapper(wv.getSession()));
		tpl.binding("servlet", wv);
		tpl.binding("request", wv.getRequest());
		tpl.binding("ctxPath", request.getContextPath());
		
		String toFile=PathKit.getWebRootPath()+htmlPath;
		log.debug("toFile="+toFile);
		File f=new File(toFile);
		FileOutputStream fos=null;
		try{
			f.getParentFile().mkdirs();
			f.createNewFile();
			fos=new FileOutputStream(f);
			tpl.renderTo(fos);
		}catch(BeetlException | IOException e){
			log.error("",e);
		}finally{
			if(fos!=null){
				try{
					fos.close();
				}catch(IOException e){
					log.error("",e);
				}
			}
		}
	}
	
	/**
	 * 生成首页
	 */
	@RequiresPermissions("createhtml:genIndex")
	public void genIndex(){
		Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("title",(String)CacheFunc.getSiteSettings().get("index_title")));
		this.setAttr("SEO",seo);
		Map<Integer, Category> categorys=CacheFunc.getIdCategorys();
		this.setAttr("CATEGORYS",categorys);
		String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
		
		String tplPath=template(style,"content","index.html");
		String htmlPath="/index.html";
		
		WebVariable wv = new WebVariable();
		wv.setRequest(this.getRequest());
		wv.setResponse(this.getResponse());
		wv.setSession(this.getSession());
		
		StrBuilder sb=new StrBuilder();
		sb.append("生成开始...\n\n");
		this.doGenHtml(tplPath,htmlPath,wv);
		sb.append("\t"+htmlPath);
		sb.append("\n\n生成结束！");
		this.renderText(sb.toString());
	}
	
	/**
	 * 生成栏目页
	 */
	@RequiresPermissions("createhtml:genCategory")
	public void genCategory(){
		StrBuilder sb=new StrBuilder();
		sb.append("生成开始...\n\n");
		Map<Integer, Category> catsMap=CacheFunc.getIdCategorys();
		for(int catid:catsMap.keySet()){
			int page=1;
			int j=1;
			int total=0;
			do{
				String htmlPath=category(catid,page);
				if(StringUtils.isNotEmpty(htmlPath)){
					sb.append("\t"+htmlPath+"\n");	
				}
				page++;
				j++;
				total=total>0?total:GlobalFunc.PAGES.get();
			}while(j<=total);
			GlobalFunc.PAGES.set(0);
		}
		sb.append("\n\n生成结束！");
		this.renderText(sb.toString());
	}
	private String category(int catid,int page){
		Map<Integer, Category> categorys=CacheFunc.getIdCategorys();
		Category cat=categorys.get(catid);
		
		if(cat.isTypeLink()){
			return null;
		}
		
		this.setAttr("CAT",cat);
		this.setAttr("CATEGORYS",categorys);
		
		Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
		
		if((int)setting.get("ishtml")==0){
			return null;
		}
		
		String style=(String)setting.get("template_list");
		
		String category_template=(String)setting.get("category_template");
		String template=StringUtils.isNotEmpty(category_template)?category_template:"category.html";
		
		String list_template=(String)setting.get("list_template");
		String template_list=StringUtils.isNotEmpty(list_template)?list_template:"list.html";
		
		this.setAttr("page",String.valueOf(page));
		
		String htmlPath=cat.getStr("url");
		if(cat.isTypeCat()){
			template = cat.getInt("child")==Category.CHILD_H ? template : template_list;
			
			String[] arrparentidArr=StringUtils.split(cat.getStr("arrparentid"),",");
			int top_parentid=arrparentidArr.length>=2?Integer.parseInt(arrparentidArr[1]):catid;
			this.setAttr("top_parentid",top_parentid);
			
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
			
			if(htmlPath.endsWith("/")){
				if(page==1){
					htmlPath+="index.html";
				}else{
					htmlPath+=page+".html";
				}
			}else{
				if(page>1){
					htmlPath=StringUtils.substring(htmlPath, 0, -5)+"_"+page+".html";
				}
			}
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
			
			if(htmlPath.endsWith("/")){
				htmlPath+="index.html";
			}
		}
		String tplPath=template(style,"content",template);
		
		WebVariable wv = new WebVariable();
		wv.setRequest(this.getRequest());
		wv.setResponse(this.getResponse());
		wv.setSession(this.getSession());
		
		this.doGenHtml(tplPath,htmlPath,wv);
		return htmlPath;
	}
	
	/**
	 * 批量生成内容页
	 */
	@RequiresPermissions("createhtml:genShow")
	public void genShow(){
		StrBuilder sb=new StrBuilder();
		sb.append("生成开始...\n\n");
		Map<Integer, Category> catsMap=CacheFunc.getIdCategorys();
		for(Map.Entry<Integer,Category> catEntry:catsMap.entrySet()){
			Category cat=catEntry.getValue();
			if(cat.isTypeCat() && cat.getInt("child")==0){
				Map<String,Object> setting=BgzKit.json2obj(cat.getStr("setting"));
				int content_ishtml=(int)setting.get("content_ishtml");
				if(content_ishtml==1){
					int catid=catEntry.getKey();
					List<Integer> newsidList=News.dao.select("id","catid=?","","","",catid);
					for(int id:newsidList){
						String htmlPath=show(catid,id);
						if(StringUtils.isNotEmpty(htmlPath)){
							sb.append("\t"+htmlPath+"\n");	
						}
					}
				}
			}
		}
		sb.append("\n\n生成结束！");
		this.renderText(sb.toString());
	}
	
	private String show(int catid,int id){
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
		
		String htmlPath=content.getStr("url");
		
		String tplPath=template(style,"content",template);
		
		WebVariable wv = new WebVariable();
		wv.setRequest(this.getRequest());
		wv.setResponse(this.getResponse());
		wv.setSession(this.getSession());
		
		this.doGenHtml(tplPath,htmlPath,wv);
		return htmlPath;
	}
	
	@RequiresPermissions("createhtml:clearCache")
	public void clearCache(){
		CacheFunc.removeCommons();
		CacheFunc.removePage();
		CacheFunc.removeAuthenticationCache();
		CacheFunc.removeAuthorizationCache();
		
		TagCache.getInstance().clear();
		
		this.renderText("清理成功！");
	}
}
