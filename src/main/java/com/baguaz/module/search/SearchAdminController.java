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

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.module.content.News;
import com.baguaz.module.content.NewsData;
import com.google.common.collect.ImmutableMap;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/search",viewPath="admin/search")
public class SearchAdminController extends BaseController {
	private static final Logger log=LoggerFactory
			.getLogger(SearchAdminController.class);
	
	@RequiresPermissions("menu:search")
	public void init(){
		this.render("search_manage.html");
	}
	
	//@RequiresPermissions("search:config")
	public void config(){
		
	}
	
	@RequiresPermissions("search:deleteindex")
	public void deleteindex(){
		TransportClient client=TransportClient.builder().build();
		try {
			client.addTransportAddresses(new InetSocketTransportAddress(
					InetAddress.getByName(BgzKit.getBgzProp("search_el_ip")),
					Integer.parseInt(BgzKit.getBgzProp("search_el_port"))));
			DeleteIndexResponse rep=client.admin().indices().prepareDelete("index").get();
			log.debug("删除索引响应="+rep.toString());
		} catch (UnknownHostException e) {
			log.error("删除索引异常", e);
		}
		
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("search:createindex")
	public void createindex(){
		final TransportClient client=TransportClient.builder().build();
		try {
			client.addTransportAddresses(new InetSocketTransportAddress(
					InetAddress.getByName(BgzKit.getBgzProp("search_el_ip")),
					Integer.parseInt(BgzKit.getBgzProp("search_el_port"))));
			
			IndicesAdminClient iac=client.admin().indices();
			
			/*CreateIndexResponse cir=iac.prepareCreate("index").get();//创建索引
			log.debug("创建索引响应="+cir.toString());*/
			
			/*
			 * 构建索引和mapping
			 */
			CreateIndexResponse cimr=iac.prepareCreate("index")
				.addMapping("fulltext",BgzKit.obj2json(
					ImmutableMap.of("fulltext",ImmutableMap.of("properties",ImmutableMap.of(
						"content", ImmutableMap.of("type", "string", "boost", 8.0, "term_vector", "with_positions_offsets", "analyzer", "ik_max_word", "include_in_all", true), 
						"inputitme", ImmutableMap.of("type", "long"), 
						"title", ImmutableMap.of("type", "string", "boost", 8.0, "term_vector", "with_positions_offsets", "analyzer", "ik_max_word", "include_in_all", true), 
						"url", ImmutableMap.of("type", "string"))))))
				.get();
			log.debug("构建索引mapping="+cimr.toString());
			
			/*
			 * 插入索引数据
			 */
			long count=News.dao.getOne("count(*)","","","");
			int pageSize=5;
			int pageNum=(int)Math.ceil((double)count/pageSize);
			for(int i=1;i<=pageNum;i++){
				BulkRequestBuilder brb=client.prepareBulk();
				Page<News> page=News.dao.paginate(i,pageSize,"select id,title,url,inputtime","from "+News.dao.tn());
				page.getList().forEach(n->{
					NewsData nd=NewsData.dao.findById(n.getInt("id"));
					brb.add(client.prepareIndex("index","fulltext",""+n.getInt("id"))
								  .setSource(BgzKit.obj2json(ImmutableMap.of(
										  "title",n.getStr("title"),
										  "content",BgzKit.stripTags(nd.getStr("content")),
										  "url",n.getStr("url"),
										  "inputtime",n.getLong("inputtime")))));
				});
				brb.get();
			}
		} catch (UnknownHostException e) {
			log.error("重建索引异常", e);
		}finally{
			client.close();
		}
		Ret ret=Ret.create();
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
}
