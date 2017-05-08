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

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.alibaba.fastjson.JSON;
import com.baguaz.AppConfig.G;
import com.baguaz.module.user.UserPrincipal;
import com.google.common.collect.Lists;
import com.jfinal.core.JFinal;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public final class BgzKit{
	static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	static final Random rand = new Random();
	
	public static String getBgzProp(String key){
		return PropKit.use(G.baguaz_prop).get(key);
	}
	public static Prop getBgzProp(){
		return PropKit.use(G.baguaz_prop);
	}

	//public static String tp=getBgzProp("db.prefix")+"_";
	
	/**
	 * 获取baguaz cms的版本号
	 * @param paras
	 * @return
	 */
	public static String getBgzVersion(String prefix){
		return prefix+PropKit.use(G.version_prop).get("version");
	}

	public static String obj2json(Object obj){
		return JSON.toJSONString(obj);
	}
	public static String obj2json(Object obj,boolean isPettyFormat){
		return JSON.toJSONString(obj,isPettyFormat);
	}
	@SuppressWarnings("unchecked")
	public static <T> T json2obj(String jsonStr){
		return (T)JSON.parseObject(jsonStr,Map.class);
	}
	/**
	 * int类型的秒转成long类型的毫秒
	 * @param s
	 * @return
	 */
	public static long s2ms(long s){
		return s*1000;
	}
	/**
	 * @param ms
	 * @return
	 */
	public static long ms2s(long ms){
		return ms/1000;
	}
	
	public static long genUnixTstamp(){
		return ms2s(new Date().getTime());
	}
	
	public static long genUnixTstamp(Date date){
		return ms2s(date.getTime());
	}
	/**
	 * formatStr "yyyy-MM-dd HH:mm:ss"
	 * @param dateStr
	 * @return
	 */
	public static long conv2UnixTstamp(String dateStr){
		Date date;
		try{
			date=DateUtils.parseDate(dateStr,"yyyy-MM-dd HH:mm:ss");
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
		return ms2s(date.getTime());
	}
	
	/**
	 * 年月日的字符串数组[yyyy,MM,dd],例如["2015","04","02"]
	 * @param time unix时间戳
	 * @return
	 */
	public static String[] getYMDArr(long time){
		String strInputDate=DateFormatUtils.format(s2ms(time),"yyyy-MM-dd");
		return StringUtils.split(strInputDate,"-");
	}
	
	public static String getUsername(){
		Subject subject=SecurityUtils.getSubject();
		UserPrincipal pp=(UserPrincipal)subject.getPrincipal();
		return pp.getUserName();
	}
	public static String stripTags(String htmlStr){
		String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式
		Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
		Matcher m_html=p_html.matcher(htmlStr);
		htmlStr=m_html.replaceAll(""); //过滤html标签 
		return htmlStr.trim(); //返回文本字符串
	}
	/**
	 * 相对于php的preg_match_all函数
	 * 
	 * @param regStr 正则表达式
	 * @param matchStr 匹配字符串
	 * @return
	 */
	public static String[] regMatchAll(String regStr,String matchStr){
		Perl5Util p5u=new Perl5Util();
		List<String> ret=Lists.newArrayList();
		PatternMatcherInput matcherInput = new PatternMatcherInput(matchStr);
		while(p5u.match(regStr,matcherInput)){
			MatchResult mr=p5u.getMatch();
			ret.add(mr.group(1));
		}
		return ret.toArray(new String[ret.size()]);
	}
	public  static boolean regMatch(String regStr,String matchStr){
		Perl5Util p5u=new Perl5Util();
		PatternMatcherInput matcherInput = new PatternMatcherInput(matchStr);
		return p5u.match(regStr,matcherInput);
	}
	/**
	 * 
	 * @param id
	 * @return 24个字符的16进制数
	 */
	public static String md5strWithidRet24(int id){
		return DigestUtils.md5Hex("daiyc"+id).substring(8);
	}
	
	public static String randHexstr(int length) {
        StringBuffer sb = new StringBuffer();
        for (int loop = 0; loop < length; ++loop) {
            sb.append(hexDigits[rand.nextInt(hexDigits.length)]);
        }
        return sb.toString();
    }
	
    public static String uuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
	
	public static String genToken(){
		return DigestUtils.sha256Hex(uuid()).substring(0, 16);
	}
	
    public static String getEncryptToken(String token) {
        for (int i = 0; i < 2; i++) {
            token =Base64.encodeBase64String(token.getBytes());
        }
        return token;
    }

    public static String getDecryptToken(String encryptToken) {
        for (int i = 0; i < 2; i++) {
        	encryptToken = new String(Base64.decodeBase64(encryptToken.getBytes()),Charset.forName("UTF-8"));
        }
        return encryptToken;
    }
    
    public static boolean devMode=JFinal.me().getConstants().getDevMode();
    
    public static String strCut(String src,int cutLen,String ellip){
		String ret=StringUtils.substring(src,0,cutLen);
		if(StringUtils.isNotBlank(ellip) && src.length()>cutLen){
			ret=ret+ellip;
		}
		return ret;
    }
    
    public static String oneOrTwo(String one,String two){
    	return StringUtils.isEmpty(one)?two:one;
    }
}