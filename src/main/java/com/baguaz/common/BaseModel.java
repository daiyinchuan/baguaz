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

import java.sql.Connection;
import java.util.List;

import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class BaseModel<M extends Model<?>> extends Model<M>{
	private static final long serialVersionUID = -4210458559643495772L;
	public static final int CHILD_NH=0;
	public static final int CHILD_H=1;
	
	public static final int ISDISPLAY_N=0;
	public static final int ISDISPLAY_Y=1;
	
	public static String getIsdisplayDesc(int key){
		if(key==ISDISPLAY_N){
			return "不显示";
		}else if(key==ISDISPLAY_Y){
			return "显示";
		}
		return "";
	}

	/**
	 * 定制查询，忽略表名
	 * @param columns
	 * @param where
	 * @param order
	 * @param group
	 * @param limit
	 * @param paras where条件参数
	 * @return
	 * 
	 * @author daiyc
	 */
	public <T> List<T> select(String columns,String where,String order,String group,String limit,Object... paras) {
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		config.dialect.select(sql,columns,this.getTable().getName(),where,order,group,limit);
		Connection conn = null;
		try {
			conn = config.getConnection();
			return Db.query(config,conn,sql.toString(),paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	/**
	 * 定制查询，忽略表名
	 * @param columns
	 * @param where
	 * @param order
	 * @param group
	 * @param limit
	 * @param paras where条件参数
	 * @return
	 * 
	 * @author daiyc
	 */
	public List<Record> selectR(String columns,String where,String order,String group,String limit,Object... paras) {
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		config.dialect.select(sql,columns,this.getTable().getName(),where,order,group,limit);
		Connection conn = null;
		try {
			conn = config.getConnection();
			return Db.find(config,conn,sql.toString(),paras);
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	/**
	 * 定制查询，忽略表名
	 * @param columns
	 * @param where
	 * @param order
	 * @param group
	 * @param limit
	 * @param paras where条件参数
	 * @return
	 * 
	 * @author daiyc
	 */
	public List<M> selectM(String columns,String where,String order,String group,String limit,Object... paras) {
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		config.dialect.select(sql,columns,this.getTable().getName(),where,order,group,limit);
		return find(sql.toString(),paras);
	}
	/**
	 * 定制查询一条记录，忽略表名
	 * @param columns
	 * @param where
	 * @param order
	 * @param group
	 * @param paras where条件参数
	 * @return
	 * @author daiyc
	 */
	public <T> T getOne(String columns,String where,String order,String group,Object... paras) {
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		config.dialect.getOne(sql,columns,this.getTable().getName(),where,order,group);
		Connection conn = null;
		try {
			conn = config.getConnection();
			List<T> result=Db.query(config,conn,sql.toString(),paras);
			return result.size() > 0 ? result.get(0) : null;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	/**
	 * 定制查询一条记录，忽略表名
	 * @param columns
	 * @param where
	 * @param order
	 * @param group
	 * @param paras where条件参数
	 * @return
	 * @author daiyc
	 */
	public Record getOneR(String columns,String where,String order,String group,Object... paras) {
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		config.dialect.getOne(sql,columns,this.getTable().getName(),where,order,group);
		Connection conn = null;
		try {
			conn = config.getConnection();
			List<Record> result=Db.find(config,conn,sql.toString(),paras);
			return result.size() > 0 ? result.get(0) : null;
		} catch (Exception e) {
			throw new ActiveRecordException(e);
		} finally {
			config.close(conn);
		}
	}
	/**
	 * 定制查询一条记录，忽略表名
	 * @param columns
	 * @param where
	 * @param order
	 * @param group
	 * @param paras where条件参数
	 * @return
	 * @author daiyc
	 */
	public M getOneM(String columns,String where,String order,String group,Object... paras) {
		Config config = getConfig();
		StringBuilder sql = new StringBuilder();
		config.dialect.getOne(sql,columns,this.getTable().getName(),where,order,group);
		return findFirst(sql.toString(),paras);
	}
	
	public String tn(){
		return this.getTable().getName();
	}
}
