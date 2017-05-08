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

import com.baguaz.BgzKit;
import com.baguaz.common.BaseModel;

/**
 * @author Yinchuan Dai 戴银川 (daiyinchuan@163.com)
 *
 */
public class Validcode extends BaseModel<Validcode> {
	private static final long serialVersionUID = -5122902949422819533L;
	public static final Validcode dao=new Validcode();
	
	/**
	 * 注册
	 */
	public static final int TYPE_REG=1;
	/**
	 * 密码找回
	 */
	public static final int TYPE_FORGET_PWD=2;
	/**
	 * 已使用状态
	 */
	public static final int STATUS_USED=1;
	/**
	 * @param validcode
	 * @param email
	 * @param typeReg
	 * @return
	 */
	public Validcode get(String validcode, String email, int typeReg) {
		return this.getOneM("*", "target=? and code=? and type=? and status=0 and expiretime>?", "", "", email,validcode,typeReg,BgzKit.genUnixTstamp());
	}
}
