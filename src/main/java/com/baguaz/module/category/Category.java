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

package com.baguaz.module.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.common.BaseModel;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Record;

/**
 * catid	smallint(5) unsigned	NO	PRI		auto_increment </br>
 * type	tinyint(2)	NO			</br>
 * modelid	smallint(5) unsigned	NO		0</br>	
 * parentid	smallint(5) unsigned	NO		0	</br>
 * arrparentid	varchar(255)	NO			</br>
 * child	tinyint(2) unsigned	NO		0	</br>
 * arrchildid	mediumtext	NO			</br>
 * catname	varchar(30)	NO			</br>
 * image	varchar(100)	NO			</br>
 * description	mediumtext	NO			</br>
 * parentdir	varchar(100)	NO			</br>
 * catdir	varchar(30)	NO			</br>
 * url	varchar(100)	NO			</br>
 * items	mediumint(8) unsigned	NO		0</br>	
 * setting	mediumtext	NO			</br>
 * listorder	smallint(5) unsigned	NO		0</br>	
 * ismenu	tinyint(2) unsigned	NO		1	</br>
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@TableBind(pkName="catid")
public class Category extends BaseModel<Category>{
	private static final Logger log=LoggerFactory
			.getLogger(Category.class);
	
	private static final long serialVersionUID=-808045087557401181L;
	
	public static final int TYPE_CAT=0;
	public static final int TYPE_PAGE=1;
	public static final int TYPE_LINK=2;
	
	public static final int ISMENU_NS=0;
	public static final int ISMENU_S=1;

	public static final Category dao=new Category();
	
	public boolean isTypeCat(){
		return this.getInt("type")==TYPE_CAT?true:false;
	}
	public boolean isTypePage(){
		return this.getInt("type")==TYPE_PAGE?true:false;
	}
	public boolean isTypeLink(){
		return this.getInt("type")==TYPE_LINK?true:false;
	}
	
	public String getTypeName(int type){
		String ret="";
		switch(type){
			case Category.TYPE_CAT:
				ret="内部栏目";
				break;
			case Category.TYPE_PAGE:
				ret="单网页";
				break;
			case Category.TYPE_LINK:
				ret="外部链接";
		}
		return ret;
	}
	
	public String getArrchildid(int parentid){
		return getOne("GROUP_CONCAT(arrchildid) arrchildid","parentid=?","","",parentid);
	}
	public List<Category> getAll() {
		return selectM("*","","","","");
	}
	
	public static class CatTreeNode{
		private int pos;
		private Record r;
		private int catid;
		private String catname;
		private boolean last;
		private CatTreeNode parent;
		private List<CatTreeNode> child=new LinkedList<>();
		private static final String[] POSTAG={
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;│ ",
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ ",
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ "};
		public CatTreeNode(Record r,int ppos){
			this.r=r;
			this.catid=r.getInt("catid");
			this.catname=r.getStr("catname");
			this.pos=ppos+1;
		}
		public CatTreeNode(int catid,String catname,int ppos){
			this.catid=catid;
			this.catname=catname;
			this.pos=ppos+1;
		}
		public CatTreeNode(Record r,CatTreeNode parent){
			this.r=r;
			this.catid=r.getInt("catid");
			this.catname=r.getStr("catname");
			this.parent=parent;
			this.pos=parent.pos+1;
		}
		public void addChild(CatTreeNode cattn){
			this.child.add(cattn);
		}
		public CatTreeNode findParent(int parentid){
			if(catid==parentid){
				return this;
			}
			for(Iterator<CatTreeNode> it=child.iterator();it.hasNext();){
				CatTreeNode cattn=it.next().findParent(parentid);
				if(cattn==null){
					continue;
				}
				return cattn;
			}
			return null;
		}
		
		public static CatTreeNode buildTree(List<Record> cats,int parentid){
			CatTreeNode root=null;
			LinkedList<Record> _cats=new LinkedList<>();
			_cats.addAll(cats);
			while(!_cats.isEmpty()){
				Record r=_cats.removeFirst();
				if(root==null){
					if(parentid==0){
						root=new CatTreeNode(0,"",-2);
					}else{
						root=new CatTreeNode(r,-1);
					}
				}
				CatTreeNode _parent=root.findParent(r.getInt("parentid"));
				if(_parent!=null){
					_parent.addChild(new CatTreeNode(r,_parent));
				}else{
					_cats.addLast(r);
				}
			}
			if(log.isDebugEnabled()){
				log.debug("排序前root="+BgzKit.obj2json(root.toTestTree(),true));
			}
			root.sorted();
			if(log.isDebugEnabled()){
				log.debug("排序后root="+BgzKit.obj2json(root.toTestTree(),true));
			}
			return root;
		}
		/*
		 * 递归对树的每个子节点进行排序
		 */
		private void sorted(){
			child=child.stream()
					.sorted((a,b)->a.r.getInt("listorder")-b.r.getInt("listorder"))
					.collect(Collectors.toList());
			child.forEach(n->n.sorted());
		}
		
		private Map<String,Object> toTestTree(){
			Map<String,Object> map=new HashMap<>();
			map.put("catid",catid);
			map.put("catname",catname);
			if(child.size()>=1){
				List<Map<String,Object>> childs=new ArrayList<>();
				child.forEach(ctn->childs.add(ctn.toTestTree()));
				map.put("child",childs);
			}
			return map;
		}
		
		public Map<String,Object> toCatTree(){
			Map<String,Object> map=new HashMap<>();
			map.put("title",catname);
			if(child.size()>=1){
				map.put("type","folder");
				List<Map<String,Object>> childs=new ArrayList<>();
				child.forEach(ctn->childs.add(ctn.toCatTree()));
				map.put("child",childs);
			}else{
				map.put("type","item");
				if(r.getInt("type")==0){
					if(r.getInt("child")==BaseModel.CHILD_H){
						return Maps.newHashMap();
					}
					map.put("attr",ImmutableMap.of("icon","am-icon-list",
					                               "href","/admin/content/list?catid="+catid));
				}else if(r.getInt("type")==1){
					map.put("attr",ImmutableMap.of("icon","am-icon-file-text-o",
					                               "href","/admin/content/add?catid="+catid));
				}
			}
			return map;
		}
		List<Map<String,Object>> toCatList(){
			List<Map<String,Object>> maps=new ArrayList<>();
			if(catid>=1){
				Map<String,Object> map=new HashMap<>();
				if(r!=null){
					map.putAll(r.getColumns());
					if(r.get("type")!=null){
						map.put("typename",Category.dao.getTypeName(r.getInt("type")));
						if(r.getInt("type")==Category.TYPE_CAT){
							map.put("modelname",com.baguaz.module.model.Model.dao.getModelNameFromCache(r.getInt("modelid")));
						}else{
							map.put("modelname","");
						}
					}
					
					map.put("ismenu", BaseModel.getIsdisplayDesc(r.getInt("ismenu")));
					
					String setting=r.getStr("setting");
					if(!Strings.isNullOrEmpty(setting)){
						Map<String,Object> setMap=BgzKit.json2obj(setting);
						int ishtml=(int)(setMap.get("ishtml"));
						String url=r.getStr("url");
						if(r.getInt("type")==Category.TYPE_CAT && ishtml==1 && url.indexOf("://")>0){
							String parentdir=r.getStr("parentdir");
							String catdir=r.getStr("catdir");
							String path="";
							int create_to_html_root=(int)(setMap.get("create_to_html_root"));
							if(Strings.isNullOrEmpty(parentdir) || create_to_html_root==1){
								path="/"+catdir+"/";
							}else{
								path="/"+parentdir+catdir+"/";
							}
							map.put("dnsbind","将域名:"+url+"\n绑定到目录\n"+path);
						}else{
							map.put("dnsbind","");
						}
						map.remove("setting");
						map.remove("parentdir");
						map.remove("catdir");
					}
				}
				map.put("catid",catid);
				map.put("catname",catname);
				map.put("pos",getPos());
				
				maps.add(map);
			}
			int i=1;
			for(Iterator<CatTreeNode> it=child.iterator();it.hasNext();i++){
				CatTreeNode ct=it.next();
				if(i==child.size()){
					ct.last=true;
				}
				maps.addAll(ct.toCatList());
			}
			return maps;
		}
		
		public List<Map<String,Object>> toCatPrivList(){
			List<Map<String,Object>> list=new ArrayList<>();
			if(catid>=1){
				Map<String,Object> map=new HashMap<>();
				if(r!=null){
					map.putAll(r.getColumns());
				}
				map.put("pos",getPos());
				list.add(map);
			}
			int i=1;
			for(Iterator<CatTreeNode> it=child.iterator();it.hasNext();i++){
				CatTreeNode ct=it.next();
				if(i==child.size()){
					ct.last=true;
				}
				list.addAll(ct.toCatPrivList());
			}
			return list;
		}
		
		private String getPos(){
			StringBuilder sb=new StringBuilder();
			if(pos>=1){
				if(last){
					sb.insert(0,POSTAG[3]);
				}else{
					sb.insert(0,POSTAG[2]);
				}
				sb.insert(0,getPosP());
			}
			return sb.toString();
		}
		private String getPosP(){
			if(parent==null || parent.pos<=0){
				return "";
			}
			StringBuilder sb=new StringBuilder();
			if(parent.last){
				sb.insert(0,POSTAG[0]);
			}else{
				sb.insert(0,POSTAG[1]);
			}
			sb.insert(0,parent.getPosP());
			return sb.toString();
		}
		public String toString(){
			StringBuilder sb=new StringBuilder();
			sb.append("{")
			  .append("catid:").append(this.catid).append(",")
			  .append("catname:\"").append(this.catname).append("\",")
			  .append("child:").append("[");
			child.forEach(ctn->sb.append(ctn.toString()));
			sb.append("]").append("}").append(",");
			return sb.toString();
		}
	}
	/**
	 * 从给定栏目的父级栏目路径向上搜索，
	 * 先找非一级栏目是栏目生成HTML的并且是生成到根目录的，就取这个栏目为topcatdir，
	 * 否则就取一级栏目为topcatdir
	 * @param catid
	 * @return
	 */
	public String getTopcatdir(Category cat) {
		if(cat.getInt("parentid")==0){
			return cat.getStr("catdir");
		}
		Map<String,Object> setMap=BgzKit.json2obj(cat.getStr("setting"));
		if((int)setMap.get("ishtml")==1
				&& cat.getInt("type")==Category.TYPE_CAT
				&& (int)setMap.get("create_to_html_root")==1){
			return cat.getStr("catdir");
		}else{
			Category pcat=this.findById(cat.getInt("parentid"));
			return getTopcatdir(pcat);
		}
	}
	public Object getCategorydir(Category cat) {
		if(cat.getInt("parentid")==0){
			return "";
		}else{
			Map<String,Object> setMap=BgzKit.json2obj(cat.getStr("setting"));
			if((int)setMap.get("ishtml")==1
					&& cat.getInt("type")==Category.TYPE_CAT
					&& (int)setMap.get("create_to_html_root")==1){
				return "";
			}else{
				Category pcat=this.findById(cat.getInt("parentid"));
				return getCategorydir(pcat)+pcat.getStr("catdir")+"/";
			}
		}
	}
}