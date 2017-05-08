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

package com.baguaz.module.user;

import java.util.List;

import com.baguaz.common.BaseModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Role extends BaseModel<Role> {
	private static final long serialVersionUID = -4334445649909369485L;
	public static final Role dao=new Role();
	
	public static final int ADMIN_ID=1;
	
	public static final String ADMIN_RN="超级管理员";
	
	public List<Record> getRolesByUserid(int userid) {
		return Db.find("SELECT r.* FROM "+Role.dao.tn()+" r LEFT JOIN "+UserRole.dao.tn()+" ur ON r.id=ur.roleid WHERE ur.userid=?", userid);
	}
	
	public List<String> getRolesNameByUserid(int userid) {
		return Db.query("SELECT r.name FROM "+Role.dao.tn()+" r LEFT JOIN "+UserRole.dao.tn()+" ur ON r.id=ur.roleid WHERE ur.userid=? ORDER BY r.id", userid);
	}
}
