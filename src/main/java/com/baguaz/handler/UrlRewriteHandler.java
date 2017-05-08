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

package com.baguaz.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baguaz.BgzKit;
import com.baguaz.CacheFunc;
import com.jfinal.handler.Handler;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class UrlRewriteHandler extends Handler{
	private String ak;
	private String iak;
	public UrlRewriteHandler(String ak,String iak){
		this.ak=ak;
		this.iak=iak;
	}
	/* (non-Javadoc)
	 * @see com.jfinal.handler.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, boolean[])
	 */
	@Override
	public void handle(	String target,
						HttpServletRequest request,
						HttpServletResponse response,
						boolean[] isHandled){
		int index = target.lastIndexOf(";JSESSIONID");
        target = index==-1?target:target.substring(0, index);
        
        if(target.startsWith("/statics/") || target.startsWith("/uploadfile/") || target.equals("/favicon.ico")){
        	
        }else if(BgzKit.regMatch("/^/$/",target)){
        	int index_ishtml=(int)CacheFunc.getSiteSettings().get("index_ishtml");
        	if(index_ishtml==1){
        		target="/index.html";
        	}else{
        		target="/content/index";
        	}
        }else if(BgzKit.regMatch("/^/admin/?$/",target)){
        	target="/admin/publicIndex";
        }else if(BgzKit.regMatch("/^/("+ak+")(/.*)?/",target)){
        	
        }else if(BgzKit.regMatch("/^/("+iak+")$/",target)){
        	
        }else if(BgzKit.regMatch("/^/.*.html$/",target)){
        	
        }else if(BgzKit.regMatch("/^/.*/?$/",target)){
        	target+="index.html";
        }
        
        nextHandler.handle(target, request, response, isHandled);
	}
}
