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

package com.baguaz.pagetpl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.baguaz.module.flink.Flink;
import com.google.common.base.Strings;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class FlinkTag extends BgzTag {

	/* (non-Javadoc)
	 * @see org.beetl.core.Tag#render()
	 */
	@Override
	public void render(){
		Object[] data=this.handleWithCache(prepare());
		this.binds(data[0]);
		this.doBodyRender();
	}

	/**
	 * @param paras
	 * @return
	 */
	Object[] doExpensive(Map<String, String> paras) {
		/*
		 * order,num
		 */
		
		int num=StringUtils.isNotEmpty(paras.get("num"))?Integer.parseInt(paras.get("num")):20;
		if(StringUtils.isNotEmpty(paras.get("start"))){
			paras.put("limit",paras.get("start")+","+num);
		}else{
			paras.put("limit",""+num);
		}
		paras.put("order",Strings.nullToEmpty(paras.get("order")));
		
		String order=paras.get("order");
		String limit=paras.get("limit");
		List<Flink> flinks=Flink.dao.selectM("*", "", order, "", limit);
		
		return new Object[]{flinks};
	}
	
}
