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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.baguaz.C;
import com.baguaz.CacheFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin/user", viewPath = "admin/user")
public class UserAdminController extends BaseController{
	@RequiresPermissions("menu:user")
	public void init(){
		this.render("user_manage.html");
	}
	
	@RequiresPermissions("menu:user")
	public void list(){
		List<Record> userList=User.dao.selectR("*","","id asc","","");
		userList=userList.stream().map(u->u.set("roles",Role.dao.getRolesNameByUserid(u.getInt("id")))).collect(Collectors.toList());
		Ret ret=Ret.create().put("data",userList).setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	
	@RequiresPermissions("user:add")
	@Before(Tx.class)
	public void add(){
		if(this.isParaExists("dosubmit")){
			User user=new User();
			String password=new String(Base64.decodeBase64(this.getPara("password")),Charset.forName("UTF-8"));
			user.set("username", this.getPara("username"))
				.set("password", password)
				.set("email", this.getPara("email"))
				.set("realname", this.getPara("realname"))
				.save();
			int userid=user.getInt("id");
			Integer[] roles=this.getParaValuesToInt("roles");
			if(roles!=null && roles.length>0){
				Stream.of(this.getParaValuesToInt("roles")).forEach(
					roleid->Db.update("insert "+UserRole.dao.tn()+" values(?,?)",userid,roleid)
				);
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			List<Record> roles=Role.dao.selectR("id,name", "", "id asc", "", "");
			this.setAttr("roles", roles);
			this.render("user_add.html");
		}
	}
	
	@RequiresPermissions("user:edit")
	@Before(Tx.class)
	public void edit(){
		if(this.isParaExists("dosubmit")){
			int userid=this.getParaToInt("id");
			User user=User.dao.findById(userid);
			user.set("email", this.getPara("email"))
				.set("realname", this.getPara("realname"));
			
			if(!isParaBlank("password") && !isParaBlank("repassword") && getPara("password").equals(getPara("repassword"))){
				String password=new String(Base64.decodeBase64(this.getPara("password")),Charset.forName("UTF-8"));
				user.set("password",password);
			}
			user.update();
			Integer[] roles=this.getParaValuesToInt("roles");
			if(roles==null){
				roles=new Integer[0];
			}
			roles=Stream.of(roles).sorted().toArray(Integer[]::new);
			List<Integer> _roles=UserRole.dao.select("roleid", "userid=?", "roleid asc", "", "", userid);
			boolean isEqual=Arrays.equals(roles, _roles.stream().toArray(Integer[]::new));
			if(!isEqual){
				Db.update("delete from "+UserRole.dao.tn()+" where userid=?",userid);
				List<String> sqlList=new ArrayList<>();
				Stream.of(roles).forEach(
					roleid->sqlList.add("insert "+UserRole.dao.tn()+" values("+userid+","+roleid+")")
				);
				Db.batch(sqlList,5);
			}
			Ret ret=Ret.create().setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Record user=User.dao.findById(this.getPara("id")).toRecord();
			List<Integer> roleids=UserRole.dao.select("roleid", "userid=?", "", "", "", user.getInt("id"));
			user.set("roles", roleids);
			this.setAttr("user",user);
			List<Record> roles=Role.dao.selectR("id,name", "", "", "", "");
			this.setAttr("roles", roles);
			this.render("user_edit.html");
		}
	}
	
	@RequiresPermissions("user:del")
	@Before(Tx.class)
	public void del(){
		int userid=this.getParaToInt("id");
		Ret ret=Ret.create();
		if(userid==User.ADMIN_ID){
			ret.setCodeAndMsg(C.nodel);
		}else{
			Db.update("delete from "+UserRole.dao.tn()+" where userid=?",userid);
			User.dao.deleteById(userid);
			ret.setCodeAndMsg(C.success);
		}
		ret.setRdurl("/admin/user/init");
		this.setAttr("ret", ret).render("/admin/retmsg.html");
	}
	
	@RequiresPermissions("menu:user")
	public void checkUsername(){
		String username=this.getPara("username");
		List<Integer> userid=User.dao.select("id", "username=?", "", "", "", username);
		Ret ret=Ret.create();
		if(userid.size()>=1){
			ret.put("error","用户名已存在");
		}else{
			ret.put("ok","");
		}
		this.renderJson(ret.getMap());
	}
	
	@RequiresPermissions("user:editPwd")
	public void editPwd(){
		if(this.isParaExists("dosubmit")) {
			User user=UserPrincipal.get().getUser();
			Ret ret=Ret.create();
			String oldpwd=this.getPara("oldpwd");
			oldpwd=new String(Base64.decodeBase64(oldpwd),Charset.forName("UTF-8"));
			if(!oldpwd.equals(user.getStr("password"))){
				ret.setCodeAndMsg(C.err_oldPassword);
				this.renderJson(ret);
				return;
			}
			if(!this.getPara("password").equals(this.getPara("renewpwd"))){
				ret.setCodeAndMsg(C.err_renewPassword);
				this.renderJson(ret);
				return;
			}
			String password=new String(Base64.decodeBase64(this.getPara("password")),Charset.forName("UTF-8"));
			user.set("password",password);
			if(user.update()){
				ret.setCodeAndMsg(C.success);
				CacheFunc.clearMyCachedAuth();
			}else{
				ret.setCodeAndMsg(C.fail);
			}
			this.renderJson(ret);
		}else{
			this.setAttrs(UserPrincipal.get().getUser().toRecord().getColumns());
			this.render("user_edit_pwd.html");
		}
	}
	
	@RequiresPermissions("user:editInfo")
	public void editInfo(){
		if(this.isParaExists("dosubmit")) {
			User user=UserPrincipal.get().getUser();
			user.set("realname",this.getPara("realname"));
			user.set("email",this.getPara("email"));
			Ret ret=Ret.create();
			if(user.update()){
				ret.setCodeAndMsg(C.success);
				CacheFunc.clearMyCachedAuth();
			}else{
				ret.setCodeAndMsg(C.fail);
			}
			this.renderJson(ret);
		}else{
			this.setAttrs(UserPrincipal.get().getUser().toRecord().getColumns());
			this.render("user_edit_info.html");
		}
	}
}