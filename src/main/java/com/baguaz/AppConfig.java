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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.WebAppResourceLoader;
import org.beetl.ext.jfinal.BeetlRenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.baguaz.handler.HtmlHandler;
import com.baguaz.handler.UrlRewriteHandler;
import com.baguaz.interceptor.CommonInterceptor;
import com.baguaz.module.member.ScoreTaskMessageListener;
import com.baguaz.module.user.ShiroExt;
import com.baguaz.pagetpl.BgzBeetlFuncs;
import com.baguaz.pagetpl.CommunityTag;
import com.baguaz.pagetpl.ContentTag;
import com.baguaz.pagetpl.FlinkTag;
import com.baguaz.util.EmailSender.EmailMessageListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.kit.ClassSearcher;
import com.jfinal.ext.kit.CpKit;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.ext.plugin.artemis.ArtemisKit;
import com.jfinal.ext.plugin.artemis.ArtemisPlugin;
import com.jfinal.ext.plugin.cron4j.Cron4jPlugin;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.ext.plugin.tablebind.AutoTableBindPlugin;
import com.jfinal.ext.plugin.tablebind.ParamNameStyles;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.ehcache.EhCachePlugin;



/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class AppConfig extends JFinalConfig{
	private static final Logger log=LoggerFactory
			.getLogger(AppConfig.class);
	
	private Routes routes;
	
	public final static class G{
		public static final String baguaz_prop="config.groovy";
		public static final String version_prop="version.properties";
		public static String bgzJarName="baguaz"+BgzKit.getBgzVersion("-")+".jar";
		
		public static final String COOKIE_MID="cookie_o_mid";
		public static final String COOKIE_MEMBER="cookie_m_token";
		
		public static final String SESSION_USER="user";
		public static final String SESSION_MEMBER="member";
		
		public static final String BEFORE_URL = "before_url";
	}
	
	/* (non-Javadoc)
	 * @see com.jfinal.config.JFinalConfig#configConstant(com.jfinal.config.Constants)
	 */
	@Override
	public void configConstant(Constants me){
		@SuppressWarnings("unchecked")
		Map<String,String> configs=(Map<String,String>)GroovyKit.runScriptFromFile(G.baguaz_prop);
		PropKit.use(G.baguaz_prop,configs);
		me.setDevMode(PropKit.getBoolean("devMode"));
		
		/*
		 * beetl配置
		 */
		me.setMainRenderFactory(new BeetlRenderFactory());
		GroupTemplate gt=BeetlRenderFactory.groupTemplate;
		Configuration conf=gt.getConf();
		conf.setStatementStart("<!--#");
		conf.setStatementEnd("-->");
		WebAppResourceLoader loader=(WebAppResourceLoader)gt.getResourceLoader();
		loader.setRoot(PathKit.getWebRootPath()+PropKit.get("tpl.root"));
		
		conf.addPkg("com.baguaz");
		
		List<Class<? extends Object>> models=ClassSearcher.of(Model.class)
				.scanPackages(Lists.newArrayList("com.baguaz"))
				.inJars(G.bgzJarName)
				.search();
		models.forEach(m->conf.addPkg(m.getPackage().getName()));
		
		gt.registerFunctionPackage("ro",new ShiroExt());
		gt.registerFunctionPackage("bc",new BgzBeetlFuncs());
		
		gt.registerTag("content",ContentTag.class);
		gt.registerTag("flink", FlinkTag.class);
		gt.registerTag("community", CommunityTag.class);
		
		/*
		 *全局变量是在controller里绑定的变量，也就是通常java代码赋值的变量，
		 *能在模板任何地方使用，包括子模板，以及扩展的Function，Tag里等，
		 *这些全局变量包含request里的属性值，session变量等。
		 *
		 *共享变量是在beetl初始化的时候赋值，每个模板都可以访问的（全局变量）。
		 *实际上，在渲染每个模板的时候，会调用binding方法逐一将共享变量绑定到模板里。
		 *对于开发人员，不太可能用到共享变量，
		 *除非架构师或者项目领导决定某些变量可以作为共享变量让每个模板都可以访问而不需要在每个contrller里赋值（成全局变量），
		 *比如有些web应用的的静态文件的cdn的url路径，这个可以作为共享变量 
		 */
		Map<String,Object> shareds=Maps.newHashMap();
		String ctxPath=JFinal.me().getContextPath();
		shareds.put("JS_PATH",ctxPath+PropKit.get("JS_PATH"));
		shareds.put("CSS_PATH",ctxPath+PropKit.get("CSS_PATH"));
		shareds.put("IMG_PATH",ctxPath+PropKit.get("IMG_PATH"));
		gt.setSharedVars(shareds);
		log.debug("SharedVars====="+gt.getSharedVars());
	}
	/* (non-Javadoc)
	 * @see com.jfinal.config.JFinalConfig#configRoute(com.jfinal.config.Routes)
	 */
	@Override
	public void configRoute(Routes me){
		this.routes=me;
		
		AutoBindRoutes abr=new AutoBindRoutes();
		abr.addJars(G.bgzJarName).scanPackages(Lists.newArrayList("com.baguaz.module"));
		me.add(abr);
	}
	/* (non-Javadoc)
	 * @see com.jfinal.config.JFinalConfig#configPlugin(com.jfinal.config.Plugins)
	 */
	@Override
	public void configPlugin(Plugins me){
		PropKit.use(G.baguaz_prop);
		DruidPlugin dp=new DruidPlugin(PropKit.get("db.url"),
		                               PropKit.get("db.username"),
		                               PropKit.get("db.password"));
		
		StatFilter sf=new StatFilter();
		dp.addFilter(sf);
		
		WallFilter wall=new WallFilter();
		wall.setDbType(PropKit.get("db.type"));
		dp.addFilter(wall);
		
		me.add(dp);
		
        AutoTableBindPlugin atbp = new AutoTableBindPlugin(dp, ParamNameStyles.lowerUnderlineModule(PropKit.get("db.prefix")));
        atbp.addJars(G.bgzJarName)
        	.scanPackages(Lists.newArrayList("com.baguaz.module"));
        atbp.setShowSql(Boolean.parseBoolean(PropKit.get("db.showsql")));
        me.add(atbp);

		//加载Shiro插件
        ShiroPlugin sp=new ShiroPlugin(routes);
        sp.setLoginUrl("/admin/login");
        sp.setUnauthorizedUrl("/admin/unauthorized");
		me.add(sp);

		//缓存插件
		me.add(new EhCachePlugin(CpKit.toAbsPath("/ehcache-shiro.xml")));
		
		
		//加载Artemis插件，嵌入式JMS服务器+客户端
		ArtemisPlugin ap=new ArtemisPlugin();
		ap.setEmailMsgListener(new EmailMessageListener());
		ap.setScoreTaskMsgListener(new ScoreTaskMessageListener());
		me.add(ap);
		
		//加载cron4j插件，轻量级定时任务，linux cron表达式
		Cron4jPlugin cp=new Cron4jPlugin();
		cp.addTask(PropKit.get("task_cron"), new Runnable(){
			@Override
			public void run() {
		    	javax.jms.Session session=null;
		    	try{
			    	session=ArtemisKit.getPooledSession();
			    	MessageProducer producer=session.createProducer(ArtemisKit.getQueue(ArtemisKit.QUEUE_SCORETASK));
			    	log.debug("任务[积分重置]发送消息的处理=====");
			    	producer.send(session.createTextMessage("taskReset==="+new Date()));
		    	} catch (JMSException e) {
		    		log.error("email send jms error",e);
				}finally{
		    		ArtemisKit.retPooledSession(session);
		    	}
			}
		});
		me.add(cp);
	}
	/* (non-Javadoc)
	 * @see com.jfinal.config.JFinalConfig#configInterceptor(com.jfinal.config.Interceptors)
	 */
	@Override
	public void configInterceptor(Interceptors me){
		//Global Class Inject Method
		
		me.addGlobalActionInterceptor(new ShiroInterceptor());
		
		me.addGlobalActionInterceptor(new CommonInterceptor());
		
		//me.addGlobalServiceInterceptor(globalServiceInterceptor);
	}
	
	public static String[] AK;
	public static String[] IAK;
	
	/* (non-Javadoc)
	 * @see com.jfinal.config.JFinalConfig#configHandler(com.jfinal.config.Handlers)
	 */
	@Override
	public void configHandler(Handlers me){
		DruidStatViewHandler dvh=new DruidStatViewHandler("/druid");
		me.add(dvh);
		
		AK=this.routes.getEntrySet().stream()
				.peek(r->log.debug("[ "+r.getKey()+" -> "+r.getValue()+" ]"))
				.filter(r->!r.getKey().equals("/"))
				.map(ak->StringUtils.split(ak.getKey(),"/")[0])
				.distinct()
				.sorted()
				.peek(ak->log.debug("ak="+ak))
				.toArray(String[]::new);
		
		IAK=new String[]{"register","login","logout","sendValidcode","forgetpwd","checkEmail","regsuccess"};
		
		Stream.of(IAK).forEach(iak->log.debug("iak="+iak));
		
		me.add(new HtmlHandler());
		me.add(new UrlRewriteHandler(
				Stream.of(AK).collect(Collectors.joining("|")),
				Stream.of(IAK).collect(Collectors.joining("|")))
				);
	}
	@Override
	public void afterJFinalStart(){
		log.info("started!");
	}
	@Override
	public void beforeJFinalStop(){
		log.info("stopping...");
	}
	
    public static void main(String[] args) {
        System.out.println(HashKit.md5("123456"));
        JFinal.start("src/main/webapp", 8080, "/", 3600);
    }
}
