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
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.module.category.Category;
import com.baguaz.module.content.News;
import com.baguaz.module.content.NewsData;
import com.baguaz.module.content.Page;
import com.baguaz.module.hits.Hits;
import com.baguaz.module.position.PositionData;
import com.baguaz.module.site.Site;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class ContentTag extends BgzTag{
	private static final Logger log=LoggerFactory
			.getLogger(ContentTag.class);
	
	/* (non-Javadoc)
	 * @see org.beetl.core.Tag#render()
	 */
	@Override
	public void render(){
		Object[] data=this.handleWithCache(prepare());
		if(data[1]==null){
			this.binds(data[0]);
		}else{
			this.binds(data[0],data[1]);
		}
		this.doBodyRender();
	}
	
	Object[] doExpensive(Map<String,String> paras){
		/*
		 * lists:		catid,where,thumb,order,num,moreinfo,page,start,setpages
		 * category:	catid,order,num,ismenu
		 * position:	posid,catid,where,thumb,order,num
		 * relation:	relation,catid,order,num
		 * hits:		catid,day,order,num
		 * page:		catid
		 */
		
		int num=StringUtils.isNotEmpty(paras.get("num"))?Integer.parseInt(paras.get("num")):20;
		if(StringUtils.isNotEmpty(paras.get("start"))){
			paras.put("limit",paras.get("start")+","+num);
		}else{
			paras.put("limit",""+num);
		}
		paras.put("order",Strings.nullToEmpty(paras.get("order")));
		String pages=null;
		if(StringUtils.isNotEmpty(paras.get("page"))){
			int pagesize=num;
			int page=Integer.parseInt(paras.get("page"));
			if(page<=0){
				page=1;
			}
			int setpages=9;//默认省略规则中间显示9个
			if(StringUtils.isNotEmpty(paras.get("setpages"))){
				setpages=Integer.parseInt(paras.get("setpages"));
			}
			int offset=(page-1)*pagesize;
			paras.put("limit",offset+","+pagesize);
			long total=this.count(paras);
			String urlrule=(String)this.ctx.getGlobal("URLRULE");
			@SuppressWarnings("unchecked")
			Map<String,Object> urlpara=(Map<String,Object>)this.ctx.getGlobal("URLPARA");
			pages=GlobalFunc.pages(total,page,pagesize,setpages,urlrule,urlpara);
		}
		String action=(String) this.getAttributeValue("action");
		Object data=null;
		try {
			data=MethodUtils.invokeMethod(this,action,paras);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			log.error("content tag反射调用action失败",e);
		}
		return new Object[]{data,pages};
	}

	/**
	 * 栏目标签
	 * @param paras
	 * catid 	默认0
	 * num 		默认""
	 */
	public List<Category> category(Map<String,String> paras){
		int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
		int ismenu=StringUtils.isNotEmpty(paras.get("ismenu"))?Integer.parseInt(paras.get("ismenu")):1;
		Site site=CacheFunc.getIdSites().get(Site.SITEID);
		String domain=site.get("domain");
		String order=paras.get("order");
		String limit=paras.get("limit");
		List<Category> cats=Category.dao.selectM("*","ismenu=? and parentid=?",order,"",limit,ismenu,catid);
		List<Category> rets=cats.stream().map(c->{
			String url=c.get("url");
			if(url.indexOf("://")==-1){
				c.set("url",StringUtils.substring(domain,0,-1)+url);
			}
			return c;
		}).collect(Collectors.toList());
		return rets;
	}
	
	private long count(Map<String,String> paras){
		int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
		StrBuilder where=new StrBuilder();
		if(StringUtils.isNotEmpty(paras.get("where"))){
			where.append(paras.get("where")).append(" and ");
		}
		if(catid>0){
			Record r=Category.dao.getOneR("child,arrchildid","catid=?","","",catid);
			if(r.getInt("child")==Category.CHILD_H){
				String arrchildid=r.getStr("arrchildid");
				int pos=arrchildid.indexOf(",")+1;
				arrchildid=arrchildid.substring(pos);
				where.append("catid in(").append(arrchildid).append(")");
			}else{
				where.append("catid=").append(catid);
			}
		}
		String thumb=(StringUtils.isNotEmpty(paras.get("thumb")) && !paras.get("thumb").equals("0"))?" and thumb !=''":"";
		where.append(thumb);
		long count=News.dao.getOne("count(*)",where.toString(),"","");
		return count;
	}
	
	/**
	 * 列表标签
	 * @param paras
	 */
	public List<Record> lists(Map<String,String> paras){
		int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
		StrBuilder where=new StrBuilder();
		if(StringUtils.isNotEmpty(paras.get("where"))){
			where.append(paras.get("where")).append(" and ");
		}
		if(catid>0){
			Record r=Category.dao.getOneR("child,arrchildid","catid=?","","",catid);
			if(r.getInt("child")==Category.CHILD_H){
				String arrchildid=r.getStr("arrchildid");
				int pos=arrchildid.indexOf(",")+1;
				arrchildid=arrchildid.substring(pos);
				where.append("catid in(").append(arrchildid).append(")");
			}else{
				where.append("catid=").append(catid);
			}
		}
		String thumb=(StringUtils.isNotEmpty(paras.get("thumb")) && !paras.get("thumb").equals("0"))?" and thumb !=''":"";
		where.append(thumb);
		String order=paras.get("order");
		String limit=paras.get("limit");
		List<Record> data=News.dao.selectR("*",where.toString(),order,"",limit);
		
		/*
		 * 调用副表数据
		 */
		int moreinfo=StringUtils.isNotEmpty(paras.get("moreinfo"))?Integer.parseInt(paras.get("moreinfo")):0;
		if(moreinfo==1){
			List<Integer> ids=data.stream().map(r1->r1.getInt("id")).collect(Collectors.toList());
			if(ids!=null & ids.size()>=1){
				Integer[] idsArr=ids.toArray(new Integer[ids.size()]);
				String idsStr=StringUtils.join(idsArr,",");
				List<Record> _data=NewsData.dao.selectR("*","id in("+idsStr+")","","","");
				Map<Integer,Record> dataMap=Maps.uniqueIndex(data,r2->r2.getInt("id"));
				_data.forEach(_r->dataMap.get(_r.getInt("id")).setColumns(_r));
			}
		}
		
		return data;
	}
	
	/**
	 * 推荐位标签
	 * @param paras
	 */
	public List<Record> position(Map<String,String> paras){
		int posid=Integer.parseInt(paras.get("posid"));
		String order=Strings.nullToEmpty(paras.get("order"));
		String limit=paras.get("limit");
		int thumb=(StringUtils.isEmpty(paras.get("thumb")) || paras.get("thumb").equals("0"))?0:1;
		int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
		StrBuilder where=new StrBuilder();
		if(catid>0){
			Category cat=CacheFunc.getIdCategorys().get(catid);
			
			if(cat.getInt("child")==Category.CHILD_H){
				String arrchildid=cat.getStr("arrchildid");
				int pos=arrchildid.indexOf(",")+1;
				arrchildid=arrchildid.substring(pos);
				where.append("catid in(").append(arrchildid).append(") and ");
			}else{
				where.append("catid=").append(catid).append(" and ");
			}
		}
		if(thumb==1){
			where.append("thumb=1 and ");
		}
		if(StringUtils.isNotEmpty(paras.get("where"))){
			where.append(paras.get("where")).append(" and ");
		}
		where.append("posid=").append(posid);
		List<Record> positionDatas=PositionData.dao.selectR("*",where.toString(),order,"",limit);
		List<Record> rets=positionDatas.stream().map(input->{
			String dataJson=input.getStr("data");
			Map<String, Object> dataMap=BgzKit.json2obj(dataJson);
			input.set("title",dataMap.get("title"));
			input.set("description",dataMap.get("description"));
			input.set("inputtime",Long.parseLong(""+dataMap.get("inputtime")));
			int id=input.getInt("id");
			Record r=News.dao.getOneR("thumb,url","id=?","","",id);
			input.set("thumb",r.getStr("thumb"));
			input.set("url",r.getStr("url"));
			return input;
		}).collect(Collectors.toList());
		return rets;
	}
	/**
	 * 相关文章标签
	 * @param paras
	 * @return
	 */
	public List<Record> relation(Map<String,String> paras){
		String order=paras.get("order");
		String limit=paras.get("limit");
		
		String relation=StringUtils.replace(paras.get("relation"),"|",",");
		if(StringUtils.isNotEmpty(relation)){
			String where="id in("+relation+")";
			int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
			where+=(catid==0?"":" and catid="+catid);
			List<Record> data=News.dao.selectR("*",where,order,"",limit);
			return data;
		}else{
			return Lists.newArrayList();
		}
	}
	/**
	 * 点击排行标签
	 * @param paras
	 * @return
	 */
	public List<Record> hits(Map<String,String> paras){
		String order=paras.get("order");
		String limit=paras.get("limit");
		
		int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
		Category cat=CacheFunc.getIdCategorys().get(catid);
		String hitsid="";
		if(cat==null){
			hitsid="'c-%'";
		}else{
			int modelid=cat.getInt("modelid");
			hitsid="'c-"+modelid+"-%'";
		}
		
		String where="hitsid LIKE "+hitsid;
		
		if(StringUtils.isNotEmpty(paras.get("day"))){
			long updatetime=BgzKit.genUnixTstamp()-Long.parseLong(paras.get("day"))*86400;
			where+=" AND updatetime>"+updatetime;
		}
		if(catid>0){
			if(cat.getInt("child")==Category.CHILD_H){
				String arrchildid=cat.getStr("arrchildid");
				arrchildid=Stream.of(StringUtils.split(arrchildid,",")).skip(1).collect(Collectors.joining(","));
				where+=" AND catid IN("+arrchildid+")";
			}else if(cat.getInt("child")==Category.CHILD_NH){
				where+=" AND catid="+catid;
			}
		}
		
		List<Record> hits=Hits.dao.selectR("*", where, order, "", limit);
		
		if(hits==null || hits.size()==0){
			return Lists.newLinkedList();
		}
		
		Map<String,Record> idHits=Maps.uniqueIndex(hits,r->(StringUtils.split(r.getStr("hitsid"),"-")[2]));
		
		String ids=idHits.keySet().stream().collect(Collectors.joining(","));
		List<Record> newses=News.dao.selectR("*", "id IN("+ids+")","","","");
		Map<String,Record> idNewses=Maps.uniqueIndex(newses,r->r.getInt("id").toString());
		
		List<Record> rets=idHits.entrySet().stream().map(e->{
			Record r=idNewses.get(e.getKey());
			e.getValue().setColumns(r);
			return e.getValue();
		}).collect(Collectors.toList());
		
		return rets;
	}
	
	/**
	 * 单页标签
	 * @param paras
	 * @return
	 */
	public Record page(Map<String,String> paras){
		int catid=StringUtils.isNotEmpty(paras.get("catid"))?Integer.parseInt(paras.get("catid")):0;
		Page page=Page.dao.findById(catid);
		Record ret=page.toRecord();
		return ret;
	}
}