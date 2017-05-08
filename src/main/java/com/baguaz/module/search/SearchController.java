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

package com.baguaz.module.search;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.common.BaseController;
import com.baguaz.module.site.Site;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.ext.route.ControllerBind;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/search")
public class SearchController extends BaseController {
	private static final Logger log=LoggerFactory
			.getLogger(SearchController.class);
	
	private ObjectPool<Client> clientPool;
	
	private int pageSize=10;
	
	public void index(){
		this.setAttr("q", this.getPara("q"));
		Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("title",this.getPara("q")+" - 搜索结果"));
		this.setAttr("SEO",seo);
		
		Client client=null;
		try {
			client = clientPool().borrowObject();
			int from=this.getFrom();
			SearchResponse response=this.search(client,from);
			
			SearchHits shs=response.getHits();
			long total=shs.getTotalHits();
			if(total>0 && shs.getHits().length==0){
				from=(int)(total/pageSize)*pageSize;
				response=this.search(client,from);
				shs=response.getHits();
			}
			
			List<Map<String,String>> srList=new ArrayList<>();
			shs.forEach(sh->{
				Map<String,String> map=new HashMap<>();
				Map<String,Object> sMap=sh.getSource();
				map.put("url",(String)sMap.get("url"));
				map.put("id", sh.getId());
				Map<String,HighlightField> hfMap=sh.getHighlightFields();
				if(hfMap.containsKey("title")){
					String title=hfMap.get("title").getFragments()[0].string();
					map.put("title", title);
				}else{
					map.put("title", (String)sMap.get("title"));
				}
				if(hfMap.containsKey("content")){
					String content=hfMap.get("content").getFragments()[0].string();
					map.put("content",content+"...");
				}
				srList.add(map);
			});
			this.setAttr("search_result", srList);
			
			long took=response.getTookInMillis();
			float fTook=(float)took/1000;
			
			this.setAttr("search_tt", "获得约 "+total+" 条结果 （用时"+fTook+" 秒）");
			
			String pages=pages(total,from,pageSize,"search/?q=${q}&f=${f}",
					ImmutableMap.of("q", this.getPara("q"), "f", from));
			this.setAttr("pages", pages);
		} catch (UnknownHostException e) {
			log.error("搜索未知主机异常", e);
		} catch (NoSuchElementException e) {
			log.error("搜索异常", e);
		} catch (IllegalStateException e) {
			log.error("搜索异常", e);
		} catch (Exception e) {
			log.error("搜索异常", e);
		}finally{
			try {
				clientPool.returnObject(client);
			} catch (Exception e) {
			}
		}
		
		String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
		this.render(this.template(style, "search", "list.html"));
	}
	
	private SearchResponse search(Client client,int from){
		SearchResponse response=client.prepareSearch("index")
				.setQuery(QueryBuilders.queryStringQuery(this.getPara("q")).field("title").field("content"))
				.setFetchSource(null,"content")
				.addHighlightedField("title")
				.addHighlightedField("content")
				.setHighlighterPreTags("<em class=\"kw\">")
				.setHighlighterPostTags("</em>")
				.setHighlighterNumOfFragments(1)
				.setSize(pageSize)
				.setFrom(from)
				.get();
		return response;
	}
	
	private int getFrom(){
		if(this.isParaBlank("f")){
			return 0;
		}
		String from=this.getPara("f");
		int ret=0;
		String[] aStrM=BgzKit.regMatchAll("/^(\\d+)/i",(String)from);
		if(aStrM.length>0){
			String strM=aStrM[0];
			if(StringUtils.isNotEmpty(strM)){
				ret=Integer.parseInt(strM);
				if(ret>=760){
					ret=0;
				}
			}
		}
		return ret;
	}
	
	/**
	 * 分页
	 * @param total 数据量总数
	 * @param from 分页开始位置
	 * @param pagesize 分页大小
	 * @param urlrule url规则
	 * @param urlparam url规则参数值
	 * @return
	 */
	private String pages(long total,int from,int pagesize,String urlrule,Map<String,Object> urlparam){
		StrBuilder multipage=new StrBuilder();
		if(total>pagesize){
			
			int pages=(int)Math.ceil((double)total/pagesize);//总页数
			
			int curpage=1;
			if(from>0){
				curpage=(int)Math.ceil((double)from/pagesize)+1;//所在页数
			}
			
			int start,end;
			if(curpage<7){
				start=1;
				if(pages>10){
					end=10;
				}else{
					end=pages;
				}
			}else if(curpage>=7 && pages-curpage>5){
				start=curpage-5;
				end=curpage+5;
			}else{
				start=pages-10;
				end=pages;
			}
			
			if(curpage>1){
				multipage.append(" <a href=\"").append(pageurl(urlrule,from-pagesize,urlparam)).append("\"><上一页</a>");
			}
			
			for(int i=start;i<=end;i++){
				int f=(i-1)*pagesize;
				if(from==f){
					multipage.append(" <span>"+i+"</span>");
				}else{
					multipage.append(" <a href=\"").append(pageurl(urlrule,f,urlparam)).append("\">"+i+"</a>");
				}
			}
			
			if(curpage<pages){
				multipage.append(" <a href=\"").append(pageurl(urlrule,from+pagesize,urlparam)).append("\">下一页></a>");
			}
		}
		
		return multipage.toString();
	}

	/**
	 * 返回分页路径
	 * @param urlrule 分页规则
	 * @param from 分页开始位置
	 * @param urlparam
	 * @return 完整的URL路径
	 */
	private String pageurl(	String urlrule,
									int from,
									Map<String,Object> urlparam){
		Map<String,Object> _urlparam=Maps.newHashMap(urlparam);
		_urlparam.put("f",from);
		urlrule=GroovyKit.j2gStr(urlrule);
		Object ret=GroovyKit.runScriptFromStr(urlrule,_urlparam);
		String retStr=Site.dao.getDomainFromCache()+ret.toString();
		return retStr;
	}
	
	private ObjectPool<Client> clientPool(){
		if(clientPool==null){
			GenericObjectPoolConfig conf=new GenericObjectPoolConfig();
			conf.setMinIdle(0);
			conf.setMaxIdle(5);
			conf.setMaxTotal(10);
			conf.setMaxWaitMillis(60000);
			clientPool=new GenericObjectPool<Client>(new ClientPooledObjectFactory(),conf);
		}
		return clientPool;
	}
	
	/**
	 * TransportClient池对象工厂
	 * @author daiyc
	 */
	private static class ClientPooledObjectFactory
		extends BasePooledObjectFactory<Client>{
		@Override
		public Client create() throws Exception {
			Client client = TransportClient.builder().build()
					.addTransportAddresses(new InetSocketTransportAddress(
							InetAddress.getByName(BgzKit.getBgzProp("search_el_ip")),
							Integer.parseInt(BgzKit.getBgzProp("search_el_port"))));
			return client;
		}
		@Override
		public PooledObject<Client> wrap(Client client) {
			return new DefaultPooledObject<Client>(client);
		}
	    @Override
	    public void destroyObject(PooledObject<Client> pooledClient)
	        throws Exception  {
	    	pooledClient.getObject().close();
	    }
	}
}
