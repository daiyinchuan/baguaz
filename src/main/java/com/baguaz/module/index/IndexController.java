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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.AppConfig.G;
import com.baguaz.BgzKit;
import com.baguaz.C;
import com.baguaz.CacheFunc;
import com.baguaz.GlobalFunc;
import com.baguaz.Ret;
import com.baguaz.common.BaseController;
import com.baguaz.module.member.Member;
import com.baguaz.module.member.TaskService;
import com.baguaz.module.member.Validcode;
import com.baguaz.module.site.Site;
import com.baguaz.util.EmailSender;
import com.google.common.collect.ImmutableMap;
import com.jfinal.aop.Before;
import com.jfinal.aop.Enhancer;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/")
public class IndexController extends BaseController{
	private static final Logger log=LoggerFactory
			.getLogger(IndexController.class);
	
	public void logout() {
		log.debug("会员用户退出调用");
        removeCookie(G.COOKIE_MEMBER);
        removeSessionAttr(G.SESSION_MEMBER);
        SecurityUtils.getSubject().logout();
        
        this.redirect("/login");
	}
	
	public void login() {
		if(this.isParaExists("dosubmit")){
			
			String account=this.getPara("account");
			String password=new String(Base64.decodeBase64(this.getPara("password")),Charset.forName("UTF-8"));
			
			Member m=Member.dao.getOneM("*", "id=? or email=?", "", "", account,account);
			
			Ret ret=Ret.create();
			if(m==null){
				ret.setCode(C.fail).setMsg("帐号或密码错误！");
				this.renderJson(ret);
				return;
			}
			
			long id=m.getLong("id");
			
			password=DigestUtils.sha256Hex(id+"_"+password);
			if(!password.equals(m.getStr("password"))){
				ret.setCode(C.fail).setMsg("帐号或密码错误！");
				this.renderJson(ret);
				return;
			}
			
			m.set("lastlogintime", BgzKit.genUnixTstamp());
			
			String realip=this.getRequest().getHeader("X-Real-IP");
			if(StringUtils.isEmpty(realip)){
				realip=this.getRequest().getRemoteAddr();
			}
			m.set("lastloginip", realip);
			m.update();
			
            setSessionAttr(G.SESSION_MEMBER, m);
            setCookie(G.COOKIE_MEMBER, BgzKit.getEncryptToken(m.getStr("token")), 30 * 24 * 60 * 60);
            
			TaskService ts=Enhancer.enhance(TaskService.class);
			ts.midLogin(m);
			
            ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("title","用户登录"));
			this.setAttr("SEO",seo);
			
			String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
			this.render(this.template(style, "", "login.html"));
		}
	}
	@Before(Tx.class)
	public void register() {
		if(this.isParaExists("dosubmit")){
			Ret ret=Ret.create();
			
			String validcode=this.getPara("validcode");
			String email=this.getPara("email");
			
			Validcode code = Validcode.dao.get(validcode, email,Validcode.TYPE_REG);
			
			if (code == null) {
				ret.setCode(C.fail).setMsg("验证码不存在或已使用(已过期)");
				this.renderJson(ret);
				return;
            }
			
			String password=new String(Base64.decodeBase64(this.getPara("password")),Charset.forName("UTF-8"));
			String repassword=new String(Base64.decodeBase64(this.getPara("repassword")),Charset.forName("UTF-8"));
			if(!password.equals(repassword)){
				ret.setCode(C.fail).setMsg("重复输入密码错误！");
				this.renderJson(ret);
				return;
			}
			String nickname=this.getPara("nickname");
			
			Member m=new Member();
			m.set("nickname", nickname)
			 .set("email", email)
			 .set("regtime",BgzKit.genUnixTstamp())
			 .set("avatar", "")
			 .set("token",BgzKit.genToken())
			 .save();
			
			long id=m.getLong("id");
			password=DigestUtils.sha256Hex(id+"_"+password);
			m.set("password", password).update();
			
			code.set("status", Validcode.STATUS_USED).update();
			
			this.setCookie(G.COOKIE_MID, ""+id, 30 * 24 * 60 * 60);
			
			ret.setCodeAndMsg(C.success);
			this.renderJson(ret);
		}else{
			Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("title","用户注册"));
			this.setAttr("SEO",seo);
			
			String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
			this.render(this.template(style, "", "register.html"));
		}
	}
	
	public void regsuccess(){
		Map<String,String> seo=GlobalFunc.seo(ImmutableMap.of("title","用户注册结果"));
		this.setAttr("SEO",seo);
		
		long mid=this.getCookieToLong(G.COOKIE_MID);
		this.setAttr("mid", mid);
		
		String style=CacheFunc.getIdSites().get(Site.SITEID).getStr("default_style");
		this.render(this.template(style, "", "regsuccess.html"));
	}
	
    public void sendValidcode() {
    	String email=this.getPara("email");
    	int type = getParaToInt("type");
    	String validcode=BgzKit.randHexstr(6);
    	
    	/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
    	
    	Ret ret=Ret.create();
    	if (type==Validcode.TYPE_FORGET_PWD){
    		Member m=Member.dao.getOneM("*", "email=?", "", "", email);
    		if (m == null) {
    			ret.setCode(C.fail).setMsg("该邮箱未被注册，请先注册");
            }else {
            	Validcode vc = new Validcode();
            	vc.set("code", validcode)
	                .set("type", type)
	                .set("gentime", BgzKit.genUnixTstamp())
	                .set("status", 0)
	                .set("expiretime",BgzKit.genUnixTstamp(DateUtils.addMinutes(new Date(), 30)))
	                .set("target", email)
	                .save();
            	StringBuffer mailBody = new StringBuffer();
                mailBody.append("您找回密码的验证码是: ")
                        .append(validcode)
                        .append("<br/>此验证码只能被使用一次，并且只在30分钟内有效。");
                EmailSender.asyncSendMail("baguaz－找回密码验证码", new String[]{email}, mailBody.toString());
            }
    	}else if(type==Validcode.TYPE_REG){
    		Member m=Member.dao.getOneM("*", "email=?", "", "", email);
    		if (m != null) {
    			ret.setCode(C.fail).setMsg("邮箱已经注册，请直接登录");
            }else {
            	Validcode vc = new Validcode();
            	vc.set("code", validcode)
	                .set("type", type)
	                .set("gentime", BgzKit.genUnixTstamp())
	                .set("status", 0)
	                .set("expiretime",BgzKit.genUnixTstamp(DateUtils.addMinutes(new Date(), 30)))
	                .set("target", email)
	                .save();
            	StringBuffer mailBody = new StringBuffer();
                mailBody.append("注册您的账号验证码是: ")
                        .append(validcode)
                        .append("<br/>此验证码只能被使用一次，并且只在30分钟内有效。");
                EmailSender.asyncSendMail("baguaz－注册账号验证码", new String[]{email}, mailBody.toString());
            }
    	}
    	
    	ret.setCode(C.success).setMsg("邮件发送成功！");
		this.renderJson(ret);
    }
    public void forgetpwd() {
    	
    }
    public void checkEmail(){
		String email=this.getPara("email");
		List<Integer> memberid=Member.dao.select("id", "email=?", "", "", "", email);
		Ret ret=Ret.create();
		if(memberid.size()>=1){
			ret.put("error","邮箱已存在");
		}else{
			ret.put("ok","");
		}
		this.renderJson(ret.getMap());
    }
}
