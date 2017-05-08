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

package com.baguaz;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import com.baguaz.module.category.Category;
import com.baguaz.module.community.section.Section;
import com.baguaz.module.copyfrom.Copyfrom;
import com.baguaz.module.model.Model;
import com.baguaz.module.site.Site;
import com.baguaz.module.urlrule.Urlrule;
import com.baguaz.module.user.realm.AdminAuthorizingRealm;
import com.google.common.collect.Maps;
import com.jfinal.plugin.ehcache.CacheKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class CacheFunc{
	/*
	 * --------------------------
	 * commons
	 * key:sites,urlrules,sitesetting,menuauths,models,categorys,copyfroms
	 * ---------------------------
	 */
	private static final String COMMONS="commons";
	
	private static final String K_SITES="sites";
	private static final String K_URLRULES="urlrules";
	private static final String K_SITESETTING="sitesetting";
	private static final String K_MODELS="models";
	private static final String K_CATEGORYS="categorys";
	private static final String K_COPYFORMS="copyfroms";
	private static final String K_ID_SECTIONS = "idSections";
	private static final String K_TAB_SECTIONS = "tabSections";
	
	public static Map<Integer,Site> getIdSites(){
		return computeIfAbsent(COMMONS, K_SITES,new Callable<Map<Integer,Site>>(){
			@Override
			public Map<Integer,Site> call() throws Exception {
				return Maps.uniqueIndex(Site.dao.getAll(),s->s.getInt("siteid"));
			}
		});
	}
	
	public static void removeCommonsSites(){
		CacheKit.remove(COMMONS,K_SITES);
	}
	
	public static Map<Integer,Urlrule> getIdUrlrules(){
		return computeIfAbsent(COMMONS, K_URLRULES,new Callable<Map<Integer,Urlrule>>(){
			@Override
			public Map<Integer,Urlrule> call() throws Exception {
				List<Urlrule> urList=Urlrule.dao.getAll();
				return Maps.uniqueIndex(urList,ur->ur.getInt("id"));
			}
		});
	}
	
	public static void removeCommonsUrlrules(){
		CacheKit.remove(COMMONS,K_URLRULES);
	}
	
	public static Map<String,Object> getSiteSettings(){
		return computeIfAbsent(COMMONS, K_SITESETTING,new Callable<Map<String,Object>>(){
			@Override
			public Map<String, Object> call() throws Exception {
				String settingStr=getIdSites().get(Site.SITEID).getStr("setting");
				return BgzKit.json2obj(settingStr);
			}
		});
	}
	
	public static void removeCommonsSiteSetting(){
		CacheKit.remove(COMMONS,K_SITESETTING);
	}
	
	public static Map<Integer,Model> getIdModels(){
		return computeIfAbsent(COMMONS, K_MODELS,new Callable<Map<Integer,Model>>(){
			@Override
			public Map<Integer, Model> call() throws Exception {
				List<Model> mList=Model.dao.selectAll();
				return Maps.uniqueIndex(mList,m->m.getInt("id"));
			}
		});
	}
	
	public static void removeCommonsModels(){
		CacheKit.remove(COMMONS,K_MODELS);
	}
	
	public static AtomicBoolean catmenuReInit=new AtomicBoolean(true);
	
	public static Map<Integer,Category> getIdCategorys(){
		return computeIfAbsent(COMMONS, K_CATEGORYS,new Callable<Map<Integer,Category>>(){
			@Override
			public Map<Integer, Category> call() throws Exception {
				List<Category> catList=Category.dao.getAll();
				catmenuReInit.set(true);
				return Maps.uniqueIndex(catList,c->c.getInt("catid"));
			}
		});
	}
	
	public static void removeCommonsCategorys(){
		CacheKit.remove(COMMONS,K_CATEGORYS);
	}
	
	public static Map<Integer,Copyfrom> getIdCopyfroms(){
		return computeIfAbsent(COMMONS, K_COPYFORMS,new Callable<Map<Integer,Copyfrom>>(){
			@Override
			public Map<Integer, Copyfrom> call() throws Exception {
				List<Copyfrom> cfList=Copyfrom.dao.getAll();
				return Maps.uniqueIndex(cfList,cf->cf.getInt("id"));
			}
		});
	}
	
	public static void removeCommonsCopyfroms(){
		CacheKit.remove(COMMONS,K_COPYFORMS);
	}
	
	public static Map<Integer,Section> getIdSections(){
		return computeIfAbsent(COMMONS, K_ID_SECTIONS,new Callable<Map<Integer,Section>>(){
			@Override
			public Map<Integer, Section> call() throws Exception {
				List<Section> secList=Section.dao.getAll();
				return Maps.uniqueIndex(secList,c->c.getInt("id"));
			}
		});
	}
	
	public static void removeCommonsIdSections(){
		CacheKit.remove(COMMONS,K_ID_SECTIONS);
	}
	
	public static Map<String,Section> getTabSections(){
		return computeIfAbsent(COMMONS, K_TAB_SECTIONS,new Callable<Map<String,Section>>(){
			@Override
			public Map<String, Section> call() throws Exception {
				List<Section> secs=Section.dao.getAll();
				return Maps.uniqueIndex(secs,c->c.getStr("tab"));
			}
		});
	}
	
	public static void removeCommonsTabSections(){
		CacheKit.remove(COMMONS,K_TAB_SECTIONS);
	}
	
	public static void removeCommons(){
		CacheKit.removeAll(COMMONS);
	}
	
	/*
	 * -----------------------------
	 * authenticationCache
	 * authorizationCache
	 * -----------------------------
	 */
	public static void clearMyCachedAuth(){
		DefaultWebSecurityManager dwsm=(DefaultWebSecurityManager)SecurityUtils.getSecurityManager();
		AdminAuthorizingRealm aar=(AdminAuthorizingRealm)dwsm.getRealms().iterator().next();
		PrincipalCollection pc=SecurityUtils.getSubject().getPrincipals();
		aar.clearCachedAuthenInfo(pc);
		aar.clearCachedAuthorInfo(pc);
	}
	public static void clearMyCachedAuthenInfo(){
		DefaultWebSecurityManager dwsm=(DefaultWebSecurityManager)SecurityUtils.getSecurityManager();
		AdminAuthorizingRealm aar=(AdminAuthorizingRealm)dwsm.getRealms().iterator().next();
		aar.clearCachedAuthenInfo(SecurityUtils.getSubject().getPrincipals());
	}
	public static void clearMyCachedAuthorInfo(){
		DefaultWebSecurityManager dwsm=(DefaultWebSecurityManager)SecurityUtils.getSecurityManager();
		AdminAuthorizingRealm aar=(AdminAuthorizingRealm)dwsm.getRealms().iterator().next();
		aar.clearCachedAuthorInfo(SecurityUtils.getSubject().getPrincipals());
	}
	
	public static void removeAuthenticationCache(){
		CacheKit.removeAll("authenticationCache");
	}
	public static void removeAuthorizationCache(){
		CacheKit.removeAll("authorizationCache");
	}
	
	//=========================================
	
	private static final String PAGE="page";
	
	public static <T> T getKeyPage(Object key,Callable<T> cv){
		return computeIfAbsent(PAGE, key, cv);
	}
	
	public static void removePage(){
		CacheKit.removeAll(PAGE);
	}
	
	
	
	
	
	//=====================================================================================
	private static <T> T computeIfAbsent(String cacheName,Object key,Callable<T> cv){
		T value=null;
		if(!BgzKit.devMode){//开发模式不使用缓存
			value=CacheKit.get(cacheName,key);
		}
		
		if(value==null){
			try {
				value=cv.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
			CacheKit.put(cacheName,key,value);
		}
		return value;
	}
}