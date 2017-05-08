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

package com.baguaz.module.model;

import java.util.List;

import com.baguaz.CacheFunc;
import com.baguaz.common.BaseModel;

/**
 * id	smallint(5) unsigned	NO	PRI		auto_increment</br>
 * name	char(30)	NO			</br>
 * description	char(100)	YES		</br>	
 * tablename	char(20)	NO			</br>
 * default_style	char(30)	NO		</br>	
 * category_template	char(30)	NO		</br>	
 * list_template	char(30)	NO			</br>
 * show_template	char(30)	NO			</br>
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Model extends BaseModel<Model>{
	private static final long serialVersionUID=7441654951201920085L;
	public static final Model dao=new Model();
	
	public List<Model> selectAll(){
		return selectM("*","","","","");
	}
	
	public String getModelNameFromCache(int id){
		return CacheFunc.getIdModels().get(id).getStr("name");
	}
}
