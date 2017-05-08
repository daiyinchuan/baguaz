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

package com.baguaz.module.site;

import java.util.List;

import com.baguaz.CacheFunc;
import com.baguaz.common.BaseModel;
import com.jfinal.ext.plugin.tablebind.TableBind;

/**
 * siteid	smallint(5) unsigned	NO	PRI		auto_increment</br>
 * name	char(30)	NO			</br>
 * dirname	char(255)	YES			</br>
 * domain	char(255)	YES			</br>
 * site_title	char(255)	YES			</br>
 * keywords	char(255)	YES			</br>
 * description	char(255)	YES			</br>
 * default_style	char(50)	YES			</br>
 * template	text	YES			</br>
 * setting	mediumtext	YES			</br>
 * uuid	char(40)	NO			</br>
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@TableBind(pkName="siteid")
public class Site extends BaseModel<Site>{
	private static final long serialVersionUID=4416195621524265264L;
	public static final Site dao=new Site();
	
	public static final int SITEID=1;
	
	public String getDomain(){
		return getOne("domain","siteid=?","","",SITEID);
	}
	public String getTemplate(){
		return getOne("template","siteid=?","","",SITEID);
	}
	
	public String getDomainFromCache(){
		return CacheFunc.getIdSites().get(SITEID).getStr("domain");
	}
	
	public List<Site> getAll(){
		return selectM("*","","","","");
	}
}