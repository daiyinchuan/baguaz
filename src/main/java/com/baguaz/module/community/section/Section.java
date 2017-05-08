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

package com.baguaz.module.community.section;

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
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Section extends BaseModel<Section> {
	private static final Logger log=LoggerFactory
			.getLogger(Section.class);
	
	private static final long serialVersionUID = 20804481825093031L;
	public static final Section dao=new Section();
	
	public String getArrchildid(int parentid){
		return getOne("GROUP_CONCAT(arrchildid) arrchildid","parentid=?","","",parentid);
	}
	
	public List<Section> getAll() {
		return selectM("*","","","","");
	}
	
	public static class SecTreeNode{
		private int pos;
		private Record r;
		private int id;
		private String name;
		private boolean last;
		private SecTreeNode parent;
		private List<SecTreeNode> child=new LinkedList<>();
		private static final String[] POSTAG={
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;│ ",
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ ",
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ "};
		
		public SecTreeNode(Record r,int ppos){
			this.r=r;
			this.id=r.getInt("id");
			this.name=r.getStr("name");
			this.pos=ppos+1;
		}
		public SecTreeNode(int id,String name,int ppos){
			this.id=id;
			this.name=name;
			this.pos=ppos+1;
		}
		public SecTreeNode(Record r,SecTreeNode parent){
			this.r=r;
			this.id=r.getInt("id");
			this.name=r.getStr("name");
			this.parent=parent;
			this.pos=parent.pos+1;
		}
		
		public static SecTreeNode buildTree(List<Record> cats,int parentid){
			SecTreeNode root=null;
			LinkedList<Record> _catList=new LinkedList<>();
			_catList.addAll(cats);
			while(!_catList.isEmpty()){
				Record r=_catList.removeFirst();
				if(root==null){
					if(parentid==0){
						root=new SecTreeNode(0,"",-2);
					}else{
						root=new SecTreeNode(r,-1);
					}
				}
				SecTreeNode _parent=root.findParent(r.getInt("parentid"));
				if(_parent!=null){
					_parent.addChild(new SecTreeNode(r,_parent));
				}else{
					_catList.addLast(r);
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
			map.put("id",id);
			map.put("name",name);
			if(child.size()>=1){
				List<Map<String,Object>> childList=new ArrayList<>();
				child.forEach(stn->childList.add(stn.toTestTree()));
				map.put("child",childList);
			}
			return map;
		}
		
		public SecTreeNode findParent(int parentid){
			if(id==parentid){
				return this;
			}
			for(Iterator<SecTreeNode> it=child.iterator();it.hasNext();){
				SecTreeNode st=it.next().findParent(parentid);
				if(st==null){
					continue;
				}
				return st;
			}
			return null;
		}
		
		public void addChild(SecTreeNode st){
			this.child.add(st);
		}
		
		public List<Map<String,Object>> toSecList(){
			List<Map<String,Object>> list=new ArrayList<>();
			if(id>=1){
				Map<String,Object> map=new HashMap<>();
				if(r!=null){
					map.putAll(r.getColumns());
				}
				
				map.put("isdisplay", BaseModel.getIsdisplayDesc(r.getInt("isdisplay")));
				
				map.put("pos",getPos());
				list.add(map);
			}
			int i=1;
			for(Iterator<SecTreeNode> it=child.iterator();it.hasNext();i++){
				SecTreeNode ct=it.next();
				if(i==child.size()){
					ct.last=true;
				}
				list.addAll(ct.toSecList());
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
	}
}
