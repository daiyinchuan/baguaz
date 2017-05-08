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

package com.baguaz.module.copyfrom;

import java.util.List;

import com.baguaz.common.BaseModel;

/**
 * id	smallint(5) unsigned	NO	PRI		auto_increment</br>
 * name	varchar(30)	NO			</br>
 * url	varchar(100)	YES			</br>
 * thumb	varchar(100)	YES			</br>
 * listorder	smallint(5) unsigned	YES		0</br>	
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Copyfrom extends BaseModel<Copyfrom>{
	private static final long serialVersionUID=4353605880810006881L;
	public static final Copyfrom dao=new Copyfrom();
	
	public List<Copyfrom> getAll() {
		return selectM("*","","","","");
	}
}
