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

package com.baguaz.pagetpl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.GlobalFunc;
import com.baguaz.module.community.section.Section;
import com.baguaz.module.community.section.Section.SecTreeNode;
import com.baguaz.module.community.topic.Reply;
import com.baguaz.module.community.topic.Topic;
import com.baguaz.module.member.Member;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class CommunityTag extends BgzTag {
	private static final Logger log=LoggerFactory
			.getLogger(CommunityTag.class);

	/* (non-Javadoc)
	 * @see org.beetl.core.Tag#render()
	 */
	@Override
	public void render() {
		Object[] data=this.handleWithCache(prepare());
		if(data[1]==null){
			this.binds(data[0]);
		}else{
			this.binds(data[0],data[1]);
		}
		this.doBodyRender();
	}
	
	/* (non-Javadoc)
	 * @see com.baguaz.cms.pagetpl.BgzTag#doExpensive(java.util.Map)
	 */
	@Override
	Object[] doExpensive(Map<String, String> paras) {
		/*
		 * section:
		 * topics:		secid,tab,where,order,page,num,setpages
		 */
		
		int num=StringUtils.isNotEmpty(paras.get("num"))?Integer.parseInt(paras.get("num")):20;
		paras.put("num", ""+num);
		paras.put("order",Strings.nullToEmpty(paras.get("order")));
		Object data=null;
		String pages=null;
		String action=(String) this.getAttributeValue("action");
		if(StringUtils.isNotEmpty(paras.get("page"))){
			int page=Integer.parseInt(paras.get("page"));
			if(page<=0){
				page=1;
				paras.put("page", "1");
			}
			int pageSize=num;
			paras.put("pageSize", ""+pageSize);
			try {
				data=MethodUtils.invokeMethod(this,action,paras);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				log.error("content tag反射调用action失败",e);
			}
			String urlrule=(String)this.ctx.getGlobal("URLRULE");
			@SuppressWarnings("unchecked")
			Map<String,Object> urlpara=(Map<String,Object>)this.ctx.getGlobal("URLPARA");
			int setpages=9;//默认省略规则中间显示9个
			if(StringUtils.isNotEmpty(paras.get("setpages"))){
				setpages=Integer.parseInt(paras.get("setpages"));
			}
			@SuppressWarnings("unchecked")
			Page<Record> _page=(Page<Record>)data;
			pages=GlobalFunc.pagesAm(_page.getTotalRow(),page,pageSize,setpages,urlrule,urlpara);
		}else{
			try {
				data=MethodUtils.invokeMethod(this,action,paras);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				log.error("content tag反射调用action失败",e);
			}
		}
		return new Object[]{data,pages};
	}
	
	/**
	 * 板块标签
	 * @param paras
	 */
	public List<Record> section(Map<String,String> paras){
		int parentid=0;
		String arrchildid=Section.dao.getArrchildid(parentid);
		List<Map<String,Object>> secTreeNodes=null;
		if(!Strings.isNullOrEmpty(arrchildid)){
			String wherein=parentid+","+arrchildid;
			List<Record> secs=Section.dao.selectR("id,name,parentid,listorder,url,isdisplay","id in("+wherein+") and isdisplay=1","parentid,listorder","","");
			if(secs==null || secs.size()==0){
				secTreeNodes=ImmutableList.of();
			}else{
				secTreeNodes=SecTreeNode.buildTree(secs,parentid).toSecList();
			}
		}else{//空数据
			secTreeNodes=ImmutableList.of();
		}
		List<Record> rets=secTreeNodes.stream().map(s->new Record().setColumns(s)).collect(Collectors.toList());
		return rets;
	}
	
	/**
	 * 话题列表标签
	 * @param paras
	 * @return
	 */
	public Page<Record> lists(Map<String,String> paras){
		String select="SELECT s.tab,s.name secname,s.url securl,t.title,t.last_reply_time,t.view,t.last_reply_authorid,t.authorid,t.top,t.good,t.reply_count,t.url,m.nickname,m.avatar,"+
				"lrm.avatar last_reply_author_avatar,lrm.nickname last_reply_author_nickname";
		String order=" ORDER BY t.top DESC,t.last_reply_time DESC";
		
		StringBuilder condition = new StringBuilder();
		condition.append(" FROM ").append(Topic.dao.tn()).append(" t")
				 .append(" LEFT JOIN ").append(Section.dao.tn()).append(" s ON t.secid=s.id")
				 .append(" LEFT JOIN ").append(Member.dao.tn()).append(" m ON t.authorid=m.id")
				 .append(" LEFT JOIN ").append(Member.dao.tn()).append(" lrm ON t.last_reply_authorid=lrm.id")
				 .append(" WHERE 1=1 AND t.isdelete=0 AND t.isdisplay=1");
		
		String label=paras.get("label");
		if(StringUtils.isNotBlank(label) && label.equals("good")){
			condition.append(" AND t.good=1");
		}else if(StringUtils.isNotBlank(label) && label.equals("top")){
			condition.append(" AND t.top=1");
		}else if(StringUtils.isNotBlank(label) && label.equals("noreply")){
			condition.append(" AND t.reply_count=0");
		}
		
		int secid=StringUtils.isNotEmpty(paras.get("secid"))?Integer.parseInt(paras.get("secid")):0;
		condition.append(" AND s.isdisplay=1");
		if(secid>0){
			String arrchildid=Section.dao.getOne("arrchildid","id=?","","",secid);
			condition.append(" AND t.secid in(").append(arrchildid).append(")");
		}
		
		int page=Integer.parseInt(paras.get("page"));
		int pageSize=Integer.parseInt(paras.get("pageSize"));
		Page<Record> _page=Db.paginate(page,pageSize, select, condition+order);
		return _page;
	}
	
	public Page<Record> replys(Map<String,String> paras){
		int page=Integer.parseInt(paras.get("page"));
		int topid=Integer.parseInt(paras.get("topid"));
		int pageSize=Integer.parseInt(paras.get("pageSize"));
		Page<Record> replys=Db.paginate(page, pageSize, 
				"SELECT r.*,m.nickname,m.avatar", 
				"FROM "+Reply.dao.tn()+" r LEFT JOIN "+Member.dao.tn()+" m ON r.authorid=m.id WHERE r.topid=? AND r.pid=0 ORDER BY r.inputtime", 
				topid);
		/*replys.getList().stream().forEach(r->{
			List<Record> child=Db.find("SELECT r.*,m.username,m.nickname,m.avatar FROM "+Reply.dao.tn()+" r LEFT JOIN "+Member.dao.tn()+" m ON r.authorid=m.id WHERE r.pid=? ORDER BY r.inputtime LIMIT 3", r.getLong("id"));
			r.set("child", child);
		});*/
		return replys;
	}
	
	/**
	 * 社区运行状态
	 */
	public Map<String,Long> status(Map<String,String> paras){
		Map<String,Long> ret=Maps.newHashMap();
		
		long mCount=Member.dao.getOne("count(*)", "", "", "");
		ret.put("m_count", mCount);
		
		long tCount=Topic.dao.getOne("count(*)", "isdelete=0", "", "");
		ret.put("t_count", tCount);
		
		long rCount=Reply.dao.getOne("count(*)", "isdelete=0", "", "");
		ret.put("r_count", rCount);
		
		return ret;
	}
	
	/**
	 *  会员用户积分榜
	 */
	public List<Record> highscore(Map<String,String> paras){
		return Member.dao.selectR("score,id,nickname", "", "score DESC", "", paras.get("num"));
	}
	
	/**
	 * 会员用户其他的话题
	 */
	public List<Record> authortopics(Map<String,String> paras){
		String select="SELECT t.url,t.title";
		String order=" ORDER BY t.last_reply_time DESC";
		
		StringBuilder condition = new StringBuilder();
		condition.append(" FROM ").append(Topic.dao.tn()).append(" t")
				 .append(" LEFT JOIN ").append(Section.dao.tn()).append(" s ON t.secid=s.id")
				 .append(" WHERE 1=1 AND t.isdelete=0 AND t.isdisplay=1 AND s.isdisplay=1")
				 .append(" AND t.authorid=? AND t.id!=?");
		
		String limit=" LIMIT "+paras.get("num");
		
		long authorid=Long.parseLong(paras.get("authorid"));
		int tid=Integer.parseInt(paras.get("excludetid"));
		
		return Db.find(select+condition.toString()+order+limit, authorid,tid);
	}
}
