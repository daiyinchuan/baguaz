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

package com.baguaz.module.member;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class TaskService {

	@Before(Tx.class)
	public void taskReset(){
		Db.update("delete from "+Task.dao.tn());
		
		List<Long> mids=Member.dao.select("id", "", "", "", "");
		mids.stream().forEach(mid->{
			Metatask.dao.selectR("id,num", "", "", "", "").stream().forEach(mt->{
				Task task=new Task();
				task.set("mid", mid).set("taskid",mt.getInt("id")).set("num", mt.getInt("num"));
				task.save();
			});
		});
	}
	
	@Before(Tx.class)
	public void midLogin(Member m){
		//查询任务积分
		List<Task> tasks=Task.dao.find("SELECT mt.id,mt.name,mt.score,mt.kind,t.mid,t.num FROM "+Metatask.dao.tn()+" mt LEFT JOIN "+Task.dao.tn()+" t ON mt.id=t.taskid WHERE t.mid=? AND mt.id=?", m.getLong("id"),1);
		
		if(tasks.size()>0 && tasks.get(0).getInt("num")>0){
			Task task=tasks.get(0);
			m.set("score", m.getLong("score")+task.getLong("score")).update();
			//更新任务次数
			int num=task.getInt("num");
			Db.update("UPDATE "+Task.dao.tn()+" SET num=? WHERE mid=? AND taskid=?", --num,m.getLong("id"),task.getInt("id"));
		}
	}
}
