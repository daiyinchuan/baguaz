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

package com.baguaz.module.content;

import com.baguaz.common.BaseModel;
import com.jfinal.ext.plugin.tablebind.TableBind;

/**
 * catid	smallint(5) unsigned	NO	MUL	0	</br>
 * title	varchar(160)	NO			</br>
 * keywords	varchar(40)	NO			</br>
 * content	text	NO			</br>
 * updatetime	int(10) unsigned	NO		0</br>	
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@TableBind(pkName="catid")
public class Page extends BaseModel<Page>{
	private static final long serialVersionUID=8502886376126063847L;
	public static final Page dao=new Page();
}
