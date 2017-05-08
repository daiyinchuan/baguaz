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

package com.baguaz.module.urlrule;

import java.util.List;

import com.baguaz.common.BaseModel;
import com.jfinal.plugin.activerecord.Record;

/**
 * id	smallint(5) unsigned	NO	PRI		auto_increment</br>
 * file	varchar(20)	NO			</br>
 * ishtml	tinyint(1) unsigned	NO		0</br>	
 * urlrule	varchar(255)	NO			</br>
 * example	varchar(255)	NO			</br>
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Urlrule extends BaseModel<Urlrule>{
	private static final long serialVersionUID=6693417768325054681L;
	
	public static final String FILE_CAT="category";
	public static final String FILE_SHOW="show";
	public static final String FILE_SEC="section";
	public static final String FILE_THR="thread";
	
	public static final int ISHTML_Y=1;
	public static final int ISHTML_N=0;
	public static final Urlrule dao=new Urlrule();
	
	public List<Record> getSelUrs(String file,int ishtml){
		return selectR("id,example","file=? and ishtml=?","","","",file,ishtml);
	}
	
	public List<Urlrule> getAll(){
		return selectM("*","","","","");
	}
}
