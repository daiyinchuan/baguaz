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

package com.baguaz.module.member;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.baguaz.AppConfig.G;
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
import com.baguaz.module.site.Site;
import com.google.common.collect.ImmutableMap;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/user")
public class MemberController extends BaseController {

	public void index(){
		Map<String, String> seo = GlobalFunc.seo(ImmutableMap.of("title", "会员中心"));
		this.setAttr("SEO", seo);
		
		Member m=Member.dao.findById(this.getParaToLong(0));
		this.setAttrs(m.toRecord().getColumns());
		
		long mid=this.getParaToLong(0);
		
		String select="SELECT s.tab,s.name secname,s.url securl,t.title,t.last_reply_time,t.view,t.last_reply_authorid,t.authorid,t.url,t.top,t.good,t.reply_count,m.nickname,m.avatar,"+
				"lrm.avatar last_reply_author_avatar,lrm.nickname last_reply_author_nickname";
		String order=" ORDER BY t.inputtime DESC";
		StringBuilder condition = new StringBuilder();
		condition.append(" FROM ").append(Topic.dao.tn()).append(" t")
				 .append(" LEFT JOIN ").append(Section.dao.tn()).append(" s ON t.secid=s.id")
				 .append(" LEFT JOIN ").append(Member.dao.tn()).append(" m ON t.authorid=m.id")
				 .append(" LEFT JOIN ").append(Member.dao.tn()).append(" lrm ON t.last_reply_authorid=lrm.id")
				 .append(" WHERE 1=1 AND t.isdelete=0 AND t.isdisplay=1 AND s.isdisplay=1 AND t.authorid=").append(mid);
		Page<Record> _page=Db.paginate(1,5, select, condition+order);
		this.setAttr("latestTopics", _page);
		
		
		String select1="SELECT s.tab,s.name secname,s.url securl,t.title,t.last_reply_time,t.view,t.last_reply_authorid,t.authorid,t.url,t.top,t.good,t.reply_count,m.nickname,m.avatar,"+
				"lrm.avatar last_reply_author_avatar,lrm.nickname last_reply_author_nickname";
		StringBuilder condition1 = new StringBuilder();
		List<Integer> rids=Reply.dao.select("DISTINCT topid", "authorid=? AND isdelete=0", "inputtime DESC", "", "5", mid);
		String ridsStr=rids.stream().map(id->String.valueOf(id)).collect(Collectors.joining(","));
		if(StringUtils.isEmpty(ridsStr)){
			ridsStr="0";
		}
		String order1=" ORDER BY FIELD(t.id,"+ridsStr+")";
		condition1.append(" FROM ").append(Topic.dao.tn()).append(" t")
				 .append(" LEFT JOIN ").append(Section.dao.tn()).append(" s ON t.secid=s.id")
				 .append(" LEFT JOIN ").append(Member.dao.tn()).append(" m ON t.authorid=m.id")
				 .append(" LEFT JOIN ").append(Member.dao.tn()).append(" lrm ON t.last_reply_authorid=lrm.id")
				 .append(" WHERE 1=1 AND t.isdelete=0 AND t.isdisplay=1 AND s.isdisplay=1 AND t.id in(").append(ridsStr).append(")");
		Page<Record> _page1=Db.paginate(1,5, select1, condition1+order1);
		this.setAttr("latestReplys", _page1);
		
		
		String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
		this.render(template(style,"member","home.html"));
	}
	
	public void topics(){
		
	}
	
	public void replies(){
		
	}
	
	public void top100(){
		
	}
	
	@Before(MemberInterceptor.class)
	public void notification(){
		Map<String, String> seo = GlobalFunc.seo(ImmutableMap.of("title", "通知"));
		this.setAttr("SEO", seo);
		
		int page=StringUtils.isNotEmpty(this.getPara(0))?this.getParaToInt(0):1;
		int pageSize=10;
		Member m_session = (Member)this.getSessionAttr(G.SESSION_MEMBER);
		long mid=m_session.getLong("id");
		Page<Notification> notifis=Notification.dao.paginate(page, pageSize, "SELECT n.*,m.nickname",
				"FROM "+Notification.dao.tn()+" n LEFT JOIN "+Member.dao.tn()+" m ON n.from_authorid=m.id WHERE n.authorid=? ORDER BY n.inputtime DESC", mid);
		
		int setpages=9;
		String urlrule="user/notification/${page}";
		String pages=GlobalFunc.pagesAm(notifis.getTotalRow(), page, pageSize, setpages, urlrule,ImmutableMap.of("page", page));
		
		this.setAttr("notifis", notifis);
		this.setAttr("pages", pages);
		
		String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
		this.render(template(style,"member","notification.html"));
	}
	
	@Before(MemberInterceptor.class)
	public void setting(){
		if(this.isParaExists("dosubmit")){
			Member m_session = (Member)this.getSessionAttr(G.SESSION_MEMBER);
			m_session.set("nickname", this.getPara("nickname"))
					 .set("signature", this.getPara("signature"))
					 .set("avatar", this.getPara("avatar"))
					 .update();
			
			Ret ret=Ret.create();
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Map<String, String> seo = GlobalFunc.seo(ImmutableMap.of("title", "设置"));
			this.setAttr("SEO", seo);
			
			Member m_session = (Member)this.getSessionAttr(G.SESSION_MEMBER);
			this.setAttrs(m_session.toRecord().getColumns());
			
			String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
			this.render(template(style,"member","setting.html"));
		}
	}
}
