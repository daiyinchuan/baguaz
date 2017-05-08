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

package com.baguaz.module.community;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.baguaz.AppConfig.G;
import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.interceptor.MemberInterceptor;
import com.baguaz.module.community.notification.Notification;
import com.baguaz.module.community.section.Section;
import com.baguaz.module.community.topic.Reply;
import com.baguaz.module.community.topic.Topic;
import com.baguaz.module.member.Member;
import com.baguaz.module.site.Site;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/community")
public class IndexController extends BaseController {
	public void index() {
		String sOrt=this.getPara(0);
		
		Map<String,Section> tabSections=CacheFunc.getTabSections();
		Map<Integer,Section> idSections=CacheFunc.getIdSections();
		this.setAttr("IDSECTIONS", idSections);
		this.setAttr("TABSECTIONS", tabSections);
		
		if(StringUtils.isEmpty(sOrt) || sOrt.equals("l")){
			this.setAttr("selected_secid", 0);
			this.setAttr("selected_sec", "所有板块");
			
			String labelParaValue=null;
			if(StringUtils.isEmpty(sOrt)){
				labelParaValue="latest";
			}else{
				labelParaValue=this.getPara(1);
			}
			this.setAttr("label", labelParaValue);
			
			this.setAttr("latest_url", "/community/l-latest.html");
			this.setAttr("good_url", "/community/l-good.html");
			this.setAttr("noreply_url", "/community/l-noreply.html");
			
			String page=StringUtils.isNotEmpty(this.getPara(2))?this.getPara(3):"1";
			this.setAttr("page",page);
			
			String urlrules="community/l-${label}.html|community/l-${label}-p-${page}.html";
			urlrules=StringUtils.replace(urlrules,"|","~");
			this.setAttr("URLRULE",urlrules);
			List<String> urList=Splitter.on("~").trimResults().splitToList(urlrules);
			String tmpUrls=urList.size()==2?urList.get(1):urList.get(0);
			String[] _urls=BgzKit.regMatchAll("/\\${([a-z0-9_]+)}/i",tmpUrls);
			Map<String,Object> urlparas=Maps.newHashMap();
			for(String param:_urls){
				urlparas.put(param,this.getPara(param));
			}
			urlparas.put("page",page);
			urlparas.put("label",labelParaValue);
			this.setAttr("URLPARA",urlparas);
			
			String labelDesc=getLabelDesc(labelParaValue);
			
			Map<String, String> seo = GlobalFunc.seo(ImmutableMap.of("title", labelDesc+" - 所有板块"));
			this.setAttr("SEO", seo);
			
			String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
			this.render(template(style,"community","section.html"));
		}else if(sOrt.equals("s")){//板块
			/*
			 * s:section
			 */
			String sTabOrId=this.getPara(1);
			Section sec=null;
			if(StringUtils.isNotEmpty(sTabOrId)){
				if(BgzKit.regMatch("/^([A-Za-z]|[A-Za-z]+\\w*[A-Za-z0-9]+)$/",sTabOrId)){//tab
					sec=tabSections.get(sTabOrId);
				}else if(BgzKit.regMatch("/^([0-9]+)$/", sTabOrId)){//id
					sec=idSections.get(Integer.parseInt(sTabOrId));
				}
				this.setAttr("SEC", sec);
			}
			
			this.setAttr("selected_secid", sec.getInt("id"));
			this.setAttr("selected_sec", sec.getStr("name"));
			
			/*
			 * l=label
			 */
			String labelParaName=this.getPara(2);
			String labelParaValue=this.getPara(3);
			
			if(StringUtils.isEmpty(labelParaName) && StringUtils.isEmpty(labelParaValue)){
				labelParaName="l";
				labelParaValue="latest";
			}else if(StringUtils.isNotEmpty(labelParaName) && !labelParaName.equals("l")){
				this.renderError(404);
			}else if(StringUtils.isNotEmpty(labelParaValue) && !BgzKit.regMatch("/^(latest|top|good|noreply)$/", labelParaValue)){
				this.renderError(404);
			}
			
			String page=StringUtils.isNotEmpty(this.getPara(4))?this.getPara(5):"1";
			this.setAttr("page",page);
			
			Map<String,Object> settings=BgzKit.json2obj(sec.getStr("setting"));
			int section_ruleid=(int)settings.get("section_ruleid");
			String urlrules=CacheFunc.getIdUrlrules().get(section_ruleid).getStr("urlrule");
			int secid=sec.getInt("id");
			String tab=sec.getStr("tab");
			String ur=urlrules;
			urlrules=StringUtils.replace(urlrules,"|","~");
			this.setAttr("URLRULE",urlrules);
			List<String> urList=Splitter.on("~").trimResults().splitToList(urlrules);
			String tmpUrls=urList.size()==3?urList.get(2):urList.get(1);
			String[] _urls=BgzKit.regMatchAll("/\\${([a-z0-9_]+)}/i",tmpUrls);
			Map<String,Object> urlparas=Maps.newHashMap();
			for(String param:_urls){
				urlparas.put(param,this.getPara(param));
			}
			urlparas.put("secid", secid);
			urlparas.put("tab", tab);
			urlparas.put("page",page);
			urlparas.put("label",labelParaValue);
			this.setAttr("URLPARA",urlparas);
			
			this.setAttr("label", labelParaValue);
			
			ur=GroovyKit.j2gStr(ur.split("\\|")[1]);
			String domain=CacheFunc.getIdSites().get(Site.SITEID).getStr("domain");
			this.setAttr("latest_url",domain+buildUrl(ur,secid,tab,"latest"));
			this.setAttr("good_url",domain+buildUrl(ur,secid,tab,"good"));
			this.setAttr("noreply_url",domain+buildUrl(ur,secid,tab,"noreply"));
			
			String labelDesc=getLabelDesc(labelParaValue);
			
			Map<String, String> seo = GlobalFunc.seo(ImmutableMap.of("secid",""+secid,"title", labelDesc));
			this.setAttr("SEO", seo);

			
			String style=(String)settings.get("template_list");
			String template=(String)settings.get("section_template");
			template=StringUtils.isNotEmpty(template)?template:"section.html";
			this.render(template(style,"community",template));
		}else if(sOrt.equals("t")){//话题
			/*
			 * t:topic
			 */
			String page=StringUtils.isNotEmpty(this.getPara(2))?this.getPara(3):"1";
			this.setAttr("page",page);
			
			List<Topic> topics=null;
			int id=this.getParaToInt(1);
			if(Integer.parseInt(page)>1){
				topics=Topic.dao.find("SELECT t.id,t.title,t.secid,t.view,t.reply_count,t.floor_count,t.url,t.authorid,m.nickname,m.avatar,m.signature,m.score FROM "+Topic.dao.tn()+" t LEFT JOIN "+Member.dao.tn()+" m ON t.authorid=m.id WHERE t.id=? AND t.isdelete=0", id);
			}else if(page.equals("1")){
				topics=Topic.dao.find("SELECT t.*,m.nickname,m.avatar,m.signature,m.score FROM "+Topic.dao.tn()+" t LEFT JOIN "+Member.dao.tn()+" m ON t.authorid=m.id WHERE t.id=? AND t.isdelete=0", id);
			}
			if(topics==null || topics.size()==0){
				this.renderError(404);
				return;
			}
			
			Topic topic=topics.get(0);
			topic.set("view", topic.getInt("view")+1).update();
			
			int secid=topic.getInt("secid");
			
			Map<Integer,Section> sections=CacheFunc.getIdSections();
			Section sec=sections.get(secid);
			this.setAttr("SEC", sec);
			
			Map<String,Object> settings=BgzKit.json2obj(sec.getStr("setting"));
			int thread_ruleid=(int)settings.get("thread_ruleid");
			String urlrules=CacheFunc.getIdUrlrules().get(thread_ruleid).getStr("urlrule");
			urlrules=StringUtils.replace(urlrules,"|","~");
			this.setAttr("URLRULE",urlrules);
			List<String> urList=Splitter.on("~").trimResults().splitToList(urlrules);
			String tmpUrls=urList.size()==2?urList.get(1):urList.get(0);
			String[] _urls=BgzKit.regMatchAll("/\\${([a-z0-9_]+)}/i",tmpUrls);
			Map<String,Object> urlparas=Maps.newHashMap();
			for(String param:_urls){
				urlparas.put(param,this.getPara(param));
			}
			urlparas.put("id", id);
			urlparas.put("page",page);
			this.setAttr("URLPARA",urlparas);
			
			Map<String, String> seo = GlobalFunc.seo(ImmutableMap.of("secid",""+secid,"title",topic.getStr("title")));
			this.setAttr("SEO", seo);
			
			String style=(String)settings.get("template_list");
			String template=(String)settings.get("thread_template");
			template=StringUtils.isNotEmpty(template)?template:"thread.html";
			
			this.setAttrs(topic.toRecord().getColumns());
			
			this.render(template(style,"community",template));
		}
	}
	
	@Before({MemberInterceptor.class})
	public void getTopicQuote(){
		Topic quote=Topic.dao.find("SELECT t.content,m.nickname FROM "+Topic.dao.tn()+" t LEFT JOIN "+Member.dao.tn()+" m ON t.authorid=m.id WHERE t.id=?", this.getParaToInt("topid")).get(0);
		
		Record quote1=quote.keep("content").toRecord();
		quote1.set("authorname", quote.getStr("nickname"));
		
		Ret ret=Ret.create();
		HashMap<Object,Object> retMap=new HashMap<>();
		retMap.put("quote", quote1);
		ret.setMap(retMap);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	private String getLabelDesc(String labelParaValue){
		String labelDesc="";
		if(labelParaValue.equals("latest")){
			labelDesc="最新";
		}else if(labelParaValue.equals("good")){
			labelDesc="精华";
		}else if(labelParaValue.equals("noreply")){
			labelDesc="等待回复";
		}
		return labelDesc;
	}
	
	private String buildUrl(String ur,int secid,String tab,String label){
		return GroovyKit.runScriptFromStr(ur,ImmutableMap.of(
				"secid",(Object)secid,
				"tab",(Object)tab,
				"label",(Object)label)
				).toString();
	}
	
	@Before({MemberInterceptor.class, Tx.class})
	public void createTopic(){
		if(this.isParaExists("dosubmit")){
			String title=this.getPara("title");
			String content=this.getPara("content");
			int secid=this.getParaToInt("secid");
			
			long ut=BgzKit.genUnixTstamp();
			Member m_session = (Member)this.getSessionAttr(G.SESSION_MEMBER);
			Topic topic=new Topic();
			topic.set("title", title)
				 .set("content", content)
				 .set("secid", secid)
				 .set("inputtime", ut)
				 .set("updatetime", ut)
				 .set("last_reply_time", ut)
				 .set("authorid", m_session.getLong("id"));
				 
			String domain=CacheFunc.getIdSites().get(Site.SITEID).getStr("domain");
			if(topic.save()){
				int topicId=topic.getInt("id");
				topic.set("url", domain+"community/t-"+topicId+".html").update();
			}
			
			//积分
			m_session.set("score", m_session.getLong("score")+3).update();
			
			Ret ret=Ret.create();
			HashMap<Object,Object> retMap=new HashMap<>();
			retMap.put("url", topic.getStr("url"));
			ret.setMap(retMap);
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}
	}
	
	/**
	 * 
	 */
	@Before({MemberInterceptor.class, Tx.class})
	public void reply(){
		if(this.isParaExists("dosubmit")){
/*			int rtype=0;//默认回复主帖
			if(this.isParaExists("pid")){
				rtype=1;//回复楼帖
				if(this.getParaToInt("targetAuthorid")!=0){
					rtype=2;//回复楼帖其他作者
				}
			}*/
			
			int topid=this.getParaToInt("topid");
			String content=this.getPara("content");
			
			Topic t=Topic.dao.findById(topid);
			int floorCount=t.getInt("floor_count")+1;
			
			Reply r=new Reply();
			long ut=BgzKit.genUnixTstamp();
			Member m_session = (Member)this.getSessionAttr(G.SESSION_MEMBER);
			r.set("topid", topid).set("content", content).set("inputtime", ut)
			 .set("floor", floorCount)
			 .set("authorid", m_session.getLong("id"));
			
/*			if(rtype>=1){
				r.set("pid", this.getParaToLong("pid"));
				if(rtype==2){
					r.set("target_authorid", this.getParaToLong("targetAuthorid"));
				}
			}*/
			
			r.save();
			
			long replyCount=t.getLong("reply_count")+1;
			t.set("last_reply_time", ut)
			 .set("last_reply_authorid", m_session.getLong("id"))
			 .set("reply_count",replyCount)
			 .set("floor_count", floorCount)
			 .update();
			
			//积分
			m_session.set("score", m_session.getLong("score")+1).update();
			
			int page=(int)Math.ceil((double)floorCount/10);
			String url=t.getStr("url");
			
			/*
			 * 发通知
			 */
			if(m_session.getLong("id").compareTo(t.getLong("authorid"))!=0){
				Notification notif=new Notification();
				notif.set("content", t.getStr("title"))
					 .set("isread", 0)
					 .set("from_authorid", m_session.getLong("id"))
					 .set("authorid", t.getLong("authorid"));
				String targetid=StringUtils.substringBetween(url, "/community/", ".html");
				targetid="/community/"+targetid+"-p-"+page+".html?f="+floorCount;
				notif.set("targetid", targetid)
					 .set("inputtime", ut)
					 .set("action", "回复了你的话题")
					 .set("source", 1)
					 .save();
			}
			
			Ret ret=Ret.create();
//			if(rtype==0){

			url=StringUtils.substringBefore(url, ".html");
			url+=("-p-"+page+".html?f="+floorCount);
			
			HashMap<Object,Object> retMap=new HashMap<>();
			retMap.put("url", url);
			ret.setMap(retMap);
/*			}else{
				
			}*/
			
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}
	}
	
	@Before({MemberInterceptor.class, Tx.class})
	public void delTopic(){
		int topid=this.getParaToInt("topid");
		Topic t=Topic.dao.findById(topid);
		t.set("isdelete", 1).update();
		
		Ret ret=Ret.create();
		HashMap<Object,Object> retMap=new HashMap<>();
		retMap.put("url", "/community");
		ret.setMap(retMap);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@Before({MemberInterceptor.class, Tx.class})
	public void delReply(){
		int replyid=this.getParaToInt("replyid");
		Reply r=Reply.dao.findById(replyid);
		r.set("isdelete", 1).update();
		Topic t=Topic.dao.findById(r.getInt("topid"));
		t.set("reply_count", t.getLong("reply_count")-1).update();
		
		Ret ret=Ret.create();
		HashMap<Object,Object> retMap=new HashMap<>();
		retMap.put("url", t.getStr("url"));
		ret.setMap(retMap);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
}
