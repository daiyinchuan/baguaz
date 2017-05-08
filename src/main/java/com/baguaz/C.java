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

package com.baguaz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class C{
	/*
	 * 错误码
	 */
	@Desc("操作成功")
	public static int success=10;
	@Desc("数据添加成功")
	public static int scs_dataadd=11;
	@Desc("更新缓存成功")
	public static int scs_updateCache=12;
	
	@Desc("操作失败")
	public static int fail=20;
	@Desc("会话失效")
	public static int invalid_session=21;
	@Desc("用户名/密码错误")
	public static int err_usernameOrPassword=22;
	@Desc("旧密码错误")
	public static int err_oldPassword=23;
	@Desc("重复新密码与新密码不一致")
	public static int err_renewPassword=24;
	@Desc("无效令牌")
	public static int invalid_token=25;
	
	@Desc("不可删除")
	public static int nodel=30;
	@Desc("不可修改")
	public static int noedit=31;
	
	private static Map<Integer,String> map;
	static{
		map=new HashMap<Integer,String>();
		Field[] fields=C.class.getFields();
		Class<C> c=C.class;
		Class<Desc> desc=Desc.class;
		try{
			for(Field field:fields){
				map.put(field.getInt(c),field.getAnnotation(desc).value());
			}
		}catch(IllegalArgumentException|IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}
	public static String getDesc(int ecode){
		return map.get(ecode);
	}
	
	/**
	 * @author daiyc
	 *
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Desc {
		String value();
	}
}