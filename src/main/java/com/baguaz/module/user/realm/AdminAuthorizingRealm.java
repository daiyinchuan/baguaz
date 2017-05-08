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

package com.baguaz.module.user.realm;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.module.category.CategoryPriv;
import com.baguaz.module.index.IndexAdminController;
import com.baguaz.module.user.Permission;
import com.baguaz.module.user.Role;
import com.baguaz.module.user.User;
import com.baguaz.module.user.UserPrincipal;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class AdminAuthorizingRealm extends AuthorizingRealm{
	private static Logger log = LoggerFactory.getLogger(AdminAuthorizingRealm.class);
	
	public AdminAuthorizingRealm(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals){
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		//获取当前登录的用户名
		UserPrincipal principal = (UserPrincipal)super.getAvailablePrincipal(principals);
		String adminName = principal.getUserName();
		try {
			if(!principal.isAuthorized()) {
				//根据用户名称，获取该用户所有的权限列表
				List<Record> roleRList=Role.dao.getRolesByUserid(principal.getUser().getInt("id"));
				Set<Role> roleMSet=roleRList.stream().map(r->new Role().put(r)).collect(Collectors.toSet());
				principal.setRoles(roleMSet);
				
				List<String> authorities = null;
				if(principal.isAdmin()){
					authorities=Permission.dao.getAllPermsName();
				}else{
					authorities = Permission.dao.getPermsNameByRoleids(principal.getRoleIds());
				}
				
				principal.setAuthorities(new LinkedHashSet<>(authorities));
				
				
				/*
				 * 栏目权限授权
				 */
				if(!principal.isAdmin()){
					List<Integer> catpriv=CategoryPriv.dao.select("catid", "roleid in("+principal.getRoleIds()+")", "catid asc", "", "");
					principal.setCatpriv(new LinkedHashSet<>(catpriv));
				}

				
				principal.setAuthorized(true);
				log.debug("用户【" + adminName + "】授权初始化成功......");
				log.debug("用户【" + adminName + "】 角色为：" + principal.getRoleNameStrSet());
				
				Map<String,List<String>> aulistGroups=principal.getAuthorities().stream()
						.sorted((a,b)->a.compareTo(b))
						.collect(Collectors.groupingBy(a->a.split(":")[0]));
				TreeMap<String,List<String>> map=new TreeMap<>();
				map.putAll(aulistGroups);
				log.debug("用户【" + adminName + "】 权限列表为：\n"+
					map.values().stream()
						.map(ss->ss.stream().collect(Collectors.joining(", ","  "+ss.size()+" [","]")))
						.collect(Collectors.joining("\n","{\n","\n}"))
				);
				log.debug("用户【" + adminName + "】 栏目权限为："+principal.getCatpriv());
			}
		} catch(RuntimeException e) {
			throw new AuthorizationException("用户【" + adminName + "】授权失败");
		}
		//给当前用户设置权限
		log.debug("看看获取授权信息的调用频率...");
		info.addStringPermissions(principal.getAuthorities());
		info.addRoles(principal.getRoleNameStrSet());
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
		throws AuthenticationException{
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String username=upToken.getUsername();
		
		if (username == null) {
			log.warn("用户名不能为空");
			throw new AccountException("用户名不能为空");
		}
		User admin = null;
		try {
			admin = User.dao.getAdminByUsername(username);
			log.debug("从数据库中查询管理员【" + username + "】的用户数据");
		} catch(Exception ex) {
			log.warn("获取用户失败\n" + ex.getMessage());
		}
		if (admin == null) {
		    log.warn("用户不存在");
		    throw new UnknownAccountException("用户不存在!");
		}
/*		if(!admin.getBoolean("isAccountEnabled")) {
		    log.warn("用户被禁止使用");
		    throw new UnknownAccountException("用户被禁止使用!");
		}
		if(admin.getBoolean("isAccountLocked")){
			log.warn("用户被锁定！");
			throw new LockedAccountException("用户被用户被锁定!");
		}*/
		UserPrincipal principal=new UserPrincipal(admin);
		
		Session session=SecurityUtils.getSubject().getSession();
		String tokenV=(String)session.getAttribute(IndexAdminController.TOKEN_NAME);
		session.removeAttribute(IndexAdminController.TOKEN_NAME);
		String password=admin.getStr("password");
		password=DigestUtils.sha256Hex(password+tokenV);
		
		//AdminRoleM role=AdminRoleM.dao.findById(admin.getInt("roleid"));
		//principal.setRole(role);
		//List<String> authorities = AdminRolePrivM.dao.getAuthoritiesName(admin.getInt("roleid"));
		//principal.setAuthorities(authorities);
		//principal.setAuthorized(true);
		return new SimpleAuthenticationInfo(principal,password,getName());
	}
	
	public void clearCachedAuthenInfo(PrincipalCollection principals){
		UserPrincipal pp=(UserPrincipal)getAvailablePrincipal(principals);
		UsernamePasswordToken token = new UsernamePasswordToken(pp.getUserName(),pp.getUser().getStr("password"));
        Cache<Object, AuthenticationInfo> cache = this.getAuthenticationCache();
        if (cache != null) {
            Object key = this.getAuthenticationCacheKey(token);
            cache.remove(key);
        }
	}
	public void clearCachedAuthorInfo(PrincipalCollection principals){
		this.clearCachedAuthorizationInfo(principals);
	}
}
