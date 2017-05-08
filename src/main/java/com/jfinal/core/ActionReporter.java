/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com). Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.jfinal.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.baguaz.BgzKit;
import com.jfinal.aop.Interceptor;

/**
 * ActionReporter
 */
final class ActionReporter{
	private static final ThreadLocal<SimpleDateFormat> sdf=new ThreadLocal<SimpleDateFormat>(){
		protected SimpleDateFormat initialValue(){
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	/**
	 * Report action before action invoking when the common request coming
	 */
	static final boolean reportCommonRequest(Controller controller,Action action){
		String content_type=controller.getRequest().getContentType();
		if(content_type==null
				||content_type.toLowerCase().indexOf("multipart")==-1){ // if (content_type == null || content_type.indexOf("multipart/form-data") == -1) {
			doReport(controller,action);
			return false;
		}
		return true;
	}
	/**
	 * Report action after action invoking when the multipart request coming
	 */
	static final void reportMultipartRequest(Controller controller,Action action){
		doReport(controller,action);
	}
	private static final void doReport(Controller controller,Action action){
		StringBuilder sb=new StringBuilder("\nJFinal action report -------- ")
				.append(sdf.get().format(new Date()))
				.append(" ------------------------------\n");
		Class<? extends Controller> cc=action.getControllerClass();
		
/*		sb.append("Controller  : ").append(cc.getName()).append(".(")
				.append(cc.getSimpleName()).append(".java:14)");
		sb.append("\nMethod      : ").append(action.getMethodName())
				.append("\n");*/
		
		sb.append("Controller  : ").append(cc.getName()).append(".(").append(cc.getSimpleName()).append(".java:")
          .append(lineNum("publicvoid"+action.getMethodName()+"(){", fileName(cc))).append(")");
        sb.append("\nMethod      : ").append(action.getMethodName()).append("\n");
		
		String urlParas=controller.getPara();
		if(urlParas!=null){
			sb.append("UrlPara     : ").append(urlParas).append("\n");
		}
		Interceptor[] inters=action.getInterceptors();
		if(inters.length>0){
			sb.append("Interceptor : ");
			for(int i=0;i<inters.length;i++){
				if(i>0)
					sb.append("\n              ");
				Interceptor inter=inters[i];
				Class<? extends Interceptor> ic=inter.getClass();
				sb.append(ic.getName()).append(".(").append(ic.getSimpleName())
						.append(".java:1)");
			}
			sb.append("\n");
		}
		// print all parameters
		HttpServletRequest request=controller.getRequest();
		Enumeration<String> e=request.getParameterNames();
		if(e.hasMoreElements()){
			sb.append("Parameter   : ");
			while(e.hasMoreElements()){
				String name=e.nextElement();
				String[] values=request.getParameterValues(name);
				if(values.length==1){
					String trancat=BgzKit.strCut(values[0], 255, "...");
					sb.append(name).append("=").append(trancat);
				}else{
					sb.append(name).append("[]={");
					for(int i=0;i<values.length;i++){
						if(i>0)
							sb.append(",");
						String trancat=BgzKit.strCut(values[i], 255, "...");
						sb.append(trancat);
					}
					sb.append("}");
				}
				sb.append("  ");
			}
			sb.append("\n");
		}
		sb.append("--------------------------------------------------------------------------------\n");
		System.out.print(sb.toString());
	}
	private static String fileName(Class clazz){
		String controllerFile=System.getProperty("user.dir")+File.separator
				+"src/main/java";
		for(String temp:clazz.getName().split("\\.")){
			controllerFile=controllerFile+File.separator+temp;
		}
		return controllerFile+".java";
	}
	private static int lineNum(String codeFragment,String fileName){
		List<String> lines=new ArrayList<>();
		int lineNum=1;
		Path path=Paths.get(fileName);
		try{
			lines=Files.readAllLines(path,Charset.forName("utf-8"));
			for(int i=0;i<lines.size();i++){
				String line=lines.get(i);
				if(codeFragment.equals(deleteWhitespace(line))){
					lineNum=i+1;
					break;
				}
			}
		}catch(NoSuchFileException e1){
			// interceptor in jfinal.jar
		}catch(IOException e2){
			e2.printStackTrace();
		}
		return lineNum;
	}
	private static String deleteWhitespace(String str){
		if(isEmpty(str)){
			return str;
		}
		int sz=str.length();
		char[] chs=new char[sz];
		int count=0;
		for(int i=0;i<sz;i++){
			if(!Character.isWhitespace(str.charAt(i))){
				chs[count++]=str.charAt(i);
			}
		}
		if(count==sz){
			return str;
		}
		return new String(chs,0,count);
	}
	private static boolean isEmpty(CharSequence cs){
		return cs==null||cs.length()==0;
	}
}
