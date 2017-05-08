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

package com.baguaz;

import static com.baguaz.BgzKit.stripTags;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig.G;
import com.baguaz.module.category.Category;
import com.baguaz.module.community.section.Section;
import com.baguaz.module.site.Site;
import com.google.common.collect.Maps;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public final class GlobalFunc{
	private static final Logger log=LoggerFactory
			.getLogger(GlobalFunc.class);
	/**
	 * 从文件系统获取模板样式列表
	 * @return
	 */
	public static List<Map<String,String>> getFsTemplateStyles(){
		PropKit.use(G.baguaz_prop);
		String templateRoot=PathKit.getWebRootPath()+PropKit.get("tpl.root");
		File root=new File(templateRoot);
		File[] templateStyles=root.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir,String name){
				if(name.equals("admin")){
					return false;
				}else{
					return true;
				}
			}
		});
		List<Map<String,String>> rets=new ArrayList<>();
		Stream.of(templateStyles).forEach(f->{
			Map<String,String> nameDirs=new HashMap<>();
			nameDirs.put("name",f.getName());
			nameDirs.put("dirname",f.getName());
			rets.add(nameDirs);
		});
		return rets;
	}

	public static List<Map<String,String>> getSiteTemplateStyles(){
		String templateStylesArr=Site.dao.getTemplate();
		String[] templateStyles=StringUtils.split(templateStylesArr,"|");
		List<Map<String,String>> rets=new ArrayList<>();
		Stream.of(templateStyles).forEach(tpl->{
			Map<String,String> nameDirs=new HashMap<>();
			nameDirs.put("name",tpl);
			nameDirs.put("dirname",tpl);
			rets.add(nameDirs);
		});
		return rets;
	}
	
	public static List<Map<String,String>> cattplList(String style){
		return tplList(style,"content","category");
	}
	public static List<Map<String,String>> listtplList(String style){
		return tplList(style,"content","list");
	}
	public static List<Map<String,String>> showtplList(String style){
		return tplList(style,"content","show");
	}
	public static List<Map<String,String>> pagetplList(String style){
		return tplList(style,"content","page");
	}
	public static List<Map<String,String>> sectplList(String style){
		return tplList(style,"community","section");
	}
	public static List<Map<String,String>> thrtplList(String style){
		return tplList(style,"community","thread");
	}
	private static List<Map<String,String>> tplList(String style,String module,final String type){
		PropKit.use(G.baguaz_prop);
		String styleRoot=PathKit.getWebRootPath()+PropKit.get("tpl.root")+File.separator+style;
		File root=new File(styleRoot+File.separator+module);
		File[] tpls=root.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir,String name){
				if(name.startsWith(type)){
					return true;
				}else{
					return false;
				}
			}
		});
		List<Map<String,String>> rets=new ArrayList<>();
		if(tpls!=null){
			Stream.of(tpls).forEach(f->{
				Map<String,String> nameNames=new HashMap<>();
				nameNames.put("name",f.getName());
				rets.add(nameNames);
			});
		}
		return rets;
	}
	
	/**
	 * 
	 * @param params catid,title,description,keywords
	 * @return
	 */
	public static Map<String,String> seo(Map<String,String> paras){
		String title=paras.get("title");
		if(StringUtils.isNotEmpty(title)){
			title=stripTags(title);
		}
		String description=paras.get("description");
		if(StringUtils.isNotEmpty(description)){
			description=stripTags(description);
		}
		String keywords=paras.get("keywords");
		if(StringUtils.isNotEmpty(keywords)){
			keywords=StringUtils.replace(stripTags(keywords)," ",",");
		}
		
		Map<Integer,Site> sites=CacheFunc.getIdSites();
		Site site=sites.get(Site.SITEID);
		
		Map<String,Object> settings=null;
		String catid=null,secid=null;
		if(paras.containsKey("catid")){
			catid=paras.get("catid");
			if(StringUtils.isNotEmpty(catid)){
				Map<Integer,Category> categorys=CacheFunc.getIdCategorys();
				Category cat=categorys.get(Integer.parseInt(catid));
				settings=BgzKit.json2obj(cat.getStr("setting"));
			}
		}else if(paras.containsKey("secid")){
			secid=paras.get("secid");
			if(StringUtils.isNotEmpty(secid)){
				Map<Integer,Section> sections=CacheFunc.getIdSections();
				Section sec=sections.get(Integer.parseInt(secid));
				settings=BgzKit.json2obj(sec.getStr("setting"));
			}
		}
		
		Map<String,String> seo=Maps.newHashMap();
		
		String meta_keywords=(settings!=null?(String)settings.get("meta_keywords"):null);
		seo.put("keywords",StringUtils.isNotEmpty(keywords)?keywords:
			(StringUtils.isNotEmpty(meta_keywords)?meta_keywords:site.getStr("keywords")));
		
		String meta_description=(settings!=null?(String)settings.get("meta_description"):null);
		seo.put("description",StringUtils.isNotEmpty(description)?description:
			(StringUtils.isNotEmpty(meta_description)?meta_description:site.getStr("description")));
		
		String meta_title=(settings!=null?(String)settings.get("meta_title"):null);
		if(StringUtils.isNotEmpty(catid) || StringUtils.isNotEmpty(secid)){
			seo.put("title",(StringUtils.isNotEmpty(title)?title+" - ":"")+
					(StringUtils.isNotEmpty(meta_title)?meta_title:""));
		}else{
			seo.put("title", title);
		}
		
		String site_title=site.getStr("site_title");
		seo.put("site_title",StringUtils.isNotEmpty(site_title)?site_title:site.getStr("name"));
		
		/*String catname=(cat!=null?cat.getStr("catname"):null);
		seo.put("title",(StringUtils.isNotEmpty(title)?title+" - ":"")+
		        (StringUtils.isNotEmpty(meta_title)?meta_title+" - ":
		        	(StringUtils.isNotEmpty(catname)?catname+" - ":"")));*/
		
		seo.forEach((k,v)->seo.put(k,StringUtils.replacePattern(v,"\\n|\\r","")));
		return seo;
	}
	
	public static ThreadLocal<Integer> PAGES=new ThreadLocal<Integer>(){
		protected Integer initialValue() {
	        return 0;
	    }
	};
	/**
	 * 分页
	 * @param total 数据量总数
	 * @param curpage 当前页数
	 * @param pagesize 分页大小
	 * @param setpages 省略规则中间显示数量
	 * @param urlrule url规则
	 * @param urlpara url规则参数值
	 * @return
	 */
	public static String pages(long total,int curpage,int pagesize,int setpages,String urlrule,Map<String,Object> urlpara){
		StrBuilder multipage=new StrBuilder();
		if(total>pagesize){
			int page=setpages+1;
			int offset=(int)Math.ceil((double)setpages/2-1);
			int pages=(int)Math.ceil((double)total/pagesize);
			PAGES.set(pages);
			int from=curpage-offset;
			int to=curpage+offset;
			int more=0;
			if(page>=pages){
				from=2;
				to=pages-1;
			}else{
				if(from<=1){
					to=page-1;
					from=2;
				}else if(to>=pages){
					from=pages-(page-2);
					to=pages-1;
				}
				more=1;
			}
			multipage.append("<span class=\"total\">").append(total).append("条</span>");
			if(curpage>0){
				int offset1=(int)Math.ceil((double)setpages/2+1);
				if(curpage==1){
					multipage.append(" <span").append(">上一页</span>");
					multipage.append(" <span class=\"cur\">1</span>");
				}else{ 
					multipage.append(" <a href=\"").append(pageurl(urlrule,curpage-1,urlpara))
					 		 .append("\">上一页</a>");
					if(curpage>offset1 && more==1){
						multipage.append(" <a href=\"").append(pageurl(urlrule,1,urlpara)).append("\">1</a><span class=\"ellip\">...</span>");
					}else{
						multipage.append(" <a href=\"").append(pageurl(urlrule,1,urlpara)).append("\">1</a>");
					}
				}
			}
			for(int i=from;i<=to;i++){
				if(i!=curpage){
					multipage.append(" <a href=\"").append(pageurl(urlrule,i,urlpara))
							 .append("\">").append(i).append("</a>");
				}else{
					multipage.append(" <span class=\"cur\">").append(i).append("</span>");
				}
			}
			if(curpage<pages){
				int offset2=(int)Math.ceil((double)setpages/2);
				if(curpage<pages-offset2 && more==1){
					multipage.append(" <span class=\"ellip\">...</span>");
				}else{
					multipage.append(" ");
				}
				multipage.append("<a href=\"").append(pageurl(urlrule,pages,urlpara))
        		 		 .append("\">").append(pages).append("</a>")
        		 		 .append(" <a href=\"").append(pageurl(urlrule,curpage+1,urlpara))
        		 		 .append("\">下一页</a>");
			}else if(curpage==pages){
				multipage.append(" <span class=\"cur\">").append(pages).append("</span> <span")
				 	 	 .append(">下一页</span>");
			}else{
				multipage.append(" <a href=\"").append(pageurl(urlrule,pages,urlpara))
						 .append("\">").append(pages).append("</a>")
						 .append(" <a href=\"").append(pageurl(urlrule,curpage+1,urlpara))
						 .append("\">下一页</a>");
			}
		}
		return multipage.toString();
	}
	
	/**
	 * 分页
	 * @param total 数据量总数
	 * @param curpage 当前页数
	 * @param pagesize 分页大小
	 * @param setpages 省略规则中间显示数量
	 * @param urlrule url规则
	 * @param urlpara url规则参数值
	 * @return
	 */
	public static String pagesAm(long total,int curpage,int pagesize,int setpages,String urlrule,Map<String,Object> urlpara){
		StrBuilder multipage=new StrBuilder();
		if(total>pagesize){
			int page=setpages+1;
			int offset=(int)Math.ceil((double)setpages/2-1);
			int pages=(int)Math.ceil((double)total/pagesize);
			int from=curpage-offset;
			int to=curpage+offset;
			int more=0;
			if(page>=pages){
				from=2;
				to=pages-1;
			}else{
				if(from<=1){
					to=page-1;
					from=2;
				}else if(to>=pages){
					from=pages-(page-2);
					to=pages-1;
				}
				more=1;
			}
			if(curpage>0){
				int offset1=(int)Math.ceil((double)setpages/2+1);
				if(curpage==1){
					multipage.append("<li><span")
					 		 .append(">&laquo;</span></li>");
					multipage.append("<li class=\"am-active\"><span>1</span></li>");
				}else{
					multipage.append("<li><a href=\"").append(pageurl(urlrule,curpage-1,urlpara))
					 		 .append("\">&laquo;</a></li>");
					if(curpage>offset1 && more==1){
						multipage.append("<li><a href=\"").append(pageurl(urlrule,1,urlpara)).append("\">1</a></li><li><div class=\"ellip\">...</div></li>");
					}else{
						multipage.append("<li><a href=\"").append(pageurl(urlrule,1,urlpara)).append("\">1</a></li>");
					}
				}
			}
			for(int i=from;i<=to;i++){
				if(i!=curpage){
					multipage.append("<li><a href=\"").append(pageurl(urlrule,i,urlpara))
							 .append("\">").append(i).append("</a></li>");
				}else{
					multipage.append("<li class=\"am-active\"><span>").append(i).append("</span></li>");
				}
			}
			if(curpage<pages){
				int offset2=(int)Math.ceil((double)setpages/2);
				if(curpage<pages-offset2 && more==1){
					multipage.append("<li><div class=\"ellip\">...</div></li>");
				}else{
					multipage.append(" ");
				}
				multipage.append("<li><a href=\"").append(pageurl(urlrule,pages,urlpara))
        		 		 .append("\">").append(pages).append("</a></li>")
        		 		 .append("<li><a href=\"").append(pageurl(urlrule,curpage+1,urlpara))
        		 		 .append("\">&raquo;</a></li>");
			}else if(curpage==pages){
				multipage.append("<li class=\"am-active\"><span>").append(pages).append("</span></li><li><span")
				 	 	 .append(">&raquo;</span></li>");
			}else{
				multipage.append("<li><a href=\"").append(pageurl(urlrule,pages,urlpara))
						 .append("\">").append(pages).append("</a></li>")
						 .append("<li><a href=\"").append(pageurl(urlrule,curpage+1,urlpara))
						 .append("\">&raquo;</a></li>");
			}
		}
		return multipage.toString();
	}

	/**
	 * 返回分页路径
	 * @param urlrule 分页规则
	 * @param page 当前页
	 * @param urlpara
	 * @return 完整的URL路径
	 */
	private static String pageurl(	String urlrule,
									int page,
									Map<String,Object> urlpara){
		if(urlrule.indexOf("~")>=0){
			String[] urlrules=urlrule.split("~");
			if(urlrules.length==2){
				urlrule=page<2?urlrules[0]:urlrules[1];
			}else if(urlrules.length==3){
				urlrule=page<2?urlrules[1]:urlrules[2];
			}
		}
		Map<String,Object> _urlpara=Maps.newHashMap(urlpara);
		_urlpara.put("page",page);
		urlrule=GroovyKit.j2gStr(urlrule);
		Object pageurl=GroovyKit.runScriptFromStr(urlrule,_urlpara);
		String _pageurl=Site.dao.getDomainFromCache()+pageurl.toString();
		log.debug("pageurl="+_pageurl);
		return _pageurl;
	}
}