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

package com.baguaz.common;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.baguaz.BgzKit;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class BaseController extends Controller {
	/**
	 * 指定模板不存在的话，返回默认风格的默认模板
	 * @param style
	 * @param module
	 * @param tpl
	 * @return
	 */
	protected String template(String style,String module,String tpl){
		String tplUrl=PathKit.getWebRootPath()+BgzKit.getBgzProp("tpl.root")+"/"+style+"/"+module+"/"+tpl;
		if(!new File(tplUrl).exists()){
			style="default";
			tpl=tpl.indexOf("_")<0?tpl:(StringUtils.split(tpl,"_")[0]+".html");
		}
		if(StringUtils.isEmpty(module)){
			return "/"+style+"/"+tpl;
		}else{
			return "/"+style+"/"+module+"/"+tpl;
		}
	}
}
