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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.module.category.Category;
import com.baguaz.module.category.CategoryPriv;
import com.baguaz.module.category.Category.CatTreeNode;
import com.baguaz.module.user.Permission.PermTreeNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/role", viewPath = "admin/user")
public class RoleAdminController extends BaseController{
	@RequiresPermissions("menu:role")
	public void init(){
		this.render("role_manage.html");
	}
	
	@RequiresPermissions("menu:role")
	public void list(){
		List<Record> roleList=Role.dao.selectR("*","","id asc","","");
		Ret ret=Ret.create();
		ret.put("data",roleList);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("role:add")
	@Before(Tx.class)
	public void add(){
		if(this.isParaExists("dosubmit")){
			Role role=new Role();
			role.set("name", this.getPara("name"))
				.set("description", this.getPara("description"))
				.save();
			int roleid=role.getInt("id");
			Integer[] perms=this.getParaValuesToInt("perms");
			if(perms!=null && perms.length>0){
				List<String> sqlList=new ArrayList<>();
				Stream.of(this.getParaValuesToInt("perms")).forEach(
					permid->sqlList.add("insert "+RolePermission.dao.tn()+" values("+roleid+","+permid+")")
				);
				if(sqlList.size()>0){
					Db.batch(sqlList,10);
				}
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			List<Permission> permMList=Permission.dao.getAll();
			Map<String,Object> perms=PermTreeNode.buildTree(permMList,0).genPermsTree();
			String strPerms=BgzKit.obj2json(perms.get("child"));
			this.setAttr("perms", strPerms);
			this.render("role_add.html");
		}
	}
	
	@RequiresPermissions("role:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")){
			int roleid=this.getParaToInt("id");
			Role role=Role.dao.findById(roleid);
			role.set("name", this.getPara("name"))
				.set("description", this.getPara("description"))
				.update();
			Integer[] perms=this.getParaValuesToInt("perms");
			perms=Stream.of(perms).sorted().toArray(Integer[]::new);
			List<Integer> _perms=RolePermission.dao.select("permissionid", "roleid=?", "permissionid asc", "", "", roleid);
			boolean isEqual=Arrays.equals(perms, _perms.stream().toArray(Integer[]::new));
			if(!isEqual){
				Db.update("delete from "+RolePermission.dao.tn()+" where roleid=?",roleid);
				List<String> sqlList=new ArrayList<>();
				Stream.of(perms).forEach(
					permid->sqlList.add("insert "+RolePermission.dao.tn()+" values("+roleid+","+permid+")")
				);
				if(sqlList.size()>0){
					Db.batch(sqlList,10);
				}
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Record role=Role.dao.findById(this.getParaToInt("id")).toRecord();
			List<Integer> permids=RolePermission.dao.select("permissionid", "roleid=?", "", "", "", role.getInt("id"));
			role.set("perms", permids);
			this.setAttr("role",role);
			
			List<Permission> permMList=Permission.dao.getAll();
			Map<String,Object> perms=PermTreeNode.buildTree(permMList,0).genPermsTree();
			String strPerms=BgzKit.obj2json(perms.get("child"));
			this.setAttr("perms", strPerms);
			this.render("role_edit.html");
		}
	}
	
	@RequiresPermissions("role:del")
	@Before(Tx.class)
	public void del(){
		int roleid=this.getParaToInt("id");
		Ret ret=Ret.create();
		if(roleid==Role.ADMIN_ID){
			ret.setCodeAndMsg(C.nodel);
		}else{
			Db.update("delete from "+RolePermission.dao.tn()+" where roleid=?",roleid);
			Db.update("delete from "+UserRole.dao.tn()+" where roleid=?",roleid);
			Role.dao.deleteById(roleid);
			ret.setCodeAndMsg(C.success);
		}
		ret.setRdurl("/admin/role/init");
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
	
	@RequiresPermissions("role:catpriv")
	@Before(Tx.class)
	public void catpriv(){
		if(this.isParaExists("dosubmit")){
			int roleid=this.getParaToInt("id");
			Integer[] catids=this.getParaValuesToInt("catids");
			if(catids==null){
				catids=new Integer[0];
			}
			catids=Stream.of(catids).sorted().toArray(Integer[]::new);
			List<Integer> _catids=CategoryPriv.dao.select("catid", "roleid=?", "catid asc", "", "", roleid);
			boolean isEqual=Arrays.equals(catids, _catids.stream().toArray(Integer[]::new));
			if(!isEqual){
				Db.update("delete from "+CategoryPriv.dao.tn()+" where roleid=?",roleid);
				List<String> sqlList=new ArrayList<>();
				Stream.of(catids).forEach(
					catid->sqlList.add("insert "+CategoryPriv.dao.tn()+" values("+catid+","+roleid+")")
				);
				if(sqlList.size()>0){
					Db.batch(sqlList,10);
				}
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Record role=Role.dao.findById(this.getParaToInt("id")).toRecord();
			this.setAttr("role",role);
			
			int parentid=0;
			String arrchildid=Category.dao.getArrchildid(parentid);
			if(!Strings.isNullOrEmpty(arrchildid)){
				String wherein=parentid+","+arrchildid;
				List<Record> catList=Category.dao.selectR("catid,catname,parentid,listorder","catid in("+wherein+") and type!=2","parentid,listorder","","");
				List<Map<String,Object>> catlist=CatTreeNode.buildTree(catList,parentid).toCatPrivList();
				this.setAttr("cats",catlist);
			}else{//空数据
				this.setAttr("cats",ImmutableList.of());
			}
			
			List<String> roleCatpriv=CategoryPriv.dao.select("catid", "roleid=?", "", "", "",role.getInt("id"));
			this.setAttr("roleCatpriv",roleCatpriv);
			
			this.render("role_catpriv.html");
		}
	}
	
	@RequiresPermissions("menu:role")
	public void checkRolename(){
		String rolename=this.getPara("name");
		int id=this.getParaToInt("id",0);
		List<Integer> roleid=Role.dao.select("id", "name=? and id not in(?)", "", "", "",rolename,id);
		Ret ret=Ret.create();
		if(roleid.size()>=1){
			ret.put("error","角色名已存在");
		}else{
			ret.put("ok","");
		}
		this.renderJson(ret.getMap());
	}
}
