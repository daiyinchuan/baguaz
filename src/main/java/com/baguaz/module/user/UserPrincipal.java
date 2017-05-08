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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.baguaz.BgzKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class UserPrincipal implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID=9009417351220065733L;
	
	// 用户对象
	private User user;
	// 用户权限列表
	private Set<String> authorities=new LinkedHashSet<>();
	// 用户角色
	private Set<Role> roles=new LinkedHashSet<>();
	
	private Set<Integer> catpriv=new LinkedHashSet<>();
	
	// 是否已授权。如果已授权，则不需要再从数据库中获取权限信息，减少数据库访问
	// 这里会导致修改权限时，需要重新登录方可有效
	private boolean isAuthorized = false;
	
	/**
	 * 构造函数，参数为Admin对象 根据Admin对象属性，赋值给Principal相应的属性上
	 * 
	 * @param admin
	 */
	public UserPrincipal(User user) {
		this.user = user;
	}
	
	public User getUser(){
		return user;
	}
	public void setUser(User user){
		this.user=user;
	}
	public boolean isAuthorized(){
		return isAuthorized;
	}
	public void setAuthorized(boolean isAuthorized){
		this.isAuthorized=isAuthorized;
	}
	public String getUserName() {
		return this.user.getStr("username");
	}

	public int getUserId() {
		return this.user.getInt("userid");
	}
	public String getLastlogintime(){
		long time=this.user.getLong("lastlogintime");
		return DateFormatUtils.format(BgzKit.s2ms(time),"yyyy-MM-dd HH:mm:ss");
	}
	public String getLastloginip(){
		return this.user.getStr("lastloginip");
	}
	
	public static UserPrincipal get(){
		Subject subject=SecurityUtils.getSubject();
		UserPrincipal pp=(UserPrincipal)subject.getPrincipal();
		return pp;
	}

	public Set<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<String> authorities) {
		this.authorities = authorities;
	}
	
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	public Set<Integer> getCatpriv(){
		return catpriv;
	}
	
	public void setCatpriv(Set<Integer> catpriv){
		this.catpriv=catpriv;
	}
	
	public Set<String> getRoleNameStrSet(){
		return getRoles().stream()
						 .sorted((r1,r2)->r1.getInt("id").compareTo(r2.getInt("id")))
						 .map(r->r.getStr("name"))
						 .collect(Collectors.toSet());
	}
	/**
	 * 逗号隔开的role name字符串
	 * @return
	 */
	public String getRoleNames(){
		return roles.stream()
					.sorted((r1,r2)->r1.getInt("id").compareTo(r2.getInt("id")))
					.map(r->r.getStr("name"))
					.collect(Collectors.joining(","));
	}
	/**
	 * 逗号隔开的roleid字符串
	 * @return
	 */
	public String getRoleIds(){
		return roles.stream().map(r->r.getInt("id").toString()).collect(Collectors.joining(","));
	}
	/**
	 * 是否拥有超级管理员角色
	 * @return
	 */
	public boolean isAdmin(){
		return getRoleNameStrSet().contains(Role.ADMIN_RN);
	}
}
