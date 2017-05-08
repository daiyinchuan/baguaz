/**
 * 
 */
package com.baguaz.cms;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baguaz.BgzKit;
import com.baguaz.AppConfig.G;
import com.baguaz.module.category.Category;
import com.baguaz.util.EmailSender;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.jfinal.ext.kit.CpKit;
import com.jfinal.ext.kit.GroovyKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * @author daiyc
 *
 */
public class BgzTest{
	private static final Logger log=LoggerFactory.getLogger(BgzTest.class);
	
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup(){
		Map<String,String> config=(Map<String,String>)GroovyKit.runScriptFromFile(G.baguaz_prop);
		PropKit.use(G.baguaz_prop,config);
	}
	
	@Test
	public void testUUID(){
		log.info(java.util.UUID.randomUUID().toString().replace("-", ""));
	}

	@Test
	public void testGroovy(){
		String scriptStr="/content/lists?&catid=${catid}";
		scriptStr=GroovyKit.j2gStr(scriptStr);
		Object ret=GroovyKit.runScriptFromStr(scriptStr,ImmutableMap
				.of("catid",(Object)25));
		log.info(ret.toString());
	}
	@Test
	public void testGroovy1() throws UnsupportedEncodingException{
		Map<String,Object> setting=new LinkedHashMap<String,Object>();
		String ss="带因此";
		setting.put("meta_title",ss);
		log.info(BgzKit.obj2json(setting));
	}
	
	@Test
	public void testStripTags(){
		log.info(BgzKit.stripTags("<a href='sss'>dddd</a></br><div class='ff'>地方巍峨</div>"));
	}
	@Test
	public void testRegMatchAll(){
		String tmpUrls="${categorydir}${catdir}/${page}.html";
		String[] ss=BgzKit.regMatchAll("/\\${([a-z0-9_]+)}/i",tmpUrls);
		Stream.of(ss).forEach(_s->log.info(_s));
	}
	
	@Test
	public void testGetFrom(){
		String tmpUrls="g20fdsd";
		String[] ss=BgzKit.regMatchAll("/^(\\d+)/i",tmpUrls);
		Stream.of(ss).forEach(_s->log.info(_s));
	}
	
	/**
	 * 为了调试修改栏目操作造的测试数据，以便于数据快速还原可重复测试
	 */
	//@Test
	public void rebuildCatTable(){
		PropKit.use(G.baguaz_prop);
		DruidPlugin dp=new DruidPlugin(	PropKit.get("db.url"),
										PropKit.get("db.username"),
										PropKit.get("db.password"));
		ActiveRecordPlugin arp=new ActiveRecordPlugin(dp);
		arp.addMapping(	PropKit.get("db.prefix")+"category",
						"catid",
						Category.class);
		dp.start();
		arp.start();
		
		/*
		 * 清理
		 */
		Db.update("delete from bgz_category");
		
		/*
		 * 插入
		 */
		ArrayList<String> list=null;
		try{
			list=Lists.newArrayList(Files
					.readLines(	new File(CpKit.toAbsPath2("bgz_category.sql")),
								Charsets.UTF_8));
		}catch(IOException e){
			e.printStackTrace();
		}
		Db.batch(list,1);
	}
	@Test
	public void hitsDatetime(){
		//long updatetime=1452050573;
		long lUpdatetime=System.currentTimeMillis();
		String strUpdatetime=DateFormatUtils.format(lUpdatetime,"yyyy-MM-dd HH:mm:ss");
		log.debug(strUpdatetime);
		String strYear=DateFormatUtils.format(lUpdatetime,"yyyy");
		log.debug(strYear);
		String strYm=DateFormatUtils.format(lUpdatetime,"yyyy-MM");
		log.debug(strYm);
		String strYmd=DateFormatUtils.format(lUpdatetime,"yyyy-MM-dd");
		log.debug(strYmd);
		String strYw=DateFormatUtils.format(lUpdatetime,"yyyy-ww");
		log.debug(strYw);
		
		String stryestYmd=DateFormatUtils.format(DateUtils.addDays(new Date(), -1),"yyyy-MM-dd");
		log.debug(stryestYmd);
	}
	
	@Test
	public void sendmail(){
		EmailSender.syncSendMail("baguaz测试邮件", new String[]{"daiyinchuan@163.com"}, "测试邮件内容发发发发发发");
	}
}