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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Ret{
	private int code;
	private String msg;
	/**
	 * 跳转url，用于先弹出消息提示，再跳转到的url
	 */
	private String rdurl;
	public int getCode(){
		return code;
	}
	public Ret setCode(int code){
		this.code=code;
		return this;
	}
	public String getMsg(){
		return msg;
	}
	public Ret setMsg(String msg){
		this.msg=msg;
		return this;
	}
	
	public String getRdurl() {
		return rdurl;
	}
	public Ret setRdurl(String rdurl) {
		this.rdurl = rdurl;
		return this;
	}
	/**
	 * 使用code默认的msg
	 * @param code
	 * @return
	 */
	public Ret setCodeAndMsg(int code){
		this.code=code;
		this.msg=C.getDesc(code);
		return this;
	}
	
	private Map<Object, Object> map = new HashMap<Object, Object>();
	
	
	public Map<Object,Object> getMap(){
		return map;
	}
	public void setMap(Map<Object,Object> map){
		this.map=map;
	}
	
	public Ret() {
		
	}
	
	public Ret(Object key, Object value) {
		map.put(key, value);
	}
	
	public static Ret create() {
		return new Ret();
	}
	
	public static Ret create(Object key, Object value) {
		return new Ret(key, value);
	}
	
	public Ret put(Object key, Object value) {
		map.put(key, value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object key) {
		return (T)map.get(key);
	}
	
	/**
	 * key 存在，但 value 可能为 null
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	/**
	 * key 存在，并且 value 不为 null
	 */
	public boolean notNull(Object key) {
		return map.get(key) != null;
	}
	
	/**
	 * key 不存在，或者 key 存在但 value 为null
	 */
	public boolean isNull(Object key) {
		return map.get(key) == null;
	}
	
	/**
	 * key 存在，并且 value 为 true，则返回 true
	 */
	public boolean isTrue(Object key) {
		Object value = map.get(key);
		return (value instanceof Boolean && ((Boolean)value == true));
	}
	
	/**
	 * key 存在，并且 value 为 false，则返回 true
	 */
	public boolean isFalse(Object key) {
		Object value = map.get(key);
		return (value instanceof Boolean && ((Boolean)value == false));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T remove(Object key) {
		return (T)map.remove(key);
	}
}
