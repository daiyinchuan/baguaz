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

package com.baguaz.module.user;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.baguaz.BgzKit;
import com.baguaz.common.BaseModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * id			mediumint(6)	NO	PRI		</br>
 * name			varchar(50)	NO			</br>
 * value		varchar(50)	NO			</br>
 * description	varchar(50)	YES			</br>
 * pid			mediumint(6)	NO			</br>
 * listorder	smallint(5)	YES			</br>
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Permission extends BaseModel<Permission> {
	private static final long serialVersionUID = -8765603892516850145L;
	
	public static final Permission dao=new Permission();
	
	public List<String> getAllPermsName(){
		return this.select("value", "", "", "", "");
	}
	
	public List<String> getPermsNameByRoleids(String roleids){
		if(StringUtils.isEmpty(roleids)){
			return Lists.newArrayList();
		}
		return Db.query("SELECT p.value FROM "+Permission.dao.tn()+" p LEFT JOIN "+RolePermission.dao.tn()+" rp ON p.id=rp.permissionid WHERE rp.roleid in("+roleids+")");
	}
	
	public List<Permission> getAll(){
		return this.selectM("*", "", "pid,listorder", "", "");
	}
	
	public List<Record> getAllByRoleid(String roleids){
		if(StringUtils.isEmpty(roleids)){
			return Lists.newArrayList();
		}
		return Db.find("SELECT p.* FROM "+Permission.dao.tn()+" p LEFT JOIN "+RolePermission.dao.tn()+" rp ON p.id=rp.permissionid WHERE rp.roleid in("+roleids+") ORDER BY p.pid,p.listorder");
	}
	
	public static class PermTreeNode{
		private int id;
		private Permission node;
		private PermTreeNode parent;
		private List<PermTreeNode> child=new LinkedList<>();
		
		public PermTreeNode(int id) {
			this.id=id;
		}
		public PermTreeNode(Permission perm) {
			this.node=perm;
			this.id=node.getInt("id");
		}
		public PermTreeNode(Permission perm, PermTreeNode parent) {
			this.node=perm;
			this.id=node.getInt("id");
			this.parent=parent;
		}
		private void addChild(PermTreeNode perm) {
			this.child.add(perm);
		}
		private PermTreeNode findParent(int pid) {
			if(id==pid){
				return this;
			}
			for(PermTreeNode perm:child){
				PermTreeNode st=perm.findParent(pid);
				if(st==null){
					continue;
				}
				return st;
			}
			return null;
		}
		public static PermTreeNode buildTree(List<Permission> permList,int parentid){
			PermTreeNode root=null;
			LinkedList<Permission> _permList=new LinkedList<>();
			_permList.addAll(permList);
			while(!_permList.isEmpty()){
				Permission perm=_permList.removeFirst();
				if(root==null){
					if(parentid==0){
						root=new PermTreeNode(parentid);
					}else{
						root=new PermTreeNode(perm);
					}
				}
				PermTreeNode _parent=root.findParent(perm.getInt("pid"));
				if(_parent!=null){
					_parent.addChild(new PermTreeNode(perm,_parent));
				}else{
					_permList.addLast(perm);
				}
			}
			root.sorted();
			return root;
		}
		/*
		 * 递归对树的每个子节点进行排序
		 */
		private void sorted(){
			child=child.stream().sorted((a,b)->a.node.getInt("listorder")-b.node.getInt("listorder")).collect(Collectors.toList());
			child.forEach(n->n.sorted());
		}
		
		public List<Map<String,Object>> genSideMenu() {
			List<Map<String,Object>> smList=new LinkedList<>();
			for(PermTreeNode perm:child){
				Map<String,Object> map=new LinkedHashMap<>();
				String name=perm.node.getStr("name");
				String url=perm.node.getStr("url");
				if(perm.parent.id==0){
					map.put("h3",name);
				}else{
					if(StringUtils.isEmpty(url)){
						map.put("span",name);
					}else{
						map.put("a",name);
						map.put("href",url);
					}
				}
				if(perm.child.size()>0){
					map.put("ul",perm.genSideMenu());
				}
				smList.add(map);
			}
			return smList;
		}
		public Map<String,Object> genPermsTree() {
			Map<String,Object> map=new LinkedHashMap<>();
			if(id!=0){
				map.put("title",node.getStr("name"));
				map.put("attr",ImmutableMap.of("id",id));
			}
			if(child.size()>=1){
				map.put("type","folder");
				List<Map<String,Object>> childList=new LinkedList<>();
				child.forEach(ptn->childList.add(ptn.genPermsTree()));
				map.put("child",childList);
			}else{
				map.put("type","item");
			}
			return map;
		}
		public String toString(){
			StringBuilder sb=new StringBuilder();
			sb.append("{")
			  .append("id:").append(this.id).append(",");
		
			if(node!=null){
				sb.append("name:\"").append(node.getStr("name")).append("\",");
			}
			  
			sb.append("child:").append("[");
			for(Iterator<PermTreeNode> it=child.iterator();it.hasNext();){
				sb.append(it.next().toString());
			}
			sb.append("]");
			sb.append("}");
			Map<String,Object> map=BgzKit.json2obj(sb.toString());
			return BgzKit.obj2json(map,true);
		}
	}
}
