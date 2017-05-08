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

package com.baguaz.module.index;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig.G;
import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.module.user.Permission;
import com.baguaz.module.user.Permission.PermTreeNode;
import com.baguaz.module.user.Role;
import com.baguaz.module.user.User;
import com.baguaz.module.user.UserPrincipal;
import com.google.common.collect.Lists;
import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/admin", viewPath = "admin")
public class IndexAdminController extends BaseController{
	private static final Logger log=LoggerFactory
			.getLogger(IndexAdminController.class);
	public static final String TOKEN_NAME="bc_token_daiyc";
	public void login(){
		if(this.isParaExists("dosubmit")) {
			Ret ret=Ret.create();
			if(!this.validateToken(TOKEN_NAME)){
				ret.setCodeAndMsg(C.invalid_token);
				this.renderJson(ret);
				return;
			}
			String username=this.getPara("username");
			String password=this.getPara("password");
			password=new String(Base64.decodeBase64(password),Charset.forName("UTF-8"));
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			try{
				this.setSessionAttr(TOKEN_NAME, getPara(TOKEN_NAME));
				SecurityUtils.getSubject().login(token);
				ret.setCodeAndMsg(C.success);
				this.renderJson(ret);
			}catch(AuthenticationException ae){
				ret.setCodeAndMsg(C.err_usernameOrPassword);
				this.renderJson(ret);
			}
		}else{
			this.createToken(TOKEN_NAME);
			this.setAttr("adminSpeedLogin", BgzKit.getBgzProp().getBoolean("adminSpeedLogin"));
			this.render("login.html");
		}
	}
	
	public void logout(){
		//SecurityUtils.getSubject().logout();//调用过了session.stop()
		//this.getSession().invalidate();
		//this.redirect("/admin/index/login");
	}
	public void publicIndex(){
		User user=UserPrincipal.get().getUser();
		String realip=this.getRequest().getHeader("X-Real-IP");
		if(StringUtils.isEmpty(realip)){
			realip=this.getRequest().getRemoteAddr();
		}
		user.set("lastloginip",realip);//this.getRequest().getRemoteAddr()
		user.set("lastlogintime",BgzKit.genUnixTstamp());
		user.update();
		this.setSessionAttr(G.SESSION_USER,UserPrincipal.get());
		SecurityUtils.getSubject().hasRole(Role.ADMIN_RN);
		this.render("index.html");
	}
	
	public void main(){
		this.render("main.html");
	}
	
	public void unauthorized(){
		this.render("access_denied.html");
	}
	
	public void sideMenu(){
		List<Permission> perms=null;
		if(SecurityUtils.getSubject().hasRole(Role.ADMIN_RN)){
			perms=Permission.dao.getAll();
		}else{
			List<Record> rolesPerms=Permission.dao.getAllByRoleid(UserPrincipal.get().getRoleIds());
			perms=rolesPerms.stream().map(r->new Permission().put(r)).collect(Collectors.toList());
		}
		List<Map<String,Object>> sideMenu=null;
		if(perms.size()>0){
			sideMenu=PermTreeNode.buildTree(perms, 0).genSideMenu();
		}else{
			sideMenu=Lists.newArrayList();
		}
		if(log.isDebugEnabled()){
			log.debug("sideMenu="+BgzKit.obj2json(sideMenu,true));
		}
		
		Ret ret=Ret.create();
		ret.put("sideMenu",sideMenu);
		ret.setCodeAndMsg(C.success);
		this.renderJson(ret);
	}
	@Clear
	public void checkSession(){
		Ret ret=Ret.create();
		if(UserPrincipal.get()==null){
			ret.setCodeAndMsg(C.invalid_session);
		}else{
			ret.setCodeAndMsg(C.success);
		}
		this.renderJson(ret);
	}
}
