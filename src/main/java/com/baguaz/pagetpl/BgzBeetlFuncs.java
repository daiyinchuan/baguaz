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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.beetl.ext.web.SessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.CacheFunc;
import com.baguaz.module.category.Category;
import com.baguaz.module.member.Member;
import com.baguaz.module.site.Site;
import com.google.common.collect.Maps;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;

/**
 * bc函数
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class BgzBeetlFuncs{
	private static final Logger log=LoggerFactory
			.getLogger(BgzBeetlFuncs.class);
	
	/**
	 * unix时间戳转Date
	 * @param paras
	 * @return
	 */
	public Date unixTstamp(Object[] paras){
		if(paras[0] instanceof Long){
			return new Date(BgzKit.s2ms((long)paras[0]));
		}
		throw new RuntimeException("Parse date Error,Args Long ");
	}
	/**
	 * 主要用于int转String
	 * @param paras
	 * @return
	 */
	public String s(Object[] paras){
		return paras[0].toString();
	}
	/**
	 * 获取baguaz cms的版本号
	 * @param paras
	 * @return
	 */
	public String get_bgz_version(){
		return BgzKit.getBgzVersion("v");
	}
	
	public String oneOrTwo(Object[] paras){
		String p1=(String)paras[0];
		String p2=(String)paras[1];
		String ret=BgzKit.oneOrTwo(p1, p2);
		return ret;
	}
	
	/**
	 * 获取站点url
	 * @param paras
	 * @return
	 */
	public String siteurl(){
		Map<Integer, Site> idSites=CacheFunc.getIdSites();
		String domain=idSites.get(Site.SITEID).getStr("domain");
		return domain;
	}
	
	/**
	 * 获取社会化分享代码
	 * @param paras
	 * @return
	 */
	public String social_share_code(){
		Map<String,Object> settings=CacheFunc.getSiteSettings();
		return (String)settings.get("social_share_code");
	}
	
	/**
	 * 获取社会化评论代码
	 * @param paras
	 * @return
	 */
	public String social_comment_code(){
		Map<String,Object> settings=CacheFunc.getSiteSettings();
		return (String)settings.get("social_comment_code");
	}
	
	/**
	 * 获取社会化推荐代码
	 * @param paras
	 * @return
	 */
	public String social_tuijian_code(){
		Map<String,Object> settings=CacheFunc.getSiteSettings();
		return (String)settings.get("social_tuijian_code");
	}
	
	/**
	 * 获取网站流量统计代码，放置到每个页面head结束标签之前
	 * @param paras
	 * @return
	 */
	public String pvetc_stati_code(){
		Map<String,Object> settings=CacheFunc.getSiteSettings();
		return (String)settings.get("pvetc_stati_code");
	}
	
	/**
	 * 过滤html标签
	 * @param paras
	 * @return
	 */
	public String strip_tags(Object[] paras){
		return BgzKit.stripTags((String)paras[0]);
	}
	/**
	 * 截取文本
	 * @param paras
	 * @return
	 */
	public String str_cut(Object[] paras){
		String txt=(String)paras[0];
		int sc=(int)paras[1];
		String ellip="";
		if(paras.length>2 && StringUtils.isNotEmpty((String)paras[2])){
			ellip=(String)paras[2];
		}
		return BgzKit.strCut(txt, sc, ellip);
	}
	/**
	 * unix时间戳格式化成指定格式的日期字符串
	 * @param paras
	 * @return
	 */
	public String datefm(Object[] paras){
		String fmTxt=paras.length>1?(String)paras[1]:"yyyy-MM-dd HH:mm:ss";
		String fmedDatetime=DateFormatUtils.format(BgzKit.s2ms((long)paras[0]),fmTxt);
		return fmedDatetime;
	}
	
	/**
	 * unix时间戳转换为人性化显示的日期字符串
	 * @param paras
	 * @return
	 */
	public String datehuman(Object[] paras){
		/*
		 * 60秒之前=片刻之前
		 * 1分钟～1小时之内=**分钟前
		 * 1小时～24小时之内=**小时前
		 * 1天～7天之内=**天前
		 * 7天～14天之内=1周前
		 * 14天～21天之内=2周前
		 * 21天～28天之内=3周前
		 * 1个月～12个月=**个月前
		 * 大于1年以上=*年前
		 */
		long interval=BgzKit.genUnixTstamp()-(long)paras[0];
		String ret="";
		if(interval<=60){
			ret="片刻之前";
		}else if(interval<=3600){
			ret=(interval/60)+"分钟前";
		}else if(interval<=3600*24){
			ret=(interval/3600)+"小时前";
		}else if(interval<=3600*24*7){
			ret=(interval/(3600*24))+"天前";
		}else if(interval<=3600*24*14){
			ret="1周前";
		}else if(interval<=3600*24*21){
			ret="2周前";
		}else if(interval<=3600*24*28){
			ret="3周前";
		}else if(interval<=3600*24*365){
			ret=(interval/(3600*24*30))+"个月前";
		}else{
			ret=(interval/(3600*24*365))+"年前";
		}
		return ret;
	}
	
	/**
	 * 当前位置
	 * @param paras
	 * @return
	 */
	public String catpos(Object[] paras){
		//演示栏目 > 演示子栏目 > 演示孙子栏目 >
		int catid=(int)paras[0];
		String cacheKey="catpos-"+catid;
		log.debug("cacheKey="+cacheKey);
		String data=CacheFunc.getKeyPage(cacheKey,new Callable<String>(){
			@Override
			public String call() throws Exception {
				Map<Integer,Category> idCategorys=CacheFunc.getIdCategorys();
				String arrparentid=idCategorys.get(catid).getStr("arrparentid");
				String arrpamid=arrparentid+","+catid;
				String[] arrpamidArr=StringUtils.split(arrpamid,",");
				arrpamidArr=ArrayUtils.remove(arrpamidArr,0);
				arrpamid=StringUtils.join(arrpamidArr,",");
				List<Record> cats=Category.dao.selectR("catid,catname,url","catid in ("+arrpamid+")","","","");
				Map<Integer,Record> _idCats=Maps.uniqueIndex(cats,r->r.getInt("catid"));
				StrBuilder sb=new StrBuilder();
				Stream.of(arrpamidArr).forEach(pamid->{
					Record r=_idCats.get(Integer.parseInt(pamid));
					sb.append("<a href=\""+r.getStr("url")+"\">")
					  .append(r.getStr("catname"))
					  .append("</a>").append(" > ");
				});
				return sb.toString();
			}
		});
		return data;
	}
	/**
	 * 
	 * @param paras src,width,height,是否保持缩放比例（默认保持）
	 * @return
	 */
	public String thumb(Object[] paras){
		String src=(String)paras[0];
		int width=(int)paras[1];
		int height=(int)paras[2];
		boolean isKeep=true;
		if(paras.length>3&&!(boolean)paras[3]){
			isKeep=false;
		}
		
		//根据src推导出缩略图路径
		String td="";
		if(width<0 && height<0){
			return src;
		}else if(width<0){
			td="_"+height;
		}else if(height<0){
			td=width+"_";
		}else{//都大于0
			td=width+"_"+height;
		}
		if(!isKeep){
			td+="_";
		}
		String fs=File.separator;
		String fsrc="";
		if(src.startsWith("/")){
			fsrc=PathKit.getWebRootPath()+src;
		}
		String name=new File(fsrc).getName();
		String thumbrp=BgzKit.getBgzProp("upload_path")+fs+"thumb";
		String thumbup=BgzKit.getBgzProp("UPLOAD_URL")+"/thumb";
		String fdest=thumbrp+fs+td+fs+name;
		String dest=thumbup+"/"+td+"/"+name;
		
		/*
		 * 判断缩略图是否已存在,存在直接返回
		 */
		if(Files.exists(Paths.get(fdest))){
			return dest;
		}
		/*
		 * 不存在则生成缩略图再返回
		 */
		try {
			Builder<File> builder=Thumbnails.of(fsrc);
			if(width<0 && height<0){
				return src;
			}else if(width<0){
				builder.height(height);
			}else if(height<0){
				builder.width(width);
			}else{//都大于0
				builder.size(width,height);
			}
			builder.keepAspectRatio(isKeep);
			new File(fdest).getParentFile().mkdirs();
			builder.toFile(fdest);
			return dest;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return src;
	}
	
	/**
	 * 
	 * @param paras parentid,type
	 * @return
	 */
	public List<Record> subcat(Object[] paras){
		int parentid=(int)paras[0];
		int type=paras[1]!=null?(int)paras[1]:-1;
		String cacheKey="subcat-"+parentid+"-"+type;
		log.debug("cacheKey="+cacheKey);
		List<Record> data=CacheFunc.getKeyPage(cacheKey,new Callable<List<Record>>(){
			@Override
			public List<Record> call() throws Exception {
				String where="parentid=?";
				if(type>=0){
					where+=" and type="+type;
				}
				List<Record> ret=Category.dao.selectR("*",where,"listorder","","",parentid);
				return ret;
			}
		});
		return data;
	}
	
	public Map<Integer,Map<String,String>> json2obj(Object[] args){
		return BgzKit.json2obj((String)args[0]);
	}
	
	public String obj2json(Object[] args){
		return BgzKit.obj2json(args[0]);
	}
	
	public boolean isAuthor(Object[] args){
		SessionWrapper session=(SessionWrapper)args[0];
		Long authorid=(Long)args[1];
		Member m=(Member)session.get("member");
		return m!=null && m.getLong("id").compareTo(authorid)==0;
	}
}
