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

package com.baguaz.module.hits;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.baguaz.BgzKit;
import com.baguaz.common.BaseController;
import com.baguaz.module.content.News;
import com.jfinal.ext.route.ControllerBind;

/**
 * 点击量
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
@ControllerBind(controllerKey = "/hits")
public class HitsController extends BaseController {
	
	/**
	 * 目前hits表是非事务的，失败不能回滚
	 */
	public void getCount(){
		int id=this.getParaToInt("id");
		String hitsid="c-"+this.getParaToInt("modelid")+"-"+id;
		/*
		 * 先进行统计
		 */
		Hits hits=Hits.dao.findById(hitsid);
		if(id==0 && hits==null){//过滤添加页面的预览情况
			this.renderJavascript("$('#hits').html('"+0+"')");
			return;
		}
		Date now=new Date();
		long nowTime=now.getTime()/1000;
		if(id!=0 && hits==null){//处理历史遗留问题
			int catid=News.dao.getOne("catid", "id=?", "", "", id);
			hits=new Hits();
			hits.set("hitsid", hitsid)
				.set("catid",catid)
				.set("updatetime",nowTime)
				.set("views", 0)
				.set("yesterdayviews", 0)
				.set("dayviews", 0)
				.set("weekviews", 0)
				.set("monthviews", 0)
				.save();
		}
		
		long lUpdatetime=BgzKit.s2ms(hits.getLong("updatetime"));
		
		int views=hits.getInt("views")+1;
		hits.set("views",views);
		int yesterdayviews=DateFormatUtils.format(lUpdatetime,"yyyy-MM-dd")
				.equals(DateFormatUtils.format(DateUtils.addDays(now, -1),"yyyy-MM-dd"))
				?hits.getInt("dayviews"):hits.getInt("yesterdayviews");
		hits.set("yesterdayviews",yesterdayviews);
		int dayviews=DateFormatUtils.format(lUpdatetime,"yyyy-MM-dd")
				.equals(DateFormatUtils.format(now,"yyyy-MM-dd"))
				?hits.getInt("dayviews")+1:1;
		hits.set("dayviews",dayviews);
		int weekviews=DateFormatUtils.format(lUpdatetime,"yyyy-ww")
				.equals(DateFormatUtils.format(now,"yyyy-ww"))
				?hits.getInt("weekviews")+1:1;
		hits.set("weekviews",weekviews);
		int monthviews=DateFormatUtils.format(lUpdatetime,"yyyy-MM")
				.equals(DateFormatUtils.format(now,"yyyy-MM"))
				?hits.getInt("monthviews")+1:1;
		hits.set("monthviews",monthviews);
		hits.set("updatetime", now.getTime()/1000);
		hits.update();
		
		this.renderJavascript("$('#hits').html('"+views+"')");
	}
}