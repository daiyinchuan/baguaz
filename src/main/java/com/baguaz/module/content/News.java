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

/**
 * id	mediumint(8) unsigned	NO	PRI		auto_increment</br>
 * catid	smallint(5) unsigned	NO	MUL	0	</br>
 * title	varchar(80)	NO			</br>
 * thumb	varchar(100)	NO			</br>
 * keywords	char(40)	NO			</br>
 * description	mediumtext	NO			</br>
 * posids	tinyint(1) unsigned	NO		0	</br>
 * url	char(100)	NO			</br>
 * listorder	tinyint(3) unsigned	NO		0	</br>
 * sysadd	tinyint(1) unsigned	NO		0	</br>
 * username	char(20)	NO			</br>
 * inputtime	int(10) unsigned	NO		0	</br>
 * updatetime	int(10) unsigned	NO		0	</br>
 * 
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class News extends BaseModel<News>{
	private static final long serialVersionUID=4404019655183869295L;
	
	public static final int POSIDS_Y=1;
	public static final int POSIDS_N=0;
	
	public static final int SYSADD_Y=1;
	public static final int SYSADD_N=0;
	
	public static final News dao=new News();
}